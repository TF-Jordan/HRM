import "server-only";

import type { NextRequest } from "next/server";

import { authenticatedRoute } from "@/server/handlers";
import * as filesApi from "@/server/ksm/modules/files";

const TARGET_TYPE = "EMPLOYEE";

/**
 * Convenience facade around file-core's document-hub for the employee 360°
 * profile: GET lists every document linked to the employee, POST uploads a
 * new file AND attaches it to the employee in one round-trip.
 */
export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ employeeId: string }> },
) {
  const { employeeId } = await params;
  return authenticatedRoute(async (session) => {
    const data = await filesApi.listTargetDocuments(TARGET_TYPE, employeeId, session);
    return Response.json({ ok: true, data });
  });
}

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ employeeId: string }> },
) {
  const { employeeId } = await params;
  return authenticatedRoute(async (session) => {
    const formData = await request.formData();
    const file = formData.get("file");
    if (!(file instanceof Blob)) {
      return Response.json(
        { ok: false, status: 400, errorCode: "VALIDATION_ERROR", message: "file is required" },
        { status: 400 },
      );
    }
    const category =
      typeof formData.get("category") === "string"
        ? (formData.get("category") as string)
        : "GENERAL";
    const labelRaw = formData.get("label");
    const label = typeof labelRaw === "string" && labelRaw.length > 0 ? labelRaw : undefined;

    // Step 1: upload bytes to file-core (multipart relay).
    const uploadFd = new FormData();
    uploadFd.append("file", file, (file as File).name ?? "upload.bin");
    const stored = await filesApi.uploadFile(uploadFd, session);

    // Step 2: attach the new file to this employee via document-hub.
    const link = await filesApi.attachDocument(
      {
        targetType: TARGET_TYPE,
        targetId: employeeId,
        fileId: stored.id,
        documentCategory: category,
        label,
      },
      session,
    );
    return Response.json({ ok: true, data: link }, { status: 201 });
  });
}
