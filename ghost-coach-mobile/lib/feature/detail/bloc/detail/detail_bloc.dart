import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:ghost_coach/core/network/api_exception.dart';
import 'package:ghost_coach/data/model/session/session_detail.dart';
import 'package:ghost_coach/data/repository/session_repository.dart';

part 'detail_event.dart';
part 'detail_state.dart';

class DetailBloc extends Bloc<DetailEvent, DetailState> {
  DetailBloc(this._repo) : super(const DetailLoading()) {
    on<DetailRequested>(_onRequested);
  }

  final SessionRepository _repo;

  Future<void> _onRequested(
    DetailRequested event,
    Emitter<DetailState> emit,
  ) async {
    emit(const DetailLoading());
    try {
      final s = await _repo.detail(event.id);
      emit(DetailLoaded(s));
    } on ApiException catch (err) {
      emit(DetailFailed(err));
    }
  }
}
