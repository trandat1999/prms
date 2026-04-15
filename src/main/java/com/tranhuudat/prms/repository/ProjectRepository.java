package com.tranhuudat.prms.repository;

import com.tranhuudat.prms.dto.project.ProjectDTO;
import com.tranhuudat.prms.dto.project.ProjectSearchRequest;
import com.tranhuudat.prms.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID id);

    @Query(value = "select new com.tranhuudat.prms.dto.project.ProjectDTO(entity) from Project entity " +
            "where (:#{#request.keyword} is null or :#{#request.keyword} = '' or entity.code like concat('%',:#{#request.keyword},'%') or entity.name like concat('%',:#{#request.keyword},'%'))",
    countQuery = "select count(entity) from Project entity " +
            "where (:#{#request.keyword} is null or :#{#request.keyword} = '' or entity.code like concat('%',:#{#request.keyword},'%') or entity.name like concat('%',:#{#request.keyword},'%'))")
    Page<ProjectDTO>  getPages(ProjectSearchRequest request, Pageable pageable);
}
