import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
} from "recharts";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { TrendingUp } from "lucide-react";
import type { SessionSummary } from "@/types/session";

interface ProgressChartProps {
  sessions: SessionSummary[];
}

export function ProgressChart({ sessions }: ProgressChartProps) {
  const data = sessions
    .filter((s) => s.overallScore != null)
    .map((s) => ({
      date: new Date(s.createdAt).toLocaleDateString(undefined, {
        month: "short",
        day: "numeric",
      }),
      score: s.overallScore,
    }))
    .reverse();

  if (data.length < 2) return null;

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2 text-base">
          <TrendingUp className="h-4 w-4 text-primary-600" />
          Progress Over Time
        </CardTitle>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={200}>
          <LineChart data={data}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" />
            <XAxis dataKey="date" fontSize={12} tick={{ fill: "#9ca3af" }} />
            <YAxis domain={[0, 10]} fontSize={12} tick={{ fill: "#9ca3af" }} />
            <Tooltip />
            <Line
              type="monotone"
              dataKey="score"
              stroke="#4f46e5"
              strokeWidth={2}
              dot={{ fill: "#4f46e5", r: 4 }}
              activeDot={{ r: 6 }}
            />
          </LineChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
}
