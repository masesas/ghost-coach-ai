export interface AreaToImprove {
  flaw: string;
  explanation: string;
}

export type ConfidenceLevel = "LOW" | "MEDIUM" | "HIGH";

export interface SessionSummary {
  id: number;
  imageUrl: string;
  overallScore: number | null;
  priorityFix: string | null;
  createdAt: string;
}

export interface SessionDetail {
  id: number;
  imageUrl: string;
  overallScore: number | null;
  strengths: string[];
  areasToImprove: AreaToImprove[];
  priorityFix: string | null;
  drillSuggestion: string | null;
  confidenceLevel: ConfidenceLevel;
  createdAt: string;
}
