import 'dart:io';

import 'package:flutter/material.dart';
import 'package:ghost_coach/common/theme/app_colors.dart';
import 'package:ghost_coach/core/extension/num_x.dart';

class ImagePreview extends StatelessWidget {
  const ImagePreview({
    required this.file,
    required this.onClear,
    super.key,
  });

  final File file;
  final VoidCallback onClear;

  @override
  Widget build(BuildContext context) {
    final size = file.lengthSync();
    return ClipRRect(
      borderRadius: BorderRadius.circular(12),
      child: ColoredBox(
        color: AppColors.surface,
        child: Stack(
          children: [
            Image.file(
              file,
              fit: BoxFit.contain,
              height: 280,
              width: double.infinity,
            ),
            Positioned(
              top: 8,
              right: 8,
              child: Material(
                color: Colors.black.withValues(alpha: 0.5),
                shape: const CircleBorder(),
                child: IconButton(
                  iconSize: 18,
                  color: Colors.white,
                  icon: const Icon(Icons.close),
                  onPressed: onClear,
                ),
              ),
            ),
            Positioned(
              bottom: 0,
              left: 0,
              right: 0,
              child: Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                color: AppColors.background,
                child: Row(
                  children: [
                    Expanded(
                      child: Text(
                        file.uri.pathSegments.last,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                    Text(
                      size.fileSize,
                      style: const TextStyle(color: AppColors.textMuted),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
