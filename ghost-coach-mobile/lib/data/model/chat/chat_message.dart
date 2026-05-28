import 'package:ghost_coach/data/model/chat/chat_role.dart';

class ChatMessage {
  const ChatMessage({
    required this.id,
    required this.role,
    required this.content,
    required this.createdAt,
  });

  final int id;
  final ChatRole role;
  final String content;
  final DateTime createdAt;

  factory ChatMessage.fromJson(Map<String, dynamic> json) => ChatMessage(
        id: (json['id'] as num).toInt(),
        role: ChatRole.fromWire(json['role'] as String),
        content: json['content'] as String,
        createdAt: DateTime.parse(json['createdAt'] as String),
      );
}
