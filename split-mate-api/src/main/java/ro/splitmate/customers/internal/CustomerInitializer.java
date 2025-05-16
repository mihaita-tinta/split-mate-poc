package ro.splitmate.customers.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.util.stream.IntStream;

@Component
class CustomerInitializer {
    private static final Logger log = LoggerFactory.getLogger(CustomerInitializer.class);

    private final CustomerManagement customers;

    CustomerInitializer(CustomerManagement customers) {
        this.customers = customers;
    }

    @EventListener
    public void onStartup(ApplicationReadyEvent event) {
        log.info("onStartup - insert some users");
        IntStream.range(0, 10)
                .forEach(i -> {
                    String username = "user" + i;
                    try {
                        customers.onboard(username, "{noop}a");
                    } catch (IllegalStateException e) {
                        log.debug("onStartup - user already present: {}", username);
                    }
                });
    }
}
