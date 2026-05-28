import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:ghost_coach/common/theme/app_colors.dart';
import 'package:ghost_coach/core/di/injector.dart';
import 'package:ghost_coach/core/env/app_env.dart';
import 'package:ghost_coach/core/storage/token_storage.dart';

class AuthCachedImage extends StatelessWidget {
  const AuthCachedImage({
    required this.path,
    this.fit = BoxFit.cover,
    this.height,
    this.width,
    super.key,
  });

  /// Path from BE; may be relative (`/api/v1/sessions/1/image`) or absolute.
  final String path;
  final BoxFit fit;
  final double? height;
  final double? width;

  String _resolveUrl() {
    if (path.startsWith('http')) return path;
    final base = getIt<AppEnv>().baseUrl; // already ends with /api/v1
    // BE returns `/api/v1/...`; strip the duplicate prefix if present.
    final tail = path.startsWith('/api/v1')
        ? path.substring('/api/v1'.length)
        : path;
    return '$base$tail';
  }

  Map<String, String> _headers() {
    final token = getIt<TokenStorage>().get();
    return token == null
        ? const <String, String>{}
        : <String, String>{'Authorization': 'Bearer $token'};
  }

  @override
  Widget build(BuildContext context) => CachedNetworkImage(
        imageUrl: _resolveUrl(),
        httpHeaders: _headers(),
        fit: fit,
        height: height,
        width: width,
        placeholder: (_, _) => Container(
          color: AppColors.background,
          child: const Center(
            child: SizedBox(
              height: 18,
              width: 18,
              child: CircularProgressIndicator(strokeWidth: 2),
            ),
          ),
        ),
        errorWidget: (_, _, _) => Container(
          color: AppColors.background,
          child: const Icon(
            Icons.broken_image_outlined,
            color: AppColors.textMuted,
          ),
        ),
      );
}
