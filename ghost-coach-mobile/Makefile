.PHONY: run-dev run-staging run-prod build-runner clean apk-prod analyze format get

get:
	flutter pub get

run-dev:
	flutter run -t lib/main_dev.dart

run-staging:
	flutter run -t lib/main_staging.dart

run-prod:
	flutter run -t lib/main_prod.dart --release

build-runner:
	dart run build_runner build --delete-conflicting-outputs

watch:
	dart run build_runner watch --delete-conflicting-outputs

clean:
	flutter clean && flutter pub get

analyze:
	flutter analyze

format:
	dart format lib/ test/

apk-prod:
	flutter build apk -t lib/main_prod.dart --release
