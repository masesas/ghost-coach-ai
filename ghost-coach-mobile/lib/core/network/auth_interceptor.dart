import 'package:dio/dio.dart';
import 'package:ghost_coach/core/network/unauthorized_callback.dart';
import 'package:ghost_coach/core/storage/token_storage.dart';

class AuthInterceptor extends Interceptor {
  AuthInterceptor({
    required this.tokenStorage,
    required this.unauthorizedNotifier,
  });

  final TokenStorage tokenStorage;
  final UnauthorizedNotifier unauthorizedNotifier;

  @override
  void onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) {
    final token = tokenStorage.get();
    if (token != null && token.isNotEmpty) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    if (err.response?.statusCode == 401) {
      // Best-effort: clear stale token + notify listeners.
      // Future is intentionally not awaited — interceptor is sync.
      // ignore: unawaited_futures
      tokenStorage.clear();
      unauthorizedNotifier.notify();
    }
    handler.next(err);
  }
}
