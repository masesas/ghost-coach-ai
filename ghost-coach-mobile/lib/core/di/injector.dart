import 'package:chucker_flutter/chucker_flutter.dart';
import 'package:dio/dio.dart';
import 'package:get_it/get_it.dart';
import 'package:ghost_coach/common/router/app_router.dart';
import 'package:ghost_coach/common/router/auth_refresh_stream.dart';
import 'package:ghost_coach/core/env/app_env.dart';
import 'package:ghost_coach/core/network/api_client.dart';
import 'package:ghost_coach/core/network/auth_interceptor.dart';
import 'package:ghost_coach/core/network/unauthorized_callback.dart';
import 'package:ghost_coach/core/storage/profile_storage.dart';
import 'package:ghost_coach/core/storage/token_storage.dart';
import 'package:ghost_coach/data/repository/auth_repository.dart';
import 'package:ghost_coach/data/repository/chat_repository.dart';
import 'package:ghost_coach/data/repository/session_repository.dart';
import 'package:ghost_coach/data/repository/systemvar_repository.dart';
import 'package:ghost_coach/data/source/auth_remote_source.dart';
import 'package:ghost_coach/data/source/chat_remote_source.dart';
import 'package:ghost_coach/data/source/session_remote_source.dart';
import 'package:ghost_coach/data/source/systemvar_remote_source.dart';
import 'package:ghost_coach/feature/auth/bloc/auth/auth_bloc.dart';
import 'package:ghost_coach/feature/auth/bloc/login/login_bloc.dart';
import 'package:ghost_coach/feature/auth/bloc/register/register_bloc.dart';
import 'package:ghost_coach/feature/auth/bloc/systemvar/systemvar_cubit.dart';
import 'package:ghost_coach/feature/chat/bloc/chat/chat_bloc.dart';
import 'package:ghost_coach/feature/detail/bloc/detail/detail_bloc.dart';
import 'package:ghost_coach/feature/history/bloc/history/history_bloc.dart';
import 'package:ghost_coach/feature/upload/bloc/upload/upload_bloc.dart';
import 'package:shared_preferences/shared_preferences.dart';

final GetIt getIt = GetIt.instance;

class Injector {
  Injector._();

  static Future<void> init(AppEnv env) async {
    // 1) Env
    getIt.registerSingleton<AppEnv>(env);

    // 2) Storage
    final prefs = await SharedPreferences.getInstance();
    getIt.registerSingleton<SharedPreferences>(prefs);
    getIt.registerSingleton<TokenStorage>(TokenStorage(prefs));
    getIt.registerSingleton<ProfileStorage>(ProfileStorage(prefs));

    // 3) Auth notifier
    getIt.registerSingleton<UnauthorizedNotifier>(UnauthorizedNotifier());

    final authInterceptor = AuthInterceptor(
      tokenStorage: getIt<TokenStorage>(),
      unauthorizedNotifier: getIt<UnauthorizedNotifier>(),
    );
    getIt.registerSingleton<AuthInterceptor>(authInterceptor);

    // 4) Dio
    getIt.registerSingleton<Dio>(
      ApiClient.create(env: env, authInterceptor: authInterceptor),
    );

    // 5) Routing
    getIt.registerSingleton<AuthRefreshNotifier>(AuthRefreshNotifier());
    getIt.registerSingleton<AppRouter>(
      AppRouter(
        tokenStorage: getIt<TokenStorage>(),
        refreshNotifier: getIt<AuthRefreshNotifier>(),
        navigatorKey: env.showChucker ? ChuckerFlutter.navigatorKey : null,
      ),
    );

    // 6) Wire 401 → clear storage + refresh router. AuthBloc nanti dapat
    //    override callback ini saat di-konstruksi (plan 05).
    getIt<UnauthorizedNotifier>().setCallback(() async {
      await getIt<TokenStorage>().clear();
      await getIt<ProfileStorage>().clear();
      getIt<AuthRefreshNotifier>().bump();
    });

    // 7) Auth + system vars
    getIt.registerLazySingleton<AuthRemoteSource>(
      () => AuthRemoteSource(getIt<Dio>()),
    );
    getIt.registerLazySingleton<SystemVarRemoteSource>(
      () => SystemVarRemoteSource(getIt<Dio>()),
    );
    getIt.registerLazySingleton<AuthRepository>(
      () => AuthRepository(
        remote: getIt<AuthRemoteSource>(),
        tokenStorage: getIt<TokenStorage>(),
        profileStorage: getIt<ProfileStorage>(),
      ),
    );
    getIt.registerLazySingleton<SystemVarRepository>(
      () => SystemVarRepository(getIt<SystemVarRemoteSource>()),
    );

    // 8) Auth bloc is global (singleton); form-scoped blocs are factory.
    getIt.registerLazySingleton<AuthBloc>(
      () => AuthBloc(
        repository: getIt<AuthRepository>(),
        refresh: getIt<AuthRefreshNotifier>(),
        unauthorized: getIt<UnauthorizedNotifier>(),
      ),
    );
    getIt.registerFactory<LoginBloc>(() => LoginBloc(getIt<AuthRepository>()));
    getIt.registerFactory<RegisterBloc>(
      () => RegisterBloc(getIt<AuthRepository>()),
    );
    getIt.registerFactory<SystemVarCubit>(
      () => SystemVarCubit(getIt<SystemVarRepository>()),
    );

    // 9) Sessions
    getIt.registerLazySingleton<SessionRemoteSource>(
      () => SessionRemoteSource(getIt<Dio>()),
    );
    getIt.registerLazySingleton<SessionRepository>(
      () => SessionRepository(getIt<SessionRemoteSource>()),
    );
    getIt.registerFactory<UploadBloc>(
      () => UploadBloc(getIt<SessionRepository>()),
    );
    getIt.registerFactory<HistoryBloc>(
      () => HistoryBloc(getIt<SessionRepository>()),
    );
    getIt.registerFactory<DetailBloc>(
      () => DetailBloc(getIt<SessionRepository>()),
    );

    // 10) Chat
    getIt.registerLazySingleton<ChatRemoteSource>(
      () => ChatRemoteSource(getIt<Dio>()),
    );
    getIt.registerLazySingleton<ChatRepository>(
      () => ChatRepository(getIt<ChatRemoteSource>()),
    );
    getIt.registerFactory<ChatBloc>(
      () => ChatBloc(getIt<ChatRepository>()),
    );
  }
}
