package hexlet.code.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class TaskCreateDTO {

    private Long index;

    @JsonProperty("assignee_id")
    private Long assigneeId;

    @JsonProperty("label_ids")
    private Set<Long> labelIds;

    @NotBlank
    private String title;

    private String content;

    @NotBlank
    private String status;
}
