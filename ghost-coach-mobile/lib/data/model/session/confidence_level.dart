enum ConfidenceLevel {
  low('LOW', 'Low'),
  medium('MEDIUM', 'Medium'),
  high('HIGH', 'High');

  const ConfidenceLevel(this.wire, this.label);

  final String wire;
  final String label;

  static ConfidenceLevel fromWire(String? value) {
    if (value == null) return ConfidenceLevel.low;
    for (final v in values) {
      if (v.wire == value) return v;
    }
    return ConfidenceLevel.low;
  }
}
