package ro.splitmate.payments.internal;

import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class PaymentControllerAdvice {

//    @ExceptionHandler
//    public ResponseEntity<ApiError> onTargetAmountExceeded(TargetAmountExceeded e) {
//        ApiError apiError = new ApiError(BAD_REQUEST, e);
//        apiError.setSubErrors(
//                List.of(new ApiValidationError("share",
//                        "targetAmount",
//                        e.amount,
//                        e.getMessage())));
//        return ResponseEntity.badRequest().body(apiError);
//    }
}
