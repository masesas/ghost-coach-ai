import { useState } from "react";
import { Camera } from "lucide-react";
import { Link } from "react-router-dom";
import { useSessions } from "../hooks/useSessions";
import { useSessionsForChart } from "../hooks/useSessionsForChart";
import { SessionCard } from "../components/SessionCard";
import { ProgressChart } from "@/features/progress/components/ProgressChart";
import { EmptyState } from "@/components/common/EmptyState";
import { ErrorState } from "@/components/common/ErrorState";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";

export function HistoryPage() {
  const [page, setPage] = useState(0);
  const { data, isLoading, isError, refetch } = useSessions(page);
  const { data: chartData } = useSessionsForChart();

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {Array.from({ length: 6 }).map((_, i) => (
          <Skeleton key={i} className="h-64 w-full" />
        ))}
      </div>
    );
  }

  if (isError) {
    return <ErrorState onRetry={() => refetch()} />;
  }

  if (!data || data.content.length === 0) {
    return (
      <EmptyState
        icon={Camera}
        title="No sessions yet"
        description="Upload your first stance to get personalized coaching."
        action={
          <Link to="/upload">
            <Button>Upload Stance</Button>
          </Link>
        }
      />
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold text-gray-900">Session History</h1>
        <Link to="/upload">
          <Button size="sm">New Analysis</Button>
        </Link>
      </div>

      {chartData?.content ? (
        <ProgressChart sessions={chartData.content} />
      ) : null}

      <div className="grid grid-cols-1 auto-rows-fr gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {data.content.map((s) => (
          <SessionCard key={s.id} session={s} />
        ))}
      </div>

      {data.totalPages > 1 ? (
        <div className="flex items-center justify-between">
          <Button
            variant="outline"
            size="sm"
            disabled={page === 0}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
          >
            Previous
          </Button>
          <span className="text-sm text-gray-500">
            Page {data.page + 1} of {data.totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            disabled={page >= data.totalPages - 1}
            onClick={() => setPage((p) => p + 1)}
          >
            Next
          </Button>
        </div>
      ) : null}
    </div>
  );
}
