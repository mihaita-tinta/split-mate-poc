package ro.splitmate.payments.internal.noda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ro.splitmate.payments.internal.Payment;

import java.util.List;

@Service
public class NodaApiClient {
    private static final Logger log = LoggerFactory.getLogger(NodaApiClient.class);
    private final RestTemplate restTemplate;

    private final String host;
    private final String apiKey;
    private final int port;

    public NodaApiClient(
            @Value("${noda.server.api.key}") String apiKey,
            @Value("${noda.server.hostname:api.stage.noda.live}") String host,
            @Value("${noda.server.port:443}") int port,
            RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.host = host;
        this.port = port;
        log.info("BlockingApiClient - calling remote server at: {}:{}", host, port);
    }

    public List<Responses.Bank> getBanks() {
        log.debug("getBanks");
        ParameterizedTypeReference<List<Responses.Bank>> typeRef = new ParameterizedTypeReference<>() {
        };
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<List<Responses.Bank>> res = restTemplate.exchange("https://%s:%d/api/providers/top?country=RO&count=20".formatted(host, port),
                HttpMethod.GET, requestEntity, typeRef);
        log.debug("getBanks - got response");
        if (res.getStatusCode().isError()) {
            log.warn("getBanks - got error: " + res);
            throw new IllegalStateException("could not get banks. received status: " + res.getStatusCode());
        }
        return res.getBody();
    }

    public Responses.PaymentResponse createPayment(String email, Payment payment) {
        log.debug("createPayment");
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        Requests.PaymentCreate requestBody = Requests.PaymentCreate.of(payment.getTargetAmount().targetAmount(),
                "Payment #" + payment.getId().id(),
                payment.getId().id().toString(),
                email,
                payment.getSenderId().id().toString());

        HttpEntity<Requests.PaymentCreate> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Responses.PaymentResponse> res = restTemplate.exchange("https://%s:%d/api/payments".formatted(host, port),
                HttpMethod.POST, requestEntity, Responses.PaymentResponse.class);
        log.debug("createPayment - got response: {}", res.getBody());
        if (res.getStatusCode().isError()) {
            log.warn("createPayment - got error: " + res);
            throw new IllegalStateException("could not create payment. received status: " + res.getStatusCode());
        }
        return res.getBody();
    }

    public Responses.PaymentResponse getPayment(Payment payment) {
        log.debug("getPayment - {}", payment);
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);

        HttpEntity<Requests.PaymentCreate> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Responses.PaymentResponse> res = restTemplate.exchange("https://%s:%d/api/payments/{id}".formatted(host, port),
                HttpMethod.GET, requestEntity, Responses.PaymentResponse.class,
                payment.getExternalPaymentId().id());
        log.debug("getPayment - got response: {}", res.getBody());
        if (res.getStatusCode().isError()) {
            log.warn("getPayment - got error: " + res);
            throw new IllegalStateException("could not create payment. received status: " + res.getStatusCode());
        }
        return res.getBody();
    }


}
