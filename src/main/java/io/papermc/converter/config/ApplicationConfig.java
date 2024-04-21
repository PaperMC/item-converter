package io.papermc.converter.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("application")
public record ApplicationConfig(List<String> allowedOrigins, int maxInputLength) {
}
