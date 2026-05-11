package com.tranhuudat.prms.repository;

import com.tranhuudat.prms.dto.autocomplete.AutocompleteSearchRequest;
import com.tranhuudat.prms.dto.autocomplete.SkillAutocompleteDto;
import com.tranhuudat.prms.dto.skill.SkillDto;
import com.tranhuudat.prms.dto.skill.SkillSearchRequest;
import com.tranhuudat.prms.entity.Skill;
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
public interface SkillRepository extends JpaRepository<Skill, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    default Page<SkillDto> getPages(EntityManager entityManager, SkillSearchRequest request, Pageable pageable) {
        String select = "select new com.tranhuudat.prms.dto.skill.SkillDto(entity) from Skill entity ";
        String count = "select count(entity) from Skill entity ";

        List<String> where = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        where.add("(entity.voided is null or entity.voided = false)");

        if (Objects.nonNull(request)) {
            if (!CollectionUtils.isEmpty(request.getIds())) {
                where.add("entity.id in :ids");
                params.put("ids", request.getIds());
            }
            if (StringUtils.hasText(request.getCategory())) {
                where.add("lower(entity.category) like :category");
                params.put("category", "%" + request.getCategory().trim().toLowerCase() + "%");
            }
            if (StringUtils.hasText(request.getKeyword())) {
                where.add(
                        "("
                                + "lower(entity.name) like :kw "
                                + "or lower(entity.code) like :kw "
                                + "or lower(entity.category) like :kw "
                                + "or lower(entity.description) like :kw"
                                + ")");
                params.put("kw", "%" + request.getKeyword().trim().toLowerCase() + "%");
            }
        }

        String jpqlWhere = " where " + String.join(" and ", where);
        String orderBy = " order by entity.createdDate desc";

        TypedQuery<SkillDto> query = entityManager.createQuery(select + jpqlWhere + orderBy, SkillDto.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(count + jpqlWhere, Long.class);

        for (Map.Entry<String, Object> e : params.entrySet()) {
            query.setParameter(e.getKey(), e.getValue());
            countQuery.setParameter(e.getKey(), e.getValue());
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<SkillDto> content = query.getResultList();
        Long total = countQuery.getSingleResult();
        return new PageImpl<>(content, pageable, Objects.nonNull(total) ? total : 0L);
    }

    default Page<SkillAutocompleteDto> autocompleteSkills(
            EntityManager entityManager, AutocompleteSearchRequest request, Pageable pageable) {
        String select =
                "select new com.tranhuudat.prms.dto.autocomplete.SkillAutocompleteDto(entity.id, entity.code, entity.name, entity.category) "
                        + "from Skill entity ";
        String count = "select count(entity) from Skill entity ";

        List<String> where = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        where.add("(entity.voided is null or entity.voided = false)");

        if (Objects.nonNull(request)) {
            if (!CollectionUtils.isEmpty(request.getIds())) {
                where.add("entity.id in :ids");
                params.put("ids", request.getIds());
            }
            if (StringUtils.hasText(request.getKeyword())) {
                where.add("(lower(entity.name) like :kw or lower(entity.code) like :kw)");
                params.put("kw", "%" + request.getKeyword().trim().toLowerCase() + "%");
            }
        }

        String jpqlWhere = " where " + String.join(" and ", where);
        String orderBy = " order by entity.createdDate desc";

        TypedQuery<SkillAutocompleteDto> query =
                entityManager.createQuery(select + jpqlWhere + orderBy, SkillAutocompleteDto.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(count + jpqlWhere, Long.class);

        for (Map.Entry<String, Object> e : params.entrySet()) {
            query.setParameter(e.getKey(), e.getValue());
            countQuery.setParameter(e.getKey(), e.getValue());
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<SkillAutocompleteDto> content = query.getResultList();
        Long total = countQuery.getSingleResult();
        return new PageImpl<>(content, pageable, Objects.nonNull(total) ? total : 0L);
    }
}

