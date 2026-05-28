import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:ghost_coach/common/router/auth_refresh_stream.dart';
import 'package:ghost_coach/core/network/unauthorized_callback.dart';
import 'package:ghost_coach/data/model/auth/profile.dart';
import 'package:ghost_coach/data/repository/auth_repository.dart';

part 'auth_event.dart';
part 'auth_state.dart';

class AuthBloc extends Bloc<AuthEvent, AuthState> {
  AuthBloc({
    required AuthRepository repository,
    required AuthRefreshNotifier refresh,
    required UnauthorizedNotifier unauthorized,
  })  : _repository = repository,
        _refresh = refresh,
        super(AuthState.initial) {
    on<AuthAppStarted>(_onStarted);
    on<AuthSignedIn>(_onSignedIn);
    on<AuthLoggedOut>(_onLoggedOut);
    on<AuthSessionExpired>(_onSessionExpired);

    // Override the bootstrap-time callback (which just clears storage +
    // bumps the router) with one that dispatches through this bloc so the
    // UI gets a single source of truth for auth state.
    unauthorized.setCallback(() => add(const AuthSessionExpired()));
  }

  final AuthRepository _repository;
  final AuthRefreshNotifier _refresh;

  Future<void> _onStarted(
    AuthAppStarted event,
    Emitter<AuthState> emit,
  ) async {
    final token = _repository.cachedToken();
    final profile = _repository.cachedProfile();
    if (token == null || token.isEmpty) {
      emit(AuthState.initial);
    } else {
      emit(AuthState(isAuthenticated: true, profile: profile));
    }
    _refresh.bump();
  }

  Future<void> _onSignedIn(
    AuthSignedIn event,
    Emitter<AuthState> emit,
  ) async {
    emit(AuthState(isAuthenticated: true, profile: event.profile));
    _refresh.bump();
  }

  Future<void> _onLoggedOut(
    AuthLoggedOut event,
    Emitter<AuthState> emit,
  ) async {
    await _repository.logout();
    emit(AuthState.initial);
    _refresh.bump();
  }

  Future<void> _onSessionExpired(
    AuthSessionExpired event,
    Emitter<AuthState> emit,
  ) async {
    await _repository.logout();
    emit(AuthState.initial);
    _refresh.bump();
  }
}
