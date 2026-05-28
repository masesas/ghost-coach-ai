import 'package:ghost_coach/core/network/error_response.dart';

class ApiResponse<T> {
  const ApiResponse({
    required this.status,
    required this.message,
    required this.timestamp,
    this.data,
    this.error,
  });

  final int status;
  final String message;
  final T? data;
  final ErrorResponse? error;
  final DateTime timestamp;

  factory ApiResponse.fromJson(
    Map<String, dynamic> json,
    T? Function(Object? rawData) parseData,
  ) {
    return ApiResponse<T>(
      status: (json['status'] as num).toInt(),
      message: json['message'] as String? ?? '',
      data: parseData(json['data']),
      error: json['error'] == null
          ? null
          : ErrorResponse.fromJson(json['error'] as Map<String, dynamic>),
      timestamp: DateTime.parse(json['timestamp'] as String),
    );
  }
}
