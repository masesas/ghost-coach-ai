import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:ghost_coach/common/theme/app_colors.dart';
import 'package:ghost_coach/common/util/toast.dart';
import 'package:ghost_coach/common/widgets/primary_button.dart';
import 'package:ghost_coach/core/di/injector.dart';
import 'package:ghost_coach/data/model/enums/experience_level.dart';
import 'package:ghost_coach/data/model/enums/sport.dart';
import 'package:ghost_coach/feature/auth/bloc/auth/auth_bloc.dart';
import 'package:ghost_coach/feature/auth/bloc/register/register_bloc.dart';
import 'package:ghost_coach/feature/auth/bloc/systemvar/systemvar_cubit.dart';
import 'package:ghost_coach/feature/auth/widget/sport_position_picker.dart';
import 'package:go_router/go_router.dart';

class RegisterPage extends StatelessWidget {
  const RegisterPage({super.key});

  @override
  Widget build(BuildContext context) => MultiBlocProvider(
        providers: <BlocProvider<dynamic>>[
          BlocProvider<RegisterBloc>(create: (_) => getIt<RegisterBloc>()),
          BlocProvider<SystemVarCubit>(
            create: (_) => getIt<SystemVarCubit>()
              ..load('POSITION', scope: Sport.cricket.wire),
          ),
        ],
        child: const _RegisterView(),
      );
}

class _RegisterView extends StatefulWidget {
  const _RegisterView();

  @override
  State<_RegisterView> createState() => _RegisterViewState();
}

class _RegisterViewState extends State<_RegisterView> {
  final _formKey = GlobalKey<FormState>();
  final _emailCtl = TextEditingController();
  final _passCtl = TextEditingController();
  final _nameCtl = TextEditingController();

  Sport _sport = Sport.cricket;
  ExperienceLevel _level = ExperienceLevel.beginner;
  String? _position;

  @override
  void dispose() {
    _emailCtl.dispose();
    _passCtl.dispose();
    _nameCtl.dispose();
    super.dispose();
  }

  void _onSportChanged(Sport? next) {
    if (next == null || next == _sport) return;
    setState(() {
      _sport = next;
      _position = null;
    });
    context.read<SystemVarCubit>().load('POSITION', scope: next.wire);
  }

  void _onPositionChanged(String? next) {
    setState(() => _position = next);
  }

  void _onLevelChanged(ExperienceLevel? next) {
    if (next == null) return;
    setState(() => _level = next);
  }

  void _submit() {
    if (!(_formKey.currentState?.validate() ?? false)) return;
    final pos = _position;
    if (pos == null || pos.isEmpty) {
      AppToast.error('Please select a position');
      return;
    }
    context.read<RegisterBloc>().add(
          RegisterSubmitted(
            email: _emailCtl.text,
            password: _passCtl.text,
            fullName: _nameCtl.text,
            sport: _sport,
            position: pos,
            experienceLevel: _level,
          ),
        );
  }

  @override
  Widget build(BuildContext context) => MultiBlocListener(
        listeners: <BlocListener<dynamic, dynamic>>[
          BlocListener<RegisterBloc, RegisterState>(
            listener: (ctx, state) {
              if (state is RegisterSuccess) {
                AppToast.success('Welcome, ${state.response.user.fullName}!');
                ctx.read<AuthBloc>().add(AuthSignedIn(state.response.user));
                ctx.go('/upload');
              } else if (state is RegisterFailure) {
                AppToast.error(state.error.message);
              }
            },
          ),
          BlocListener<SystemVarCubit, SystemVarState>(
            listenWhen: (prev, curr) =>
                prev.items != curr.items && curr.items.isNotEmpty,
            listener: (_, state) {
              // Default position to the first item once data lands, mirroring web.
              if (_position == null && state.items.isNotEmpty) {
                setState(() => _position = state.items.first.key);
              }
            },
          ),
        ],
        child: Scaffold(
          body: SafeArea(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(20),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    const SizedBox(height: 16),
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
                      'Create your account',
                      style: TextStyle(
                        fontSize: 22,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                    const Text(
                      'Tell us about yourself for personalized coaching.',
                      style: TextStyle(color: AppColors.textSecondary),
                    ),
                    const SizedBox(height: 24),
                    TextFormField(
                      controller: _nameCtl,
                      decoration: const InputDecoration(labelText: 'Full name'),
                      textCapitalization: TextCapitalization.words,
                      validator: (v) {
                        if (v == null || v.trim().length < 2) {
                          return 'Required';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 12),
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
                      autofillHints: const [AutofillHints.newPassword],
                      validator: (v) {
                        if (v == null || v.isEmpty) return 'Required';
                        if (v.length < 8) return 'Min 8 characters';
                        return null;
                      },
                    ),
                    const SizedBox(height: 12),
                    DropdownButtonFormField<Sport>(
                      initialValue: _sport,
                      decoration: const InputDecoration(labelText: 'Sport'),
                      items: Sport.values
                          .map(
                            (s) => DropdownMenuItem<Sport>(
                              value: s,
                              child: Text(s.label),
                            ),
                          )
                          .toList(growable: false),
                      onChanged: _onSportChanged,
                    ),
                    const SizedBox(height: 12),
                    SportPositionPicker(
                      value: _position,
                      onChanged: _onPositionChanged,
                    ),
                    const SizedBox(height: 12),
                    DropdownButtonFormField<ExperienceLevel>(
                      initialValue: _level,
                      decoration:
                          const InputDecoration(labelText: 'Experience level'),
                      items: ExperienceLevel.values
                          .map(
                            (e) => DropdownMenuItem<ExperienceLevel>(
                              value: e,
                              child: Text(e.label),
                            ),
                          )
                          .toList(growable: false),
                      onChanged: _onLevelChanged,
                    ),
                    const SizedBox(height: 20),
                    BlocBuilder<RegisterBloc, RegisterState>(
                      builder: (_, state) => PrimaryButton(
                        label: 'Create account',
                        loading: state is RegisterInProgress,
                        onPressed: _submit,
                      ),
                    ),
                    const SizedBox(height: 12),
                    Center(
                      child: TextButton(
                        onPressed: () => context.go('/login'),
                        child: const Text('Already registered? Log in'),
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
