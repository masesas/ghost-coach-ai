part of 'register_bloc.dart';

sealed class RegisterState extends Equatable {
  const RegisterState();

  @override
  List<Object?> get props => <Object?>[];
}

class RegisterIdle extends RegisterState {
  const RegisterIdle();
}

class RegisterInProgress extends RegisterState {
  const RegisterInProgress();
}

class RegisterSuccess extends RegisterState {
  const RegisterSuccess(this.response);

  final AuthResponse response;

  @override
  List<Object?> get props => <Object?>[response.user.id];
}

class RegisterFailure extends RegisterState {
  const RegisterFailure(this.error);

  final ApiException error;

  @override
  List<Object?> get props => <Object?>[error.code, error.message];
}
