package ro.splitmate.expenses.internal;

import java.math.BigDecimal;

public class TargetAmountExceeded extends RuntimeException {
    final BigDecimal targetAmount;
    final BigDecimal shareAmount;
    final BigDecimal exceededAmount;

    public TargetAmountExceeded(String s,
                                BigDecimal targetAmount, BigDecimal shareAmount, BigDecimal exceededAmount) {
        super(s);
        this.targetAmount = targetAmount;
        this.shareAmount = shareAmount;
        this.exceededAmount = exceededAmount;
    }
}
