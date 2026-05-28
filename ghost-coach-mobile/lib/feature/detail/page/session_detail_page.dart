import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:ghost_coach/common/theme/app_colors.dart';
import 'package:ghost_coach/common/widgets/auth_cached_image.dart';
import 'package:ghost_coach/common/widgets/error_state.dart';
import 'package:ghost_coach/common/widgets/feedback_report_view.dart';
import 'package:ghost_coach/core/di/injector.dart';
import 'package:ghost_coach/core/extension/datetime_x.dart';
import 'package:ghost_coach/feature/chat/widget/chat_panel.dart';
import 'package:ghost_coach/feature/detail/bloc/detail/detail_bloc.dart';
import 'package:go_router/go_router.dart';

class SessionDetailPage extends StatelessWidget {
  const SessionDetailPage({required this.sessionId, super.key});

  final int sessionId;

  @override
  Widget build(BuildContext context) => BlocProvider<DetailBloc>(
        create: (_) =>
            getIt<DetailBloc>()..add(DetailRequested(sessionId)),
        child: _DetailView(sessionId: sessionId),
      );
}

class _DetailView extends StatelessWidget {
  const _DetailView({required this.sessionId});

  final int sessionId;

  @override
  Widget build(BuildContext context) =>
      BlocBuilder<DetailBloc, DetailState>(
        builder: (ctx, state) {
          if (state is DetailLoading) {
            return const Center(child: CircularProgressIndicator());
          }
          if (state is DetailFailed) {
            return ErrorState(
              message: state.error.message,
              onRetry: () => ctx
                  .read<DetailBloc>()
                  .add(DetailRequested(sessionId)),
            );
          }
          final s = (state as DetailLoaded).session;
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Align(
                  alignment: Alignment.centerLeft,
                  child: TextButton.icon(
                    onPressed: () => ctx.go('/history'),
                    icon: const Icon(Icons.chevron_left, size: 18),
                    label: const Text('Back to History'),
                    style: TextButton.styleFrom(
                      padding: EdgeInsets.zero,
                      minimumSize: const Size(0, 32),
                      tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                    ),
                  ),
                ),
                const SizedBox(height: 4),
                ClipRRect(
                  borderRadius: BorderRadius.circular(12),
                  child: AuthCachedImage(
                    path: s.imageUrl,
                    height: 280,
                    fit: BoxFit.contain,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  'Uploaded ${s.createdAt.formatted}',
                  style: const TextStyle(
                    color: AppColors.textMuted,
                    fontSize: 12,
                  ),
                ),
                const SizedBox(height: 16),
                FeedbackReportView(session: s),
                const SizedBox(height: 16),
                ChatPanel(sessionId: s.id),
                const SizedBox(height: 24),
              ],
            ),
          );
        },
      );
}
