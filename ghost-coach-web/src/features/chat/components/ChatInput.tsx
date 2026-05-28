import { useState } from "react";
import { Send } from "lucide-react";
import { Button } from "@/components/ui/button";
import { LoadingSpinner } from "@/components/common/LoadingSpinner";

interface ChatInputProps {
  onSend: (message: string) => void;
  disabled?: boolean;
}

export function ChatInput({ onSend, disabled }: ChatInputProps) {
  const [value, setValue] = useState("");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = value.trim();
    if (!trimmed) return;
    onSend(trimmed);
    setValue("");
  };

  return (
    <form onSubmit={handleSubmit} className="flex gap-2">
      <input
        type="text"
        placeholder="Ask your coach..."
        value={value}
        onChange={(e) => setValue(e.target.value)}
        disabled={disabled}
        className="flex-1 rounded-md border border-gray-300 px-3 py-2 text-sm placeholder:text-gray-400 focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-100 disabled:opacity-50"
        maxLength={1000}
      />
      <Button type="submit" size="icon" disabled={disabled || !value.trim()}>
        {disabled ? <LoadingSpinner className="h-4 w-4 text-white" /> : <Send className="h-4 w-4" />}
      </Button>
    </form>
  );
}
