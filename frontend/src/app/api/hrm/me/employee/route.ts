import { NextResponse } from "next/server";
import { getKsmContext } from "@/server/ksm/context";
import { ksmListEmployees } from "@/server/ksm/modules/employees";
import { requireSession } from "@/server/session";
import { HttpError } from "@/lib/types/api";

/**
 * Resolves the Employee row for the current session's actor.
 * Returns 404 with errorCode NO_EMPLOYEE when the user has no employee record
 * (e.g. tenant admin who only manages but isn't part of payroll).
 */
export async function GET() {
  try {
    const session = await requireSession();
    const ctx = await getKsmContext();
    const list = await ksmListEmployees(ctx);
    const me = list.find((e) => e.actorId === session.user.actorId);
    if (!me) {
      return NextResponse.json(
        {
          success: false,
          message: "Not an employee in this organisation",
          errorCode: "NO_EMPLOYEE",
        },
        { status: 404 },
      );
    }
    return NextResponse.json({ success: true, data: me });
  } catch (err) {
    if (err instanceof Error && err.message === "Not authenticated") {
      return NextResponse.json(
        { success: false, message: "Unauthorized", errorCode: "UNAUTHORIZED" },
        { status: 401 },
      );
    }
    if (err instanceof HttpError) {
      return NextResponse.json(
        { success: false, message: err.message, errorCode: err.errorCode ?? "UPSTREAM" },
        { status: err.status === 401 ? 401 : 502 },
      );
    }
    return NextResponse.json(
      { success: false, message: "Unexpected error", errorCode: "INTERNAL" },
      { status: 500 },
    );
  }
}
