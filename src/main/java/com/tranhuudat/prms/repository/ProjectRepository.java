package com.tranhuudat.prms.repository;

import com.tranhuudat.prms.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID id);

    @EntityGraph(attributePaths = "manager")
    @Override
    Page<Project> findAll(Specification<Project> spec, Pageable pageable);
}
