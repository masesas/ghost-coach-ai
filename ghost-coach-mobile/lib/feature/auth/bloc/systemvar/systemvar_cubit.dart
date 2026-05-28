import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:ghost_coach/core/network/api_exception.dart';
import 'package:ghost_coach/data/model/systemvar/system_var_item.dart';
import 'package:ghost_coach/data/repository/systemvar_repository.dart';

part 'systemvar_state.dart';

class SystemVarCubit extends Cubit<SystemVarState> {
  SystemVarCubit(this._repo) : super(SystemVarState.initial);

  final SystemVarRepository _repo;

  Future<void> load(String category, {String? scope}) async {
    emit(state.copyWith(loading: true, clearError: true));
    try {
      final items = await _repo.list(category, scope: scope);
      emit(state.copyWith(loading: false, items: items));
    } on ApiException catch (e) {
      emit(state.copyWith(loading: false, error: e));
    }
  }
}
