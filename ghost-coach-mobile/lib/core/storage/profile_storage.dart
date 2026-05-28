import 'dart:convert';

import 'package:shared_preferences/shared_preferences.dart';

class ProfileStorage {
  ProfileStorage(this._prefs);

  static const _profileKey = 'ghostcoach.profile';
  final SharedPreferences _prefs;

  Map<String, dynamic>? get() {
    final raw = _prefs.getString(_profileKey);
    if (raw == null) return null;
    try {
      final decoded = jsonDecode(raw);
      if (decoded is Map<String, dynamic>) return decoded;
      // Stored shape unexpected — drop it.
      _prefs.remove(_profileKey);
      return null;
    } catch (_) {
      // Corrupt JSON — clear to keep storage healthy.
      _prefs.remove(_profileKey);
      return null;
    }
  }

  Future<void> set(Map<String, dynamic> profile) =>
      _prefs.setString(_profileKey, jsonEncode(profile));

  Future<void> clear() => _prefs.remove(_profileKey);
}
