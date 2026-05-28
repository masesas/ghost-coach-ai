import 'package:flutter/material.dart';
import 'package:ghost_coach/feature/auth/page/login_page.dart';
import 'package:ghost_coach/feature/auth/page/register_page.dart';
import 'package:ghost_coach/feature/detail/page/session_detail_page.dart';
import 'package:ghost_coach/feature/history/page/history_page.dart';
import 'package:ghost_coach/feature/shell/page/shell_page.dart';
import 'package:ghost_coach/feature/upload/page/upload_page.dart';
import 'package:go_router/go_router.dart';

part 'app_routes.g.dart';

@TypedGoRoute<LoginRoute>(path: '/login')
class LoginRoute extends GoRouteData with $LoginRoute {
  const LoginRoute();
  @override
  Widget build(BuildContext context, GoRouterState state) => const LoginPage();
}

@TypedGoRoute<RegisterRoute>(path: '/register')
class RegisterRoute extends GoRouteData with $RegisterRoute {
  const RegisterRoute();
  @override
  Widget build(BuildContext context, GoRouterState state) =>
      const RegisterPage();
}

@TypedShellRoute<HomeShellRoute>(
  routes: <TypedRoute<RouteData>>[
    TypedGoRoute<UploadRoute>(path: '/upload'),
    TypedGoRoute<HistoryRoute>(
      path: '/history',
      routes: <TypedRoute<RouteData>>[
        TypedGoRoute<SessionDetailRoute>(path: ':id'),
      ],
    ),
  ],
)
class HomeShellRoute extends ShellRouteData {
  const HomeShellRoute();
  @override
  Widget builder(BuildContext context, GoRouterState state, Widget navigator) =>
      ShellPage(child: navigator);
}

class UploadRoute extends GoRouteData with $UploadRoute {
  const UploadRoute();
  @override
  Widget build(BuildContext context, GoRouterState state) => const UploadPage();
}

class HistoryRoute extends GoRouteData with $HistoryRoute {
  const HistoryRoute();
  @override
  Widget build(BuildContext context, GoRouterState state) =>
      const HistoryPage();
}

class SessionDetailRoute extends GoRouteData with $SessionDetailRoute {
  const SessionDetailRoute({required this.id});
  final int id;
  @override
  Widget build(BuildContext context, GoRouterState state) =>
      SessionDetailPage(sessionId: id);
}
