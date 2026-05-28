import 'package:permission_handler/permission_handler.dart';

class AppPermissions {
  AppPermissions._();

  static Future<bool> ensureCamera() async {
    final status = await Permission.camera.request();
    return status.isGranted;
  }

  /// Android 13+ uses `Permission.photos`; iOS uses `Permission.photos`.
  /// Older Android falls back to `Permission.storage`.
  static Future<bool> ensurePhotoLibrary() async {
    final photos = await Permission.photos.request();
    if (photos.isGranted || photos.isLimited) return true;
    final storage = await Permission.storage.request();
    return storage.isGranted;
  }
}
