package com.devit.devitcertificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableEurekaClient
@EnableJpaAuditing
@SpringBootApplication
public class DevitCertificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevitCertificationServiceApplication.class, args);
    }

}
