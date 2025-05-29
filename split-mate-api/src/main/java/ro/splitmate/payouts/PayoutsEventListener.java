package ro.splitmate.payouts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ro.splitmate.expenses.api.ExpenseSettled;

@Component
public class PayoutsEventListener {
    private static final Logger log = LoggerFactory.getLogger(PayoutsEventListener.class);

    @ApplicationModuleListener
    public void onExpenseSettled(ExpenseSettled event) {
        log.info("onExpenseSettled -  {} will be paid to {}", event.amount(), event.id());
    }

}
