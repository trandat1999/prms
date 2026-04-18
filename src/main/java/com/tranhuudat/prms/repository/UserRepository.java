package com.tranhuudat.prms.repository;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.tranhuudat.prms.dto.autocomplete.AutocompleteSearchRequest;
import com.tranhuudat.prms.dto.autocomplete.UserAutocompleteDto;
import com.tranhuudat.prms.dto.user.UserDetailDto;
import com.tranhuudat.prms.dto.user.UserSearchRequest;
import com.tranhuudat.prms.entity.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
        Optional<User> findByUsername(String username);

        Optional<User> findByUsernameAndEmail(String username, String email);

        boolean existsByUsername(String username);

        boolean existsByEmail(String email);

        boolean existsByUsernameAndIdNot(String username, UUID id);

        boolean existsByEmailAndIdNot(String email, UUID id);

        default Page<UserAutocompleteDto> autocompleteUsers(
                EntityManager entityManager, AutocompleteSearchRequest request, Pageable pageable) {
                String select =
                        "select new com.tranhuudat.prms.dto.autocomplete.UserAutocompleteDto(entity) from User entity ";
                String count = "select count(entity) from User entity ";

                List<String> where = new ArrayList<>();
                Map<String, Object> params = new HashMap<>();
                where.add("(entity.voided is null or entity.voided = false)");

                if (Objects.nonNull(request)) {
                        if (!CollectionUtils.isEmpty(request.getIds())) {
                                where.add("entity.id in :ids");
                                params.put("ids", request.getIds());
                        }
                        if (StringUtils.hasText(request.getKeyword())) {
                                where.add(
                                        "("
                                                + "lower(entity.username) like :kw "
                                                + "or lower(entity.fullName) like :kw "
                                                + "or lower(entity.email) like :kw"
                                                + ")");
                                params.put("kw", "%" + request.getKeyword().trim().toLowerCase() + "%");
                        }
                }

                String jpqlWhere = " where " + String.join(" and ", where);
                String orderBy = " order by entity.createdDate desc";

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

        default Page<UserDetailDto> getPages(EntityManager entityManager, UserSearchRequest request, Pageable pageable) {
                String select =
                        "select new com.tranhuudat.prms.dto.user.UserDetailDto(entity) from User entity ";
                String count = "select count(entity) from User entity ";

                List<String> where = new ArrayList<>();
                Map<String, Object> params = new HashMap<>();
                where.add("(entity.voided is null or entity.voided = false)");

                if (Objects.nonNull(request)) {
                        if (!CollectionUtils.isEmpty(request.getIds())) {
                                where.add("entity.id in :ids");
                                params.put("ids", request.getIds());
                        }
                        if (Objects.nonNull(request.getEnabled())) {
                                where.add("entity.enabled = :enabled");
                                params.put("enabled", request.getEnabled());
                        }
                        if (StringUtils.hasText(request.getKeyword())) {
                                where.add(
                                        "("
                                                + "lower(entity.username) like :kw "
                                                + "or lower(entity.fullName) like :kw "
                                                + "or lower(entity.email) like :kw"
                                                + ")");
                                params.put("kw", "%" + request.getKeyword().trim().toLowerCase() + "%");
                        }
                }

                String jpqlWhere = " where " + String.join(" and ", where);
                String orderBy = " order by entity.createdDate desc";

                TypedQuery<UserDetailDto> query =
                        entityManager.createQuery(select + jpqlWhere + orderBy, UserDetailDto.class);
                TypedQuery<Long> countQuery = entityManager.createQuery(count + jpqlWhere, Long.class);

                for (Map.Entry<String, Object> e : params.entrySet()) {
                        query.setParameter(e.getKey(), e.getValue());
                        countQuery.setParameter(e.getKey(), e.getValue());
                }

                query.setFirstResult((int) pageable.getOffset());
                query.setMaxResults(pageable.getPageSize());

                List<UserDetailDto> content = query.getResultList();
                Long total = countQuery.getSingleResult();
                return new PageImpl<>(content, pageable, Objects.nonNull(total) ? total : 0L);
        }
}
