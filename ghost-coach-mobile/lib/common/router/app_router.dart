import 'package:flutter/widgets.dart';
import 'package:ghost_coach/common/router/app_routes.dart';
import 'package:ghost_coach/common/router/auth_refresh_stream.dart';
import 'package:ghost_coach/core/storage/token_storage.dart';
import 'package:go_router/go_router.dart';

class AppRouter {
  AppRouter({
    required TokenStorage tokenStorage,
    required AuthRefreshNotifier refreshNotifier,
    GlobalKey<NavigatorState>? navigatorKey,
  })  : _tokenStorage = tokenStorage,
        _refreshNotifier = refreshNotifier {
    router = GoRouter(
      navigatorKey: navigatorKey,
      initialLocation: '/upload',
      refreshListenable: _refreshNotifier,
      redirect: _redirect,
      routes: $appRoutes,
    );
  }

  final TokenStorage _tokenStorage;
  final AuthRefreshNotifier _refreshNotifier;
  late final GoRouter router;

  String? _redirect(BuildContext context, GoRouterState state) {
    final token = _tokenStorage.get();
    final loggedIn = token != null && token.isNotEmpty;
    final goingToAuth = state.matchedLocation == '/login' ||
        state.matchedLocation == '/register';

    if (!loggedIn && !goingToAuth) {
      return '/login';
    }
    if (loggedIn && goingToAuth) {
      return '/upload';
    }
    return null;
  }
}
