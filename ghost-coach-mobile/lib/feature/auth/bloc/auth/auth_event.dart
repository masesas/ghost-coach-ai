part of 'auth_bloc.dart';

sealed class AuthEvent extends Equatable {
  const AuthEvent();

  @override
  List<Object?> get props => <Object?>[];
}

class AuthAppStarted extends AuthEvent {
  const AuthAppStarted();
}

class AuthSignedIn extends AuthEvent {
  const AuthSignedIn(this.profile);

  final Profile profile;

  @override
  List<Object?> get props => <Object?>[profile.id];
}

class AuthLoggedOut extends AuthEvent {
  const AuthLoggedOut();
}

class AuthSessionExpired extends AuthEvent {
  const AuthSessionExpired();
}
