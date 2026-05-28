import 'package:flutter/material.dart';
import 'package:ghost_coach/common/theme/app_colors.dart';
import 'package:ghost_coach/common/theme/app_typography.dart';
import 'package:ghost_coach/common/widgets/confidence_badge.dart';
import 'package:ghost_coach/common/widgets/priority_fix_card.dart';
import 'package:ghost_coach/common/widgets/score_badge.dart';
import 'package:ghost_coach/common/widgets/section_card.dart';
import 'package:ghost_coach/data/model/session/session_detail.dart';

class FeedbackReportView extends StatelessWidget {
  const FeedbackReportView({required this.session, super.key});

  final SessionDetail session;

  @override
  Widget build(BuildContext context) => Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            children: [
              ScoreBadge(score: session.overallScore),
              const SizedBox(width: 12),
              ConfidenceBadge(level: session.confidenceLevel),
            ],
          ),
          const SizedBox(height: 16),
          if (session.strengths.isNotEmpty)
            SectionCard(
              title: 'Strengths',
              icon: Icons.check_circle_outline,
              iconColor: AppColors.success,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: session.strengths
                    .map(
                      (s) => Padding(
                        padding: const EdgeInsets.only(bottom: 6),
                        child: Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              '✓ ',
                              style: TextStyle(color: AppColors.success),
                            ),
                            Expanded(
                              child: Text(s, style: AppTypography.bodyMedium),
                            ),
                          ],
                        ),
                      ),
                    )
                    .toList(growable: false),
              ),
            ),
          if (session.areasToImprove.isNotEmpty) ...[
            const SizedBox(height: 12),
            SectionCard(
              title: 'Areas to Improve',
              icon: Icons.warning_amber_outlined,
              iconColor: AppColors.warning,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: session.areasToImprove
                    .map(
                      (a) => Padding(
                        padding: const EdgeInsets.only(bottom: 10),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              a.flaw,
                              style: AppTypography.bodyMedium.copyWith(
                                fontWeight: FontWeight.w700,
                              ),
                            ),
                            const SizedBox(height: 2),
                            Text(
                              a.explanation,
                              style: AppTypography.bodySmall,
                            ),
                          ],
                        ),
                      ),
                    )
                    .toList(growable: false),
              ),
            ),
          ],
          const SizedBox(height: 12),
          PriorityFixCard(fix: session.priorityFix),
          if (session.drillSuggestion != null &&
              session.drillSuggestion!.isNotEmpty) ...[
            const SizedBox(height: 12),
            SectionCard(
              title: 'Drill Suggestion',
              icon: Icons.fitness_center,
              iconColor: AppColors.primary600,
              child: Text(
                session.drillSuggestion!,
                style: AppTypography.bodyMedium,
              ),
            ),
          ],
        ],
      );
}
