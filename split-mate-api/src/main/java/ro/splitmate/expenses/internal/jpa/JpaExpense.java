package ro.splitmate.expenses.internal.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.types.CreationTime;
import ro.splitmate.expenses.api.ExpenseIdentifier;
import ro.splitmate.types.TargetAmount;
import ro.splitmate.types.Title;
import ro.splitmate.expenses.internal.Expense;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "expenses")
public class JpaExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private Long userId;
    private String title;
    private BigDecimal targetAmount;
    private Timestamp creationDate;

    @OneToMany
    private List<JpaShare> shares;

    public Timestamp getCreationDate() {
        return creationDate;
    }


    static Expense toDomain(JpaExpense jpa) {
        return new Expense(
                new ExpenseIdentifier(jpa.id),
                new CustomerIdentifier(jpa.userId),
                new Title(jpa.title),
                new TargetAmount(jpa.targetAmount),
                new CreationTime(jpa.getCreationDate().toLocalDateTime()),
                jpa.shares
                        .stream()
                        .map(JpaShare::toDomain)
                        .toList());
    }

    static JpaExpense fromDomain(Expense e) {
        JpaExpense jpa = new JpaExpense();
        jpa.title = e.title().title();
        jpa.targetAmount = e.targetAmount().targetAmount();
        jpa.id = e.id().id();
        jpa.userId = e.userId().id();
        jpa.creationDate = Timestamp.from(e.creationDate().creationTime().toInstant(ZoneOffset.UTC));
        jpa.shares = e.shares().stream()
                .map(JpaShare::fromDomain)
                .toList();
        return jpa;
    }
    static JpaExpense create(Title title, TargetAmount targetAmount, CustomerIdentifier userId) {
        JpaExpense newUser = new JpaExpense();
        newUser.creationDate = Timestamp.from(Instant.now());
        newUser.title = title.title();
        newUser.userId = userId.id();
        newUser.targetAmount = targetAmount.targetAmount();
        newUser.shares = new ArrayList<>();
        return newUser;
    }

    public List<JpaShare> getShares() {
        return shares;
    }
}
