part of 'register_bloc.dart';

sealed class RegisterEvent extends Equatable {
  const RegisterEvent();

  @override
  List<Object?> get props => <Object?>[];
}

class RegisterSubmitted extends RegisterEvent {
  const RegisterSubmitted({
    required this.email,
    required this.password,
    required this.fullName,
    required this.sport,
    required this.position,
    required this.experienceLevel,
  });

  final String email;
  final String password;
  final String fullName;
  final Sport sport;
  final String position;
  final ExperienceLevel experienceLevel;

  @override
  List<Object?> get props => <Object?>[email, sport, position];
}
