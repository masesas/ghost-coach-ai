import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:ghost_coach/feature/auth/bloc/systemvar/systemvar_cubit.dart';

class SportPositionPicker extends StatelessWidget {
  const SportPositionPicker({
    required this.value,
    required this.onChanged,
    super.key,
  });

  final String? value;
  final ValueChanged<String?> onChanged;

  @override
  Widget build(BuildContext context) =>
      BlocBuilder<SystemVarCubit, SystemVarState>(
        builder: (_, state) {
          final items = state.items;
          final hasValue = value != null && items.any((e) => e.key == value);
          return DropdownButtonFormField<String>(
            // Key forces FormField recreation when the value or item list
            // changes externally (e.g. sport switch resets position).
            key: ValueKey<String>('pos-${value ?? ''}-${items.length}'),
            initialValue: hasValue ? value : null,
            decoration: const InputDecoration(labelText: 'Position'),
            items: items
                .map(
                  (e) => DropdownMenuItem<String>(
                    value: e.key,
                    child: Text(e.label),
                  ),
                )
                .toList(growable: false),
            onChanged: state.loading ? null : onChanged,
            validator: (v) => (v == null || v.isEmpty) ? 'Required' : null,
          );
        },
      );
}
