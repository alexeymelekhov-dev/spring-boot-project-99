package hexlet.code.service;

import hexlet.code.dto.task_status.TaskStatusCreateDTO;
import hexlet.code.dto.task_status.TaskStatusDTO;
import hexlet.code.dto.task_status.TaskStatusUpdateDTO;
import hexlet.code.exception.ConflictException;
import hexlet.code.exception.ErrorMessage;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskStatusService {

    private final TaskStatusRepository taskStatusRepository;
    private final TaskStatusMapper taskStatusMapper;
    private final TaskRepository taskRepository;

    public List<TaskStatusDTO> getTaskStatuses() {
        return taskStatusRepository.findAll()
                .stream()
                .map(taskStatusMapper::toDTO)
                .toList();
    }

    public TaskStatusDTO createTaskStatus(TaskStatusCreateDTO dto) {
        validateUniqueNameAndSlug(dto.name(), dto.slug());

        var taskStatus = taskStatusMapper.toEntity(dto);
        var savedTaskStatus = taskStatusRepository.save(taskStatus);

        return taskStatusMapper.toDTO(savedTaskStatus);
    }

    public TaskStatusDTO getTaskStatus(Long id) {
        return taskStatusRepository.findById(id)
                .map(taskStatusMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.TASK_STATUS_NOT_FOUND.format(id)));
    }

    public TaskStatusDTO updateTaskStatus(Long id, TaskStatusUpdateDTO dto) {
        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.TASK_STATUS_NOT_FOUND.format(id)));

        validateUniqueNameAndSlug(dto.name(), dto.slug());

        taskStatusMapper.updateTaskStatusFromDTO(dto, taskStatus);

        var updatedTaskStatus = taskStatusRepository.save(taskStatus);

        return taskStatusMapper.toDTO(updatedTaskStatus);
    }

    public void deleteTaskStatus(Long id) {
        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.TASK_STATUS_NOT_FOUND.format(id)));

        boolean hasTasks = taskRepository.existsByStatus_Id(id);

        if (hasTasks) {
            throw new ConflictException(ErrorMessage.TASK_STATUS_HAS_TASK.getMessage());
        }

        taskStatusRepository.delete(taskStatus);
    }

    public void validateUniqueNameAndSlug(String name, String slug) {
        if (taskStatusRepository.existsByName(name)) {
            throw new ConflictException(ErrorMessage.TASK_STATUS_NAME_ALREADY_EXISTS.format(name));
        }

        if (taskStatusRepository.existsBySlug(slug)) {
            throw new ConflictException(ErrorMessage.TASK_STATUS_NAME_ALREADY_EXISTS.format(slug));
        }
    }

}
