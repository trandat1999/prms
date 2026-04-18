package com.tranhuudat.prms.repository;

import com.tranhuudat.prms.dto.app_param.AppParamDto;
import com.tranhuudat.prms.dto.app_param.AppParamSearchRequest;
import com.tranhuudat.prms.entity.AppParam;
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
import java.util.Optional;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Repository
public interface AppParamRepository extends JpaRepository<AppParam, UUID> {

    @Query("select p.paramValue from AppParam p where (p.voided is null or p.voided = false) "
            + "and p.paramGroup = :group and p.paramName = :name")
    Optional<String> findParamValueByGroupAndName(@Param("group") String group, @Param("name") String name);

    @Query("select case when count(p) > 0 then true else false end from AppParam p " +
            "where (p.voided is null or p.voided = false) " +
            "and p.paramGroup = :group and p.paramType = :type and p.paramValue = :value")
    boolean existsActiveParamValue(@Param("group") String group, @Param("type") String type, @Param("value") String value);

    default Page<AppParamDto> getPages(EntityManager entityManager, AppParamSearchRequest request, Pageable pageable) {
        String select =
                "select new com.tranhuudat.prms.dto.app_param.AppParamDto(entity) from AppParam entity ";
        String count = "select count(entity) from AppParam entity ";

        List<String> where = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        where.add("(entity.voided is null or entity.voided = false)");

        if (Objects.nonNull(request)) {
            if (!CollectionUtils.isEmpty(request.getIds())) {
                where.add("entity.id in :ids");
                params.put("ids", request.getIds());
            }
            if (StringUtils.hasText(request.getParamGroup())) {
                where.add("lower(entity.paramGroup) like :paramGroup");
                params.put("paramGroup", "%" + request.getParamGroup().trim().toLowerCase() + "%");
            }
            if (StringUtils.hasText(request.getParamType())) {
                where.add("lower(entity.paramType) like :paramType");
                params.put("paramType", "%" + request.getParamType().trim().toLowerCase() + "%");
            }
            if (StringUtils.hasText(request.getKeyword())) {
                where.add(
                        "("
                                + "lower(entity.paramName) like :kw "
                                + "or lower(entity.paramValue) like :kw "
                                + "or lower(entity.description) like :kw"
                                + ")");
                params.put("kw", "%" + request.getKeyword().trim().toLowerCase() + "%");
            }
        }

        String jpqlWhere = " where " + String.join(" and ", where);
        String orderBy = " order by entity.createdDate desc";

        TypedQuery<AppParamDto> query = entityManager.createQuery(select + jpqlWhere + orderBy, AppParamDto.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(count + jpqlWhere, Long.class);

        for (Map.Entry<String, Object> e : params.entrySet()) {
            query.setParameter(e.getKey(), e.getValue());
            countQuery.setParameter(e.getKey(), e.getValue());
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<AppParamDto> content = query.getResultList();
        Long total = countQuery.getSingleResult();
        return new PageImpl<>(content, pageable, Objects.nonNull(total) ? total : 0L);
    }
}

