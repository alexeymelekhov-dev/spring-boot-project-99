package hexlet.code.config;

import hexlet.code.model.Label;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class ModelGenerator {

    private final UserRepository userRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final LabelRepository labelRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default-user-password}")
    private String defaultPassword;

    @PostConstruct
    public void generateData() {
        createDefaultUser();
        createDefaultTaskStatuses();
        createDefaultLabels();
    }

    private void createDefaultUser() {
        var user = new User();
        user.setFirstName("Ivan");
        user.setLastName("Ivanov");
        user.setEmail("hexlet@example.com");
        user.setPassword(passwordEncoder.encode(defaultPassword));

        userRepository.save(user);
    }

    private void createDefaultTaskStatuses() {
        List<String> slugNames = List.of("draft", "to_review", "to_be_fixed", "to_publish", "published");

        slugNames.forEach(slug -> {
            var taskStatus = new TaskStatus();
            var name = slugToName(slug);
            taskStatus.setName(name);
            taskStatus.setSlug(slug);
            taskStatusRepository.save(taskStatus);
        });
    }

    private void createDefaultLabels() {
        List<String> labelNames = List.of("feature", "bub");

        labelNames.forEach(labelName -> {
            var label = new Label();
            label.setName(labelName);
            labelRepository.save(label);
        });
    }

    private String slugToName(String slug) {
        return Arrays.stream(slug.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }

}
