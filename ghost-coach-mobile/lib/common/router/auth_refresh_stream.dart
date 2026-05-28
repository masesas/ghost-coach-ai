import 'package:flutter/foundation.dart';

/// `Listenable` yang di-`notifyListeners()` setiap kali auth state berubah
/// (login / logout / 401). Dipakai oleh `GoRouter.refreshListenable` agar
/// `redirect` di-evaluate ulang.
class AuthRefreshNotifier extends ChangeNotifier {
  void bump() => notifyListeners();
}
