class ApiException implements Exception {
  const ApiException({
    required this.code,
    required this.message,
    this.statusCode,
    this.fieldErrors,
  });

  final String code;
  final String message;
  final int? statusCode;
  final List<FieldErrorView>? fieldErrors;

  factory ApiException.network(Object source) => const ApiException(
        code: 'NETWORK_ERROR',
        message: 'Network error. Periksa koneksi internet.',
      );

  factory ApiException.unknown(Object source) => ApiException(
        code: 'UNKNOWN',
        message: source.toString(),
      );

  @override
  String toString() => 'ApiException($code, $statusCode): $message';
}

class FieldErrorView {
  const FieldErrorView({required this.field, required this.message});

  final String field;
  final String message;
}
