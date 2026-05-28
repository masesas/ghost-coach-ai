import 'package:dio/dio.dart';
import 'package:ghost_coach/core/network/api_exception.dart';
import 'package:ghost_coach/core/network/api_response.dart';
import 'package:ghost_coach/core/network/error_response.dart';

class ApiUnwrap {
  ApiUnwrap._();

  /// Eksekusi request lalu unwrap `ApiResponse<T>.data`. Lempar
  /// [ApiException] jika error/network gagal.
  static Future<T> call<T>({
    required Future<Response<dynamic>> request,
    required T? Function(Object? rawData) parseData,
  }) async {
    try {
      final response = await request;
      final body = response.data;
      if (body is! Map<String, dynamic>) {
        throw const ApiException(
          code: 'MALFORMED_BODY',
          message: 'Invalid response shape',
        );
      }
      final envelope = ApiResponse<T>.fromJson(body, parseData);
      if (envelope.error != null) {
        throw ApiException(
          code: envelope.error!.code,
          message: envelope.message,
          statusCode: envelope.status,
          fieldErrors: envelope.error!.fieldErrors
              ?.map(
                (e) => FieldErrorView(field: e.field, message: e.message),
              )
              .toList(growable: false),
        );
      }
      if (envelope.data == null) {
        throw ApiException(
          code: 'EMPTY_DATA',
          message: envelope.message,
          statusCode: envelope.status,
        );
      }
      return envelope.data as T;
    } on DioException catch (e) {
      final body = e.response?.data;
      if (body is Map<String, dynamic> && body['error'] is Map) {
        final err = ErrorResponse.fromJson(
          body['error'] as Map<String, dynamic>,
        );
        throw ApiException(
          code: err.code,
          message: (body['message'] as String?) ?? e.message ?? 'Request failed',
          statusCode: e.response?.statusCode,
          fieldErrors: err.fieldErrors
              ?.map(
                (fe) => FieldErrorView(field: fe.field, message: fe.message),
              )
              .toList(growable: false),
        );
      }
      throw ApiException(
        code: 'NETWORK_ERROR',
        message: e.message ?? 'Network error',
        statusCode: e.response?.statusCode,
      );
    } on ApiException {
      rethrow;
    } catch (e) {
      throw ApiException.unknown(e);
    }
  }
}
