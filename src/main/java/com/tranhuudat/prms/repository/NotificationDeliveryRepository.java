package com.tranhuudat.prms.repository;

import com.tranhuudat.prms.entity.NotificationDelivery;
import com.tranhuudat.prms.enums.NotificationDeliveryStatusEnum;
import jakarta.persistence.EntityManager;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, UUID> {

    @SuppressWarnings("unchecked")
    default List<UUID> claimBatch(EntityManager entityManager, int limit, Date now) {
        // Postgres: claim rows without blocking other workers
        String sql = """
                select id
                from tbl_notification_delivery
                where (voided is null or voided = false)
                  and status in ('PENDING','RETRY')
                  and (next_attempt_at is null or next_attempt_at <= :now)
                order by created_date asc
                limit :lim
                for update skip locked
                """;
        NativeQuery<?> q = (NativeQuery<?>) entityManager.createNativeQuery(sql).unwrap(NativeQuery.class);
        q.addScalar("id", StandardBasicTypes.UUID_CHAR);
        q.setParameter("now", now);
        q.setParameter("lim", limit);
        List<?> rows = q.getResultList();
        return rows.stream()
                .filter(r -> r != null)
                .map(r -> r instanceof UUID ? (UUID) r : UUID.fromString(String.valueOf(r)))
                .toList();
    }

    List<NotificationDelivery> findByIdIn(List<UUID> ids);

    long countByStatusAndVoidedFalse(NotificationDeliveryStatusEnum status);
}

