package hexlet.code.dto.task_status;

import jakarta.validation.constraints.Size;

public record TaskStatusUpdateDTO(
        @Size(min = 1)
        String name,

        @Size(min = 1)
        String slug
) {
}
