part of 'login_bloc.dart';

sealed class LoginState extends Equatable {
  const LoginState();

  @override
  List<Object?> get props => <Object?>[];
}

class LoginIdle extends LoginState {
  const LoginIdle();
}

class LoginInProgress extends LoginState {
  const LoginInProgress();
}

class LoginSuccess extends LoginState {
  const LoginSuccess(this.response);

  final AuthResponse response;

  @override
  List<Object?> get props => <Object?>[response.user.id];
}

class LoginFailure extends LoginState {
  const LoginFailure(this.error);

  final ApiException error;

  @override
  List<Object?> get props => <Object?>[error.code, error.message];
}
