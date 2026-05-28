import { useCallback, useState } from "react";
import { X, ImageIcon } from "lucide-react";
import { ACCEPTED_IMAGE_TYPES, MAX_UPLOAD_SIZE } from "@/lib/constants";
import { cn } from "@/lib/utils";
import { toast } from "sonner";

interface ImageDropzoneProps {
  file: File | null;
  onFileSelect: (file: File | null) => void;
  disabled?: boolean;
}

export function ImageDropzone({ file, onFileSelect, disabled }: ImageDropzoneProps) {
  const [isDragOver, setIsDragOver] = useState(false);
  const [preview, setPreview] = useState<string | null>(null);

  const handleFile = useCallback(
    (f: File) => {
      if (!ACCEPTED_IMAGE_TYPES.includes(f.type)) {
        toast.error("Only JPEG and PNG images are allowed");
        return;
      }
      if (f.size > MAX_UPLOAD_SIZE) {
        toast.error("Image must be under 5MB");
        return;
      }
      setPreview(URL.createObjectURL(f));
      onFileSelect(f);
    },
    [onFileSelect],
  );

  const clear = () => {
    if (preview) URL.revokeObjectURL(preview);
    setPreview(null);
    onFileSelect(null);
  };

  const onDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragOver(false);
    const droppedFile = e.dataTransfer.files[0];
    if (droppedFile) handleFile(droppedFile);
  };

  const onInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selected = e.target.files?.[0];
    if (selected) handleFile(selected);
    e.target.value = "";
  };

  if (file && preview) {
    return (
      <div className="relative overflow-hidden rounded-lg border border-gray-200 bg-white">
        <img src={preview} alt="Preview" className="mx-auto max-h-80 object-contain p-2" />
        <button
          onClick={clear}
          disabled={disabled}
          className="absolute right-2 top-2 rounded-full bg-black/50 p-1 text-white hover:bg-black/70 disabled:opacity-50"
        >
          <X className="h-4 w-4" />
        </button>
        <div className="border-t border-gray-200 bg-gray-50 px-3 py-2">
          <p className="truncate text-sm text-gray-700">{file.name}</p>
          <p className="text-xs text-gray-500">{(file.size / 1024).toFixed(0)} KB</p>
        </div>
      </div>
    );
  }

  return (
    <label
      className={cn(
        "flex min-h-[250px] cursor-pointer flex-col items-center justify-center rounded-lg border-2 border-dashed transition-colors",
        isDragOver ? "border-primary-500 bg-primary-50" : "border-gray-300 bg-white hover:border-primary-400",
        disabled && "pointer-events-none opacity-50",
      )}
      onDragOver={(e) => {
        e.preventDefault();
        setIsDragOver(true);
      }}
      onDragLeave={() => setIsDragOver(false)}
      onDrop={onDrop}
    >
      <input
        type="file"
        accept="image/jpeg,image/png"
        className="hidden"
        onChange={onInputChange}
        disabled={disabled}
      />
      <ImageIcon className="mb-3 h-10 w-10 text-gray-400" />
      <p className="text-sm font-medium text-gray-700">
        <span className="text-primary-600">Click to upload</span> or drag and drop
      </p>
      <p className="mt-1 text-xs text-gray-500">JPEG or PNG, max 5MB</p>
    </label>
  );
}
