part of 'detail_bloc.dart';

sealed class DetailEvent extends Equatable {
  const DetailEvent();

  @override
  List<Object?> get props => <Object?>[];
}

class DetailRequested extends DetailEvent {
  const DetailRequested(this.id);

  final int id;

  @override
  List<Object?> get props => <Object?>[id];
}
