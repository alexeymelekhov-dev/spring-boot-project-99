package hexlet.code.app.service;

import hexlet.code.app.dto.task_status.TaskStatusCreateDTO;
import hexlet.code.app.dto.task_status.TaskStatusDTO;
import hexlet.code.app.dto.task_status.TaskStatusUpdateDTO;
import hexlet.code.app.exception.ConflictException;
import hexlet.code.app.exception.ErrorMessage;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
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
        validateUniqueNameAndSlug(dto.getName(), dto.getSlug());

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

        validateUniqueNameAndSlug(dto.getName(), dto.getSlug());

        taskStatusMapper.updateTaskStatusFromDTO(dto, taskStatus);

        if (dto.getName() != null) taskStatus.setName(dto.getName());
        if (dto.getSlug() != null) taskStatus.setSlug(dto.getSlug());

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
