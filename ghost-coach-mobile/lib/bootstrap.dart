import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:ghost_coach/app.dart';
import 'package:ghost_coach/core/di/injector.dart';
import 'package:ghost_coach/core/env/app_env.dart';

Future<void> bootstrap(AppEnv env) async {
  WidgetsFlutterBinding.ensureInitialized();
  await SystemChrome.setPreferredOrientations(<DeviceOrientation>[
    DeviceOrientation.portraitUp,
  ]);

  await Injector.init(env);

  runApp(const GhostCoachApp());
}
