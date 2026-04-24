package hexlet.code.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class TaskUpdateDTO {

    private Long index;

    @JsonProperty("assignee_id")
    private Long assigneeId;

    @JsonProperty("label_ids")
    private Set<Long> labelIds;

    @Size(min = 1)
    private String title;
    private String content;

    @Size(min = 1)
    private String status;

}
