package io.papermc.converter;

import io.papermc.converter.config.ApplicationConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(ApplicationConfig.class)
@SpringBootApplication
public class ConverterApplication {

    public static void main(final String[] args) {
        SpringApplication.run(ConverterApplication.class, args);
    }
}
