package ro.splitmate.customers.internal;

import ro.splitmate.customers.api.CustomerIdentifier;

import java.time.LocalDateTime;
import java.util.List;

public class Customer {

    private CustomerIdentifier id;

    private String username;
    private String password;
    private List<String> roles;
    private LocalDateTime creationDate;

    public Customer(String username, String password, CustomerIdentifier id,
                    LocalDateTime creationDate) {

        this.roles = List.of("USER");
        this.creationDate = creationDate;
        this.id = id;
        this.password = password;
        this.username = username;
    }

    public CustomerIdentifier getId() {
        return id;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

}