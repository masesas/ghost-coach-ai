class SessionSummary {
  const SessionSummary({
    required this.id,
    required this.imageUrl,
    required this.overallScore,
    required this.priorityFix,
    required this.createdAt,
  });

  final int id;
  final String imageUrl;
  final double? overallScore;
  final String? priorityFix;
  final DateTime createdAt;

  factory SessionSummary.fromJson(Map<String, dynamic> json) => SessionSummary(
        id: (json['id'] as num).toInt(),
        imageUrl: json['imageUrl'] as String,
        overallScore: (json['overallScore'] as num?)?.toDouble(),
        priorityFix: json['priorityFix'] as String?,
        createdAt: DateTime.parse(json['createdAt'] as String),
      );
}
