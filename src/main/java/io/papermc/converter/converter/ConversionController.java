package io.papermc.converter.converter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class ConversionController {

    @GetMapping("/convert")
    public String convert(final String input) {
        System.out.println(input);
        return "Hello!";
    }
}
