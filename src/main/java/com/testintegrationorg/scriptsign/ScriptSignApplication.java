package com.testintegrationorg.scriptsign;

import com.testintegrationorg.scriptsign.config.SigningProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SigningProperties.class)
public class ScriptSignApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScriptSignApplication.class, args);
    }
}
