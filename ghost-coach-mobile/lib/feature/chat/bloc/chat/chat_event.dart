part of 'chat_bloc.dart';

sealed class ChatEvent extends Equatable {
  const ChatEvent();

  @override
  List<Object?> get props => <Object?>[];
}

class ChatStarted extends ChatEvent {
  const ChatStarted(this.sessionId);

  final int sessionId;

  @override
  List<Object?> get props => <Object?>[sessionId];
}

class ChatMessageSent extends ChatEvent {
  const ChatMessageSent(this.text);

  final String text;

  @override
  List<Object?> get props => <Object?>[text];
}
