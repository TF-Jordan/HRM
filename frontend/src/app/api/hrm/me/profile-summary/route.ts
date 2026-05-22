import { NextResponse } from "next/server";
import { getProfileSummary } from "@/server/profile";

export async function GET() {
  const profile = await getProfileSummary();
  if (!profile) {
    return NextResponse.json(
      { success: false, message: "Unauthorized", errorCode: "UNAUTHORIZED" },
      { status: 401 },
    );
  }
  // Strip permissions from the JSON wire payload (already exposed via /api/auth/me).
  const { permissions: _omit, ...payload } = profile;
  void _omit;
  return NextResponse.json({ success: true, data: payload });
}
