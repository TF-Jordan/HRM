import "server-only";

import type { NextRequest } from "next/server";

import { authenticatedRoute } from "@/server/handlers";
import * as profileApi from "@/server/ksm/modules/employee-profile";

export async function PATCH(
  request: NextRequest,
  { params }: { params: Promise<{ contactId: string }> },
) {
  const { contactId } = await params;
  return authenticatedRoute(async (session) => {
    const body = await request.json();
    const data = await profileApi.updateMyEmergencyContact(contactId, body, session);
    return Response.json({ ok: true, data });
  });
}

export async function DELETE(
  _request: NextRequest,
  { params }: { params: Promise<{ contactId: string }> },
) {
  const { contactId } = await params;
  return authenticatedRoute(async (session) => {
    await profileApi.deleteMyEmergencyContact(contactId, session);
    return new Response(null, { status: 204 });
  });
}
