part of 'detail_bloc.dart';

sealed class DetailState extends Equatable {
  const DetailState();

  @override
  List<Object?> get props => <Object?>[];
}

class DetailLoading extends DetailState {
  const DetailLoading();
}

class DetailLoaded extends DetailState {
  const DetailLoaded(this.session);

  final SessionDetail session;

  @override
  List<Object?> get props => <Object?>[session.id];
}

class DetailFailed extends DetailState {
  const DetailFailed(this.error);

  final ApiException error;

  @override
  List<Object?> get props => <Object?>[error.code];
}
