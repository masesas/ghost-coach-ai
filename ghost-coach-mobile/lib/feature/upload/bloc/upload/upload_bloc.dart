import 'dart:io';

import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:ghost_coach/core/network/api_exception.dart';
import 'package:ghost_coach/data/model/session/session_detail.dart';
import 'package:ghost_coach/data/repository/session_repository.dart';

part 'upload_event.dart';
part 'upload_state.dart';

const int _maxBytes = 5 * 1024 * 1024;
const List<String> _allowedMime = <String>['image/jpeg', 'image/png'];

class UploadBloc extends Bloc<UploadEvent, UploadState> {
  UploadBloc(this._repo) : super(const UploadIdle()) {
    on<UploadFileSelected>(_onSelect);
    on<UploadCleared>((event, emit) => emit(const UploadIdle()));
    on<UploadSubmitted>(_onSubmit);
    on<UploadResetToPick>((event, emit) => emit(const UploadIdle()));
  }

  final SessionRepository _repo;

  Future<void> _onSelect(
    UploadFileSelected event,
    Emitter<UploadState> emit,
  ) async {
    if (!_allowedMime.contains(event.mimeType.toLowerCase())) {
      emit(const UploadFailure(
        ApiException(
          code: 'INVALID_MIME',
          message: 'Only JPEG or PNG images are allowed',
        ),
      ));
      emit(const UploadIdle());
      return;
    }
    final size = await event.file.length();
    if (size > _maxBytes) {
      emit(const UploadFailure(
        ApiException(
          code: 'FILE_TOO_LARGE',
          message: 'Image must be under 5MB',
        ),
      ));
      emit(const UploadIdle());
      return;
    }
    emit(UploadIdle(pendingFile: event.file));
  }

  Future<void> _onSubmit(
    UploadSubmitted event,
    Emitter<UploadState> emit,
  ) async {
    final current = state;
    if (current is! UploadIdle || current.pendingFile == null) return;
    emit(const UploadInProgress());
    try {
      final result = await _repo.upload(current.pendingFile!);
      emit(UploadSuccess(result));
    } on ApiException catch (err) {
      emit(UploadFailure(err));
    }
  }
}
