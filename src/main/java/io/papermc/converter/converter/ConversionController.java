package io.papermc.converter.converter;

import io.papermc.converter.config.ApplicationConfig;
import io.papermc.converter.service.MinecraftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class ConversionController {
    private final MinecraftService minecraftService;
    private final ApplicationConfig config;

    @Autowired
    public ConversionController(final MinecraftService minecraftService, final ApplicationConfig config) {
        this.minecraftService = minecraftService;
        this.config = config;
    }

    @PostMapping(value = "/convert-command", consumes = "text/plain", produces = "text/plain")
    public ResponseEntity<String> convertCommand(@RequestBody final String inputText) {
        if (inputText.length() > config.maxInputLength()) {
            return ResponseEntity.badRequest().body("Input is too long");
        }
        return ResponseEntity.ok(minecraftService.upgradeCommand(inputText.trim()));
    }

    @PostMapping(value = "/convert-item-argument", consumes = "text/plain", produces = "text/plain")
    public ResponseEntity<String> convertItemArgument(@RequestBody final String inputText) {
        if (inputText.length() > config.maxInputLength()) {
            return ResponseEntity.badRequest().body("Input is too long");
        }
        return ResponseEntity.ok(minecraftService.upgradeItemArgument(inputText.trim()));
    }
}
