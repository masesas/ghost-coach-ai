import 'package:flutter/material.dart';
import 'package:ghost_coach/common/theme/app_colors.dart';

class _Palette {
  const _Palette({required this.text, required this.bg, required this.border});
  final Color text;
  final Color bg;
  final Color border;
}

class ScoreBadge extends StatelessWidget {
  const ScoreBadge({required this.score, super.key});

  final double? score;

  _Palette _paletteFor(double s) {
    if (s >= 8) {
      return const _Palette(
        text: AppColors.success,
        bg: AppColors.successBg,
        border: AppColors.success,
      );
    }
    if (s >= 5) {
      return const _Palette(
        text: AppColors.warning,
        bg: AppColors.warningBg,
        border: AppColors.warning,
      );
    }
    return const _Palette(
      text: AppColors.danger,
      bg: AppColors.dangerBg,
      border: AppColors.danger,
    );
  }

  @override
  Widget build(BuildContext context) {
    final s = score;
    if (s == null) {
      return const Text('N/A', style: TextStyle(color: AppColors.textMuted));
    }
    final p = _paletteFor(s);
    final isInt = s.truncateToDouble() == s;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: p.bg,
        border: Border.all(color: p.border.withValues(alpha: 0.3)),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        '${s.toStringAsFixed(isInt ? 0 : 1)}/10',
        style: TextStyle(
          color: p.text,
          fontSize: 16,
          fontWeight: FontWeight.w800,
        ),
      ),
    );
  }
}
