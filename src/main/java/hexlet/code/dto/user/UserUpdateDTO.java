package hexlet.code.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateDTO(
        @Email
        String email,

        String firstName,
        String lastName,

        @Size(min = 2, max = 50)
        String password
) {}
