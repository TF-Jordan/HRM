import "server-only";

import type { Session } from "@/lib/types/auth";

/**
 * In-memory session store keyed by a random sessionId. The cookie only carries
 * the sessionId (small), while the heavy KSM accessToken (which can exceed 4 KiB
 * once it embeds all permissions) lives here. The store is process-local;
 * for a multi-instance deployment, swap this with a Redis-backed implementation.
 */

type Entry = { session: Session; expiresAt: number };

const STORE: Map<string, Entry> = (globalThis as unknown as { __hrmSessionStore?: Map<string, Entry> }).__hrmSessionStore ?? new Map();
(globalThis as unknown as { __hrmSessionStore?: Map<string, Entry> }).__hrmSessionStore = STORE;

function evictExpired(now = Date.now()): void {
  for (const [id, entry] of STORE) {
    if (entry.expiresAt <= now) STORE.delete(id);
  }
}

export function putSessionEntry(sessionId: string, session: Session): void {
  evictExpired();
  STORE.set(sessionId, { session, expiresAt: session.expiresAt });
}

export function getSessionEntry(sessionId: string): Session | null {
  const entry = STORE.get(sessionId);
  if (!entry) return null;
  if (entry.expiresAt <= Date.now()) {
    STORE.delete(sessionId);
    return null;
  }
  return entry.session;
}

export function deleteSessionEntry(sessionId: string): void {
  STORE.delete(sessionId);
}
