package com.tranhuudat.prms.repository;

import com.tranhuudat.prms.dto.task.TaskDto;
import com.tranhuudat.prms.dto.task.TaskSearchRequest;
import com.tranhuudat.prms.entity.Task;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query("select new com.tranhuudat.prms.dto.task.TaskDto(entity) from Task entity " +
            "left join entity.project p " +
            "left join entity.assigned a " +
            "left join entity.reporter rep " +
            "left join entity.reviewer rev " +
            "left join entity.parentTask pt " +
            "where (entity.voided is null or entity.voided = false) " +
            "and (:projectId is null or entity.projectId = :projectId) " +
            "order by entity.status asc, coalesce(entity.kanbanOrder, 2147483647) asc, entity.createdDate asc")
    List<TaskDto> getKanbanTasks(UUID projectId);

    @Query("select new com.tranhuudat.prms.dto.task.TaskDto(entity) from Task entity " +
            "left join entity.project p " +
            "left join entity.assigned a " +
            "left join entity.reporter rep " +
            "left join entity.reviewer rev " +
            "left join entity.parentTask pt " +
            "where (entity.voided is null or entity.voided = false) " +
            "and (entity.projectId in :projectIds) " +
            "order by entity.status asc, coalesce(entity.kanbanOrder, 2147483647) asc, entity.createdDate asc")
    List<TaskDto> getKanbanTasksInProjects(@Param("projectIds") List<UUID> projectIds);

    default Page<TaskDto> getPages(EntityManager entityManager, TaskSearchRequest request, Pageable pageable) {
        String select =
                "select new com.tranhuudat.prms.dto.task.TaskDto(entity) "
                        + "from Task entity "
                        + "left join entity.project p "
                        + "left join entity.assigned a "
                        + "left join entity.reporter rep "
                        + "left join entity.reviewer rev "
                        + "left join entity.parentTask pt ";
        String count =
                "select count(entity) "
                        + "from Task entity "
                        + "left join entity.project p "
                        + "left join entity.assigned a "
                        + "left join entity.reporter rep "
                        + "left join entity.reviewer rev "
                        + "left join entity.parentTask pt ";

        List<String> where = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        where.add("(entity.voided is null or entity.voided = false)");

        if (Objects.nonNull(request)) {
            if (!CollectionUtils.isEmpty(request.getIds())) {
                where.add("entity.id in :ids");
                params.put("ids", request.getIds());
            }
            if (Objects.nonNull(request.getProjectId())) {
                where.add("entity.projectId = :projectId");
                params.put("projectId", request.getProjectId());
            }
            if (Objects.nonNull(request.getAssignedId())) {
                where.add("entity.assignedId = :assignedId");
                params.put("assignedId", request.getAssignedId());
            }
            if (Objects.nonNull(request.getReporterId())) {
                where.add("entity.reporterId = :reporterId");
                params.put("reporterId", request.getReporterId());
            }
            if (Objects.nonNull(request.getReviewerId())) {
                where.add("entity.reviewerId = :reviewerId");
                params.put("reviewerId", request.getReviewerId());
            }
            if (Objects.nonNull(request.getParentTaskId())) {
                where.add("entity.parentTaskId = :parentTaskId");
                params.put("parentTaskId", request.getParentTaskId());
            }
            if (Objects.nonNull(request.getExcludeTaskId())) {
                where.add("entity.id <> :excludeTaskId");
                params.put("excludeTaskId", request.getExcludeTaskId());
            }
            if (Objects.nonNull(request.getStatus())) {
                where.add("entity.status = :status");
                params.put("status", request.getStatus());
            }
            if (StringUtils.hasText(request.getType())) {
                where.add("entity.type = :type");
                params.put("type", request.getType().trim());
            }
            if (StringUtils.hasText(request.getKeyword())) {
                where.add(
                        "("
                                + "lower(entity.code) like :kw "
                                + "or lower(entity.name) like :kw "
                                + "or lower(entity.description) like :kw "
                                + "or lower(entity.shortDescription) like :kw "
                                + "or lower(entity.type) like :kw "
                                + "or lower(p.name) like :kw "
                                + "or lower(a.username) like :kw "
                                + "or lower(a.fullName) like :kw "
                                + "or lower(rep.username) like :kw "
                                + "or lower(rep.fullName) like :kw "
                                + "or lower(rev.username) like :kw "
                                + "or lower(rev.fullName) like :kw"
                                + ")");
                params.put("kw", "%" + request.getKeyword().trim().toLowerCase() + "%");
            }
        }

        String jpqlWhere = " where " + String.join(" and ", where);
        String orderBy = " order by entity.createdDate desc";

        TypedQuery<TaskDto> query = entityManager.createQuery(select + jpqlWhere + orderBy, TaskDto.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(count + jpqlWhere, Long.class);

        for (Map.Entry<String, Object> e : params.entrySet()) {
            query.setParameter(e.getKey(), e.getValue());
            countQuery.setParameter(e.getKey(), e.getValue());
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<TaskDto> content = query.getResultList();
        Long total = countQuery.getSingleResult();
        return new PageImpl<>(content, pageable, Objects.nonNull(total) ? total : 0L);
    }
}

