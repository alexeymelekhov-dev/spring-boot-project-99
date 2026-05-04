package hexlet.code.service;

import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import hexlet.code.exception.ConflictException;
import hexlet.code.exception.ErrorMessage;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.repository.LabelRepository;
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
        validateUniqueName(dto.name());

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

        validateUniqueName(dto.name());

        labelMapper.updateLabelFromDTO(dto, label);

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
