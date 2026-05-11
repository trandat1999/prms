package com.tranhuudat.prms.repository;

import com.tranhuudat.prms.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, UUID> {
    List<UserSkill> findByUserIdAndVoidedFalseOrderByCreatedDateDesc(UUID userId);

    List<UserSkill> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}

