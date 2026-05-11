package com.pium;

import org.springframework.boot.SpringApplication;

public class TestLayerdApplication {

    public static void main(String[] args) {
        SpringApplication.from(PiumApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
