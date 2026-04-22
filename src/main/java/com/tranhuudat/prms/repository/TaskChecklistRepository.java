package com.tranhuudat.prms.repository;

import com.tranhuudat.prms.entity.TaskChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskChecklistRepository extends JpaRepository<TaskChecklist, UUID> {

    List<TaskChecklist> findByTaskIdAndVoidedFalseOrderBySortOrderAscCreatedDateAsc(UUID taskId);

    List<TaskChecklist> findByTaskId(UUID taskId);

    interface TaskChecklistCountRow {
        UUID getTaskId();
        long getCnt();
    }

    @Query("select c.taskId as taskId, count(c) as cnt from TaskChecklist c " +
            "where (c.voided is null or c.voided = false) and c.taskId in :taskIds group by c.taskId")
    List<TaskChecklistCountRow> countAllByTaskIds(@Param("taskIds") List<UUID> taskIds);

    @Query("select c.taskId as taskId, count(c) as cnt from TaskChecklist c " +
            "where (c.voided is null or c.voided = false) and c.checked = true and c.taskId in :taskIds group by c.taskId")
    List<TaskChecklistCountRow> countDoneByTaskIds(@Param("taskIds") List<UUID> taskIds);
}
