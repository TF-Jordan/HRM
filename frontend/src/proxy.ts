import createMiddleware from "next-intl/middleware";
import type { NextRequest } from "next/server";
import { NextResponse } from "next/server";
import { routing } from "@/i18n/routing";

const intlMiddleware = createMiddleware(routing);

const PUBLIC_PATHS = ["/login", "/mfa", "/select-context", "/forgot-password"];
const PUBLIC_API_PREFIXES = ["/api/auth/", "/api/health"];

function stripLocalePrefix(pathname: string): string {
  const segments = pathname.split("/");
  if (segments.length > 1 && segments[1] && routing.locales.includes(segments[1] as never)) {
    return "/" + segments.slice(2).join("/");
  }
  return pathname;
}

function isPublicPath(pathname: string): boolean {
  const stripped = stripLocalePrefix(pathname);
  if (stripped === "/" || stripped === "") return true;
  if (PUBLIC_PATHS.some((p) => stripped === p || stripped.startsWith(p + "/"))) return true;
  if (PUBLIC_API_PREFIXES.some((p) => pathname.startsWith(p))) return true;
  return false;
}

export function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // API routes: only run guard, no locale routing
  if (pathname.startsWith("/api/")) {
    if (isPublicPath(pathname)) return NextResponse.next();
    const sessionCookie = request.cookies.get(
      process.env.SESSION_COOKIE_NAME ?? "__Host-hrm_session",
    );
    if (!sessionCookie?.value) {
      return NextResponse.json(
        { success: false, message: "Unauthorized", errorCode: "UNAUTHORIZED" },
        { status: 401 },
      );
    }
    return NextResponse.next();
  }

  // Page routes: handle locale first, then auth
  const intlResponse = intlMiddleware(request);

  if (!isPublicPath(pathname)) {
    const sessionCookie = request.cookies.get(
      process.env.SESSION_COOKIE_NAME ?? "__Host-hrm_session",
    );
    if (!sessionCookie?.value) {
      const url = request.nextUrl.clone();
      const locale = pathname.split("/")[1];
      const isLocalePrefixed = locale && routing.locales.includes(locale as never);
      url.pathname = isLocalePrefixed ? `/${locale}/login` : "/login";
      url.searchParams.set("from", pathname);
      return NextResponse.redirect(url);
    }
  }

  return intlResponse;
}

export const config = {
  matcher: [
    // Skip Next.js internals, static files, and image optimizer
    "/((?!_next/static|_next/image|favicon.ico|.*\\.svg|.*\\.png|.*\\.jpg).*)",
  ],
};
