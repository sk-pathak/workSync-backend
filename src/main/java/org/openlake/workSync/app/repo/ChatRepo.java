package org.openlake.workSync.app.repo;

import org.openlake.workSync.app.domain.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChatRepo extends JpaRepository<Chat, UUID> {
}
