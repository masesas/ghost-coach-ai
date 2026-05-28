extension NumX on num {
  /// 1024 → "1 KB", 1_500_000 → "1.4 MB"
  String get fileSize {
    if (this < 1024) return '$this B';
    if (this < 1024 * 1024) return '${(this / 1024).toStringAsFixed(0)} KB';
    return '${(this / (1024 * 1024)).toStringAsFixed(1)} MB';
  }
}
