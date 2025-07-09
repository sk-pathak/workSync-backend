package org.openlake.workSync.app.repo;

import org.openlake.workSync.app.domain.entity.Message;
import org.openlake.workSync.app.dto.MessageResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepo extends JpaRepository<Message, UUID> {
    List<MessageResponseDTO> findByChatId(UUID chatId);
    Page<Message> findByChatId(UUID chatId, Pageable pageable);
}
