part of 'auth_bloc.dart';

class AuthState extends Equatable {
  const AuthState({required this.isAuthenticated, required this.profile});

  final bool isAuthenticated;
  final Profile? profile;

  static const initial = AuthState(isAuthenticated: false, profile: null);

  AuthState copyWith({bool? isAuthenticated, Profile? profile}) => AuthState(
        isAuthenticated: isAuthenticated ?? this.isAuthenticated,
        profile: profile ?? this.profile,
      );

  @override
  List<Object?> get props => <Object?>[isAuthenticated, profile?.id];
}
