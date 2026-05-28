import 'package:ghost_coach/data/model/session/area_to_improve.dart';
import 'package:ghost_coach/data/model/session/confidence_level.dart';

class SessionDetail {
  const SessionDetail({
    required this.id,
    required this.imageUrl,
    required this.overallScore,
    required this.strengths,
    required this.areasToImprove,
    required this.priorityFix,
    required this.drillSuggestion,
    required this.confidenceLevel,
    required this.createdAt,
  });

  final int id;
  final String imageUrl;
  final double? overallScore;
  final List<String> strengths;
  final List<AreaToImprove> areasToImprove;
  final String? priorityFix;
  final String? drillSuggestion;
  final ConfidenceLevel confidenceLevel;
  final DateTime createdAt;

  factory SessionDetail.fromJson(Map<String, dynamic> json) => SessionDetail(
        id: (json['id'] as num).toInt(),
        imageUrl: json['imageUrl'] as String,
        overallScore: (json['overallScore'] as num?)?.toDouble(),
        strengths: (json['strengths'] as List<dynamic>? ?? const <dynamic>[])
            .map((e) => e as String)
            .toList(growable: false),
        areasToImprove:
            (json['areasToImprove'] as List<dynamic>? ?? const <dynamic>[])
                .map((e) => AreaToImprove.fromJson(e as Map<String, dynamic>))
                .toList(growable: false),
        priorityFix: json['priorityFix'] as String?,
        drillSuggestion: json['drillSuggestion'] as String?,
        confidenceLevel:
            ConfidenceLevel.fromWire(json['confidenceLevel'] as String?),
        createdAt: DateTime.parse(json['createdAt'] as String),
      );
}
