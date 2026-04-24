package hexlet.code.service;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskFilterDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.exception.ErrorMessage;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.specification.TaskSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

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

        var labelIds = dto.getLabelIds();

        if (labelIds != null && !labelIds.isEmpty()) {
            Set<Label> labels = resolveLabels(labelIds);
            task.getLabels().clear();
            task.getLabels().addAll(labels);
        }

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
        return taskRepository.findByIdWithLabels(id)
                .map(taskMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.TASK_NOT_FOUND.format(id)));
    }

    public TaskDTO updateTask(Long id, TaskUpdateDTO dto) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.TASK_NOT_FOUND.format(id)));

        taskMapper.updateTaskFromDTO(dto, task);

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

        var labelIds = dto.getLabelIds();

        if (labelIds != null && !labelIds.isEmpty()) {
            Set<Label> labels = resolveLabels(labelIds);
            task.getLabels().clear();
            task.getLabels().addAll(labels);
        }

        Task updatedTask = taskRepository.save(task);

        return  taskMapper.toDTO(updatedTask);
    }

    public void deleteTaskById(Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.TASK_NOT_FOUND.format(id)));

        taskRepository.deleteById(task.getId());
    }

    private Set<Label> resolveLabels(Set<Long> labelIds) {
        if (labelIds == null || labelIds.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Label> labels = new HashSet<>();

        for (Long id : labelIds) {
            Label label = labelRepository.findById(id)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    ErrorMessage.LABEL_NOT_FOUND.format(id)
                            )
                    );
            labels.add(label);
        }

        return labels;
    }

}
