class SystemVarItem {
  const SystemVarItem({required this.key, required this.label});

  final String key;
  final String label;

  factory SystemVarItem.fromJson(Map<String, dynamic> json) => SystemVarItem(
        key: json['key'] as String,
        label: json['label'] as String,
      );
}
