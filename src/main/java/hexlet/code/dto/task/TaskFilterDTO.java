package hexlet.code.dto.task;

public record TaskFilterDTO(
        String titleCont,
        Long assigneeId,
        String status,
        Long labelId
) {
}
