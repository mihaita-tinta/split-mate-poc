package ro.splitmate.payouts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ro.splitmate.expenses.api.AllMoneyArePaid;

@Component
public class PayoutsEventListener {
    private static final Logger log = LoggerFactory.getLogger(PayoutsEventListener.class);

    @ApplicationModuleListener
    public void onAllMoneyArePaid(MoneySentToUser event) {
        log.info("onAllMoneyArePaid -  {} will be paid to {}", event.amount(), event.id());
    }

}
