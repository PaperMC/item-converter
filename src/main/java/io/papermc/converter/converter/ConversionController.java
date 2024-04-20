package io.papermc.converter.converter;

import io.papermc.converter.service.MinecraftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class ConversionController {
    private final MinecraftService minecraftService;

    @Autowired
    public ConversionController(final MinecraftService minecraftService) {
        this.minecraftService = minecraftService;
    }

    @PostMapping(value = "/convert", consumes = "text/plain", produces = "text/plain")
    public String convert(@RequestBody final String inputText) {
        return minecraftService.upgradeCommand(inputText);
    }
}
