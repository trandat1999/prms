package com.tranhuudat.prms.repository;

import com.tranhuudat.prms.dto.autocomplete.AutocompleteSearchRequest;
import com.tranhuudat.prms.dto.autocomplete.ProjectAutocompleteDto;
import com.tranhuudat.prms.dto.project.ProjectDTO;
import com.tranhuudat.prms.dto.project.ProjectSearchRequest;
import com.tranhuudat.prms.entity.Project;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID id);

    default Page<ProjectDTO> getPages(EntityManager entityManager, ProjectSearchRequest request, Pageable pageable) {
        String select = "select new com.tranhuudat.prms.dto.project.ProjectDTO(entity) from Project entity ";
        String count = "select count(entity) from Project entity ";

        List<String> where = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        if (Objects.nonNull(request) && StringUtils.hasText(request.getKeyword())) {
            where.add("(entity.code like :kw or entity.name like :kw)");
            params.put("kw", "%" + request.getKeyword().trim() + "%");
        }

        String jpqlWhere = where.isEmpty() ? "" : " where " + String.join(" and ", where);
        String orderBy = " order by entity.createdDate desc";

        TypedQuery<ProjectDTO> query =
                entityManager.createQuery(select + jpqlWhere + orderBy, ProjectDTO.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(count + jpqlWhere, Long.class);

        for (Map.Entry<String, Object> e : params.entrySet()) {
            query.setParameter(e.getKey(), e.getValue());
            countQuery.setParameter(e.getKey(), e.getValue());
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<ProjectDTO> content = query.getResultList();
        Long total = countQuery.getSingleResult();
        return new PageImpl<>(content, pageable, Objects.nonNull(total) ? total : 0L);
    }

    default Page<ProjectAutocompleteDto> autocompleteProjects(
            EntityManager entityManager, AutocompleteSearchRequest request, Pageable pageable) {
        String select =
                "select new com.tranhuudat.prms.dto.autocomplete.ProjectAutocompleteDto(entity) from Project entity ";
        String count = "select count(entity) from Project entity ";

        List<String> where = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        where.add("(entity.voided is null or entity.voided = false)");

        if (Objects.nonNull(request)) {
            if (request.getIds() != null && !request.getIds().isEmpty()) {
                where.add("entity.id in :ids");
                params.put("ids", request.getIds());
            }
            if (StringUtils.hasText(request.getKeyword())) {
                where.add("(lower(entity.code) like :kw or lower(entity.name) like :kw)");
                params.put("kw", "%" + request.getKeyword().trim().toLowerCase() + "%");
            }
        }

        String jpqlWhere = " where " + String.join(" and ", where);
        String orderBy = " order by entity.createdDate desc";

        TypedQuery<ProjectAutocompleteDto> query =
                entityManager.createQuery(select + jpqlWhere + orderBy, ProjectAutocompleteDto.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(count + jpqlWhere, Long.class);

        for (Map.Entry<String, Object> e : params.entrySet()) {
            query.setParameter(e.getKey(), e.getValue());
            countQuery.setParameter(e.getKey(), e.getValue());
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<ProjectAutocompleteDto> content = query.getResultList();
        Long total = countQuery.getSingleResult();
        return new PageImpl<>(content, pageable, Objects.nonNull(total) ? total : 0L);
    }
}
