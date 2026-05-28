import { cn } from "@/lib/utils";

function scoreColor(score: number): string {
  if (score >= 8) return "text-emerald-600 bg-emerald-50 border-emerald-200";
  if (score >= 5) return "text-amber-600 bg-amber-50 border-amber-200";
  return "text-red-600 bg-red-50 border-red-200";
}

export function ScoreBadge({ score }: { score: number | null }) {
  if (score == null) return <span className="text-sm text-gray-400">N/A</span>;
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full border px-3 py-1 text-lg font-bold",
        scoreColor(score),
      )}
    >
      {score}/10
    </span>
  );
}
