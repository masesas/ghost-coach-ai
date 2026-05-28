import 'package:chucker_flutter/chucker_flutter.dart';
import 'package:dio/dio.dart';
import 'package:ghost_coach/core/env/app_env.dart';
import 'package:ghost_coach/core/network/auth_interceptor.dart';

class ApiClient {
  ApiClient._();

  static Dio create({
    required AppEnv env,
    required AuthInterceptor authInterceptor,
  }) {
    const overrideUrl = String.fromEnvironment('API_BASE_URL');
    final dio = Dio(
      BaseOptions(
        baseUrl: overrideUrl.isNotEmpty ? overrideUrl : env.baseUrl,
        connectTimeout: const Duration(seconds: 30),
        receiveTimeout: const Duration(seconds: 60),
        sendTimeout: const Duration(seconds: 60),
        headers: const <String, String>{'Content-Type': 'application/json'},
        responseType: ResponseType.json,
      ),
    );
    dio.interceptors.add(authInterceptor);
    if (env.showChucker) {
      dio.interceptors.add(ChuckerDioInterceptor());
    }
    return dio;
  }
}
