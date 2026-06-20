import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";

export type DocumentSequence = {
  id: string;
  tenantId: string;
  organizationId?: string | null;
  agencyId?: string | null;
  documentType: string;
  prefix?: string | null;
  suffix?: string | null;
  paddingWidth: number;
  nextNumber: number;
};

/** List the document-numbering sequences configured for an organisation. */
export function listDocumentSequences(organizationId: string, session: AppSession) {
  return callKsm<DocumentSequence[]>(
    `/api/settings/document-sequences?organizationId=${organizationId}`,
    {},
    { session },
  );
}

export type UpsertDocumentSequenceBody = {
  organizationId: string;
  agencyId?: string | null;
  documentType: string;
  prefix?: string | null;
  suffix?: string | null;
  paddingWidth: number;
  nextNumber: number;
};

/**
 * Create or update a document-numbering sequence. Upsert semantics overwrite
 * `nextNumber` — callers must only invoke this when the sequence is absent, never
 * unconditionally, or they will reset the counter.
 */
export function upsertDocumentSequence(body: UpsertDocumentSequenceBody, session: AppSession) {
  return callKsm<DocumentSequence>(
    "/api/settings/document-sequences",
    { method: "POST", body },
    { session },
  );
}
