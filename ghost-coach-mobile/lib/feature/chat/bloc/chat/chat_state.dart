part of 'chat_bloc.dart';

class ChatState extends Equatable {
  const ChatState({
    required this.loadingHistory,
    required this.sending,
    required this.messages,
    this.error,
  });

  final bool loadingHistory;
  final bool sending;
  final List<ChatMessage> messages;
  final ApiException? error;

  static const initial = ChatState(
    loadingHistory: true,
    sending: false,
    messages: <ChatMessage>[],
  );

  ChatState copyWith({
    bool? loadingHistory,
    bool? sending,
    List<ChatMessage>? messages,
    ApiException? error,
    bool clearError = false,
  }) =>
      ChatState(
        loadingHistory: loadingHistory ?? this.loadingHistory,
        sending: sending ?? this.sending,
        messages: messages ?? this.messages,
        error: clearError ? null : (error ?? this.error),
      );

  @override
  List<Object?> get props => <Object?>[
        loadingHistory,
        sending,
        messages.length,
        error?.code,
      ];
}
