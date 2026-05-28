import 'package:ghost_coach/core/storage/profile_storage.dart';
import 'package:ghost_coach/core/storage/token_storage.dart';
import 'package:ghost_coach/data/model/auth/auth_response.dart';
import 'package:ghost_coach/data/model/auth/login_request.dart';
import 'package:ghost_coach/data/model/auth/profile.dart';
import 'package:ghost_coach/data/model/auth/register_request.dart';
import 'package:ghost_coach/data/source/auth_remote_source.dart';

class AuthRepository {
  AuthRepository({
    required AuthRemoteSource remote,
    required TokenStorage tokenStorage,
    required ProfileStorage profileStorage,
  })  : _remote = remote,
        _token = tokenStorage,
        _profile = profileStorage;

  final AuthRemoteSource _remote;
  final TokenStorage _token;
  final ProfileStorage _profile;

  Future<AuthResponse> login(LoginRequest req) async {
    final res = await _remote.login(req);
    await _persist(res);
    return res;
  }

  Future<AuthResponse> register(RegisterRequest req) async {
    final res = await _remote.register(req);
    await _persist(res);
    return res;
  }

  Future<void> _persist(AuthResponse res) async {
    await _token.set(res.token);
    await _profile.set(res.user.toJson());
  }

  Future<void> logout() async {
    await _token.clear();
    await _profile.clear();
  }

  Profile? cachedProfile() {
    final raw = _profile.get();
    if (raw == null) return null;
    try {
      return Profile.fromJson(raw);
    } catch (_) {
      // Old/incompatible cached profile — drop it to keep startup healthy.
      _profile.clear();
      return null;
    }
  }

  String? cachedToken() => _token.get();

  Future<Profile> refreshProfile() async {
    final p = await _remote.me();
    await _profile.set(p.toJson());
    return p;
  }
}
