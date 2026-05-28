part of 'history_bloc.dart';

class HistoryState extends Equatable {
  const HistoryState({
    required this.loading,
    required this.refreshing,
    required this.page,
    required this.totalPages,
    required this.items,
    this.error,
  });

  final bool loading;
  final bool refreshing;
  final int page;
  final int totalPages;
  final List<SessionSummary> items;
  final ApiException? error;

  static const initial = HistoryState(
    loading: true,
    refreshing: false,
    page: 0,
    totalPages: 0,
    items: <SessionSummary>[],
  );

  HistoryState copyWith({
    bool? loading,
    bool? refreshing,
    int? page,
    int? totalPages,
    List<SessionSummary>? items,
    ApiException? error,
    bool clearError = false,
  }) =>
      HistoryState(
        loading: loading ?? this.loading,
        refreshing: refreshing ?? this.refreshing,
        page: page ?? this.page,
        totalPages: totalPages ?? this.totalPages,
        items: items ?? this.items,
        error: clearError ? null : (error ?? this.error),
      );

  @override
  List<Object?> get props => <Object?>[
        loading,
        refreshing,
        page,
        totalPages,
        items.length,
        error?.code,
      ];
}
