import { NextResponse } from "next/server";
import { getKsmContext } from "@/server/ksm/context";
import { ksmUploadFile } from "@/server/ksm/modules/files";
import { HttpError } from "@/lib/types/api";
import { logger } from "@/lib/log";

export async function POST(request: Request) {
  try {
    const ctx = await getKsmContext();
    const stored = await ksmUploadFile(request, ctx);
    return NextResponse.json({ success: true, data: stored }, { status: 201 });
  } catch (err) {
    if (err instanceof HttpError) {
      return NextResponse.json(
        { success: false, message: err.message, errorCode: err.errorCode ?? "UPSTREAM" },
        { status: err.status === 401 || err.status === 403 ? err.status : 502 },
      );
    }
    logger.error({ err: String(err) }, "POST /api/hrm/files failed");
    return NextResponse.json(
      { success: false, message: "Upload failed", errorCode: "INTERNAL" },
      { status: 500 },
    );
  }
}
