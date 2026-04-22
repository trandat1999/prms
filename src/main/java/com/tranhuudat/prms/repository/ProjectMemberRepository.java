package com.tranhuudat.prms.repository;

import com.tranhuudat.prms.dto.autocomplete.AutocompleteSearchRequest;
import com.tranhuudat.prms.dto.autocomplete.UserAutocompleteDto;
import com.tranhuudat.prms.dto.project_member.ProjectMemberDto;
import com.tranhuudat.prms.dto.project_member.ProjectMemberSearchRequest;
import com.tranhuudat.prms.entity.ProjectMember;
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
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    boolean existsByProjectIdAndUserIdAndVoidedFalse(UUID projectId, UUID userId);

    boolean existsByProjectIdAndUserIdAndIdNotAndVoidedFalse(UUID projectId, UUID userId, UUID id);

    @Query(
            "select case when count(m) > 0 then true else false end from ProjectMember m "
                    + "where m.projectId = :projectId and m.userId = :userId "
                    + "and (m.voided is null or m.voided = false) "
                    + "and (m.active is null or m.active = true)")
    boolean existsActiveMember(@Param("projectId") UUID projectId, @Param("userId") UUID userId);

    @Query(
            "select case when count(m) > 0 then true else false end from ProjectMember m "
                    + "where m.projectId = :projectId and m.userId = :userId "
                    + "and m.roleInProject = :role "
                    + "and (m.voided is null or m.voided = false) "
                    + "and (m.active is null or m.active = true)")
    boolean existsActiveMemberWithRole(
            @Param("projectId") UUID projectId,
            @Param("userId") UUID userId,
            @Param("role") String role);

    default Page<ProjectMemberDto> getPages(
            EntityManager entityManager, ProjectMemberSearchRequest request, Pageable pageable) {
        String select =
                "select new com.tranhuudat.prms.dto.project_member.ProjectMemberDto(entity) "
                        + "from ProjectMember entity "
                        + "left join entity.project p "
                        + "left join entity.user u ";
        String count =
                "select count(entity) "
                        + "from ProjectMember entity "
                        + "left join entity.project p "
                        + "left join entity.user u ";

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
            if (Objects.nonNull(request.getUserId())) {
                where.add("entity.userId = :userId");
                params.put("userId", request.getUserId());
            }
            if (Objects.nonNull(request.getRoleInProject())) {
                where.add("entity.roleInProject = :roleInProject");
                params.put("roleInProject", request.getRoleInProject());
            }
            if (Objects.nonNull(request.getActive())) {
                where.add("entity.active = :active");
                params.put("active", request.getActive());
            }
            if (StringUtils.hasText(request.getKeyword())) {
                where.add("("
                        + "lower(p.code) like :kw "
                        + "or lower(p.name) like :kw "
                        + "or lower(u.username) like :kw "
                        + "or lower(u.fullName) like :kw"
                        + ")");
                params.put("kw", "%" + request.getKeyword().trim().toLowerCase() + "%");
            }
        }

        String jpqlWhere = " where " + String.join(" and ", where);
        String orderBy = " order by entity.createdDate desc";

        TypedQuery<ProjectMemberDto> query =
                entityManager.createQuery(select + jpqlWhere + orderBy, ProjectMemberDto.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(count + jpqlWhere, Long.class);

        for (Map.Entry<String, Object> e : params.entrySet()) {
            query.setParameter(e.getKey(), e.getValue());
            countQuery.setParameter(e.getKey(), e.getValue());
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<ProjectMemberDto> content = query.getResultList();
        Long total = countQuery.getSingleResult();
        return new PageImpl<>(content, pageable, Objects.nonNull(total) ? total : 0L);
    }

    default Page<UserAutocompleteDto> autocompleteActiveMembers(
            EntityManager entityManager, AutocompleteSearchRequest request, Pageable pageable) {
        if (Objects.isNull(request) || Objects.isNull(request.getProjectId())) {
            return new PageImpl<>(List.of(), pageable, 0L);
        }

        String select = "select new com.tranhuudat.prms.dto.autocomplete.UserAutocompleteDto(u) "
                + "from ProjectMember m join m.user u ";
        String count = "select count(m) from ProjectMember m join m.user u ";

        List<String> where = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        where.add("(m.voided is null or m.voided = false)");
        where.add("(m.active is null or m.active = true)");
        where.add("(u.voided is null or u.voided = false)");
        where.add("m.projectId = :projectId");
        params.put("projectId", request.getProjectId());

        if (!CollectionUtils.isEmpty(request.getIds())) {
            where.add("u.id in :ids");
            params.put("ids", request.getIds());
        }
        if (StringUtils.hasText(request.getKeyword())) {
            where.add("("
                    + "lower(u.username) like :kw "
                    + "or lower(u.fullName) like :kw "
                    + "or lower(u.email) like :kw"
                    + ")");
            params.put("kw", "%" + request.getKeyword().trim().toLowerCase() + "%");
        }

        String jpqlWhere = " where " + String.join(" and ", where);
        String orderBy = " order by coalesce(u.fullName, '') asc, u.username asc";

        TypedQuery<UserAutocompleteDto> query =
                entityManager.createQuery(select + jpqlWhere + orderBy, UserAutocompleteDto.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(count + jpqlWhere, Long.class);

        for (Map.Entry<String, Object> e : params.entrySet()) {
            query.setParameter(e.getKey(), e.getValue());
            countQuery.setParameter(e.getKey(), e.getValue());
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<UserAutocompleteDto> content = query.getResultList();
        Long total = countQuery.getSingleResult();
        return new PageImpl<>(content, pageable, Objects.nonNull(total) ? total : 0L);
    }
}
