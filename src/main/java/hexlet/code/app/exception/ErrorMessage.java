package hexlet.code.app.exception;

import lombok.Getter;

@Getter
public enum ErrorMessage {

    TASK_STATUS_NOT_FOUND("Task status not found with id: %s"),
    TASK_STATUS_HAS_TASK("Task status has tasks and cannot be deleted"),
    TASK_STATUS_NAME_ALREADY_EXISTS("Task status with name '%s' already exists"),
    TASK_STATUS_SLUG_ALREADY_EXISTS("Task status with slug '%s' already exists"),

    USER_NOT_FOUND("User not found with id: %s"),
    USER_CANNOT_BE_DELETED_HAS_TASKS("User has tasks and cannot be deleted"),
    USER_DELETE_ACCESS_DENIED("You can only delete your own account"),

    TASK_NOT_FOUND("Task not found with id: %s"),

    LABEL_NOT_FOUND("Label not found with id: %s"),
    LABEL_NAME_ALREADY_EXISTS("Label with name '%s' already exists");

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    public String format(Object... args) {
        return message.formatted(args);
    }

}
