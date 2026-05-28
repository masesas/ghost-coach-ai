import 'package:dio/dio.dart';
import 'package:ghost_coach/core/network/api_unwrap.dart';
import 'package:ghost_coach/data/model/chat/chat_message.dart';
import 'package:ghost_coach/data/model/session/page_response.dart';

class ChatRemoteSource {
  ChatRemoteSource(this._dio);

  final Dio _dio;

  Future<PageResponse<ChatMessage>> history(
    int sessionId, {
    int page = 0,
    int size = 50,
  }) =>
      ApiUnwrap.call<PageResponse<ChatMessage>>(
        request: _dio.get<dynamic>(
          '/sessions/$sessionId/chat',
          queryParameters: <String, dynamic>{'page': page, 'size': size},
        ),
        parseData: (raw) => PageResponse<ChatMessage>.fromJson(
          raw! as Map<String, dynamic>,
          ChatMessage.fromJson,
        ),
      );

  Future<List<ChatMessage>> send(int sessionId, String message) =>
      ApiUnwrap.call<List<ChatMessage>>(
        request: _dio.post<dynamic>(
          '/sessions/$sessionId/chat',
          data: <String, dynamic>{'message': message},
          options: Options(receiveTimeout: const Duration(seconds: 60)),
        ),
        parseData: (raw) => (raw! as List<dynamic>)
            .map((e) => ChatMessage.fromJson(e as Map<String, dynamic>))
            .toList(growable: false),
      );
}
