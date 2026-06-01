import "server-only";

import type { NextRequest } from "next/server";

import { authenticatedRoute } from "@/server/handlers";
import * as filesApi from "@/server/ksm/modules/files";

/**
 * Upload raw bytes to file-core and return the stored file descriptor. Used by
 * surfaces that store a bare fileId on a domain row (e.g. an expense line's
 * justificatif) rather than a document-hub link.
 */
export async function POST(request: NextRequest) {
  return authenticatedRoute(async (session) => {
    const formData = await request.formData();
    const file = formData.get("file");
    if (!(file instanceof Blob)) {
      return Response.json(
        { ok: false, status: 400, errorCode: "VALIDATION_ERROR", message: "file is required" },
        { status: 400 },
      );
    }
    const fd = new FormData();
    fd.append("file", file, (file as File).name ?? "upload.bin");
    const stored = await filesApi.uploadFile(fd, session);
    return Response.json({ ok: true, data: stored }, { status: 201 });
  });
}
