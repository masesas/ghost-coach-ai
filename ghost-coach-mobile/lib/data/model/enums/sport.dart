enum Sport {
  cricket('CRICKET', 'Cricket'),
  football('FOOTBALL', 'Football'),
  basketball('BASKETBALL', 'Basketball'),
  badminton('BADMINTON', 'Badminton');

  const Sport(this.wire, this.label);

  final String wire;
  final String label;

  static Sport fromWire(String value) {
    for (final s in values) {
      if (s.wire == value) return s;
    }
    throw ArgumentError('Unknown sport: $value');
  }
}
