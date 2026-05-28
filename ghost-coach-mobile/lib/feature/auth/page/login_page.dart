import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:ghost_coach/common/theme/app_colors.dart';
import 'package:ghost_coach/common/util/toast.dart';
import 'package:ghost_coach/common/widgets/primary_button.dart';
import 'package:ghost_coach/core/di/injector.dart';
import 'package:ghost_coach/feature/auth/bloc/auth/auth_bloc.dart';
import 'package:ghost_coach/feature/auth/bloc/login/login_bloc.dart';
import 'package:go_router/go_router.dart';

class LoginPage extends StatelessWidget {
  const LoginPage({super.key});

  @override
  Widget build(BuildContext context) => BlocProvider<LoginBloc>(
        create: (_) => getIt<LoginBloc>(),
        child: const _LoginView(),
      );
}

class _LoginView extends StatefulWidget {
  const _LoginView();

  @override
  State<_LoginView> createState() => _LoginViewState();
}

class _LoginViewState extends State<_LoginView> {
  final _formKey = GlobalKey<FormState>();
  final _emailCtl = TextEditingController();
  final _passCtl = TextEditingController();

  @override
  void dispose() {
    _emailCtl.dispose();
    _passCtl.dispose();
    super.dispose();
  }

  void _submit() {
    if (!(_formKey.currentState?.validate() ?? false)) return;
    context.read<LoginBloc>().add(
          LoginSubmitted(email: _emailCtl.text, password: _passCtl.text),
        );
  }

  @override
  Widget build(BuildContext context) => BlocListener<LoginBloc, LoginState>(
        listener: (ctx, state) {
          if (state is LoginSuccess) {
            AppToast.success('Welcome back, ${state.response.user.fullName}');
            ctx.read<AuthBloc>().add(AuthSignedIn(state.response.user));
            ctx.go('/upload');
          } else if (state is LoginFailure) {
            AppToast.error(state.error.message);
          }
        },
        child: Scaffold(
          body: SafeArea(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(20),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    const SizedBox(height: 24),
                    const Row(
                      children: [
                        Icon(Icons.auto_awesome, color: AppColors.primary600),
                        SizedBox(width: 8),
                        Text(
                          'Ghost Coach',
                          style: TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.w800,
                            color: AppColors.primary700,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    const Text(
                      'Welcome back',
                      style: TextStyle(
                        fontSize: 22,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                    const Text(
                      'Log in to analyze your stance.',
                      style: TextStyle(color: AppColors.textSecondary),
                    ),
                    const SizedBox(height: 24),
                    TextFormField(
                      controller: _emailCtl,
                      keyboardType: TextInputType.emailAddress,
                      decoration: const InputDecoration(labelText: 'Email'),
                      autofillHints: const [AutofillHints.email],
                      validator: (v) {
                        if (v == null || v.trim().isEmpty) return 'Required';
                        if (!v.contains('@')) return 'Invalid email';
                        return null;
                      },
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _passCtl,
                      obscureText: true,
                      decoration: const InputDecoration(labelText: 'Password'),
                      autofillHints: const [AutofillHints.password],
                      validator: (v) =>
                          (v == null || v.isEmpty) ? 'Required' : null,
                    ),
                    const SizedBox(height: 20),
                    BlocBuilder<LoginBloc, LoginState>(
                      builder: (_, state) => PrimaryButton(
                        label: 'Log in',
                        loading: state is LoginInProgress,
                        onPressed: _submit,
                      ),
                    ),
                    const SizedBox(height: 12),
                    Center(
                      child: TextButton(
                        onPressed: () => context.go('/register'),
                        child: const Text('No account? Sign up'),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      );
}
