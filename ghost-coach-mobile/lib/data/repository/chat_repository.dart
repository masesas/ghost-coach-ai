import 'package:ghost_coach/data/model/chat/chat_message.dart';
import 'package:ghost_coach/data/source/chat_remote_source.dart';

class ChatRepository {
  ChatRepository(this._remote);

  final ChatRemoteSource _remote;

  Future<List<ChatMessage>> history(int sessionId) async {
    final page = await _remote.history(sessionId);
    return page.content;
  }

  Future<List<ChatMessage>> send(int sessionId, String message) =>
      _remote.send(sessionId, message);
}
