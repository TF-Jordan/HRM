"use client";

import * as React from "react";

import { useSession } from "@/components/providers/session-provider";
import { Link, usePathname, useRouter } from "@/i18n/navigation";
import { activeSlugFromPath, entitledSlugs, isMigratedRole } from "@/lib/roles";

/**
 * Role-aware navigation for the partitioned frontend.
 *
 * Shared feature components keep authoring app-internal links with their plain,
 * role-agnostic path (e.g. `/leaves/new`). At runtime AppLink / useAppRouter
 * prepend the active role's slug so the user stays inside their own namespace
 * (`/employee/leaves/new`). For roles not yet migrated to a dedicated route tree
 * the prefix is empty, preserving the legacy flat routes untouched.
 *
 * Only absolute in-app paths are rewritten. External URLs, API routes, hashes,
 * and already-prefixed paths are passed through unchanged.
 */
function useRolePrefix(): string {
  const { session } = useSession();
  const pathname = usePathname();
  const entitled = entitledSlugs(session?.user.roles, session?.user.permissions);
  const active = activeSlugFromPath(pathname, entitled);
  return isMigratedRole(active) ? `/${active}` : "";
}

export function withRolePrefix(prefix: string, href: string): string {
  if (!prefix) return href;
  if (!href.startsWith("/")) return href; // relative, hash, or external-ish
  if (href.startsWith("//")) return href; // protocol-relative external
  if (href.startsWith("/api/")) return href; // BFF routes are not localized/prefixed
  // Avoid double-prefixing if a caller already passed the role-scoped path.
  if (href === prefix || href.startsWith(`${prefix}/`)) return href;
  return `${prefix}${href}`;
}

type LinkProps = React.ComponentProps<typeof Link>;

/** Drop-in replacement for the i18n <Link> that prepends the active role slug. */
export function AppLink({ href, ...rest }: LinkProps) {
  const prefix = useRolePrefix();
  const resolved = typeof href === "string" ? withRolePrefix(prefix, href) : href;
  return <Link href={resolved} {...rest} />;
}

type NavOptions = Parameters<ReturnType<typeof useRouter>["push"]>[1];

/** Drop-in replacement for the i18n useRouter() that prepends the active role slug. */
export function useAppRouter() {
  const router = useRouter();
  const prefix = useRolePrefix();

  return React.useMemo(
    () => ({
      push: (href: string, options?: NavOptions) =>
        router.push(withRolePrefix(prefix, href), options),
      replace: (href: string, options?: NavOptions) =>
        router.replace(withRolePrefix(prefix, href), options),
      back: () => router.back(),
      forward: () => router.forward(),
      refresh: () => router.refresh(),
      prefetch: (href: string) => router.prefetch(withRolePrefix(prefix, href)),
    }),
    [router, prefix],
  );
}
