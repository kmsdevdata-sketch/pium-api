package com.layerd;

import org.springframework.boot.SpringApplication;

public class TestLayerdApplication {

    public static void main(String[] args) {
        SpringApplication.from(LayerdApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
