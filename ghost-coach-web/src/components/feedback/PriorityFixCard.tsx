import { Target } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export function PriorityFixCard({ fix }: { fix: string | null }) {
  if (!fix) return null;
  return (
    <Card className="border-amber-200 bg-amber-50">
      <CardHeader>
        <CardTitle className="flex items-center gap-2 text-amber-800">
          <Target className="h-5 w-5" />
          Priority Fix
        </CardTitle>
      </CardHeader>
      <CardContent>
        <p className="text-sm text-amber-900">{fix}</p>
      </CardContent>
    </Card>
  );
}
