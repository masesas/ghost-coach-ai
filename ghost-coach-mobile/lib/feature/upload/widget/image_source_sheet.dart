import 'package:flutter/material.dart';

enum ImageSourceChoice { camera, gallery }

class ImageSourceSheet extends StatelessWidget {
  const ImageSourceSheet({super.key});

  static Future<ImageSourceChoice?> show(BuildContext context) =>
      showModalBottomSheet<ImageSourceChoice>(
        context: context,
        showDragHandle: true,
        builder: (_) => const ImageSourceSheet(),
      );

  @override
  Widget build(BuildContext context) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              leading: const Icon(Icons.photo_camera_outlined),
              title: const Text('Take photo'),
              onTap: () =>
                  Navigator.of(context).pop(ImageSourceChoice.camera),
            ),
            ListTile(
              leading: const Icon(Icons.photo_library_outlined),
              title: const Text('Choose from gallery'),
              onTap: () =>
                  Navigator.of(context).pop(ImageSourceChoice.gallery),
            ),
            const SizedBox(height: 8),
          ],
        ),
      );
}
