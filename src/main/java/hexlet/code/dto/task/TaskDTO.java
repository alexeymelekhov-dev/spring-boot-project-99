package hexlet.code.dto.task;

import java.time.LocalDate;
import java.util.Set;

public record TaskDTO(
        Long id,
        String title,
        String content,
        Long index,
        String status,
        Long assignee_id,
        Set<Long> taskLabelIds,
        LocalDate createdAt
) {
}
