import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ImageDropzone } from "../components/ImageDropzone";
import { AnalyzingState } from "../components/AnalyzingState";
import { FeedbackReportView } from "@/components/feedback/FeedbackReportView";
import { useUploadSession } from "../hooks/useUploadSession";
import { extractError } from "@/lib/api";
import type { SessionDetail } from "@/types/session";

export function UploadPage() {
  const [file, setFile] = useState<File | null>(null);
  const [result, setResult] = useState<SessionDetail | null>(null);
  const navigate = useNavigate();
  const mutation = useUploadSession();

  const handleAnalyze = async () => {
    if (!file) return;
    try {
      const data = await mutation.mutateAsync(file);
      setResult(data);
      toast.success("Analysis complete!");
    } catch (error) {
      toast.error(extractError(error).message, {
        duration: Infinity,
        closeButton: true,
      });
    }
  };

  const reset = () => {
    setFile(null);
    setResult(null);
  };

  if (mutation.isPending) {
    return (
      <div className="mx-auto max-w-xl">
        <AnalyzingState />
      </div>
    );
  }

  if (result) {
    return (
      <div className="mx-auto max-w-2xl space-y-4">
        <div className="flex items-center justify-between">
          <h1 className="text-xl font-bold text-gray-900">Coaching Report</h1>
          <div className="flex gap-2">
            <Button variant="outline" onClick={() => navigate(`/history/${result.id}`)}>
              View in History
            </Button>
            <Button onClick={reset}>Analyze Another</Button>
          </div>
        </div>
        <FeedbackReportView session={result} />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-xl">
      <Card>
        <CardHeader>
          <CardTitle>Upload your stance</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <ImageDropzone file={file} onFileSelect={setFile} />
          <Button className="w-full" disabled={!file} onClick={handleAnalyze}>
            Analyze Stance
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
