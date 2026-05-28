import 'package:ghost_coach/data/model/systemvar/system_var_item.dart';
import 'package:ghost_coach/data/source/systemvar_remote_source.dart';

class SystemVarRepository {
  SystemVarRepository(this._remote);

  final SystemVarRemoteSource _remote;
  final Map<String, List<SystemVarItem>> _cache = <String, List<SystemVarItem>>{};

  Future<List<SystemVarItem>> list(String category, {String? scope}) async {
    final key = '$category::${scope ?? ''}';
    final cached = _cache[key];
    if (cached != null) return cached;
    final fresh = await _remote.list(category, scope: scope);
    _cache[key] = fresh;
    return fresh;
  }

  void invalidate() => _cache.clear();
}
