import "server-only";

import type { NextRequest } from "next/server";

import { authenticatedRoute } from "@/server/handlers";
import * as timesheetsApi from "@/server/ksm/modules/timesheets";
import type { TimesheetResponse } from "@/server/ksm/modules/timesheets";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

/** How many months of history (including the selected one) the trend strip shows. */
const HISTORY_MONTHS = 6;

function currentPeriode(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

/** The N periods ending at (and including) `periode`, oldest first, as YYYY-MM strings. */
function periodWindow(periode: string, count: number): string[] {
  const [y, m] = periode.split("-").map(Number);
  const out: string[] = [];
  for (let i = count - 1; i >= 0; i -= 1) {
    const d = new Date(y, (m ?? 1) - 1 - i, 1);
    out.push(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`);
  }
  return out;
}

/**
 * Self-service time payload for the authenticated employee:
 * - `timesheets`: entries for the selected period (detailed view + actions),
 * - `history`: entries across the last {@link HISTORY_MONTHS} months for the trend strip.
 *
 * History is fetched per month in parallel and degrades gracefully (a failed month is skipped),
 * so a single slow/empty period never breaks the page.
 */
export async function GET(request: NextRequest) {
  const periode = request.nextUrl.searchParams.get("periode") ?? currentPeriode();
  return authenticatedRoute(async (session) => {
    const employee = await findMyEmployee(session);
    if (!employee) {
      return Response.json({
        ok: true,
        data: { employee: null, periode, timesheets: [], history: [] },
      });
    }

    const window = periodWindow(periode, HISTORY_MONTHS);
    const results = await Promise.allSettled(
      window.map((p) => timesheetsApi.listByEmployee(employee.id, p, session)),
    );
    const history: TimesheetResponse[] = results.flatMap((r) =>
      r.status === "fulfilled" ? r.value : [],
    );
    const timesheets = history.filter((ts) => ts.periode === periode);

    return Response.json({ ok: true, data: { employee, periode, timesheets, history } });
  });
}
