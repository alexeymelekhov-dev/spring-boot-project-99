package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    protected ObjectMapper objectMapper;

    private static final Faker faker = new Faker();
    private static final String USER_EMAIL = "admin@mail.com";

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnUsersList() throws Exception {
        var user = createAndSaveUser(faker.internet().emailAddress());

        mockMvc.perform(get("/api/users"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(result -> {
                    var json = result.getResponse().getContentAsString();
                    var users = objectMapper.readTree(json);

                    var usersJson = users.get(0);

                    assertAll(
                        () -> assertEquals(user.getId(), usersJson.get("id").asLong()),
                        () -> assertEquals(user.getFirstName(), usersJson.get("firstName").asText()),
                        () -> assertEquals(user.getLastName(), usersJson.get("lastName").asText()),
                        () -> assertEquals(user.getEmail(), usersJson.get("email").asText()),
                        () -> assertEquals(user.getCreatedAt().toString(), usersJson.get("createdAt").asText())
                    );
                });
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldCreateUser() throws Exception {
        var userCreateDTO = new UserCreateDTO(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().emailAddress(),
                faker.credentials().password(8, 16, true, true)
        );

        var requestBody = objectMapper.writeValueAsString(userCreateDTO);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").value(userCreateDTO.email()))
                .andExpect(jsonPath("$.firstName").value(userCreateDTO.firstName()))
                .andExpect(jsonPath("$.lastName").value(userCreateDTO.lastName()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnBadRequest_whenCreateUserWithInvalidData() throws Exception {
        var createUserRequestBody = """
            {
                "email": "not-email",
                "password": ""
            }
        """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturn200_withExistingUser_onGet() throws Exception {
        var user = createAndSaveUser(USER_EMAIL);

        mockMvc.perform(get("/api/users/{id}", user.getId()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.firstName").value(user.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(user.getLastName()))
                .andExpect(jsonPath("$.createdAt").value(user.getCreatedAt().toString()));
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnNotFound_whenUserNotFound_onGet() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 1))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldUpdateUserWhenUserIsOwner_withAllFieldsProvided() throws Exception {
        var user = createAndSaveUser(USER_EMAIL);

        var userUpdateDTO = new UserUpdateDTO(
                faker.internet().emailAddress(),
                faker.name().firstName(),
                faker.name().lastName(),
                faker.credentials().password(8, 16)
        );

        var requestBody = objectMapper.writeValueAsString(userUpdateDTO);

        mockMvc.perform(put("/api/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value(userUpdateDTO.firstName()))
                .andExpect(jsonPath("$.lastName").value(userUpdateDTO.lastName()))
                .andExpect(jsonPath("$.email").value(userUpdateDTO.email()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldUpdateUserWhenUserIsOwner() throws Exception {
        var user = createAndSaveUser(USER_EMAIL);

        var newEmail = faker.internet().emailAddress();
        var newPassword = faker.credentials().password(8, 16, true, true);

        var updateUserRequestBody = """
            {
                "email": "%s",
                "password": "%s"
            }
        """.formatted(newEmail, newPassword);

        mockMvc.perform(put("/api/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateUserRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value(newEmail));
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturn403WhenUserTriesToUpdateAnotherUser() throws Exception {
        createAndSaveUser(USER_EMAIL);
        var user = createAndSaveUser(faker.internet().emailAddress());

        var newEmail = faker.internet().emailAddress();
        var newPassword = faker.credentials().password(8, 16, true, true);

        var updateUserRequestBody = """
            {
                "email": "%s",
                "password": "%s"
            }
        """.formatted(newEmail, newPassword);

        mockMvc.perform(put("/api/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateUserRequestBody))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnBadRequest_withInvalidData_whenUserIsOwner_onUpdate() throws Exception {
        var user = createAndSaveUser(USER_EMAIL);

        var updateUserRequestBody = """
            {
                "email": "not-email",
                "password": ""
            }
        """;

        mockMvc.perform(put("/api/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateUserRequestBody))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnNotFound_whenUpdatingNonExistingUser() throws Exception {
        var newEmail = faker.internet().emailAddress();
        var newPassword = faker.credentials().password(8, 16, true, true);
        var updateUserRequestBody = """
            {
                "email": "%s",
                "password": "%s"
            }
        """.formatted(newEmail, newPassword);

        mockMvc.perform(put("/api/users/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateUserRequestBody))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnNoContent_whenDeletingUser() throws Exception {
        var user = createAndSaveUser(USER_EMAIL);

        mockMvc.perform(delete("/api/users/{id}", user.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnNotFound_whenUserDoesNotExist_onDelete() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 99))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturn403WhenUserTriesToDeleteAnotherUser() throws Exception {
        createAndSaveUser(USER_EMAIL);
        var user = createAndSaveUser(faker.internet().emailAddress());

        mockMvc.perform(delete("/api/users/{id}", user.getId()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnConflictWhenUserHasTask() throws Exception {
        var user = createAndSaveUser(USER_EMAIL);
        createAndSaveTask(user);

        mockMvc.perform(delete("/api/users/{id}", user.getId()))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    private User createAndSaveUser(String email) {
        var user = Instancio.of(User.class)
                .ignore(field(User::getId))
                .set(field(User::getEmail), email)
                .set(field(User::getPassword), faker.credentials().password(8, 16, true, true))
                .create();

        return userRepository.save(user);
    }

    private Task createAndSaveTask(User user) {
        var task = Instancio.of(Task.class)
                .ignore(field(Task::getId))
                .ignore(field(Task::getAssignee))
                .ignore(field(Task::getLabels))
                .ignore(field(Task::getStatus))
                .create();

        var taskStatus = createAndSaveTaskStatus("New", "new");

        task.setStatus(taskStatus);
        task.setAssignee(user);

        return taskRepository.save(task);
    }

    private TaskStatus createAndSaveTaskStatus(String name, String slug) {
        var taskStatus = Instancio.of(TaskStatus.class)
                .ignore(field(TaskStatus::getId))
                .set(field(TaskStatus::getName), name)
                .set(field(TaskStatus::getSlug), slug)
                .create();

        return taskStatusRepository.save(taskStatus);
    }

}
