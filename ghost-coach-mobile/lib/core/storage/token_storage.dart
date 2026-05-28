import 'package:shared_preferences/shared_preferences.dart';

class TokenStorage {
  TokenStorage(this._prefs);

  static const _tokenKey = 'ghostcoach.token';
  final SharedPreferences _prefs;

  String? get() => _prefs.getString(_tokenKey);

  Future<void> set(String token) => _prefs.setString(_tokenKey, token);

  Future<void> clear() => _prefs.remove(_tokenKey);
}
