import 'package:flutter/material.dart';
import 'package:ghost_coach/common/theme/app_colors.dart';
import 'package:ghost_coach/common/widgets/auth_cached_image.dart';
import 'package:ghost_coach/common/widgets/score_badge.dart';
import 'package:ghost_coach/core/extension/datetime_x.dart';
import 'package:ghost_coach/data/model/session/session_summary.dart';
import 'package:go_router/go_router.dart';

class SessionCard extends StatelessWidget {
  const SessionCard({required this.session, super.key});

  final SessionSummary session;

  @override
  Widget build(BuildContext context) => GestureDetector(
        onTap: () => context.go('/history/${session.id}'),
        child: Card(
          clipBehavior: Clip.antiAlias,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              AspectRatio(
                aspectRatio: 16 / 9,
                child: AuthCachedImage(path: session.imageUrl),
              ),
              Padding(
                padding: const EdgeInsets.all(12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        ScoreBadge(score: session.overallScore),
                        Text(
                          session.createdAt.relative,
                          style: const TextStyle(
                            fontSize: 12,
                            color: AppColors.textMuted,
                          ),
                        ),
                      ],
                    ),
                    if (session.priorityFix != null) ...[
                      const SizedBox(height: 8),
                      Text(
                        session.priorityFix!,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(
                          fontSize: 13,
                          color: AppColors.textSecondary,
                        ),
                      ),
                    ],
                  ],
                ),
              ),
            ],
          ),
        ),
      );
}
