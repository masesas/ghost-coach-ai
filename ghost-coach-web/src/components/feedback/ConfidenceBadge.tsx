import { Badge } from "@/components/ui/badge";
import type { ConfidenceLevel } from "@/types/session";

const variantMap: Record<ConfidenceLevel, "success" | "warning" | "danger"> = {
  HIGH: "success",
  MEDIUM: "warning",
  LOW: "danger",
};

export function ConfidenceBadge({ level }: { level: ConfidenceLevel }) {
  return <Badge variant={variantMap[level]}>Confidence: {level}</Badge>;
}
