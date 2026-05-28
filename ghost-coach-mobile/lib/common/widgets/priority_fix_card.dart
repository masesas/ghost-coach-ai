import 'package:flutter/material.dart';
import 'package:ghost_coach/common/theme/app_colors.dart';

class PriorityFixCard extends StatelessWidget {
  const PriorityFixCard({required this.fix, super.key});

  final String? fix;

  @override
  Widget build(BuildContext context) {
    final value = fix;
    if (value == null || value.isEmpty) return const SizedBox.shrink();
    return Card(
      color: AppColors.warningBg,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: const BorderSide(color: AppColors.warning, width: 0.5),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Row(
              children: [
                Icon(Icons.flag, size: 18, color: AppColors.warning),
                SizedBox(width: 8),
                Text(
                  'Priority Fix',
                  style: TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w700,
                    color: AppColors.warning,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Text(
              value,
              style: const TextStyle(
                fontSize: 14,
                color: AppColors.textPrimary,
                height: 1.4,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
