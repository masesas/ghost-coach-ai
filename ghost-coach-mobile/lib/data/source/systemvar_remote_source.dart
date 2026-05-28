import 'package:dio/dio.dart';
import 'package:ghost_coach/core/network/api_unwrap.dart';
import 'package:ghost_coach/data/model/systemvar/system_var_item.dart';

class SystemVarRemoteSource {
  SystemVarRemoteSource(this._dio);

  final Dio _dio;

  Future<List<SystemVarItem>> list(String category, {String? scope}) =>
      ApiUnwrap.call<List<SystemVarItem>>(
        request: _dio.get<dynamic>(
          '/system-vars/$category',
          queryParameters:
              scope == null ? null : <String, dynamic>{'scope': scope},
        ),
        parseData: (raw) => (raw! as List<dynamic>)
            .map((e) => SystemVarItem.fromJson(e as Map<String, dynamic>))
            .toList(growable: false),
      );
}
