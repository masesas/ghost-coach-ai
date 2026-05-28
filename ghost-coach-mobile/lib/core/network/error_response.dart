class ErrorResponse {
  const ErrorResponse({required this.code, this.fieldErrors});

  final String code;
  final List<FieldError>? fieldErrors;

  factory ErrorResponse.fromJson(Map<String, dynamic> json) {
    final raw = json['fieldErrors'] as List<dynamic>?;
    return ErrorResponse(
      code: json['code'] as String? ?? 'UNKNOWN',
      fieldErrors: raw
          ?.map((e) => FieldError.fromJson(e as Map<String, dynamic>))
          .toList(growable: false),
    );
  }
}

class FieldError {
  const FieldError({required this.field, required this.message});

  final String field;
  final String message;

  factory FieldError.fromJson(Map<String, dynamic> json) => FieldError(
        field: json['field'] as String? ?? '',
        message: json['message'] as String? ?? '',
      );
}
