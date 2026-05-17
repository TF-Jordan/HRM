"use client";

import { useTranslations } from "next-intl";
import { useQuery } from "@tanstack/react-query";
import { bffFetch } from "@/lib/api-client";
import { useMyEmployee } from "@/hooks/modules/useMe";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Avatar } from "@/components/ui/avatar";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";

type Me = {
  user: { userId: string; actorId: string; email: string; displayName: string };
  context: { tenantId: string; organizationId: string; agencyId: string | null };
  permissions: string[];
};

export function MyProfileClient() {
  const t = useTranslations("selfService.profile");
  const tNav = useTranslations("navigation");
  const tEmp = useTranslations("employees");
  const fmt = useFormat();
  const me = useQuery({ queryKey: ["auth", "me"], queryFn: () => bffFetch<Me>("/api/auth/me") });
  const employee = useMyEmployee();

  if (me.isLoading) return <Skeleton className="h-40 w-full" />;
  if (!me.data) return null;

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        crumbs={[{ label: tNav("items.profile") }]}
        title={
          <div className="flex items-center gap-3">
            <Avatar name={me.data.user.displayName} size="lg" />
            <span>{me.data.user.displayName}</span>
          </div>
        }
        subtitle={t("subtitle")}
      />

      <Card>
        <CardContent>
          <dl className="grid grid-cols-1 gap-x-8 gap-y-4 sm:grid-cols-2">
            <Field label="Email" value={me.data.user.email} />
            <Field label="User ID" value={<span className="font-mono text-[12px]">{me.data.user.userId}</span>} />
            <Field label="Tenant" value={<span className="font-mono text-[12px]">{me.data.context.tenantId}</span>} />
            <Field label="Organisation" value={<span className="font-mono text-[12px]">{me.data.context.organizationId}</span>} />
          </dl>
        </CardContent>
      </Card>

      {employee.data && (
        <Card>
          <CardContent>
            <div className="mb-4 font-display text-base font-bold text-ink">{tEmp("detail.tabs.identity")}</div>
            <dl className="grid grid-cols-1 gap-x-8 gap-y-4 sm:grid-cols-2">
              <Field label={tEmp("detail.fields.matricule")} value={<span className="font-mono">{employee.data.matricule}</span>} />
              <Field label={tEmp("detail.fields.hireDate")} value={fmt.date(employee.data.dateEmbauche)} />
              <Field label={tEmp("detail.fields.department")} value={employee.data.departmentCode ?? "—"} />
              <Field label={tEmp("detail.fields.category")} value={String(employee.data.categorie)} />
              <Field label={tEmp("table.status")} value={<StatusBadge kind="employee" status={employee.data.status} />} />
            </dl>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardContent>
          <div className="mb-4 font-display text-base font-bold text-ink">
            Permissions ({me.data.permissions.length})
          </div>
          <div className="flex flex-wrap gap-1.5">
            {me.data.permissions.slice(0, 30).map((p) => (
              <span key={p} className="rounded-md bg-cream-soft px-2 py-0.5 font-mono text-[11px] text-ink-3">
                {p}
              </span>
            ))}
            {me.data.permissions.length > 30 && (
              <span className="text-[11px] text-ink-4">+{me.data.permissions.length - 30}…</span>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

function Field({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex flex-col gap-1 border-b border-line-soft py-2 last:border-b-0">
      <dt className="text-[11px] font-semibold uppercase tracking-wider text-ink-3">{label}</dt>
      <dd className="text-[14px] text-ink">{value}</dd>
    </div>
  );
}
