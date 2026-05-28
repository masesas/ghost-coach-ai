import 'package:ghost_coach/data/model/auth/profile.dart';

class AuthResponse {
  const AuthResponse({required this.token, required this.user});

  final String token;
  final Profile user;

  factory AuthResponse.fromJson(Map<String, dynamic> json) => AuthResponse(
        token: json['token'] as String,
        user: Profile.fromJson(json['user'] as Map<String, dynamic>),
      );
}
