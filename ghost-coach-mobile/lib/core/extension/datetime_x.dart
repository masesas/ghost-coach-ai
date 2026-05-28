import 'package:intl/intl.dart';

extension DateTimeX on DateTime {
  /// "May 28, 2026 14:30"
  String get formatted => DateFormat('MMM d, y HH:mm').format(toLocal());

  /// "just now" / "5m ago" / "3h ago" / "2d ago" / formatted
  String get relative {
    final diff = DateTime.now().difference(toLocal());
    if (diff.inMinutes < 1) return 'just now';
    if (diff.inMinutes < 60) return '${diff.inMinutes}m ago';
    if (diff.inHours < 24) return '${diff.inHours}h ago';
    if (diff.inDays < 7) return '${diff.inDays}d ago';
    return formatted;
  }
}
