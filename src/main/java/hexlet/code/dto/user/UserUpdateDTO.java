package hexlet.code.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateDTO {

    @Email
    private String email;

    private String firstName;
    private String lastName;

    @Size(min = 2, max = 50)
    private String password;

}
