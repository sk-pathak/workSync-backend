package org.openlake.workSync.app.repo;

import org.openlake.workSync.app.domain.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, UUID> {
    List<Notification> findByRecipientId(UUID recipientId);
    Optional<Notification> findByIdAndRecipientId(UUID id, UUID recipientId);
    Page<Notification> findByRecipientId(UUID recipientId, Pageable pageable);
    
    List<Notification> findByRecipientIdAndStatusNot(UUID recipientId, org.openlake.workSync.app.domain.enumeration.NotificationStatus status);
    Page<Notification> findByRecipientIdAndStatusNot(UUID recipientId, org.openlake.workSync.app.domain.enumeration.NotificationStatus status, Pageable pageable);
    
    List<Notification> findByRecipientIdAndStatus(UUID recipientId, org.openlake.workSync.app.domain.enumeration.NotificationStatus status);
    Page<Notification> findByRecipientIdAndStatus(UUID recipientId, org.openlake.workSync.app.domain.enumeration.NotificationStatus status, Pageable pageable);
}
