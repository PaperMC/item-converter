package io.papermc.converter.model;

import jakarta.validation.constraints.Size;
import java.util.List;

public record CommandList(@Size(max = 200) List<String> commands) {
}
