package hexlet.code.dto.task_status;

import jakarta.validation.constraints.NotBlank;

public record TaskStatusCreateDTO(
        @NotBlank
        String name,

        @NotBlank
        String slug
) {
}
