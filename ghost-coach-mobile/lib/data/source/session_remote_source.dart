import 'dart:io';

import 'package:dio/dio.dart';
import 'package:ghost_coach/core/network/api_unwrap.dart';
import 'package:ghost_coach/data/model/session/page_response.dart';
import 'package:ghost_coach/data/model/session/session_detail.dart';
import 'package:ghost_coach/data/model/session/session_summary.dart';

class SessionRemoteSource {
  SessionRemoteSource(this._dio);

  final Dio _dio;

  Future<SessionDetail> upload(File image) async {
    final form = FormData.fromMap(<String, dynamic>{
      'image': await MultipartFile.fromFile(
        image.path,
        filename: image.uri.pathSegments.last,
      ),
    });
    return ApiUnwrap.call<SessionDetail>(
      request: _dio.post<dynamic>(
        '/sessions',
        data: form,
        options: Options(
          contentType: 'multipart/form-data',
          receiveTimeout: const Duration(seconds: 90),
          sendTimeout: const Duration(seconds: 60),
        ),
      ),
      parseData: (raw) => SessionDetail.fromJson(raw! as Map<String, dynamic>),
    );
  }

  Future<PageResponse<SessionSummary>> list({int page = 0, int size = 20}) =>
      ApiUnwrap.call<PageResponse<SessionSummary>>(
        request: _dio.get<dynamic>(
          '/sessions',
          queryParameters: <String, dynamic>{'page': page, 'size': size},
        ),
        parseData: (raw) => PageResponse<SessionSummary>.fromJson(
          raw! as Map<String, dynamic>,
          SessionSummary.fromJson,
        ),
      );

  Future<SessionDetail> detail(int id) => ApiUnwrap.call<SessionDetail>(
        request: _dio.get<dynamic>('/sessions/$id'),
        parseData: (raw) => SessionDetail.fromJson(raw! as Map<String, dynamic>),
      );
}
