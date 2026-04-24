package hexlet.code.service;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.exception.ConflictException;
import hexlet.code.exception.ErrorMessage;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserUtils userUtils;

    private final TaskRepository taskRepository;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }

    public UserDTO createUser(UserCreateDTO dto) {
        var user = userMapper.toEntity(dto);
        var savedUser = userRepository.save(user);

        return userMapper.toDTO(savedUser);
    }

    public UserDTO getUser(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND.format(id)));

        return userMapper.toDTO(user);
    }

    public UserDTO updateUser(Long id, UserUpdateDTO dto) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND.format(id)));

        checkAccess(user);

        userMapper.updateUserFromDTO(dto, user);

        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPassword() != null) user.setPassword(dto.getPassword());

        var updatedUser = userRepository.save(user);

        return userMapper.toDTO(updatedUser);
    }

    public void deleteUser(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND.format(id)));

        checkAccess(user);

        boolean hasTasks = taskRepository.existsByAssigneeId(id);
        if (hasTasks) {
            throw new ConflictException(ErrorMessage.USER_CANNOT_BE_DELETED_HAS_TASKS.getMessage());
        }

        userRepository.delete(user);
    }

    private void checkAccess(User targetUser) {
        var currentUser = userUtils.getCurrentUser();

        if (currentUser == null || !currentUser.getId().equals(targetUser.getId())) {
            throw new AccessDeniedException(ErrorMessage.USER_DELETE_ACCESS_DENIED.getMessage());
        }
    }

}
