import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:ghost_coach/common/theme/app_colors.dart';

class AppToast {
  AppToast._();

  static void success(String message) => _show(message, AppColors.success);
  static void error(String message) => _show(message, AppColors.danger);
  static void info(String message) => _show(message, AppColors.primary600);

  static void _show(String message, Color bg) {
    Fluttertoast.showToast(
      msg: message,
      backgroundColor: bg,
      textColor: const Color(0xFFFFFFFF),
      gravity: ToastGravity.TOP,
    );
  }
}
