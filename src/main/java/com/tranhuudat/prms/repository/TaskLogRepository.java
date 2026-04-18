package com.tranhuudat.prms.repository;

import com.tranhuudat.prms.entity.TaskLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskLogRepository extends JpaRepository<TaskLog, UUID> {
    List<TaskLog> findAllByTaskIdOrderByCreatedDateDesc(UUID taskId);
}

