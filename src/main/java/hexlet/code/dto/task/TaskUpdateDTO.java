package hexlet.code.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record TaskUpdateDTO(
        Long index,

        @JsonProperty("assignee_id")
        Long assigneeId,

        @JsonProperty("label_ids")
        Set<Long> labelIds,

        @Size(min = 1)
        String title,
        String content,

        @Size(min = 1)
        String status
) {
}
