import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:ghost_coach/common/theme/app_colors.dart';
import 'package:ghost_coach/data/model/session/session_summary.dart';
import 'package:intl/intl.dart';

class ProgressChart extends StatelessWidget {
  const ProgressChart({required this.sessions, super.key});

  final List<SessionSummary> sessions;

  @override
  Widget build(BuildContext context) {
    final scored = sessions
        .where((s) => s.overallScore != null)
        .toList(growable: false);
    scored.sort((a, b) => a.createdAt.compareTo(b.createdAt));
    if (scored.length < 2) return const SizedBox.shrink();

    final spots = <FlSpot>[
      for (var i = 0; i < scored.length; i++)
        FlSpot(i.toDouble(), scored[i].overallScore!),
    ];
    final fmt = DateFormat('MMM d');

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Row(
              children: [
                Icon(
                  Icons.trending_up,
                  size: 18,
                  color: AppColors.primary600,
                ),
                SizedBox(width: 6),
                Text(
                  'Progress Over Time',
                  style: TextStyle(fontWeight: FontWeight.w700),
                ),
              ],
            ),
            const SizedBox(height: 12),
            SizedBox(
              height: 180,
              child: LineChart(
                LineChartData(
                  minY: 0,
                  maxY: 10,
                  gridData:
                      const FlGridData(drawVerticalLine: false),
                  titlesData: FlTitlesData(
                    leftTitles: const AxisTitles(
                      sideTitles: SideTitles(
                        showTitles: true,
                        reservedSize: 28,
                        interval: 2,
                      ),
                    ),
                    bottomTitles: AxisTitles(
                      sideTitles: SideTitles(
                        showTitles: true,
                        reservedSize: 22,
                        interval: 1,
                        getTitlesWidget: (v, _) {
                          final i = v.toInt();
                          if (i < 0 || i >= scored.length) {
                            return const SizedBox.shrink();
                          }
                          return Text(
                            fmt.format(scored[i].createdAt.toLocal()),
                            style: const TextStyle(fontSize: 9),
                          );
                        },
                      ),
                    ),
                    rightTitles: const AxisTitles(
                      sideTitles: SideTitles(showTitles: false),
                    ),
                    topTitles: const AxisTitles(
                      sideTitles: SideTitles(showTitles: false),
                    ),
                  ),
                  borderData: FlBorderData(show: false),
                  lineBarsData: <LineChartBarData>[
                    LineChartBarData(
                      spots: spots,
                      color: AppColors.primary600,
                      barWidth: 2,
                      isCurved: true,
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
