import 'dart:io';

import 'package:ghost_coach/data/model/session/page_response.dart';
import 'package:ghost_coach/data/model/session/session_detail.dart';
import 'package:ghost_coach/data/model/session/session_summary.dart';
import 'package:ghost_coach/data/source/session_remote_source.dart';

class SessionRepository {
  SessionRepository(this._remote);

  final SessionRemoteSource _remote;

  Future<SessionDetail> upload(File image) => _remote.upload(image);

  Future<PageResponse<SessionSummary>> list({int page = 0, int size = 20}) =>
      _remote.list(page: page, size: size);

  Future<SessionDetail> detail(int id) => _remote.detail(id);
}
