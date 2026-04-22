package com.tranhuudat.prms.repository;

import com.tranhuudat.prms.dto.resource_allocation.ResourceAllocationDto;
import com.tranhuudat.prms.dto.resource_allocation.ResourceAllocationSearchRequest;
import com.tranhuudat.prms.entity.ResourceAllocation;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Repository
public interface ResourceAllocationRepository extends JpaRepository<ResourceAllocation, UUID> {
    default Page<ResourceAllocationDto> getPages(
            EntityManager entityManager, ResourceAllocationSearchRequest request, Pageable pageable) {
        String select =
                "select new com.tranhuudat.prms.dto.resource_allocation.ResourceAllocationDto(entity) "
                        + "from ResourceAllocation entity "
                        + "left join entity.user u ";
        String count =
                "select count(entity) "
                        + "from ResourceAllocation entity "
                        + "left join entity.user u ";

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
            if (Objects.nonNull(request.getYear())) {
                where.add("year(entity.resourceMonth) = :year");
                params.put("year", request.getYear());
            }
            if (Objects.nonNull(request.getMonthYear())) {
                where.add("month(entity.resourceMonth) = :monthYear");
                params.put("monthYear", request.getMonthYear());
            }
            if (StringUtils.hasText(request.getRole())) {
                where.add("entity.role = :role");
                params.put("role", request.getRole().trim());
            }
            if (StringUtils.hasText(request.getKeyword())) {
                where.add(
                        "("
                                + "lower(u.username) like :kw "
                                + "or lower(u.fullName) like :kw "
                                + "or lower(entity.role) like :kw"
                                + ")");
                params.put("kw", "%" + request.getKeyword().trim().toLowerCase() + "%");
            }
        }

        String jpqlWhere = " where " + String.join(" and ", where);
        String orderBy = " order by entity.createdDate desc";

        TypedQuery<ResourceAllocationDto> query =
                entityManager.createQuery(select + jpqlWhere + orderBy, ResourceAllocationDto.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(count + jpqlWhere, Long.class);

        for (Map.Entry<String, Object> e : params.entrySet()) {
            query.setParameter(e.getKey(), e.getValue());
            countQuery.setParameter(e.getKey(), e.getValue());
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<ResourceAllocationDto> content = query.getResultList();
        Long total = countQuery.getSingleResult();
        return new PageImpl<>(content, pageable, Objects.nonNull(total) ? total : 0L);
    }

    /**
     * Danh sách phân bổ theo tháng (năm + tháng của {@code resourceMonth}), không phân trang.
     * Dùng cho xuất Excel; chỉ áp dụng điều kiện tháng, không lọc theo user/role/keyword.
     */
    default List<ResourceAllocationDto> listAllByResourceMonth(
            EntityManager entityManager, int year, int monthValue) {
        String jpql =
                "select new com.tranhuudat.prms.dto.resource_allocation.ResourceAllocationDto(entity) "
                        + "from ResourceAllocation entity "
                        + "left join entity.user u "
                        + "where (entity.voided is null or entity.voided = false) "
                        + "and year(entity.resourceMonth) = :year "
                        + "and month(entity.resourceMonth) = :monthValue "
                        + "order by lower(coalesce(u.fullName, u.username, '')) asc, entity.role asc";
        return entityManager
                .createQuery(jpql, ResourceAllocationDto.class)
                .setParameter("year", year)
                .setParameter("monthValue", monthValue)
                .getResultList();
    }
}
