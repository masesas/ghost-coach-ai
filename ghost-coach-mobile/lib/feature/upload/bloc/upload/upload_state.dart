part of 'upload_bloc.dart';

sealed class UploadState extends Equatable {
  const UploadState();

  @override
  List<Object?> get props => <Object?>[];
}

class UploadIdle extends UploadState {
  const UploadIdle({this.pendingFile});

  final File? pendingFile;

  @override
  List<Object?> get props => <Object?>[pendingFile?.path];
}

class UploadInProgress extends UploadState {
  const UploadInProgress();
}

class UploadSuccess extends UploadState {
  const UploadSuccess(this.session);

  final SessionDetail session;

  @override
  List<Object?> get props => <Object?>[session.id];
}

class UploadFailure extends UploadState {
  const UploadFailure(this.error);

  final ApiException error;

  @override
  List<Object?> get props => <Object?>[error.code, error.message];
}
