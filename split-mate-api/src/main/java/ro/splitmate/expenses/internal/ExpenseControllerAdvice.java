package ro.splitmate.expenses.internal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ro.splitmate.types.ApiError;
import ro.splitmate.types.ApiValidationError;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
class ExpenseControllerAdvice {

    @ExceptionHandler
    public ResponseEntity<ApiError> onTargetAmountExceeded(TargetAmountExceeded e) {
        ApiError apiError = new ApiError(BAD_REQUEST, e);
        apiError.setSubErrors(
                List.of(new ApiValidationError("share",
                        "targetAmount",
                        e.shareAmount,
                        e.getMessage())));
        return ResponseEntity.badRequest().body(apiError);
    }
}
