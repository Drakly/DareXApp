package org.darexapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DareXAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(DareXAppApplication.class, args);
    }

}
