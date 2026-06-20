"use client";

import { useMutation, useQuery } from "@tanstack/react-query";
import { Check, Copy, KeyRound, Loader2 } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { useSession } from "@/components/providers/session-provider";
import { PageHeader } from "@/components/shell/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field, Input } from "@/components/ui/input";
import { SectionTitle } from "@/components/ui/section-title";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { cn } from "@/lib/utils";
import type { AdministrationRole } from "@/server/ksm/modules/admin";

type FormValues = {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  roleId: string;
};

type CreateUserResult = {
  actorId: string;
  userId: string;
  username: string;
  email: string;
  temporaryPassword: string;
  rolesAssigned: number;
  warnings: string[];
};

type RoleAssignmentsView = {
  functions: Array<{
    roleId: string;
    code: string;
    holders: Array<{ userId: string; username: string; email: string }>;
  }>;
};

export function UserCreateForm() {
  const t = useTranslations("admin");
  const tCreate = useTranslations("admin.users.create");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const router = useRouter();
  const { session } = useSession();

  const rolesQuery = useQuery({
    queryKey: ["admin", "roles"],
    queryFn: () => apiFetch<AdministrationRole[]>("/api/admin/roles"),
  });

  // Exclusive function roles already held by an account (single-holder rule).
  // Used to disable those roles in the picker; the server re-checks authoritatively.
  const assignmentsQuery = useQuery({
    queryKey: ["admin", "role-assignments"],
    queryFn: () => apiFetch<RoleAssignmentsView>("/api/admin/role-assignments"),
  });
  const takenByRoleId = React.useMemo(() => {
    const map = new Map<string, string>();
    for (const fn of assignmentsQuery.data?.functions ?? []) {
      const holder = fn.holders[0];
      if (holder) map.set(fn.roleId, holder.username);
    }
    return map;
  }, [assignmentsQuery.data]);

  const { register, handleSubmit, watch, formState: { isSubmitting, errors } } = useForm<FormValues>({
    defaultValues: { firstName: "", lastName: "", email: "", phoneNumber: "", roleId: "" },
  });

  const [result, setResult] = React.useState<CreateUserResult | null>(null);

  const mutation = useMutation({
    mutationFn: async (v: FormValues) => {
      const orgId = session?.workspace?.organizationId;
      const role = rolesQuery.data?.find((r) => r.id === v.roleId);
      if (!role) throw new Error("Role not found");
      const isOrgScope = role.scopeType === "ORGANIZATION";
      return apiFetch<CreateUserResult>("/api/admin/users", {
        method: "POST",
        body: {
          firstName: v.firstName.trim(),
          lastName: v.lastName.trim(),
          email: v.email.trim().toLowerCase(),
          phoneNumber: v.phoneNumber.trim() || undefined,
          assignments: [
            {
              roleId: role.id,
              scopeType: role.scopeType,
              scopeId: isOrgScope ? orgId : null,
              scope: isOrgScope && orgId ? `ORGANIZATION:${orgId}` : role.scopeType,
            },
          ],
          forcePasswordChange: true,
        },
      });
    },
    onSuccess: (data) => {
      toast.success(tCreate("success"));
      setResult(data);
    },
    onError: (cause) => {
      if (cause instanceof BffApiError) {
        toast.error(
          cause.errorCode === "ROLE_ALREADY_ASSIGNED"
            ? tCreate("roleTakenError")
            : cause.message,
        );
      } else {
        toast.error(tErrors("unknown"));
      }
    },
  });

  const fullName = `${watch("firstName")} ${watch("lastName")}`.trim() || "—";

  if (result) {
    return (
      <>
        <PageHeader
          ucBadge={t("ucBadge")}
          breadcrumb={[{ label: "HR Core" }, { label: "Admin" }, { label: tCreate("title") }]}
          title={tCreate("success")}
          subtitle={result.email}
          actions={
            <Link href="/admin/users">
              <Button variant="secondary">{tCreate("openUsers")}</Button>
            </Link>
          }
        />
        <Card>
          <CardContent padding="lg">
            <div className="flex items-start gap-4">
              <span className="grid h-12 w-12 place-items-center rounded-[14px] bg-warning-50 text-warning-600">
                <KeyRound className="h-6 w-6" />
              </span>
              <div className="flex-1">
                <h2 className="font-display text-[18px] font-bold tracking-tight text-ink">
                  {tCreate("tempPasswordTitle")}
                </h2>
                <p className="mt-1 text-[13.5px] text-ink-3">
                  {tCreate("tempPasswordHint", { name: fullName })}
                </p>
                <TemporaryPasswordReveal value={result.temporaryPassword} />
                {result.warnings.length > 0 && (
                  <ul className="mt-3 list-disc pl-5 text-[12.5px] text-warning-600">
                    {result.warnings.map((w, i) => (
                      <li key={i}>{w}</li>
                    ))}
                  </ul>
                )}
              </div>
            </div>
          </CardContent>
        </Card>
      </>
    );
  }

  return (
    <form
      onSubmit={handleSubmit((v) => mutation.mutate(v))}
      onKeyDown={(e) => {
        if (e.key === "Enter" && (e.target as HTMLElement).tagName !== "TEXTAREA") {
          e.preventDefault();
        }
      }}
    >
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: "Admin" },
          { label: "Users", href: "/admin/users" },
          { label: tCreate("title") },
        ]}
        title={tCreate("title")}
        subtitle={tCreate("subtitle")}
        actions={
          <>
            <Button
              type="button"
              variant="secondary"
              onClick={() => router.push("/admin/users")}
            >
              {tCommon("actions.cancel")}
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  {tCommon("actions.loading")}
                </>
              ) : (
                tCreate("submit")
              )}
            </Button>
          </>
        }
      />

      <SectionTitle>Identité</SectionTitle>
      <Card className="mb-6">
        <CardContent padding="lg">
          <div className="grid grid-cols-2 gap-4">
            <Field label={tCreate("firstName")}>
              <Input {...register("firstName", { required: true })} />
            </Field>
            <Field label={tCreate("lastName")}>
              <Input {...register("lastName", { required: true })} />
            </Field>
            <Field label={tCreate("email")}>
              <Input
                type="email"
                {...register("email", { required: true })}
                placeholder="prenom.nom@example.com"
              />
            </Field>
            <Field label={tCreate("phone")}>
              <Input {...register("phoneNumber")} placeholder="+237 6XX XX XX XX" />
            </Field>
          </div>
        </CardContent>
      </Card>

      <SectionTitle>Rôle initial</SectionTitle>
      <Card>
        <CardContent padding="lg">
          <p className="mb-3 text-[13px] text-ink-3">{tCreate("roleHint")}</p>
          {rolesQuery.isLoading ? (
            <Loader2 className="h-5 w-5 animate-spin text-orange-500" />
          ) : (
            <div className="grid grid-cols-2 gap-2">
              {(rolesQuery.data ?? []).map((role) => {
                const isSelected = watch("roleId") === role.id;
                const takenBy = takenByRoleId.get(role.id);
                const disabled = Boolean(takenBy);
                return (
                  <label
                    key={role.id}
                    className={cn(
                      "flex items-start gap-3 rounded-[14px] border bg-white p-3.5 transition-all",
                      disabled
                        ? "cursor-not-allowed border-line opacity-60"
                        : "cursor-pointer",
                      isSelected
                        ? "border-orange-400 shadow-orange-brand"
                        : !disabled && "border-line hover:border-line-strong",
                    )}
                  >
                    <input
                      type="radio"
                      value={role.id}
                      disabled={disabled}
                      {...register("roleId", { required: true })}
                      className="mt-1"
                    />
                    <div className="flex-1">
                      <div className="flex items-center gap-2">
                        <span className="text-[13.5px] font-semibold text-ink">{role.name}</span>
                        <span className="rounded-md border border-line bg-bg-soft px-1.5 py-0.5 font-mono-tabular text-[10px] uppercase text-ink-3">
                          {role.scopeType}
                        </span>
                      </div>
                      <div className="mt-0.5 font-mono-tabular text-[11px] text-ink-3">{role.code}</div>
                      {takenBy ? (
                        <div className="mt-1 text-[11.5px] font-medium text-warning-600">
                          {tCreate("roleTaken", { holder: takenBy })}
                        </div>
                      ) : (
                        <div className="mt-1 text-[11.5px] text-ink-3">
                          {role.permissions.length} permission(s)
                        </div>
                      )}
                    </div>
                  </label>
                );
              })}
            </div>
          )}
          {errors.roleId && (
            <p className="mt-2 text-[12px] text-danger-600">
              {tCreate("roleHint")}
            </p>
          )}
        </CardContent>
      </Card>
    </form>
  );
}

function TemporaryPasswordReveal({ value }: { value: string }) {
  const t = useTranslations("admin.users.create");
  const [copied, setCopied] = React.useState(false);
  async function copy() {
    try {
      await navigator.clipboard.writeText(value);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // ignored
    }
  }
  return (
    <div className="mt-3 flex items-center gap-3 rounded-[14px] border border-warning-500/30 bg-warning-50 p-4">
      <code className="flex-1 font-mono-tabular text-[15px] font-semibold tracking-wider text-ink">
        {value}
      </code>
      <Button type="button" size="sm" variant="secondary" onClick={copy}>
        {copied ? (
          <>
            <Check className="h-4 w-4" />
            {t("copied")}
          </>
        ) : (
          <>
            <Copy className="h-4 w-4" />
            {t("copy")}
          </>
        )}
      </Button>
    </div>
  );
}
