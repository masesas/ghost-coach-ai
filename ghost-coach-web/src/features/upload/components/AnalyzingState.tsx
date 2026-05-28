import { Bot, ScanEye } from "lucide-react";

export function AnalyzingState() {
  return (
    <div className="flex flex-col items-center justify-center rounded-lg border border-primary-200 bg-primary-50 p-12 text-center">
      <div className="relative mb-4">
        <Bot className="h-12 w-12 text-primary-600" />
        <ScanEye className="absolute -right-1 -top-1 h-5 w-5 animate-pulse text-primary-500" />
      </div>
      <p className="text-base font-semibold text-primary-900">Analyzing your stance…</p>
      <p className="mt-1 text-sm text-primary-700">
        Ghost Coach is reviewing your technique with AI. This may take a few seconds.
      </p>
      <div className="mt-4 h-1 w-48 overflow-hidden rounded-full bg-primary-200">
        <div className="h-full animate-[pulse_2s_ease-in-out_infinite] rounded-full bg-primary-500" />
      </div>
    </div>
  );
}
