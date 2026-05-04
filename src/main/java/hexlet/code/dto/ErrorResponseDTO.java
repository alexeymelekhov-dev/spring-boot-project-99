package hexlet.code.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponseDTO(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> validationErrors
) {
    public ErrorResponseDTO(
            int status,
            String error,
            String message,
            Map<String, String> validationErrors
    ) {
        this(LocalDateTime.now(), status, error, message, validationErrors);
    }
}
