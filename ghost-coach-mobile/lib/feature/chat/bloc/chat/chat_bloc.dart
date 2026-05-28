import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:ghost_coach/core/network/api_exception.dart';
import 'package:ghost_coach/data/model/chat/chat_message.dart';
import 'package:ghost_coach/data/repository/chat_repository.dart';

part 'chat_event.dart';
part 'chat_state.dart';

class ChatBloc extends Bloc<ChatEvent, ChatState> {
  ChatBloc(this._repo) : super(ChatState.initial) {
    on<ChatStarted>(_onStarted);
    on<ChatMessageSent>(_onSent);
  }

  final ChatRepository _repo;
  int? _sessionId;

  Future<void> _onStarted(
    ChatStarted event,
    Emitter<ChatState> emit,
  ) async {
    _sessionId = event.sessionId;
    emit(state.copyWith(loadingHistory: true, clearError: true));
    try {
      final list = await _repo.history(event.sessionId);
      emit(state.copyWith(loadingHistory: false, messages: list));
    } on ApiException catch (err) {
      emit(state.copyWith(loadingHistory: false, error: err));
    }
  }

  Future<void> _onSent(
    ChatMessageSent event,
    Emitter<ChatState> emit,
  ) async {
    final sessionId = _sessionId;
    if (sessionId == null) return;
    emit(state.copyWith(sending: true, clearError: true));
    try {
      final newMessages = await _repo.send(sessionId, event.text);
      emit(state.copyWith(
        sending: false,
        messages: <ChatMessage>[...state.messages, ...newMessages],
      ));
    } on ApiException catch (err) {
      emit(state.copyWith(sending: false, error: err));
    }
  }
}
