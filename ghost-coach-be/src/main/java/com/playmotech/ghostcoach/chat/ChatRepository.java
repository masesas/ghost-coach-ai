package com.playmotech.ghostcoach.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId, Pageable pageable);

    // Sliding-window memory: fetch recent messages strictly BEFORE the just-saved user message.
    // Returns DESC for efficient LIMIT; service reverses to chronological order for the prompt.
    List<ChatMessage> findBySessionIdAndIdLessThanOrderByIdDesc(
            Long sessionId, Long beforeId, Pageable pageable);
}
