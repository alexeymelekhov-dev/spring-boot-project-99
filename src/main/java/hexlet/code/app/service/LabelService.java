package hexlet.code.app.service;

import hexlet.code.app.dto.label.LabelCreateDTO;
import hexlet.code.app.dto.label.LabelDTO;
import hexlet.code.app.dto.label.LabelUpdateDTO;
import hexlet.code.app.exception.ConflictException;
import hexlet.code.app.exception.ErrorMessage;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.LabelMapper;
import hexlet.code.app.repository.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;
    private final LabelMapper labelMapper;

    public List<LabelDTO> getLabels() {
        return labelRepository.findAll()
                .stream()
                .map(labelMapper::toDTO)
                .toList();
    }

    public LabelDTO createLabel(LabelCreateDTO dto) {
        validateUniqueName(dto.getName());

        var label = labelMapper.toEntity(dto);
        var savedLabel = labelRepository.save(label);

        return labelMapper.toDTO(savedLabel);
    }

    public LabelDTO getLabelById(Long id) {
        return labelRepository.findById(id)
                .map(labelMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.LABEL_NOT_FOUND.format(id)));
    }

    public LabelDTO updateLabel(Long id, LabelUpdateDTO dto) {
        var label =  labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.LABEL_NOT_FOUND.format(id)));

        validateUniqueName(dto.getName());

        labelMapper.updateLabelFromDTO(dto, label);

        if (dto.getName() != null) label.setName(dto.getName());

        var updatedLabel = labelRepository.save(label);

        return labelMapper.toDTO(updatedLabel);
    }

    public void deleteLabel(Long id) {
        var label =  labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.LABEL_NOT_FOUND.format(id)));

        labelRepository.deleteById(label.getId());
    }

    private void validateUniqueName(String name) {
        if (labelRepository.existsByName(name)) {
            throw new ConflictException(ErrorMessage.TASK_STATUS_NAME_ALREADY_EXISTS.format(name));
        }
    }

}
