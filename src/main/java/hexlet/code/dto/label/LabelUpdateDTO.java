package hexlet.code.dto.label;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LabelUpdateDTO(
        @NotBlank
        @Size(min = 3, max = 1000)
        String name
) {
}
