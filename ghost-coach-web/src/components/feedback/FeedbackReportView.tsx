import { CheckCircle2, AlertCircle, Dumbbell } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ScoreBadge } from "./ScoreBadge";
import { ConfidenceBadge } from "./ConfidenceBadge";
import { PriorityFixCard } from "./PriorityFixCard";
import type { SessionDetail } from "@/types/session";

export function FeedbackReportView({ session }: { session: SessionDetail }) {
  return (
    <div className="space-y-4">
      <div className="flex items-center gap-4">
        <ScoreBadge score={session.overallScore} />
        <ConfidenceBadge level={session.confidenceLevel} />
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-emerald-700">
            <CheckCircle2 className="h-5 w-5" />
            Strengths
          </CardTitle>
        </CardHeader>
        <CardContent>
          <ul className="space-y-1">
            {session.strengths.map((s, i) => (
              <li key={i} className="flex items-start gap-2 text-sm text-gray-700">
                <span className="mt-0.5 text-emerald-500">✓</span>
                {s}
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-amber-700">
            <AlertCircle className="h-5 w-5" />
            Areas to Improve
          </CardTitle>
        </CardHeader>
        <CardContent>
          <ul className="space-y-3">
            {session.areasToImprove.map((area, i) => (
              <li key={i} className="text-sm">
                <p className="font-medium text-gray-900">{area.flaw}</p>
                <p className="mt-0.5 text-gray-600">{area.explanation}</p>
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>

      <PriorityFixCard fix={session.priorityFix} />

      {session.drillSuggestion ? (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-primary-700">
              <Dumbbell className="h-5 w-5" />
              Drill Suggestion
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-gray-700">{session.drillSuggestion}</p>
          </CardContent>
        </Card>
      ) : null}
    </div>
  );
}
