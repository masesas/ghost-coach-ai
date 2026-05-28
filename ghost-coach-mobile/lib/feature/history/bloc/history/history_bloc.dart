import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:ghost_coach/core/network/api_exception.dart';
import 'package:ghost_coach/data/model/session/session_summary.dart';
import 'package:ghost_coach/data/repository/session_repository.dart';

part 'history_event.dart';
part 'history_state.dart';

class HistoryBloc extends Bloc<HistoryEvent, HistoryState> {
  HistoryBloc(this._repo) : super(HistoryState.initial) {
    on<HistoryRequested>(_onRequested);
    on<HistoryRefreshed>(_onRefreshed);
  }

  final SessionRepository _repo;

  Future<void> _onRequested(
    HistoryRequested event,
    Emitter<HistoryState> emit,
  ) async {
    emit(state.copyWith(loading: true, clearError: true));
    await _load(event.page, emit);
  }

  Future<void> _onRefreshed(
    HistoryRefreshed event,
    Emitter<HistoryState> emit,
  ) async {
    emit(state.copyWith(refreshing: true, clearError: true));
    await _load(0, emit);
  }

  Future<void> _load(int page, Emitter<HistoryState> emit) async {
    try {
      final res = await _repo.list(page: page);
      emit(HistoryState(
        loading: false,
        refreshing: false,
        page: res.page,
        totalPages: res.totalPages,
        items: res.content,
      ));
    } on ApiException catch (e) {
      emit(state.copyWith(loading: false, refreshing: false, error: e));
    }
  }
}
