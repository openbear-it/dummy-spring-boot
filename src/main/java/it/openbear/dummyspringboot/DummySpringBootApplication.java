package it.openbear.dummyspringboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DummySpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(DummySpringBootApplication.class, args);
    }
}
