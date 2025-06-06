package ro.splitmate.payments.internal.noda;

public class Responses {
    public record Bank(String id, String name, String logoUrl, boolean isInstant) {

    }

    public record PaymentResponse(String id,
                                  String url,
                                  String qrCode,
                                  String token,
                                  PaymentStatus status) {
    }

}
