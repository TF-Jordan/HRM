import "server-only";

import { callKsm } from "@/server/ksm/client";
import { serverEnv } from "@/env";
import type { AppSession } from "@/lib/types/auth";
import { logger } from "@/server/logger";

/* ============================== Types ============================== */

export type StoredFileResponse = {
  id: string;
  organizationId?: string | null;
  uploadedByUserId?: string | null;
  fileName: string;
  contentType: string;
  size: number;
};

export type DocumentLinkView = {
  id: string;
  organizationId: string;
  targetType: string;
  targetId: string;
  fileId: string;
  fileName: string;
  contentType: string;
  fileSize: number;
  documentCategory: string;
  label?: string | null;
  attachedByUserId?: string | null;
  attachedAt: string;
};

export type AttachDocumentRequest = {
  targetType: string;
  targetId: string;
  fileId: string;
  documentCategory: string;
  label?: string;
};

/* ============================== Calls ============================== */

/**
 * Upload a file to file-core. We forward a multipart/form-data body to KSM
 * preserving the user's session JWT and tenant headers — the existing
 * callKsm() does not handle multipart, so we craft the fetch manually with
 * the same header injection contract.
 */
export async function uploadFile(
  formData: FormData,
  session: AppSession,
): Promise<StoredFileResponse> {
  if (!serverEnv) throw new Error("uploadFile must be called server-side");
  const headers: Record<string, string> = {
    Accept: "application/json",
    "X-Client-Id": serverEnv.KSM_CLIENT_ID,
    "X-Api-Key": serverEnv.KSM_API_KEY,
    "X-Request-Id": crypto.randomUUID(),
    Authorization: `Bearer ${session.accessToken}`,
    "X-Tenant-Id": session.user.tenantId,
  };
  if (session.workspace?.organizationId) {
    headers["X-Organization-Id"] = session.workspace.organizationId;
  }
  if (session.workspace?.agencyId) {
    headers["X-Agency-Id"] = session.workspace.agencyId;
  }

  const res = await fetch(`${serverEnv.KSM_BASE_URL}/api/files`, {
    method: "POST",
    headers,
    body: formData,
    cache: "no-store",
  });
  const text = await res.text();
  if (!res.ok) {
    logger.error({ status: res.status, body: text.slice(0, 200) }, "uploadFile.failed");
    throw new Error(`Upload failed: ${res.status} ${text.slice(0, 120)}`);
  }
  const envelope = JSON.parse(text) as { success: boolean; data: StoredFileResponse };
  if (!envelope.success) throw new Error("Upload rejected by KSM");
  return envelope.data;
}

export function attachDocument(body: AttachDocumentRequest, session: AppSession) {
  return callKsm<DocumentLinkView>(
    "/api/document-hub/links",
    { method: "POST", body },
    { session },
  );
}

export function listTargetDocuments(
  targetType: string,
  targetId: string,
  session: AppSession,
) {
  return callKsm<DocumentLinkView[]>(
    `/api/document-hub/targets/${encodeURIComponent(targetType)}/${encodeURIComponent(targetId)}`,
    {},
    { session },
  );
}
