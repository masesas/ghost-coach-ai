import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:ghost_coach/common/widgets/empty_state.dart';
import 'package:ghost_coach/common/widgets/error_state.dart';
import 'package:ghost_coach/common/widgets/primary_button.dart';
import 'package:ghost_coach/core/di/injector.dart';
import 'package:ghost_coach/feature/history/bloc/history/history_bloc.dart';
import 'package:ghost_coach/feature/history/widget/progress_chart.dart';
import 'package:ghost_coach/feature/history/widget/session_card.dart';
import 'package:go_router/go_router.dart';

class HistoryPage extends StatelessWidget {
  const HistoryPage({super.key});

  @override
  Widget build(BuildContext context) => BlocProvider<HistoryBloc>(
        create: (_) =>
            getIt<HistoryBloc>()..add(const HistoryRequested()),
        child: const _HistoryView(),
      );
}

class _HistoryView extends StatelessWidget {
  const _HistoryView();

  Future<void> _onRefresh(BuildContext context) async {
    final bloc = context.read<HistoryBloc>()..add(const HistoryRefreshed());
    await bloc.stream.firstWhere((s) => !s.refreshing);
  }

  @override
  Widget build(BuildContext context) =>
      BlocBuilder<HistoryBloc, HistoryState>(
        builder: (ctx, state) {
          if (state.loading && state.items.isEmpty) {
            return const Center(child: CircularProgressIndicator());
          }
          if (state.error != null && state.items.isEmpty) {
            return ErrorState(
              message: state.error!.message,
              onRetry: () =>
                  ctx.read<HistoryBloc>().add(const HistoryRequested()),
            );
          }
          if (state.items.isEmpty) {
            return EmptyState(
              icon: Icons.photo_camera_outlined,
              title: 'No sessions yet',
              description:
                  'Upload your first stance to get personalized coaching.',
              action: PrimaryButton(
                label: 'Upload Stance',
                onPressed: () => ctx.go('/upload'),
              ),
            );
          }
          return RefreshIndicator(
            onRefresh: () => _onRefresh(ctx),
            child: ListView(
              padding: const EdgeInsets.all(16),
              children: [
                ProgressChart(sessions: state.items),
                if (state.items.isNotEmpty) const SizedBox(height: 12),
                ...state.items.map(
                  (s) => Padding(
                    padding: const EdgeInsets.only(bottom: 12),
                    child: SessionCard(session: s),
                  ),
                ),
                if (state.totalPages > 1)
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      OutlinedButton(
                        onPressed: state.page == 0
                            ? null
                            : () => ctx.read<HistoryBloc>().add(
                                  HistoryRequested(page: state.page - 1),
                                ),
                        child: const Text('Previous'),
                      ),
                      Text('Page ${state.page + 1} of ${state.totalPages}'),
                      OutlinedButton(
                        onPressed: state.page >= state.totalPages - 1
                            ? null
                            : () => ctx.read<HistoryBloc>().add(
                                  HistoryRequested(page: state.page + 1),
                                ),
                        child: const Text('Next'),
                      ),
                    ],
                  ),
              ],
            ),
          );
        },
      );
}
