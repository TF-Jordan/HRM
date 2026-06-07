"use client";

import { FileSearch, ShieldCheck, Sparkles, UserCog, Users } from "lucide-react";
import { useTranslations } from "next-intl";

import { PageHeader } from "@/components/shell/page-header";
import { Card, CardContent } from "@/components/ui/card";
import { IconTile } from "@/components/ui/icon-tile";
import { Link } from "@/i18n/navigation";

const CARDS: Array<{
  href: string;
  icon: typeof Users;
  tone: "orange" | "info" | "violet" | "success" | "warning" | "teal" | "gray";
  key: "roleAssignments" | "users" | "roles" | "audit";
}> = [
  { href: "/admin/role-assignments", icon: UserCog, tone: "orange", key: "roleAssignments" },
  { href: "/admin/users", icon: Users, tone: "violet", key: "users" },
  { href: "/admin/roles", icon: ShieldCheck, tone: "success", key: "roles" },
  { href: "/admin/audit", icon: FileSearch, tone: "warning", key: "audit" },
];

export function AdminLanding() {
  const t = useTranslations("admin");
  const tLanding = useTranslations("admin.landing");
  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: tLanding("title") }]}
        title={tLanding("title")}
        subtitle={tLanding("subtitle")}
      />

      <div className="grid grid-cols-3 gap-4">
        {CARDS.map((card) => (
          <Link key={card.href} href={card.href} className="block">
            <Card clickable className="h-full">
              <CardContent padding="lg">
                <div className="mb-3 flex items-center gap-3">
                  <IconTile icon={card.icon} tone={card.tone} size="lg" />
                  <Sparkles className="ml-auto h-4 w-4 text-ink-4" aria-hidden="true" />
                </div>
                <h3 className="font-display text-[18px] font-bold tracking-tight text-ink">
                  {tLanding(`cards.${card.key}.title`)}
                </h3>
                <p className="mt-1 text-[13.5px] text-ink-3">
                  {tLanding(`cards.${card.key}.description`)}
                </p>
              </CardContent>
            </Card>
          </Link>
        ))}
      </div>
    </>
  );
}
