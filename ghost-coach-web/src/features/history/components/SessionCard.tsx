import { Link } from "react-router-dom";
import { Card } from "@/components/ui/card";
import { ScoreBadge } from "@/components/feedback/ScoreBadge";
import { AuthImage } from "./AuthImage";
import { relativeTime } from "@/lib/utils";
import type { SessionSummary } from "@/types/session";

export function SessionCard({ session }: { session: SessionSummary }) {
  return (
    <Link to={`/history/${session.id}`} className="group">
      <Card className="overflow-hidden transition-shadow group-hover:shadow-md h-full">
        <div className="aspect-video bg-gray-100">
          <AuthImage path={session.imageUrl} alt="Stance" className="h-full w-full object-cover" />
        </div>
        <div className="space-y-2 p-4">
          <div className="flex items-center justify-between">
            <ScoreBadge score={session.overallScore} />
            <span className="text-xs text-gray-500">{relativeTime(session.createdAt)}</span>
          </div>
          {session.priorityFix ? (
            <p className="line-clamp-2 text-sm text-gray-600">{session.priorityFix}</p>
          ) : null}
        </div>
      </Card>
    </Link>
  );
}
