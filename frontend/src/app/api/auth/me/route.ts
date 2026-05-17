import { NextResponse } from "next/server";
import { decodeJwt } from "jose";
import { getSession } from "@/server/session";

/**
 * Returns the live session view. Permissions are NOT stored in the BFF cookie
 * (the JWT would exceed Chromium's 4 KiB cookie limit with 50+ HRM perms);
 * we decode them from the KSM access token instead. The KSM token has already
 * been issued by auth-core, so we trust its payload without re-verifying.
 */
export async function GET() {
  const session = await getSession();
  if (!session) {
    return NextResponse.json(
      { success: false, message: "Not authenticated", errorCode: "UNAUTHORIZED" },
      { status: 401 },
    );
  }
  let permissions: string[] = [];
  try {
    const payload = decodeJwt(session.accessToken) as { permissions?: string[] };
    permissions = payload.permissions ?? [];
  } catch {
    permissions = [];
  }
  return NextResponse.json({
    success: true,
    data: {
      user: session.user,
      context: session.context,
      permissions,
      expiresAt: session.expiresAt,
    },
  });
}
