@ApplicationModule(
        allowedDependencies = {"payments::api", "customers::api", "rest", "types"}

)
package ro.splitmate.expenses;

import org.springframework.modulith.ApplicationModule;
