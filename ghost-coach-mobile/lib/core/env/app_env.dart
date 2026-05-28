enum AppEnv {
  dev(
    baseUrl: 'http://10.0.2.2:8080/api/v1',
    showChucker: true,
  ),
  staging(
    baseUrl: 'https://staging.ghostcoach.example.com/api/v1',
    showChucker: true,
  ),
  prod(
    baseUrl: 'https://api.ghostcoach.example.com/api/v1',
    showChucker: false,
  );

  const AppEnv({required this.baseUrl, required this.showChucker});

  final String baseUrl;
  final bool showChucker;
}
