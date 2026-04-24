package hexlet.code.dto.task_status;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusUpdateDTO {

    @Size(min = 1)
    private String name;

    @Size(min = 1)
    private String slug;

}
