package hexlet.code.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateDTO(
        String firstName,
        String lastName,

        @Email
        @NotBlank
        String email,

        @NotBlank
        @Size(min = 2, max = 50)
        String password
) {
}
