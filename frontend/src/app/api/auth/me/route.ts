import { NextResponse } from "next/server";
import { getSession } from "@/server/session";

export async function GET() {
  const session = await getSession();
  if (!session) {
    return NextResponse.json(
      { success: false, message: "Not authenticated", errorCode: "UNAUTHORIZED" },
      { status: 401 },
    );
  }
  return NextResponse.json({
    success: true,
    user: session.user,
    context: session.context,
    permissions: session.permissions,
    expiresAt: session.expiresAt,
  });
}
