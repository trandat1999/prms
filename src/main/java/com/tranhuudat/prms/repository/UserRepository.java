package com.tranhuudat.prms.repository;

import com.tranhuudat.prms.dto.autocomplete.AutocompleteSearchRequest;
import com.tranhuudat.prms.dto.autocomplete.UserAutocompleteDto;
import com.tranhuudat.prms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndEmail(String username, String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query(value = "select new com.tranhuudat.prms.dto.autocomplete.UserAutocompleteDto(entity) from User entity " +
            "where (entity.voided is null or entity.voided = false) " +
            "and (:#{#request.ids} is null or :#{#request.ids.size()} = 0 or entity.id in :#{#request.ids}) " +
            "and (:#{#request.keyword} is null or :#{#request.keyword} = '' " +
            "or lower(entity.username) like lower(concat('%',:#{#request.keyword},'%')) " +
            "or lower(entity.fullName) like lower(concat('%',:#{#request.keyword},'%')) " +
            "or lower(entity.email) like lower(concat('%',:#{#request.keyword},'%')))",
            countQuery = "select count(entity) from User entity " +
                    "where (entity.voided is null or entity.voided = false) " +
                    "and (:#{#request.ids} is null or :#{#request.ids.size()} = 0 or entity.id in :#{#request.ids}) " +
                    "and (:#{#request.keyword} is null or :#{#request.keyword} = '' " +
                    "or lower(entity.username) like lower(concat('%',:#{#request.keyword},'%')) " +
                    "or lower(entity.fullName) like lower(concat('%',:#{#request.keyword},'%')) " +
                    "or lower(entity.email) like lower(concat('%',:#{#request.keyword},'%')))")
    Page<UserAutocompleteDto> autocompleteUsers(AutocompleteSearchRequest request, Pageable pageable);
}
