package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
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

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    private static final String USER_EMAIL = "user@mail.com";

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();
        labelRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnAllTasks_whenNoFiltersProvided() throws Exception {
        var task = createAndSaveTask(null, null, null, null);

        mockMvc.perform(get("/api/tasks"))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.length()").value(1)
                )
                .andExpect(result -> {
                    var json = result.getResponse().getContentAsString();
                    var tasks = objectMapper.readTree(json);

                    JsonNode taskJson = tasks.get(0);

                    assertAll(
                            () -> assertEquals(task.getId(), taskJson.get("id").asLong()),
                            () -> assertEquals(task.getName(), taskJson.get("title").asText()),
                            () -> assertEquals(task.getDescription(), taskJson.get("content").asText()),
                            () -> assertEquals(task.getIndex(), taskJson.get("index").asLong()),
                            () -> assertEquals(task.getStatus().getSlug(), taskJson.get("status").asText()),
                            () -> assertEquals(task.getAssignee().getId(), taskJson.get("assignee_id").asLong()),
                            () -> assertEquals(
                                    task.getLabels().stream().findFirst().orElseThrow().getId(),
                                    taskJson.get("taskLabelIds").get(0).asLong()
                            ),
                            () -> assertEquals(task.getCreatedAt().toString(), taskJson.get("createdAt").asText())
                    );
                });
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnTasksFilteredByTitle_whenTitleContProvided() throws Exception {
        createAndSaveTask("random title", null, null, null);
        var targetTask = createAndSaveTask("create task", null, null, null);

        mockMvc.perform(get("/api/tasks")
                        .param("titleCont", "create"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(targetTask.getId()));
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnTasksFilteredByAssignee_whenAssigneeIdProvided() throws Exception {
        var user1 = createAndSaveUser(null);
        var user2 = createAndSaveUser(null);

        createAndSaveTask(null, user1, null, null);
        var targetTask = createAndSaveTask(null, user2, null, null);

        mockMvc.perform(get("/api/tasks")
                        .param("assigneeId", String.valueOf(user2.getId())))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(targetTask.getId()));
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnTasksFilteredByStatus_whenStatusProvided() throws Exception {
        var newStatus = createAndSaveTaskStatus("New", "new");
        var doneStatus = createAndSaveTaskStatus("Done", "done");

        createAndSaveTask(null, null, newStatus, null);
        var targetTask = createAndSaveTask(null, null, doneStatus, null);

        mockMvc.perform(get("/api/tasks")
                        .param("status", doneStatus.getSlug()))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(targetTask.getId()));
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnTasksFilteredByLabel_whenLabelIdProvided() throws Exception {
        var label1 = createAndSaveLabel();
        var label2 = createAndSaveLabel();

        createAndSaveTask(null, null, null, Set.of(label1));
        var targetTask = createAndSaveTask(null, null, null, Set.of(label2));

        mockMvc.perform(get("/api/tasks")
                        .param("labelId", String.valueOf(label2.getId())))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(targetTask.getId()));
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldCreateTaskWithValidParams() throws Exception {
        var user = createAndSaveUser("email@example.com");
        var status = createAndSaveTaskStatus("New", "new");

        var firstLabel = createAndSaveLabel();
        var secondLabel = createAndSaveLabel();
        var labels = Set.of(firstLabel, secondLabel);
        Set<Long> labelIds = labels.stream().map(Label::getId).collect(Collectors.toSet());

        var taskCreateDTO = new TaskCreateDTO(
                null,
                user.getId(),
                labelIds,
                "Test title",
                "Test content",
                status.getSlug()
        );

        var taskRequestBody = objectMapper.writeValueAsString(taskCreateDTO);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskRequestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value(taskCreateDTO.title()))
                .andExpect(jsonPath("$.content").value(taskCreateDTO.content()))
                .andExpect(jsonPath("$.index").value(taskCreateDTO.index()))
                .andExpect(jsonPath("$.status").value(status.getSlug()))
                .andExpect(jsonPath("$.assignee_id").value(user.getId()))
                .andExpect(jsonPath("$.taskLabelIds", containsInAnyOrder(
                        firstLabel.getId().intValue(),
                        secondLabel.getId().intValue()
                )))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldCreateTaskWithoutLabels() throws Exception {
        var user = createAndSaveUser("email@example.com");
        var status = createAndSaveTaskStatus("New", "new");

        var taskCreateDTO = new TaskCreateDTO(
                null,
                user.getId(),
                null,
                "Test title",
                "Test content",
                status.getSlug()
        );

        var body = objectMapper.writeValueAsString(taskCreateDTO);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskLabelIds").isEmpty());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnBadRequestWhenInvalidJson_onCreate() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnTask_whenGetTaskById() throws Exception {
        var task = createAndSaveTask(null, null, null, null);

        mockMvc.perform(get("/api/tasks/{id}", task.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value(task.getName()))
                .andExpect(jsonPath("$.content").value(task.getDescription()))
                .andExpect(jsonPath("$.index").value(task.getIndex()))
                .andExpect(jsonPath("$.status").value(task.getStatus().getSlug()))
                .andExpect(jsonPath("$.assignee_id").value(task.getAssignee().getId()))
                .andExpect(jsonPath("$.taskLabelIds", containsInAnyOrder(
                        task.getLabels().stream()
                                .map(Label::getId)
                                .map(Long::intValue)
                                .toArray()
                )))
                .andExpect(jsonPath("$.createdAt").value(task.getCreatedAt().toString()));
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnNotFoundWhenTaskDoesNotExisting_onGetById() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", 99))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldUpdateTaskPartially_whenOnlyTitleAndContentProvided() throws Exception {
        var task = createAndSaveTask(null, null, null, null);

        var taskUpdateDTO = new TaskUpdateDTO(
                null,
                null,
                null,
                "New title",
                "New content",
                null
        );

        var taskRequestBody = objectMapper.writeValueAsString(taskUpdateDTO);

        mockMvc.perform(put("/api/tasks/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskRequestBody))
                .andExpect(status().isOk())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value(taskUpdateDTO.title()))
                .andExpect(jsonPath("$.content").value(taskUpdateDTO.content()))
                .andExpect(jsonPath("$.index").value(task.getIndex()))
                .andExpect(jsonPath("$.status").value(task.getStatus().getSlug()))
                .andExpect(jsonPath("$.assignee_id").value(task.getAssignee().getId()))
                .andExpect(jsonPath("$.taskLabelIds", containsInAnyOrder(
                        task.getLabels().stream()
                                .map(Label::getId)
                                .map(Long::intValue)
                                .toArray()
                )))
                .andExpect(jsonPath("$.createdAt").value(task.getCreatedAt().toString()));
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldUpdateTask_whenAllFieldsProvided() throws Exception {
        var task = createAndSaveTask(null, null, null, null);

        var newStatus = createAndSaveTaskStatus("Done", "done");
        var newAssignee = createAndSaveUser("new-assignee@example.com");

        var firstLabel = createAndSaveLabel();
        var secondLabel = createAndSaveLabel();
        var labels = Set.of(firstLabel, secondLabel);
        Set<Long> labelIds = labels.stream().map(Label::getId).collect(Collectors.toSet());

        var taskUpdateDTO = new TaskUpdateDTO(
                1L,
                newAssignee.getId(),
                labelIds,
                "New title",
                "New content",
                newStatus.getSlug()
        );

        var taskRequestBody = objectMapper.writeValueAsString(taskUpdateDTO);

        mockMvc.perform(put("/api/tasks/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value(taskUpdateDTO.title()))
                .andExpect(jsonPath("$.content").value(taskUpdateDTO.content()))
                .andExpect(jsonPath("$.index").value(taskUpdateDTO.index()))
                .andExpect(jsonPath("$.status").value(taskUpdateDTO.status()))
                .andExpect(jsonPath("$.assignee_id").value(taskUpdateDTO.assigneeId()))
                .andExpect(jsonPath("$.taskLabelIds", containsInAnyOrder(
                        firstLabel.getId().intValue(),
                        secondLabel.getId().intValue()
                )))
                .andExpect(jsonPath("$.createdAt").value(task.getCreatedAt().toString()));
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnBadRequestWhenTriesToUpdateWithInvalidParams() throws Exception {
        var task = createAndSaveTask(null, null, null, null);

        var taskRequestBody = """
                {
                    "title": ""
                }
                """;

        mockMvc.perform(put("/api/tasks/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors.title").exists());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldDeleteTask() throws Exception {
        var task = createAndSaveTask(null, null, null, null);

        mockMvc.perform(delete("/api/tasks/{id}", task.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldNotFoundTaskWheTaskDoesNotExists_onDelete() throws Exception {
        mockMvc.perform(delete("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists());
    }

    private User createAndSaveUser(String email) {
        var builder = Instancio.of(User.class)
                .ignore(field(User::getId));

        if (email == null) {
            builder.generate(field(User::getEmail), gen -> gen.net().email());
        } else {
            builder.set(field(User::getEmail), email);
        }

        var user = builder.create();

        return userRepository.save(user);
    }

    private TaskStatus createAndSaveTaskStatus(String name, String slug) {
        var builder = Instancio.of(TaskStatus.class)
                .ignore(field(TaskStatus::getId));

        if (name != null) {
            builder.set(field(TaskStatus::getName), name);
        }

        if (slug != null) {
            builder.set(field(TaskStatus::getSlug), slug);
        }

        var taskStatus = builder.create();

        return taskStatusRepository.save(taskStatus);
    }

    private Task createTask() {
        return Instancio.of(Task.class)
                .ignore(field(Task::getId))
                .ignore(field(Task::getAssignee))
                .ignore(field(Task::getLabels))
                .ignore(field(Task::getStatus))
                .create();
    }

    private Task createAndSaveTask(
            String title,
            User assignee,
            TaskStatus status,
            Set<Label> labels
    ) {
        var task = createTask();

        if (title != null) task.setName(title);
        task.setAssignee(assignee != null ? assignee : createAndSaveUser(null));
        task.setStatus(status != null ? status : createAndSaveTaskStatus(null, null));
        task.setLabels(labels != null ? labels : Set.of(createAndSaveLabel()));

        return taskRepository.save(task);
    }

    private Label createAndSaveLabel() {
        var label = Instancio.of(Label.class)
                .ignore(field(Label::getId))
                .create();

        return labelRepository.save(label);
    }
}
