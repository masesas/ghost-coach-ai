import 'package:ghost_coach/data/model/enums/experience_level.dart';
import 'package:ghost_coach/data/model/enums/sport.dart';

class Profile {
  const Profile({
    required this.id,
    required this.email,
    required this.fullName,
    required this.sport,
    required this.position,
    required this.experienceLevel,
    required this.createdAt,
  });

  final int id;
  final String email;
  final String fullName;
  final Sport sport;
  final String position;
  final ExperienceLevel experienceLevel;
  final DateTime createdAt;

  factory Profile.fromJson(Map<String, dynamic> json) => Profile(
        id: (json['id'] as num).toInt(),
        email: json['email'] as String,
        fullName: json['fullName'] as String,
        sport: Sport.fromWire(json['sport'] as String),
        position: json['position'] as String,
        experienceLevel:
            ExperienceLevel.fromWire(json['experienceLevel'] as String),
        createdAt: DateTime.parse(json['createdAt'] as String),
      );

  Map<String, dynamic> toJson() => <String, dynamic>{
        'id': id,
        'email': email,
        'fullName': fullName,
        'sport': sport.wire,
        'position': position,
        'experienceLevel': experienceLevel.wire,
        'createdAt': createdAt.toIso8601String(),
      };
}
