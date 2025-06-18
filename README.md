# split-mate-poc
This project is the supporting material of [From Strategic Design to Modular, Eventful, and Reliable Systems](https://www.meetup.com/bucharest-big-data-meetup/events/307912553/) 
meetup presentation.
You can also find more information from my Medium [post](https://mihaita-tinta.medium.com).

# Description
This application helps friends split various costs.
It represents a Proof-Of-Concept to address several things:
- Apply Strategic Design to discover key deliverables and the business domain
- Translate business knowledge into working code
- Apply best practices in a distributed environment
- Identify key metrics and potential performance problems


To view the events open `drawio`:
```shell
docker run -it --rm --name="draw" -p 8082:8080 -p 8443:8443 jgraph/drawio
```
Open http://localhost:8082


## Benefits of choosing this modular architecture

Spring Modulith enforces the modular structure we chose for this application.
- api package can be used by other modules
- internal package is hidden from the rest of the application
- instead of calling methods directly, we use events to communicate async between modules

### customers
- the api package has only input and output events
- other modules interested in customers can use the [CustomerIdentifier.java](split-mate-api/src/main/java/ro/splitmate/customers/api/CustomerIdentifier.java)
- authenticated customers can be referenced in the endpoints directly based on the spring security authentication information
- internal implementation is hidden from the rest of the application.
- internal classes are restricted to package access and they can't be imported outside the module
- testing is very easy with the testing support from Spring Modulith
```java
    @Test
    void testOnboardNewUser(Scenario scenario) {
        scenario.stimulate(() -> customers.onboard("test" + System.currentTimeMillis(), "{noop}a"))
                .andWaitForEventOfType(CustomerOnboarded.class)
                .toArrive();
    }
```


### expenses
- the api package reflects the event storming checkpoints: ExpenseCreated and ExpenseSettled
- claim and settle command are encapsulated in the [Expense.java](split-mate-api/src/main/java/ro/splitmate/expenses/internal/Expense.java) aggregate
- the [ExpenseManagement.java](split-mate-api/src/main/java/ro/splitmate/expenses/internal/ExpenseManagement.java) 
service is starting the transactions for each operation and reliably sending events.
- when payments are confirmed, the expense settles automatically
- shares are hidden details of the expense and are not exposed in the api package
- to avoid a database driven design, the jpa package implements the repositories.

### payments
- the api package has only input and output events: PaymentConfirmed and PaymentOpened