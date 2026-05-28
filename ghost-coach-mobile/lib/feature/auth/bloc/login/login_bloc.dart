import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:ghost_coach/core/network/api_exception.dart';
import 'package:ghost_coach/data/model/auth/auth_response.dart';
import 'package:ghost_coach/data/model/auth/login_request.dart';
import 'package:ghost_coach/data/repository/auth_repository.dart';

part 'login_event.dart';
part 'login_state.dart';

class LoginBloc extends Bloc<LoginEvent, LoginState> {
  LoginBloc(this._repo) : super(const LoginIdle()) {
    on<LoginSubmitted>(_onSubmit);
  }

  final AuthRepository _repo;

  Future<void> _onSubmit(
    LoginSubmitted event,
    Emitter<LoginState> emit,
  ) async {
    emit(const LoginInProgress());
    try {
      final res = await _repo.login(
        LoginRequest(email: event.email.trim(), password: event.password),
      );
      emit(LoginSuccess(res));
    } on ApiException catch (err) {
      emit(LoginFailure(err));
    }
  }
}
