import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:ghost_coach/core/network/api_exception.dart';
import 'package:ghost_coach/data/model/auth/auth_response.dart';
import 'package:ghost_coach/data/model/auth/register_request.dart';
import 'package:ghost_coach/data/model/enums/experience_level.dart';
import 'package:ghost_coach/data/model/enums/sport.dart';
import 'package:ghost_coach/data/repository/auth_repository.dart';

part 'register_event.dart';
part 'register_state.dart';

class RegisterBloc extends Bloc<RegisterEvent, RegisterState> {
  RegisterBloc(this._repo) : super(const RegisterIdle()) {
    on<RegisterSubmitted>(_onSubmit);
  }

  final AuthRepository _repo;

  Future<void> _onSubmit(
    RegisterSubmitted event,
    Emitter<RegisterState> emit,
  ) async {
    emit(const RegisterInProgress());
    try {
      final res = await _repo.register(
        RegisterRequest(
          email: event.email.trim(),
          password: event.password,
          fullName: event.fullName.trim(),
          sport: event.sport,
          position: event.position,
          experienceLevel: event.experienceLevel,
        ),
      );
      emit(RegisterSuccess(res));
    } on ApiException catch (err) {
      emit(RegisterFailure(err));
    }
  }
}
