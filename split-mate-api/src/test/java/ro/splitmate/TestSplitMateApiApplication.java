package ro.splitmate;

import org.springframework.boot.SpringApplication;

public class TestSplitMateApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(SplitMateApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
