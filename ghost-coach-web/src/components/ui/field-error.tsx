import { cn } from "@/lib/utils";

export function FieldError({ children, className }: { children?: React.ReactNode; className?: string }) {
  if (!children) return null;
  return <p className={cn("text-xs text-red-600", className)}>{children}</p>;
}
