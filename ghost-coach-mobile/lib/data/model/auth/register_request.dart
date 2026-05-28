import 'package:ghost_coach/data/model/enums/experience_level.dart';
import 'package:ghost_coach/data/model/enums/sport.dart';

class RegisterRequest {
  const RegisterRequest({
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

  Map<String, dynamic> toJson() => <String, dynamic>{
        'email': email,
        'password': password,
        'fullName': fullName,
        'sport': sport.wire,
        'position': position,
        'experienceLevel': experienceLevel.wire,
      };
}
