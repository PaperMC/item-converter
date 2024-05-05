package io.papermc.converter.converter;

import io.papermc.converter.config.ApplicationConfig;
import io.papermc.converter.model.CommandList;
import io.papermc.converter.service.MinecraftService;
import io.papermc.converter.service.MinecraftServiceWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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

    @PostMapping(value = "/convert-commands")
    public ResponseEntity<List<String>> convertCommands(@RequestBody final CommandList commandList) {
        final List<String> output = new ArrayList<>(commandList.commands().size());
        for (final String command : commandList.commands()) {
            if (command.length() > this.config.maxInputLength()) {
                return ResponseEntity.badRequest().build();
            }
            output.add(this.minecraftService.upgradeCommand(command.trim()));
        }
        return ResponseEntity.ok(output);
    }

    @PostMapping(value = "/convert-item-argument", consumes = "text/plain", produces = "text/plain")
    public ResponseEntity<String> convertItemArgument(@RequestBody final String inputText) {
        return this.convertValidating(inputText, this.minecraftService::upgradeItemArgument);
    }

    @PostMapping(value = "/convert-entity-argument", consumes = "text/plain", produces = "text/plain")
    public ResponseEntity<String> convertEntityArgument(@RequestParam final String entityType, @RequestBody final String inputText) {
        return this.convertValidating(inputText, s -> this.minecraftService.upgradeEntity(entityType, s));
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
