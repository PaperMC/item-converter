package io.papermc.converter.converter;

import io.papermc.converter.config.ApplicationConfig;
import io.papermc.converter.service.MinecraftService;
import io.papermc.converter.service.MinecraftServiceWrapper;
import java.util.function.Function;
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
    public ConversionController(final MinecraftServiceWrapper minecraftService, final ApplicationConfig config) {
        this.minecraftService = minecraftService;
        this.config = config;
    }

    @PostMapping(value = "/convert-command", consumes = "text/plain", produces = "text/plain")
    public ResponseEntity<String> convertCommand(@RequestBody final String inputText) {
        return this.convertValidating(inputText, this.minecraftService::upgradeCommand);
    }

    @PostMapping(value = "/convert-item-argument", consumes = "text/plain", produces = "text/plain")
    public ResponseEntity<String> convertItemArgument(@RequestBody final String inputText) {
        return this.convertValidating(inputText, this.minecraftService::upgradeItemArgument);
    }

    @PostMapping(value = "/convert-component-argument", consumes = "text/plain", produces = "text/plain")
    public ResponseEntity<String> convertComponentArgument(@RequestBody final String inputText) {
        return this.convertValidating(inputText, this.minecraftService::upgradeComponentArgument);
    }

    private ResponseEntity<String> convertValidating(final String input, final Function<String, String> upgrader) {
        if (input.length() > this.config.maxInputLength()) {
            return ResponseEntity.badRequest().body("Input is too long");
        }
        return ResponseEntity.ok(upgrader.apply(input.trim()));
    }
}
