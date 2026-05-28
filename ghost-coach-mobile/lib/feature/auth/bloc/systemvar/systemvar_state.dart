part of 'systemvar_cubit.dart';

class SystemVarState extends Equatable {
  const SystemVarState({
    required this.loading,
    required this.items,
    this.error,
  });

  final bool loading;
  final List<SystemVarItem> items;
  final ApiException? error;

  static const initial = SystemVarState(
    loading: false,
    items: <SystemVarItem>[],
  );

  SystemVarState copyWith({
    bool? loading,
    List<SystemVarItem>? items,
    ApiException? error,
    bool clearError = false,
  }) =>
      SystemVarState(
        loading: loading ?? this.loading,
        items: items ?? this.items,
        error: clearError ? null : (error ?? this.error),
      );

  @override
  List<Object?> get props => <Object?>[loading, items.length, error?.code];
}
