package hexlet.code.dto.task;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class TaskDTO {

    private Long id;
    private String title;
    private String content;
    private Long index;
    private String status;
    private Long assignee_id;
    private Set<Long> taskLabelIds;
    private LocalDate createdAt;

}
