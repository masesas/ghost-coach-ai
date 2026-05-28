package com.playmotech.ghostcoach.session;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<CoachingSession, Long> {

    Page<CoachingSession> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<CoachingSession> findByIdAndUserId(Long id, Long userId);
}
