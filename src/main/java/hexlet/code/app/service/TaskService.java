package hexlet.code.app.service;

import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task.TaskDTO;
import hexlet.code.app.dto.task.TaskFilterDTO;
import hexlet.code.app.dto.task.TaskUpdateDTO;
import hexlet.code.app.exception.ErrorMessage;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.model.Label;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.specification.TaskSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserRepository userRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final LabelRepository labelRepository;
    private final TaskSpecification taskSpecification;

    public List<TaskDTO> getTasks(TaskFilterDTO filter) {
        var specification = taskSpecification.build(filter);
        return taskRepository.findAll(specification)
                .stream()
                .map(taskMapper::toDTO)
                .toList();
    }

    public TaskDTO createTask(TaskCreateDTO dto) {

        Task task = taskMapper.toEntity(dto);

        TaskStatus status = taskStatusRepository.findBySlug(dto.getStatus())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessage.TASK_STATUS_NOT_FOUND.format(dto.getStatus())));
        task.setStatus(status);

        Set<Label> labels = new HashSet<>(labelRepository.findAllById(dto.getLabelIds()));
        task.setLabels(labels);

        if (dto.getAssigneeId() != null) {
            User user = userRepository.findById(dto.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            ErrorMessage.USER_NOT_FOUND.format(dto.getAssigneeId())));

            task.setAssignee(user);
        }

        Task saved = taskRepository.save(task);

        return taskMapper.toDTO(saved);
    }

    public TaskDTO getTaskById(Long id) {
        return taskRepository.findById(id)
                .map(taskMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.TASK_NOT_FOUND.format(id)));
    }

    public TaskDTO updateTask(Long id, TaskUpdateDTO dto) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.TASK_NOT_FOUND.format(id)));

        taskMapper.updateTaskFromDTO(dto, task);

        if (dto.getTitle() != null) task.setName(dto.getTitle());
        if (dto.getContent() != null) task.setDescription(dto.getContent());
        if (dto.getIndex() != null) task.setIndex(dto.getIndex());
        if (dto.getStatus() != null) {
            TaskStatus status = taskStatusRepository.findBySlug(dto.getStatus())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            ErrorMessage.TASK_STATUS_NOT_FOUND.format(dto.getStatus()))
                    );
            task.setStatus(status);
        }
        if (dto.getAssigneeId() != null) {
            User user = userRepository.findById(dto.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            ErrorMessage.USER_NOT_FOUND.format(dto.getAssigneeId())));

            task.setAssignee(user);
        }
        if (dto.getLabelIds() != null) {
            Set<Label> labels = new HashSet<>(labelRepository.findAllById(dto.getLabelIds()));
            task.setLabels(labels);
        }

        Task updatedTask = taskRepository.save(task);

        return  taskMapper.toDTO(updatedTask);
    }

    public void deleteTaskById(Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.TASK_NOT_FOUND.format(id)));

        taskRepository.deleteById(task.getId());
    }

}
