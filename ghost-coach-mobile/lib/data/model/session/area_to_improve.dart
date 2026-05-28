class AreaToImprove {
  const AreaToImprove({required this.flaw, required this.explanation});

  final String flaw;
  final String explanation;

  factory AreaToImprove.fromJson(Map<String, dynamic> json) => AreaToImprove(
        flaw: json['flaw'] as String? ?? '',
        explanation: json['explanation'] as String? ?? '',
      );
}
