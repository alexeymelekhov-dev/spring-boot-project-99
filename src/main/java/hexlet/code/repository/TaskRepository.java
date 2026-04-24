package hexlet.code.repository;

import hexlet.code.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    boolean existsByAssigneeId(Long id);

    boolean existsByStatus_Id(Long id);

    @Query("select t from Task t left join fetch t.labels where t.id = :id")
    Optional<Task> findByIdWithLabels(Long id);
}
