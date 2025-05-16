@ApplicationModule(
        allowedDependencies = {"customers::api", "expenses::api", "types"}

)
package ro.splitmate.payments;

import org.springframework.modulith.ApplicationModule;
