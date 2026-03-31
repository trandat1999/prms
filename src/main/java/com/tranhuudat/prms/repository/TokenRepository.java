package com.tranhuudat.prms.repository;

import com.tranhuudat.prms.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {
    Optional<Token> findByToken(String token);
    List<Token> findAllByUsernameAndExpiredFalseAndRevokedFalse(String username);
}