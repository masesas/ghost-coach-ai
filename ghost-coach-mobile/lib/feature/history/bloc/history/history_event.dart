part of 'history_bloc.dart';

sealed class HistoryEvent extends Equatable {
  const HistoryEvent();

  @override
  List<Object?> get props => <Object?>[];
}

class HistoryRequested extends HistoryEvent {
  const HistoryRequested({this.page = 0});

  final int page;

  @override
  List<Object?> get props => <Object?>[page];
}

class HistoryRefreshed extends HistoryEvent {
  const HistoryRefreshed();
}
