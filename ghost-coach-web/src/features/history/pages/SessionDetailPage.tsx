import { Link, useParams } from "react-router-dom";
import { ChevronLeft } from "lucide-react";
import { useSession } from "../hooks/useSession";
import { FeedbackReportView } from "@/components/feedback/FeedbackReportView";
import { AuthImage } from "../components/AuthImage";
import { ChatPanel } from "@/features/chat/components/ChatPanel";
import { Skeleton } from "@/components/ui/skeleton";
import { ErrorState } from "@/components/common/ErrorState";
import { Button } from "@/components/ui/button";
import { formatDate } from "@/lib/utils";

export function SessionDetailPage() {
  const { id } = useParams<{ id: string }>();
  const sessionId = id ? Number(id) : undefined;
  const { data, isLoading, isError, refetch } = useSession(sessionId);

  if (isLoading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-6 w-32" />
        <Skeleton className="h-64 w-full" />
      </div>
    );
  }

  if (isError || !data) {
    return <ErrorState onRetry={() => refetch()} />;
  }

  return (
    <div className="space-y-4">
      <Link to="/history">
        <Button variant="ghost" size="sm">
          <ChevronLeft className="h-4 w-4" />
          Back to History
        </Button>
      </Link>

      <div className="grid gap-6 lg:grid-cols-[1fr_360px]">
        <div className="space-y-4">
          <div className="overflow-hidden rounded-lg border border-gray-200 bg-white">
            <AuthImage
              path={data.imageUrl}
              alt="Stance"
              className="mx-auto max-h-96 object-contain p-2"
            />
            <div className="border-t border-gray-200 bg-gray-50 px-4 py-2 text-xs text-gray-500">
              Uploaded {formatDate(data.createdAt)}
            </div>
          </div>
          <FeedbackReportView session={data} />
        </div>

        <div className="lg:sticky lg:top-20 lg:self-start">
          <ChatPanel sessionId={data.id} />
        </div>
      </div>
    </div>
  );
}
