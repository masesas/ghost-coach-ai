enum ExperienceLevel {
  beginner('BEGINNER', 'Beginner'),
  intermediate('INTERMEDIATE', 'Intermediate'),
  advanced('ADVANCED', 'Advanced');

  const ExperienceLevel(this.wire, this.label);

  final String wire;
  final String label;

  static ExperienceLevel fromWire(String value) {
    for (final e in values) {
      if (e.wire == value) return e;
    }
    throw ArgumentError('Unknown level: $value');
  }
}
