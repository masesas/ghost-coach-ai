import 'package:dio/dio.dart';
import 'package:ghost_coach/core/network/api_unwrap.dart';
import 'package:ghost_coach/data/model/auth/auth_response.dart';
import 'package:ghost_coach/data/model/auth/login_request.dart';
import 'package:ghost_coach/data/model/auth/profile.dart';
import 'package:ghost_coach/data/model/auth/register_request.dart';

class AuthRemoteSource {
  AuthRemoteSource(this._dio);

  final Dio _dio;

  Future<AuthResponse> login(LoginRequest req) => ApiUnwrap.call<AuthResponse>(
        request: _dio.post<dynamic>('/auth/login', data: req.toJson()),
        parseData: (raw) =>
            AuthResponse.fromJson(raw! as Map<String, dynamic>),
      );

  Future<AuthResponse> register(RegisterRequest req) =>
      ApiUnwrap.call<AuthResponse>(
        request: _dio.post<dynamic>('/auth/register', data: req.toJson()),
        parseData: (raw) =>
            AuthResponse.fromJson(raw! as Map<String, dynamic>),
      );

  Future<Profile> me() => ApiUnwrap.call<Profile>(
        request: _dio.get<dynamic>('/users/me'),
        parseData: (raw) => Profile.fromJson(raw! as Map<String, dynamic>),
      );
}
