import "server-only";

import { cookies } from "next/headers";
import { getIronSession } from "iron-session";
import Redis from "ioredis";

import { serverEnv } from "@/env";
import type { AppSession } from "@/lib/types/auth";

/**
 * Session is split in two for size reasons (a single user can hold dozens of
 * permissions which would inflate an iron-session sealed cookie above the 4KB
 * browser cookie limit):
 *
 *   - iron-session cookie  →  small `{ sid: string; expiresAt: number }`
 *     (httpOnly, signed, encrypted; survives without Redis but only carries
 *      enough to identify a server-side blob)
 *   - Redis hash           →  full AppSession under `hrm:session:{sid}`
 *
 * If Redis is offline the BFF transparently degrades to "no session" — the
 * user is asked to log in again.
 */

type CookiePayload = {
  sid?: string;
  expiresAt?: number;
};

const SESSION_OPTIONS = {
  cookieName: serverEnv?.SESSION_COOKIE_NAME ?? "hrm_session",
  password: serverEnv?.SESSION_SECRET ?? "fallback-secret-do-not-use-in-production-12345678",
  cookieOptions: {
    httpOnly: true,
    sameSite: "lax" as const,
    secure: serverEnv?.NODE_ENV === "production",
    maxAge: serverEnv?.SESSION_TTL_SECONDS ?? 3600,
    path: "/",
  },
};

// Cached Redis singleton — Next.js dev mode hot-reloads modules but we keep a
// single connection per process via globalThis.
declare global {
  // eslint-disable-next-line no-var
  var __hrmRedis: Redis | undefined;
}

function redis(): Redis {
  if (!globalThis.__hrmRedis) {
    const host = process.env.REDIS_HOST ?? "localhost";
    const port = Number(process.env.REDIS_PORT ?? "6379");
    const password = process.env.REDIS_PASSWORD || undefined;
    globalThis.__hrmRedis = new Redis({ host, port, password, lazyConnect: false });
    globalThis.__hrmRedis.on("error", (err) => {
      // eslint-disable-next-line no-console
      console.error("redis", err.message);
    });
  }
  return globalThis.__hrmRedis;
}

function sessionKey(sid: string): string {
  return `hrm:session:${sid}`;
}

function newSid(): string {
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return (crypto as Crypto).randomUUID();
  }
  return Math.random().toString(36).slice(2) + Date.now().toString(36);
}

async function getCookieSession() {
  const cookieStore = await cookies();
  return getIronSession<CookiePayload>(cookieStore, SESSION_OPTIONS);
}

/**
 * Persist (or overwrite) the active session in Redis and the matching SID in
 * the iron-session cookie. Returns the SID.
 */
export async function writeSession(data: AppSession): Promise<string> {
  const cookie = await getCookieSession();
  const sid = cookie.sid ?? newSid();
  const ttl = serverEnv?.SESSION_TTL_SECONDS ?? 3600;
  await redis().set(sessionKey(sid), JSON.stringify(data), "EX", ttl);
  cookie.sid = sid;
  cookie.expiresAt = data.expiresAt;
  await cookie.save();
  return sid;
}

/**
 * Read the live session from Redis, or null if absent/expired.
 *
 * NOTE: this function is safe to call from Server Components. It intentionally
 * does NOT call cookie.destroy() — modifying cookies is only permitted in
 * Route Handlers and Server Actions (Next.js App Router constraint). The cookie
 * expires on its own via maxAge; actual destruction is done in destroySession()
 * which is called from the logout Route Handler.
 */
export async function readSession(): Promise<AppSession | null> {
  const cookie = await getCookieSession();
  if (!cookie.sid) return null;
  if (cookie.expiresAt && cookie.expiresAt * 1000 < Date.now()) {
    return null;
  }
  try {
    const raw = await redis().get(sessionKey(cookie.sid));
    if (!raw) return null;
    const session = JSON.parse(raw) as AppSession;
    if (session.expiresAt * 1000 < Date.now()) {
      await redis().del(sessionKey(cookie.sid));
      return null;
    }
    return session;
  } catch {
    return null;
  }
}

export async function requireSession(): Promise<AppSession> {
  const session = await readSession();
  if (!session) {
    throw new Error("UNAUTHORIZED");
  }
  return session;
}

export async function destroySession(): Promise<void> {
  const cookie = await getCookieSession();
  if (cookie.sid) {
    try {
      await redis().del(sessionKey(cookie.sid));
    } catch {
      // ignore
    }
  }
  cookie.destroy();
}

/**
 * Partial update of an existing session (used after change-password to flip
 * forcePasswordChange off). No-op if the session is gone.
 */
export async function patchSession(patch: Partial<AppSession>): Promise<void> {
  const cookie = await getCookieSession();
  if (!cookie.sid) return;
  try {
    const raw = await redis().get(sessionKey(cookie.sid));
    if (!raw) return;
    const current = JSON.parse(raw) as AppSession;
    const next = { ...current, ...patch };
    const ttl = serverEnv?.SESSION_TTL_SECONDS ?? 3600;
    await redis().set(sessionKey(cookie.sid), JSON.stringify(next), "EX", ttl);
  } catch {
    // ignore
  }
}
