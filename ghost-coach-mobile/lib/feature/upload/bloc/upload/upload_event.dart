part of 'upload_bloc.dart';

sealed class UploadEvent extends Equatable {
  const UploadEvent();

  @override
  List<Object?> get props => <Object?>[];
}

class UploadFileSelected extends UploadEvent {
  const UploadFileSelected(this.file, this.mimeType);

  final File file;
  final String mimeType;

  @override
  List<Object?> get props => <Object?>[file.path, mimeType];
}

class UploadCleared extends UploadEvent {
  const UploadCleared();
}

class UploadSubmitted extends UploadEvent {
  const UploadSubmitted();
}

class UploadResetToPick extends UploadEvent {
  const UploadResetToPick();
}
