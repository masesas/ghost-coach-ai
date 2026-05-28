enum ChatRole {
  user('USER'),
  assistant('ASSISTANT');

  const ChatRole(this.wire);

  final String wire;

  static ChatRole fromWire(String value) {
    for (final r in values) {
      if (r.wire == value) return r;
    }
    throw ArgumentError('Unknown chat role: $value');
  }
}
