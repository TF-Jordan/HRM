"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Download, FileBadge, FileText, Loader2, Stethoscope, Upload } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { IconTile } from "@/components/ui/icon-tile";
import { BffApiError, apiFetch } from "@/lib/api-client";
import { formatDate } from "@/lib/format";
import type { DocumentLinkView } from "@/server/ksm/modules/files";

type Category = "CONTRACT" | "ID" | "MEDICAL" | "CV" | "DIPLOMA" | "GENERAL";

const CATEGORY_ICON: Record<string, typeof FileText> = {
  CONTRACT: FileText,
  ID: FileBadge,
  MEDICAL: Stethoscope,
  CV: FileText,
  DIPLOMA: FileText,
  GENERAL: FileText,
};
const CATEGORY_TONE: Record<string, "orange" | "info" | "danger" | "success" | "violet" | "gray"> = {
  CONTRACT: "orange",
  ID: "info",
  MEDICAL: "danger",
  CV: "violet",
  DIPLOMA: "success",
  GENERAL: "gray",
};

function humanSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export function EmployeeDocuments({ employeeId }: { employeeId: string }) {
  const tOv = useTranslations("employees.detail.overview");
  const tCat = useTranslations("employees.detail.overview.documentCategories");
  const tErrors = useTranslations("errors");
  const queryClient = useQueryClient();
  const fileInputRef = React.useRef<HTMLInputElement>(null);
  const [category, setCategory] = React.useState<Category>("GENERAL");

  const query = useQuery({
    queryKey: ["hrm", "employee", "documents", employeeId],
    queryFn: () =>
      apiFetch<DocumentLinkView[]>(`/api/hrm/employees/${employeeId}/documents`),
  });

  const mutation = useMutation({
    mutationFn: async (file: File) => {
      const fd = new FormData();
      fd.append("file", file, file.name);
      fd.append("category", category);
      const res = await fetch(`/api/hrm/employees/${employeeId}/documents`, {
        method: "POST",
        body: fd,
        credentials: "include",
      });
      const json = await res.json();
      if (!res.ok || json.ok === false) {
        throw new BffApiError({
          ok: false,
          status: res.status,
          errorCode: json.errorCode ?? null,
          message: json.message ?? "Upload failed",
        });
      }
      return json.data as DocumentLinkView;
    },
    onSuccess: () => {
      toast.success(tOv("documentsSuccess"));
      queryClient.invalidateQueries({
        queryKey: ["hrm", "employee", "documents", employeeId],
      });
      if (fileInputRef.current) fileInputRef.current.value = "";
    },
    onError: (cause) => {
      if (cause instanceof BffApiError) toast.error(cause.message);
      else toast.error(tErrors("unknown"));
    },
  });

  function onFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    mutation.mutate(file);
  }

  return (
    <>
      <div className="mb-3 flex items-center justify-between">
        <div className="text-[14px] font-bold tracking-tight text-ink">{tOv("documents")}</div>
        <Button
          type="button"
          size="sm"
          variant="ghost"
          onClick={() => fileInputRef.current?.click()}
          disabled={mutation.isPending}
        >
          {mutation.isPending ? (
            <>
              <Loader2 className="h-3.5 w-3.5 animate-spin" />
              {tOv("documentsUploading")}
            </>
          ) : (
            <>
              <Upload className="h-3.5 w-3.5" />
              {tOv("documentsUpload")}
            </>
          )}
        </Button>
        <input
          ref={fileInputRef}
          type="file"
          accept="application/pdf,image/*"
          className="hidden"
          onChange={onFileChange}
        />
      </div>

      {/* Category picker — small chips, design-faithful */}
      <div className="mb-3 flex flex-wrap gap-1">
        {(["CONTRACT", "ID", "MEDICAL", "CV", "DIPLOMA", "GENERAL"] as const).map((c) => (
          <button
            key={c}
            type="button"
            onClick={() => setCategory(c)}
            className={
              "rounded-full border px-2 py-0.5 font-mono-tabular text-[10px] uppercase tracking-wider transition-colors " +
              (category === c
                ? "border-transparent bg-orange-50 text-orange-700"
                : "border-line bg-white text-ink-3 hover:border-line-strong")
            }
          >
            {tCat(c)}
          </button>
        ))}
      </div>

      {query.isLoading ? (
        <Loader2 className="h-5 w-5 animate-spin text-orange-500" />
      ) : query.error || !query.data ? (
        <div className="rounded-[10px] border border-dashed border-line bg-bg-soft px-3 py-3 text-[12px] text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "—"}
        </div>
      ) : query.data.length === 0 ? (
        <div className="rounded-[10px] border border-dashed border-line bg-bg-soft px-3 py-3 text-[12px] text-ink-3">
          {tOv("documentsEmpty")}
        </div>
      ) : (
        <div className="flex flex-col">
          {query.data.map((d, idx) => {
            const Icon = CATEGORY_ICON[d.documentCategory] ?? FileText;
            const tone = CATEGORY_TONE[d.documentCategory] ?? "gray";
            const isLast = idx === query.data!.length - 1;
            return (
              <div
                key={d.id}
                className={
                  "flex items-center gap-2.5 py-2 " +
                  (isLast ? "" : "border-b border-line-soft")
                }
              >
                <IconTile icon={Icon} tone={tone} size="sm" />
                <div className="min-w-0 flex-1">
                  <div className="truncate text-[12.5px] font-medium text-ink">
                    {d.label || d.fileName}
                  </div>
                  <div className="font-mono-tabular text-[11px] text-ink-3">
                    {humanSize(d.fileSize)} · {formatDate(d.attachedAt, { locale: "fr" })}
                  </div>
                </div>
                <a
                  href={`/api/files/${d.fileId}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="grid h-7 w-7 place-items-center rounded-[8px] border border-line bg-white text-ink-3 hover:border-line-strong hover:text-ink"
                  aria-label="Download"
                >
                  <Download className="h-3.5 w-3.5" />
                </a>
              </div>
            );
          })}
        </div>
      )}
    </>
  );
}
