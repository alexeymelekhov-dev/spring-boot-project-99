package hexlet.code.specification;

import hexlet.code.dto.task.TaskFilterDTO;
import hexlet.code.model.Task;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TaskSpecification {

    public Specification<Task> build(TaskFilterDTO dto) {
        return withTitleCount(dto.getTitleCont())
                .and(withAssigneeId(dto.getAssigneeId()))
                .and(withStatus(dto.getStatus()))
                .and(withLabelId(dto.getLabelId()));
    }

    public Specification<Task> withTitleCount(String titleCount) {
        return (root, query, cb) -> {
            if (titleCount == null) return cb.conjunction();

            return cb.like(cb.lower(root.get("name")), "%" + titleCount.toLowerCase() + "%");
        };
    }

    public Specification<Task> withAssigneeId(Long assigneeId) {
        return (root, query, cb) -> {
            if (assigneeId == null) return cb.conjunction();

            return cb.equal(root.get("assignee").get("id"), assigneeId);
        };
    }

    public Specification<Task> withStatus(String status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();

            return cb.equal(root.get("status").get("slug"), status);
        };
    }

    public Specification<Task> withLabelId(Long labelId) {
        return (root, query, cb) -> {
            if (labelId == null) return cb.conjunction();

            query.distinct(true);

            return cb.equal(
                    root.join("labels", JoinType.LEFT).get("id"),
                    labelId
            );
        };
    }
}
