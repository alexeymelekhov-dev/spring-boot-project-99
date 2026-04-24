package hexlet.code.app.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
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
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TaskStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskRepository taskRepository;

    private static final String USER_EMAIL = "admin@mail.com";

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnTaskStatuses_onGetList() throws Exception {
        var taskStatus = createAndSaveTaskStatus("Done", "done");

        mockMvc.perform(get("/api/task_statuses"))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.length()").value(1)
                )
                .andExpect(result -> {
                    var json = result.getResponse().getContentAsString();
                    var taskStatuses = objectMapper.readTree(json);

                    var taskStatusJson = taskStatuses.get(0);

                    assertAll(
                            () -> assertEquals(taskStatus.getId(), taskStatusJson.get("id").asLong()),
                            () -> assertEquals(taskStatus.getName(), taskStatusJson.get("name").asText()),
                            () -> assertEquals(taskStatus.getSlug(), taskStatusJson.get("slug").asText()),

                            () -> assertThat(taskStatusJson.get("createdAt").asText())
                                    .startsWith(taskStatus.getCreatedAt().toString().substring(0, 19)),

                            () -> assertThat(taskStatusJson.get("updatedAt").asText())
                                    .startsWith(taskStatus.getUpdatedAt().toString().substring(0, 19))
                    );
                });
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldCreateTaskStatus() throws Exception {
        var taskStatus = Instancio.of(TaskStatus.class)
                .ignore(field(TaskStatus::getId))
                .ignore(field(TaskStatus::getCreatedAt))
                .ignore(field(TaskStatus::getUpdatedAt))
                .create();

        mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskStatus)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(taskStatus.getName()))
                .andExpect(jsonPath("$.slug").value(taskStatus.getSlug()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnBadRequestWhenCreateTaskStatusWithInvalidData() throws Exception {
        var taskStatusInvalidRequestBody = """
            {
                "name": "",
                "slug": ""
            }
        """;

        mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskStatusInvalidRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.slug").exists());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnConflictWhenNameExists_onCreate() throws Exception {
        var taskStatus = createAndSaveTaskStatus("In Progress", "in-progress");

        var taskStatusRequestBody = """
            {
                "name": "%s",
                "slug": "%s"
            }
        """.formatted(taskStatus.getName(), "new");

        mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskStatusRequestBody))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnConflictWhenSlugExists_onCreate() throws Exception {
        var taskStatus = createAndSaveTaskStatus("In Progress", "in-progress");

        var taskStatusRequestBody = """
            {
                "name": "%s",
                "slug": "%s"
            }
        """.formatted("new", taskStatus.getSlug());

        mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskStatusRequestBody))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnSuccessWhenExistingTaskStatus_onGet() throws Exception {
        var taskStatus = createAndSaveTaskStatus("Done", "done");

        mockMvc.perform(get("/api/task_statuses/{id}", taskStatus.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    var json = result.getResponse().getContentAsString();
                    var taskStatusJson = objectMapper.readTree(json);

                    assertAll(
                            () -> assertEquals(taskStatus.getId(), taskStatusJson.get("id").asLong()),
                            () -> assertEquals(taskStatus.getName(), taskStatusJson.get("name").asText()),
                            () -> assertEquals(taskStatus.getSlug(), taskStatusJson.get("slug").asText()),
                            () -> assertThat(taskStatusJson.get("createdAt").asText())
                                    .startsWith(taskStatus.getCreatedAt().toString().substring(0, 19)),

                            () -> assertThat(taskStatusJson.get("updatedAt").asText())
                                    .startsWith(taskStatus.getUpdatedAt().toString().substring(0, 19))
                    );
                });
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnNotFoundWhenTaskStatusDoesNotExists_onUpdate() throws Exception {
        mockMvc.perform(get("/api/task_statuses/{id}", 99))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldUpdateTaskStatusWithAllFields() throws  Exception {
        var taskStatus = Instancio.of(TaskStatus.class)
                            .ignore(field(TaskStatus::getId))
                            .ignore(field(TaskStatus::getCreatedAt))
                            .ignore(field(TaskStatus::getUpdatedAt))
                            .create();

        taskStatusRepository.save(taskStatus);

        var beforeUpdatedAt = taskStatus.getUpdatedAt();

        var newName = "Done";
        var newSlug = "done";
        var taskStatusRequestBody = """
        {
            "name": "%s",
            "slug": "%s"
        }
        """.formatted(newName, newSlug);

        mockMvc.perform(put("/api/task_statuses/{id}", taskStatus.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskStatusRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(taskStatus.getId()))
                .andExpect(jsonPath("$.name").value(newName))
                .andExpect(jsonPath("$.slug").value(newSlug))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        var afterUpdatedAt = taskStatusRepository.findById(taskStatus.getId())
                .orElseThrow()
                .getUpdatedAt();

        assertTrue(afterUpdatedAt.isAfter(beforeUpdatedAt));
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldUpdateTaskStatusWithOnlyName() throws  Exception {
        var taskStatus = Instancio.of(TaskStatus.class)
                .ignore(field(TaskStatus::getId))
                .ignore(field(TaskStatus::getCreatedAt))
                .ignore(field(TaskStatus::getUpdatedAt))
                .create();

        taskStatusRepository.save(taskStatus);

        var newValue = "newValue";
        var taskStatusRequestBody = """
        {
            "name": "%s"
        }
        """.formatted(newValue);

        mockMvc.perform(put("/api/task_statuses/{id}", taskStatus.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskStatusRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(taskStatus.getId()))
                .andExpect(jsonPath("$.name").value(newValue))
                .andExpect(jsonPath("$.slug").value(taskStatus.getSlug()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnConflictWhenNameExists_onUpdate() throws Exception {
        var taskStatus = createAndSaveTaskStatus("New", "new");

        var taskStatusToUpdate = createAndSaveTaskStatus("In Progress", "in-progress");

        var requestBody = """
        {
            "name": "%s",
            "slug": "%s"
        }
        """.formatted(taskStatus.getName(), "newSlug");

        mockMvc.perform(put("/api/task_statuses/{id}", taskStatusToUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnConflictWhenSlugExists_onUpdate() throws Exception {
        var taskStatus = createAndSaveTaskStatus("New", "new");

        var taskStatusToUpdate = createAndSaveTaskStatus("In Progress", "in-progress");

        var requestBody = """
        {
            "name": "%s",
            "slug": "%s"
        }
        """.formatted("newName", taskStatus.getSlug());

        mockMvc.perform(put("/api/task_statuses/{id}", taskStatusToUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldDeleteTaskStatus() throws Exception {
        var taskStatus = createAndSaveTaskStatus("Done", "done");

        mockMvc.perform(delete("/api/task_statuses/{id}", taskStatus.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnConflictWhenTaskStatusHasTask() throws Exception {
        var taskStatus = createAndSaveTaskStatus("Done", "done");
        createAndSaveTaskWithStatus(taskStatus);

        mockMvc.perform(delete("/api/task_statuses/{id}", taskStatus.getId()))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnNotFoundWhenTaskStatusDoesNotExists_onDelete() throws Exception {
        mockMvc.perform(delete("/api/task_statuses/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists());
    }

    private TaskStatus createAndSaveTaskStatus(String name, String slug) {
        var taskStatus = Instancio.of(TaskStatus.class)
                .ignore(field(TaskStatus::getId))
                .set(field(TaskStatus::getName), name)
                .set(field(TaskStatus::getSlug), slug)
                .create();

        return taskStatusRepository.save(taskStatus);
    }

    private Task createAndSaveTaskWithStatus(TaskStatus status) {
        var task = Instancio.of(Task.class)
                .ignore(field(Task::getId))
                .ignore(field(Task::getAssignee))
                .ignore(field(Task::getLabels))
                .set(field(Task::getStatus), status)
                .create();

        return taskRepository.save(task);
    }

}
