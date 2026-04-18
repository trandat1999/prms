package com.tranhuudat.prms.repository;

import com.tranhuudat.prms.dto.employee_ot.EmployeeOtDto;
import com.tranhuudat.prms.dto.employee_ot.EmployeeOtSearchRequest;
import com.tranhuudat.prms.entity.EmployeeOt;
import com.tranhuudat.prms.enums.EmployeeOtStatusEnum;
import com.tranhuudat.prms.util.DateUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Repository
public interface EmployeeOtRepository extends JpaRepository<EmployeeOt, UUID> {

    default Page<EmployeeOtDto> getPages(EntityManager entityManager, EmployeeOtSearchRequest request, Pageable pageable) {
        String select =
                "select new com.tranhuudat.prms.dto.employee_ot.EmployeeOtDto(entity) "
                        + "from EmployeeOt entity "
                        + "left join entity.user u "
                        + "left join entity.project p ";

        String count =
                "select count(entity) "
                        + "from EmployeeOt entity "
                        + "left join entity.user u "
                        + "left join entity.project p ";

        List<String> where = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        where.add("(entity.voided is null or entity.voided = false)");

        if (Objects.nonNull(request)) {
            if (!CollectionUtils.isEmpty(request.getIds())) {
                where.add("entity.id in :ids");
                params.put("ids", request.getIds());
            }
            if (Objects.nonNull(request.getUserId())) {
                where.add("entity.userId = :userId");
                params.put("userId", request.getUserId());
            }
            if (Objects.nonNull(request.getProjectId())) {
                where.add("entity.projectId = :projectId");
                params.put("projectId", request.getProjectId());
            }
            if (Objects.nonNull(request.getStatus())) {
                where.add("entity.status = :status");
                params.put("status", request.getStatus());
            }
            if (Objects.nonNull(request.getOtType())) {
                where.add("entity.otType = :otType");
                params.put("otType", request.getOtType());
            }
            if (Objects.nonNull(request.getOtDateFrom())) {
                where.add("entity.otDate >= :otDateFrom");
                params.put("otDateFrom", DateUtil.getStartOfDay(request.getOtDateFrom()));
            }
            if (Objects.nonNull(request.getOtDateTo())) {
                where.add("entity.otDate <= :otDateTo");
                params.put("otDateTo", DateUtil.getEndOfDay(request.getOtDateTo()));
            }

            if (StringUtils.hasText(request.getKeyword())) {
                where.add(
                        "("
                                + "lower(u.username) like :kw "
                                + "or lower(u.fullName) like :kw "
                                + "or lower(coalesce(entity.reason,'')) like :kw "
                                + "or lower(p.code) like :kw "
                                + "or lower(p.name) like :kw"
                                + ")");
                params.put("kw", "%" + request.getKeyword().trim().toLowerCase() + "%");
            }
        }

        String whereClause = " where " + String.join(" and ", where);
        String orderBy = " order by entity.createdDate desc";

        TypedQuery<EmployeeOtDto> query =
                entityManager.createQuery(select + whereClause + orderBy, EmployeeOtDto.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(count + whereClause, Long.class);

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<EmployeeOtDto> content = query.getResultList();
        Long total = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, Objects.nonNull(total) ? total : 0L);
    }

    default List<EmployeeOt> findForMonthlyReport(
            EntityManager entityManager,
            UUID userId,
            UUID projectId,
            EmployeeOtStatusEnum status,
            com.tranhuudat.prms.enums.EmployeeOtTypeEnum otType,
            String keyword,
            Date fromDate,
            Date toDate
    ) {
        String base =
                "select e from EmployeeOt e "
                        + "left join fetch e.user u "
                        + "left join fetch e.project p ";
        List<String> where = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        where.add("(e.voided is null or e.voided = false)");
        where.add("e.otDate >= :fromDate and e.otDate <= :toDate");
        params.put("fromDate", fromDate);
        params.put("toDate", toDate);

        if (Objects.nonNull(userId)) {
            where.add("e.userId = :userId");
            params.put("userId", userId);
        }
        if (Objects.nonNull(projectId)) {
            where.add("e.projectId = :projectId");
            params.put("projectId", projectId);
        }
        if (Objects.nonNull(status)) {
            where.add("e.status = :status");
            params.put("status", status);
        }
        if (Objects.nonNull(otType)) {
            where.add("e.otType = :otType");
            params.put("otType", otType);
        }
        if (StringUtils.hasText(keyword)) {
            where.add(
                    "("
                            + "lower(u.username) like :kw "
                            + "or lower(u.fullName) like :kw "
                            + "or lower(coalesce(e.reason,'')) like :kw "
                            + "or lower(p.code) like :kw "
                            + "or lower(p.name) like :kw"
                            + ")");
            params.put("kw", "%" + keyword.trim().toLowerCase() + "%");
        }

        String whereClause = " where " + String.join(" and ", where);
        String orderBy = " order by e.otDate asc, e.startTime asc, e.userId asc";

        TypedQuery<EmployeeOt> query = entityManager.createQuery(base + whereClause + orderBy, EmployeeOt.class);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query.getResultList();
    }
}
