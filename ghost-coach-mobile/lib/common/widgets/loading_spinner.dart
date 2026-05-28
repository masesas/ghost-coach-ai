import 'package:flutter/material.dart';
import 'package:ghost_coach/common/theme/app_colors.dart';

class LoadingSpinner extends StatelessWidget {
  const LoadingSpinner({this.size = 20, this.color, super.key});

  final double size;
  final Color? color;

  @override
  Widget build(BuildContext context) => SizedBox(
        height: size,
        width: size,
        child: CircularProgressIndicator(
          strokeWidth: 2,
          valueColor: AlwaysStoppedAnimation<Color>(
            color ?? AppColors.primary600,
          ),
        ),
      );
}
