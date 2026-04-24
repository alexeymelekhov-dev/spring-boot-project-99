package hexlet.code.dto.task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskFilterDTO {
    private String titleCont;
    private Long assigneeId;
    private String status;
    private Long labelId;
}
