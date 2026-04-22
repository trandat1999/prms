package com.tranhuudat.prms.repository;

import com.tranhuudat.prms.entity.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, UUID> {

    List<TaskDependency> findBySuccessorTaskIdAndVoidedFalse(UUID successorTaskId);

    List<TaskDependency> findByPredecessorTaskIdAndVoidedFalse(UUID predecessorTaskId);

    boolean existsByPredecessorTaskIdAndSuccessorTaskIdAndVoidedFalse(UUID predecessorTaskId, UUID successorTaskId);

    @Query(
            "select d from TaskDependency d join Task pr on pr.id = d.predecessorTaskId join Task su on su.id = d.successorTaskId "
                    + "where pr.projectId = :projectId and su.projectId = :projectId and (d.voided is null or d.voided = false)")
    List<TaskDependency> findAllActiveInProject(@Param("projectId") UUID projectId);

    @Query(
            "select d from TaskDependency d where (d.voided is null or d.voided = false) and (d.predecessorTaskId = :taskId or d.successorTaskId = :taskId)")
    List<TaskDependency> findAllActiveByPredecessorOrSuccessor(@Param("taskId") UUID taskId);
}
