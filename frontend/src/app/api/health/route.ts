import { NextResponse } from "next/server";
import { ksmHealth } from "@/server/ksm/client";

export const dynamic = "force-dynamic";

export async function GET() {
  const ksm = await ksmHealth();
  return NextResponse.json({
    success: true,
    frontend: { status: "up", ts: new Date().toISOString() },
    ksm,
  });
}
