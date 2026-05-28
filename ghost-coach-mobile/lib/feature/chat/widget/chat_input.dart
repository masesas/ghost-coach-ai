import 'package:flutter/material.dart';
import 'package:ghost_coach/common/theme/app_colors.dart';
import 'package:ghost_coach/common/widgets/loading_spinner.dart';

class ChatInput extends StatefulWidget {
  const ChatInput({
    required this.onSend,
    required this.sending,
    super.key,
  });

  final ValueChanged<String> onSend;
  final bool sending;

  @override
  State<ChatInput> createState() => _ChatInputState();
}

class _ChatInputState extends State<ChatInput> {
  final _controller = TextEditingController();

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _submit() {
    final text = _controller.text.trim();
    if (text.isEmpty) return;
    widget.onSend(text);
    _controller.clear();
  }

  @override
  Widget build(BuildContext context) => Row(
        children: [
          Expanded(
            child: TextField(
              controller: _controller,
              enabled: !widget.sending,
              maxLength: 1000,
              decoration: const InputDecoration(
                hintText: 'Ask your coach...',
                counterText: '',
              ),
              onSubmitted: (_) => _submit(),
              textInputAction: TextInputAction.send,
            ),
          ),
          const SizedBox(width: 8),
          SizedBox(
            width: 44,
            height: 44,
            child: ElevatedButton(
              onPressed: widget.sending ? null : _submit,
              style: ElevatedButton.styleFrom(
                padding: EdgeInsets.zero,
                shape: const CircleBorder(),
                backgroundColor: AppColors.primary600,
              ),
              child: widget.sending
                  ? const LoadingSpinner(color: Colors.white, size: 18)
                  : const Icon(Icons.send, size: 18, color: Colors.white),
            ),
          ),
        ],
      );
}
