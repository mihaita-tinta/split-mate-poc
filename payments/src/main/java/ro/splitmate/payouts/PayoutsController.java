package ro.splitmate.payouts;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ro.splitmate.payouts.ExpenseSettled.CustomerIdentifier;

import java.math.BigDecimal;
import java.util.List;


@RestController
class PayoutsController {
    private static final Logger log = LoggerFactory.getLogger(PayoutsController.class);
    private final ApplicationEventPublisher publisher;

    PayoutsController(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @GetMapping("/payouts")
    public PayoutsResponse get() {
        return new PayoutsResponse(
                List.of(
                new PayoutDto(
                new CustomerIdentifier(1L),
                new ExpenseSettled.ExpenseIdentifier(2L),
                new ExpenseSettled.TargetAmount(new BigDecimal(9.99))))
        );
    }

    @PostMapping("/payouts") //TODO only admin
    @Transactional
    public PayoutDto create(@RequestBody PayoutCreateRequest req) {

        publisher.publishEvent(
                new MoneySentToUser(
                        new CustomerIdentifier(req.receiverId),
                        new ExpenseSettled.ExpenseIdentifier(req.expenseId),
                        new ExpenseSettled.Title(req.title),
                        new ExpenseSettled.TargetAmount(req.amount))
        );
        return new PayoutDto(new CustomerIdentifier(req.receiverId),
                new ExpenseSettled.ExpenseIdentifier(req.expenseId),
                new ExpenseSettled.TargetAmount(req.amount));
    }

    public record PayoutCreateRequest(Long receiverId,
                                            Long expenseId,
                                            String title, BigDecimal amount) {

    }

    record PayoutsResponse(List<PayoutDto> payouts) {
    }
    record PayoutDto(@JsonUnwrapped(prefix = "receiver") CustomerIdentifier receiver,
                     @JsonUnwrapped(prefix = "expense") ExpenseSettled.ExpenseIdentifier expenseId,
                     @JsonUnwrapped ExpenseSettled.TargetAmount amount) {
    }

}
