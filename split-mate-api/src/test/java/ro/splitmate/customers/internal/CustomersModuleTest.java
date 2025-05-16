package ro.splitmate.customers.internal;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.TestPropertySource;
import ro.splitmate.SplitMateApiApplication;
import ro.splitmate.customers.api.CustomerOnboarded;

@ApplicationModuleTest
@TestPropertySource(properties = {"logging.level.org.springframework.modulith=TRACE",
"spring.sql.init.mode= always"})
class CustomersModuleTest {
    private static final Logger log = LoggerFactory.getLogger(CustomersModuleTest.class);
    ApplicationModules modules = ApplicationModules.of(SplitMateApiApplication.class);

    @Autowired
    CustomerManagement customers;

    @Test
    void verifyModule() {
        modules
                .forEach(module -> log.info("\n{}", module));
        modules
                .verify();
    }

    @Test
    void testOnboardNewUser(Scenario scenario) {
        scenario.stimulate(() -> customers.onboard("test" + System.currentTimeMillis(), "{noop}a"))
                .andWaitForEventOfType(CustomerOnboarded.class)
                .toArrive();
    }


    @Test
    void createModuleDocumentation() {
        new Documenter(modules).writeDocumentation();
    }
}