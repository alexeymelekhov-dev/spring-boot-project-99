package hexlet.code.app.controller.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.dto.label.LabelCreateDTO;
import hexlet.code.app.dto.label.LabelUpdateDTO;
import hexlet.code.app.model.Label;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
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
public class LabelControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    private static final String USER_EMAIL = "user@mail.com";

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        labelRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnLabels_onGetList() throws Exception {
        var label = createAndSaveLabel("new");

        mockMvc.perform(get("/api/labels"))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.length()").value(1)
                )
                .andExpect(result -> {
                    var json = result.getResponse().getContentAsString();
                    var labels = objectMapper.readTree(json);

                    JsonNode labelJson = labels.get(0);

                    assertAll(
                            () -> assertEquals(label.getId(), labelJson.get("id").asLong()),
                            () -> assertEquals(label.getName(), labelJson.get("name").asText()),

                            () -> assertThat(labelJson.get("createdAt").asText())
                                    .startsWith(label.getCreatedAt().toString().substring(0, 19)),

                            () -> assertThat(labelJson.get("updatedAt").asText())
                                    .startsWith(label.getUpdatedAt().toString().substring(0, 19))
                    );
                });
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldCreateLabelWithValidParams() throws Exception {
        var labelCreateDTO = new LabelCreateDTO();
        labelCreateDTO.setName("question");

        var labelCreateRequestBody = objectMapper.writeValueAsString(labelCreateDTO);

        mockMvc.perform(post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(labelCreateRequestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(labelCreateDTO.getName()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnBadRequestWhenInvalidJson_onCreate() throws Exception {
        mockMvc.perform(post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnLabel_onGetByID() throws Exception {
        var label = createAndSaveLabel("question");

        mockMvc.perform(get("/api/labels/{id}", label.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(label.getId()))
                .andExpect(jsonPath("$.name").value(label.getName()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnNotFoundWhenLabelDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/labels/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldUpdateSuccessfulLabelWithValidJson() throws Exception {
        var label = createAndSaveLabel("question");

        System.out.println(label.getId());

        var beforeUpdatedAt = label.getUpdatedAt();

        var labelUpdateDTO = new LabelUpdateDTO();
        labelUpdateDTO.setName("new-label-name");

        var labelRequestBody = objectMapper.writeValueAsString(labelUpdateDTO);

        mockMvc.perform(put("/api/labels/{id}", label.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(labelRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(label.getId()))
                .andExpect(jsonPath("$.name").value(labelUpdateDTO.getName()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());

        var afterUpdatedAt = labelRepository.findById(label.getId())
                .orElseThrow()
                .getUpdatedAt();

        assertTrue(afterUpdatedAt.isAfter(beforeUpdatedAt));
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnBadRequestWithInvalidJson() throws Exception {
        var label = createAndSaveLabel("question");

        var labelRequestBody = """
        {
            "name": ""
        }
        """;

        mockMvc.perform(put("/api/labels/{id}", label.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(labelRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnNotFoundWhenLabelDoesNotExist_onUpdate() throws Exception {
        var labelRequestBody = """
        {
            "name": "question"
        }
        """;

        mockMvc.perform(put("/api/labels/{id}", 99)
                        .content(labelRequestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnConflictWhenUpdateLabelWithExistingName() throws Exception {
        var label = createAndSaveLabel("question");
        var labelToUpdate = createAndSaveLabel("issue");

        var labelRequestBody = """
        {
            "name": "%s"
        }
        """.formatted(label.getName());

        mockMvc.perform(put("/api/labels/{id}", labelToUpdate.getId())
                        .content(labelRequestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldDeleteLabel() throws Exception {
        var label = createAndSaveLabel("question");

        mockMvc.perform(delete("/api/labels/{id}", label.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldNotFoundWheLabelDoesNotExists_onDelete() throws Exception {
        mockMvc.perform(delete("/api/labels/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists());
    }

    private Label createAndSaveLabel(String name) {
        var label = Instancio.of(Label.class)
                .ignore(field(Label::getId))
                .set(field(Label::getName), name)
                .create();

        return labelRepository.save(label);
    }
}
