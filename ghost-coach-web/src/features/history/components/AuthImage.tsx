import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { cn } from "@/lib/utils";
import { Skeleton } from "@/components/ui/skeleton";

interface AuthImageProps {
  path: string;
  alt: string;
  className?: string;
}

export function AuthImage({ path, alt, className }: AuthImageProps) {
  const [src, setSrc] = useState<string | null>(null);

  useEffect(() => {
    let revoke: string | null = null;
    let cancelled = false;
    api
      .get<Blob>(path.replace(/^\/api\/v1/, ""), { responseType: "blob" })
      .then((res) => {
        if (cancelled) return;
        const url = URL.createObjectURL(res.data);
        revoke = url;
        setSrc(url);
      })
      .catch(() => {
        if (!cancelled) setSrc(null);
      });
    return () => {
      cancelled = true;
      if (revoke) URL.revokeObjectURL(revoke);
    };
  }, [path]);

  if (!src) {
    return <Skeleton className={cn("h-full w-full", className)} />;
  }

  return <img src={src} alt={alt} className={className} />;
}
