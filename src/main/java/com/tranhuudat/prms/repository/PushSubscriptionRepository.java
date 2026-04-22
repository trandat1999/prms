package com.tranhuudat.prms.repository;

import com.tranhuudat.prms.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, UUID> {
    Optional<PushSubscription> findByEndpoint(String endpoint);

    List<PushSubscription> findByUserIdAndVoidedFalse(UUID userId);
}

