package com.testintegrationorg.scriptsign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ScriptSignApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScriptSignApplication.class, args);
    }
}
