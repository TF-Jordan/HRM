import "server-only";

import { serverEnv } from "@/env";
import { HttpError } from "@/lib/types/api";

type KsmCtx = { tenantId: string; organizationId: string; bearer: string };

export type StoredFile = {
  id: string;
  organizationId: string;
  uploadedByUserId: string;
  fileName: string;
  contentType: string;
  size: number;
};

/**
 * Forward a multipart file upload to KSM /api/files.
 * The original Request is required so we can pipe its multipart body unchanged.
 */
export async function ksmUploadFile(request: Request, ctx: KsmCtx): Promise<StoredFile> {
  const base = serverEnv.KSM_BASE_URL.replace(/\/$/, "");
  const contentType = request.headers.get("content-type") ?? "";
  const buffer = await request.arrayBuffer();

  const res = await fetch(`${base}/api/files`, {
    method: "POST",
    headers: {
      "Content-Type": contentType,
      Accept: "application/json",
      "X-Client-Id": serverEnv.KSM_CLIENT_ID,
      "X-Api-Key": serverEnv.KSM_API_KEY,
      "X-Tenant-Id": ctx.tenantId,
      "X-Organization-Id": ctx.organizationId,
      Authorization: `Bearer ${ctx.bearer}`,
    },
    body: buffer,
  });

  const text = await res.text();
  let parsed: { success?: boolean; data?: StoredFile; message?: string; errorCode?: string } | null = null;
  try {
    parsed = text ? JSON.parse(text) : null;
  } catch {
    parsed = null;
  }

  if (!res.ok || !parsed?.success || !parsed.data) {
    throw new HttpError({
      status: res.status,
      message: parsed?.message ?? `KSM POST /api/files → ${res.status}`,
      errorCode: parsed?.errorCode ?? "UPSTREAM",
    });
  }
  return parsed.data;
}
