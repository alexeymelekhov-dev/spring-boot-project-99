package hexlet.code.app.controller.api;

import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task.TaskDTO;
import hexlet.code.app.dto.task.TaskFilterDTO;
import hexlet.code.app.dto.task.TaskUpdateDTO;
import hexlet.code.app.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    private ResponseEntity<List<TaskDTO>> getTasks(@ModelAttribute TaskFilterDTO dto) {
        var tasks = taskService.getTasks(dto);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(tasks.size()))
                .body(tasks);
    }

    @PostMapping
    private ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskCreateDTO dto) {
        var createdTask = taskService.createTask(dto);
        return ResponseEntity
                .created(URI.create("/api/tasks/" + createdTask.getId()))
                .body(createdTask);
    }

    @GetMapping("/{id}")
    private TaskDTO getTask(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @PutMapping("/{id}")
    private TaskDTO updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskUpdateDTO dto
    ) {
        return taskService.updateTask(id, dto);
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTaskById(id);
        return ResponseEntity.noContent().build();
    }
}
