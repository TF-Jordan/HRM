import "server-only";

import type { NextRequest } from "next/server";

import { authenticatedRoute } from "@/server/handlers";
import * as profileApi from "@/server/ksm/modules/employee-profile";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

export async function PATCH(
  request: NextRequest,
  { params }: { params: Promise<{ contactId: string }> },
) {
  const { contactId } = await params;
  return authenticatedRoute(async (session) => {
    const me = await findMyEmployee(session);
    if (!me) {
      return Response.json({ ok: false, status: 404, errorCode: "NOT_FOUND", message: "Employee not found" }, { status: 404 });
    }
    const body = await request.json();
    const data = await profileApi.updateEmergencyContact(me.id, contactId, body, session);
    return Response.json({ ok: true, data });
  });
}

export async function DELETE(
  _request: NextRequest,
  { params }: { params: Promise<{ contactId: string }> },
) {
  const { contactId } = await params;
  return authenticatedRoute(async (session) => {
    const me = await findMyEmployee(session);
    if (!me) {
      return Response.json({ ok: false, status: 404, errorCode: "NOT_FOUND", message: "Employee not found" }, { status: 404 });
    }
    await profileApi.deleteEmergencyContact(me.id, contactId, session);
    return new Response(null, { status: 204 });
  });
}
