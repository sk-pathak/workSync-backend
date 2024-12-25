package org.openlake.workSync.app.repo;

import org.openlake.workSync.app.domain.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepo extends JpaRepository<ChatEntity, Long> {
    List<ChatEntity> findByProjectId(String projectId);
}
