import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:ghost_coach/common/theme/app_colors.dart';
import 'package:ghost_coach/common/util/toast.dart';
import 'package:ghost_coach/core/di/injector.dart';
import 'package:ghost_coach/feature/chat/bloc/chat/chat_bloc.dart';
import 'package:ghost_coach/feature/chat/widget/chat_input.dart';
import 'package:ghost_coach/feature/chat/widget/message_bubble.dart';

class ChatPanel extends StatelessWidget {
  const ChatPanel({required this.sessionId, super.key});

  final int sessionId;

  @override
  Widget build(BuildContext context) => BlocProvider<ChatBloc>(
        create: (_) => getIt<ChatBloc>()..add(ChatStarted(sessionId)),
        child: const _ChatView(),
      );
}

class _ChatView extends StatefulWidget {
  const _ChatView();

  @override
  State<_ChatView> createState() => _ChatViewState();
}

class _ChatViewState extends State<_ChatView> {
  final _scrollCtl = ScrollController();

  @override
  void dispose() {
    _scrollCtl.dispose();
    super.dispose();
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!_scrollCtl.hasClients) return;
      _scrollCtl.animateTo(
        _scrollCtl.position.maxScrollExtent,
        duration: const Duration(milliseconds: 200),
        curve: Curves.easeOut,
      );
    });
  }

  @override
  Widget build(BuildContext context) =>
      BlocConsumer<ChatBloc, ChatState>(
        listener: (ctx, state) {
          _scrollToBottom();
          if (state.error != null) AppToast.error(state.error!.message);
        },
        builder: (ctx, state) => Container(
          decoration: BoxDecoration(
            color: AppColors.surface,
            border: Border.all(color: AppColors.border),
            borderRadius: BorderRadius.circular(12),
          ),
          padding: const EdgeInsets.all(12),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Row(
                children: [
                  Icon(
                    Icons.chat_bubble_outline,
                    size: 18,
                    color: AppColors.primary600,
                  ),
                  SizedBox(width: 6),
                  Text(
                    'Coaching Chat',
                    style: TextStyle(fontWeight: FontWeight.w700),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              SizedBox(
                height: 320,
                child: state.loadingHistory
                    ? const Center(
                        child: SizedBox(
                          height: 24,
                          width: 24,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        ),
                      )
                    : state.messages.isEmpty
                        ? const Center(
                            child: Text(
                              'Ask a follow-up question about your session.',
                              style: TextStyle(color: AppColors.textMuted),
                            ),
                          )
                        : ListView.builder(
                            controller: _scrollCtl,
                            itemCount: state.messages.length +
                                (state.sending ? 1 : 0),
                            itemBuilder: (_, i) {
                              if (i == state.messages.length) {
                                return const Padding(
                                  padding:
                                      EdgeInsets.symmetric(vertical: 4),
                                  child: Text(
                                    'AI is typing…',
                                    style: TextStyle(
                                      color: AppColors.textMuted,
                                      fontSize: 12,
                                    ),
                                  ),
                                );
                              }
                              return MessageBubble(
                                message: state.messages[i],
                              );
                            },
                          ),
              ),
              const SizedBox(height: 8),
              ChatInput(
                sending: state.sending,
                onSend: (text) =>
                    ctx.read<ChatBloc>().add(ChatMessageSent(text)),
              ),
            ],
          ),
        ),
      );
}
