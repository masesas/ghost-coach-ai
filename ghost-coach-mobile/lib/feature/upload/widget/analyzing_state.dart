import 'package:flutter/material.dart';
import 'package:ghost_coach/common/theme/app_colors.dart';

class AnalyzingState extends StatelessWidget {
  const AnalyzingState({super.key});

  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.all(24),
        decoration: BoxDecoration(
          color: AppColors.primary50,
          border: Border.all(color: AppColors.primary200),
          borderRadius: BorderRadius.circular(12),
        ),
        child: const Column(
          children: [
            Icon(
              Icons.center_focus_strong,
              size: 40,
              color: AppColors.primary600,
            ),
            SizedBox(height: 8),
            Text(
              'Analyzing your stance…',
              style: TextStyle(
                color: AppColors.primary900,
                fontWeight: FontWeight.w700,
                fontSize: 16,
              ),
            ),
            SizedBox(height: 4),
            Text(
              'Ghost Coach is reviewing your technique with AI. '
              'This may take a few seconds.',
              textAlign: TextAlign.center,
              style: TextStyle(color: AppColors.primary700),
            ),
            SizedBox(height: 16),
            SizedBox(
              width: 160,
              child: LinearProgressIndicator(),
            ),
          ],
        ),
      );
}
