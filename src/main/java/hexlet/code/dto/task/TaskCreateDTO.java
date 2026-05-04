package hexlet.code.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record TaskCreateDTO(
        Long index,

        @JsonProperty("assignee_id")
        Long assigneeId,

        @JsonProperty("label_ids")
        Set<Long> labelIds,

        @NotBlank
        String title,

        String content,

        @NotBlank
        String status
) {
}
