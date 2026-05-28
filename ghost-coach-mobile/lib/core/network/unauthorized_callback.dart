typedef UnauthorizedCallback = void Function();

/// Holder global agar router/AuthBloc bisa di-subscribe tanpa
/// circular dependency dari interceptor.
class UnauthorizedNotifier {
  UnauthorizedNotifier();

  UnauthorizedCallback? _callback;

  void setCallback(UnauthorizedCallback cb) {
    _callback = cb;
  }

  void notify() {
    final cb = _callback;
    if (cb != null) cb();
  }
}
