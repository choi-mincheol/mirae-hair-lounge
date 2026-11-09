package com.mirae.hairlounge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MiraeHairLoungeApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiraeHairLoungeApplication.class, args);
    }
}
