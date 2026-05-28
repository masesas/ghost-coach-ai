import 'package:chucker_flutter/chucker_flutter.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:ghost_coach/common/router/app_router.dart';
import 'package:ghost_coach/common/theme/app_theme.dart';
import 'package:ghost_coach/core/di/injector.dart';
import 'package:ghost_coach/core/env/app_env.dart';
import 'package:ghost_coach/feature/auth/bloc/auth/auth_bloc.dart';

class GhostCoachApp extends StatelessWidget {
  const GhostCoachApp({super.key});

  @override
  Widget build(BuildContext context) {
    final router = getIt<AppRouter>().router;
    final env = getIt<AppEnv>();
    return MultiBlocProvider(
      providers: <BlocProvider<dynamic>>[
        BlocProvider<AuthBloc>(
          create: (_) => getIt<AuthBloc>()..add(const AuthAppStarted()),
          lazy: false,
        ),
      ],
      child: MaterialApp.router(
        title: 'Ghost Coach',
        debugShowCheckedModeBanner: false,
        theme: AppTheme.light,
        routerConfig: router,
        builder: (context, child) {
          final safeChild = child ?? const SizedBox.shrink();
          if (!env.showChucker) return safeChild;
          return Stack(
            children: [
              safeChild,
              Positioned(
                right: 8,
                bottom: 8,
                child: SafeArea(child: ChuckerFlutter.chuckerButton),
              ),
            ],
          );
        },
      ),
    );
  }
}
