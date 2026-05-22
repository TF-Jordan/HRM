"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { Settings } from "lucide-react";
import { Link } from "@/i18n/navigation";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { useUsers } from "@/hooks/modules/useAdmin";
import { useFormat } from "@/hooks/useFormat";

export function AdminUsersClient() {
  const t = useTranslations("admin.users");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const users = useUsers();

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        crumbs={[{ label: tNav("sections.administration") }, { label: tNav("items.adminUsers") }]}
        title={t("title")}
        subtitle={t("subtitle")}
      />

      {users.isLoading && <Skeleton className="h-32 w-full" />}

      {!users.isLoading && users.data && users.data.length === 0 && (
        <Card>
          <CardContent className="py-8 text-center text-sm text-ink-3">{t("empty")}</CardContent>
        </Card>
      )}

      {!users.isLoading && users.data && users.data.length > 0 && (
        <Card>
          <CardContent className="p-0">
            <table className="w-full border-separate border-spacing-0">
              <thead>
                <tr>
                  {[
                    t("table.username"),
                    t("table.email"),
                    t("table.status"),
                    t("table.createdAt"),
                    "",
                  ].map((h, i) => (
                    <th
                      key={i}
                      className="border-b border-line bg-gradient-to-b from-cream-dim to-cream-soft px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3"
                    >
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {users.data.map((u) => (
                  <tr key={u.id} className="hover:bg-brand-50/40 dark:hover:bg-dark-3">
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] font-mono text-ink">
                      {u.username}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2">
                      {u.email}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px]">
                      <Badge tone={u.status === "ACTIVE" ? "green" : "gray"}>{u.status}</Badge>
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {fmt.date(u.createdAt)}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-right">
                      <Button asChild size="sm" variant="secondary">
                        <Link href={`/admin/users/${u.id}` as never}>
                          <Settings className="size-4" />
                          {t("actions.manageRoles")}
                        </Link>
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
