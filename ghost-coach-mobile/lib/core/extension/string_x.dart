extension StringX on String {
  /// "CRICKET" → "Cricket"
  String get pascal {
    if (isEmpty) return this;
    return '${this[0].toUpperCase()}${substring(1).toLowerCase()}';
  }
}
