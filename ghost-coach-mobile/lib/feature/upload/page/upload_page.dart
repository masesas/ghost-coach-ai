import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:ghost_coach/common/theme/app_colors.dart';
import 'package:ghost_coach/common/util/permission.dart';
import 'package:ghost_coach/common/util/toast.dart';
import 'package:ghost_coach/common/widgets/feedback_report_view.dart';
import 'package:ghost_coach/common/widgets/primary_button.dart';
import 'package:ghost_coach/core/di/injector.dart';
import 'package:ghost_coach/feature/upload/bloc/upload/upload_bloc.dart';
import 'package:ghost_coach/feature/upload/widget/analyzing_state.dart';
import 'package:ghost_coach/feature/upload/widget/image_preview.dart';
import 'package:ghost_coach/feature/upload/widget/image_source_sheet.dart';
import 'package:go_router/go_router.dart';
import 'package:image_picker/image_picker.dart';

class UploadPage extends StatelessWidget {
  const UploadPage({super.key});

  @override
  Widget build(BuildContext context) => BlocProvider<UploadBloc>(
        create: (_) => getIt<UploadBloc>(),
        child: const _UploadView(),
      );
}

class _UploadView extends StatelessWidget {
  const _UploadView();

  Future<void> _pickImage(BuildContext context) async {
    final choice = await ImageSourceSheet.show(context);
    if (choice == null || !context.mounted) return;

    final source = choice == ImageSourceChoice.camera
        ? ImageSource.camera
        : ImageSource.gallery;

    final hasPerm = choice == ImageSourceChoice.camera
        ? await AppPermissions.ensureCamera()
        : await AppPermissions.ensurePhotoLibrary();
    if (!hasPerm) {
      if (!context.mounted) return;
      AppToast.error('Permission denied');
      return;
    }

    final picker = ImagePicker();
    final XFile? picked =
        await picker.pickImage(source: source, imageQuality: 92);
    if (picked == null || !context.mounted) return;

    final lower = picked.name.toLowerCase();
    final mime = picked.mimeType ??
        (lower.endsWith('.png') ? 'image/png' : 'image/jpeg');
    context
        .read<UploadBloc>()
        .add(UploadFileSelected(File(picked.path), mime));
  }

  @override
  Widget build(BuildContext context) => BlocConsumer<UploadBloc, UploadState>(
        listener: (ctx, state) {
          if (state is UploadFailure) AppToast.error(state.error.message);
          if (state is UploadSuccess) AppToast.success('Analysis complete!');
        },
        builder: (ctx, state) {
          if (state is UploadInProgress) {
            return const Padding(
              padding: EdgeInsets.all(16),
              child: AnalyzingState(),
            );
          }
          if (state is UploadSuccess) {
            return SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text(
                        'Coaching Report',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                      TextButton(
                        onPressed: () =>
                            ctx.go('/history/${state.session.id}'),
                        child: const Text('View in History'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  FeedbackReportView(session: state.session),
                  const SizedBox(height: 12),
                  PrimaryButton(
                    label: 'Analyze Another',
                    onPressed: () => ctx
                        .read<UploadBloc>()
                        .add(const UploadResetToPick()),
                  ),
                ],
              ),
            );
          }
          final pending = state is UploadIdle ? state.pendingFile : null;
          return Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                if (pending == null)
                  GestureDetector(
                    onTap: () => _pickImage(ctx),
                    child: Container(
                      height: 240,
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(color: AppColors.border, width: 2),
                      ),
                      child: const Center(
                        child: Column(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Icon(Icons.add_a_photo_outlined, size: 36),
                            SizedBox(height: 8),
                            Text('Tap to choose photo'),
                            SizedBox(height: 2),
                            Text(
                              'JPEG or PNG, max 5MB',
                              style: TextStyle(
                                fontSize: 12,
                                color: AppColors.textMuted,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  )
                else
                  ImagePreview(
                    file: pending,
                    onClear: () =>
                        ctx.read<UploadBloc>().add(const UploadCleared()),
                  ),
                const SizedBox(height: 16),
                PrimaryButton(
                  label: 'Analyze Stance',
                  onPressed: pending == null
                      ? null
                      : () => ctx
                          .read<UploadBloc>()
                          .add(const UploadSubmitted()),
                ),
              ],
            ),
          );
        },
      );
}
