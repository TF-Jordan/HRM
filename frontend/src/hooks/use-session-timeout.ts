"use client";

import * as React from "react";

import { useSession } from "@/components/providers/session-provider";

/**
 * Warn the user before the session JWT expires, then sign them out automatically.
 * Returns the number of seconds remaining (or null if no session).
 */
export function useSessionTimeout(
  options: { warnBeforeSeconds?: number; onWarn?: () => void; onExpire?: () => void } = {},
): number | null {
  const { warnBeforeSeconds = 300, onWarn, onExpire } = options;
  const { session, setSession } = useSession();
  const [remaining, setRemaining] = React.useState<number | null>(null);
  const warnedRef = React.useRef(false);

  React.useEffect(() => {
    if (!session) {
      setRemaining(null);
      return;
    }
    const update = () => {
      const left = session.expiresAt - Math.floor(Date.now() / 1000);
      setRemaining(left);
      if (left <= 0) {
        setSession(null);
        onExpire?.();
        fetch("/api/auth/logout", { method: "POST" }).catch(() => null);
        if (typeof window !== "undefined") {
          window.location.assign("/login?reason=expired");
        }
        return;
      }
      if (left <= warnBeforeSeconds && !warnedRef.current) {
        warnedRef.current = true;
        onWarn?.();
      }
    };
    update();
    const id = window.setInterval(update, 5000);
    return () => window.clearInterval(id);
  }, [session, warnBeforeSeconds, onWarn, onExpire, setSession]);

  return remaining;
}
