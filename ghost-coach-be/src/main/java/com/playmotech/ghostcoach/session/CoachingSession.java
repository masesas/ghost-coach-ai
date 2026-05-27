package com.playmotech.ghostcoach.session;

import com.playmotech.ghostcoach.ai.dto.FeedbackReport;
import com.playmotech.ghostcoach.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "coaching_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoachingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "image_path", nullable = false, length = 500)
    private String imagePath;

    @Column(name = "overall_score", precision = 3, scale = 1)
    private BigDecimal overallScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "strengths", columnDefinition = "jsonb")
    private List<String> strengths;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "areas_to_improve", columnDefinition = "jsonb")
    private List<FeedbackReport.AreaToImprove> areasToImprove;

    @Column(name = "priority_fix", columnDefinition = "TEXT")
    private String priorityFix;

    @Column(name = "drill_suggestion", columnDefinition = "TEXT")
    private String drillSuggestion;

    @Enumerated(EnumType.STRING)
    @Column(name = "confidence_level", length = 10)
    private ConfidenceLevel confidenceLevel;

    @Column(name = "raw_ai_response", columnDefinition = "TEXT")
    private String rawAiResponse;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
