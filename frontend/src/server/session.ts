import "server-only";

import { cookies } from "next/headers";
import { SignJWT, jwtVerify } from "jose";
import { serverEnv } from "@/env";
import type { Session } from "@/lib/types/auth";
import {
  deleteSessionEntry,
  getSessionEntry,
  putSessionEntry,
} from "./session-store";

const issuer = "hrm-frontend";
const audience = "hrm-session";

function getKey(): Uint8Array {
  return new TextEncoder().encode(serverEnv.SESSION_SECRET);
}

/** Cookie payload — only carries an opaque session id, never the KSM token. */
type CookiePayload = { sid: string };

async function signCookie(payload: CookiePayload, exp: number): Promise<string> {
  return new SignJWT({ ...payload } as Record<string, unknown>)
    .setProtectedHeader({ alg: "HS256" })
    .setIssuer(issuer)
    .setAudience(audience)
    .setIssuedAt()
    .setExpirationTime(Math.floor(exp / 1000))
    .sign(getKey());
}

async function verifyCookie(token: string): Promise<CookiePayload | null> {
  try {
    const { payload } = await jwtVerify(token, getKey(), { issuer, audience });
    if (typeof payload.sid !== "string") return null;
    return { sid: payload.sid };
  } catch {
    return null;
  }
}

export async function getSession(): Promise<Session | null> {
  const store = await cookies();
  const cookie = store.get(serverEnv.SESSION_COOKIE_NAME);
  if (!cookie?.value) return null;
  const decoded = await verifyCookie(cookie.value);
  if (!decoded) return null;
  return getSessionEntry(decoded.sid);
}

export async function requireSession(): Promise<Session> {
  const session = await getSession();
  if (!session) throw new Error("Not authenticated");
  if (session.expiresAt < Date.now()) throw new Error("Session expired");
  return session;
}

export async function setSession(session: Session): Promise<void> {
  const sid = crypto.randomUUID();
  putSessionEntry(sid, session);
  const token = await signCookie({ sid }, session.expiresAt);
  const store = await cookies();
  store.set(serverEnv.SESSION_COOKIE_NAME, token, {
    httpOnly: true,
    secure: serverEnv.NODE_ENV !== "development",
    sameSite: "lax",
    path: "/",
    maxAge: Math.max(1, Math.floor((session.expiresAt - Date.now()) / 1000)),
  });
}

export async function clearSession(): Promise<void> {
  const store = await cookies();
  const cookie = store.get(serverEnv.SESSION_COOKIE_NAME);
  if (cookie?.value) {
    const decoded = await verifyCookie(cookie.value);
    if (decoded) deleteSessionEntry(decoded.sid);
  }
  store.delete(serverEnv.SESSION_COOKIE_NAME);
  store.delete(serverEnv.WORKSPACE_COOKIE_NAME);
}

export async function ensureCsrfToken(): Promise<string> {
  const store = await cookies();
  const existing = store.get(serverEnv.CSRF_COOKIE_NAME);
  if (existing?.value) return existing.value;
  const token = crypto.randomUUID();
  store.set(serverEnv.CSRF_COOKIE_NAME, token, {
    httpOnly: false,
    secure: serverEnv.NODE_ENV !== "development",
    sameSite: "lax",
    path: "/",
  });
  return token;
}
