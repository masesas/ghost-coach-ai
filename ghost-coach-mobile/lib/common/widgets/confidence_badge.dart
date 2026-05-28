import 'package:flutter/material.dart';
import 'package:ghost_coach/common/theme/app_colors.dart';
import 'package:ghost_coach/data/model/session/confidence_level.dart';

class ConfidenceBadge extends StatelessWidget {
  const ConfidenceBadge({required this.level, super.key});

  final ConfidenceLevel level;

  ({Color text, Color bg}) _palette() {
    switch (level) {
      case ConfidenceLevel.high:
        return (text: AppColors.success, bg: AppColors.successBg);
      case ConfidenceLevel.medium:
        return (text: AppColors.warning, bg: AppColors.warningBg);
      case ConfidenceLevel.low:
        return (text: AppColors.danger, bg: AppColors.dangerBg);
    }
  }

  @override
  Widget build(BuildContext context) {
    final p = _palette();
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: p.bg,
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        'Confidence: ${level.label}',
        style: TextStyle(
          color: p.text,
          fontSize: 12,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}
