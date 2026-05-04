package hexlet.code.controller.api;

import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import hexlet.code.service.LabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/labels")
public class LabelController {

    private final LabelService labelService;

    @GetMapping
    public ResponseEntity<List<LabelDTO>> getLabels() {
        var labels = labelService.getLabels();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(labels.size()))
                .body(labels);
    }

    @PostMapping
    public ResponseEntity<LabelDTO> createLabel(@Valid @RequestBody LabelCreateDTO dto) {
        var label = labelService.createLabel(dto);
        return ResponseEntity
                .created(URI.create("/api/labels/" + label.id()))
                .body(label);
    }

    @GetMapping("/{id}")
    public LabelDTO getLabel(@PathVariable Long id) {
        return labelService.getLabelById(id);
    }

    @PutMapping("/{id}")
    public LabelDTO updateLabel(
            @PathVariable Long id,
            @Valid @RequestBody LabelUpdateDTO dto
    ) {
        return labelService.updateLabel(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLabel(@PathVariable Long id) {
        labelService.deleteLabel(id);
        return ResponseEntity.noContent().build();
    }

}
