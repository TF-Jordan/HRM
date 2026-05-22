"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Loader2, Upload, Save, KeyRound, AlertTriangle, CheckCircle2 } from "lucide-react";
import { toast } from "sonner";
import { bffFetch } from "@/lib/api-client";
import { useMyEmployee } from "@/hooks/modules/useMe";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Avatar } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";

type Me = {
  user: { userId: string; actorId: string; email: string; displayName: string };
  context: { tenantId: string; organizationId: string; agencyId: string | null };
  permissions: string[];
};

type StoredFile = { id: string; fileName: string; contentType: string; size: number };

async function uploadPhoto(file: File): Promise<StoredFile> {
  const fd = new FormData();
  fd.append("file", file);
  const res = await fetch("/api/hrm/files", { method: "POST", body: fd });
  const json = await res.json();
  if (!res.ok || !json.success) {
    throw new Error(json.message ?? `Upload failed (${res.status})`);
  }
  return json.data as StoredFile;
}

export function MyProfileClient() {
  const t = useTranslations("selfService.profile");
  const tNav = useTranslations("navigation");
  const tEmp = useTranslations("employees");
  const tCommon = useTranslations("common");
  const fmt = useFormat();
  const qc = useQueryClient();
  const me = useQuery({ queryKey: ["auth", "me"], queryFn: () => bffFetch<Me>("/api/auth/me") });
  const employee = useMyEmployee();

  const fileInputRef = React.useRef<HTMLInputElement>(null);
  const [pwdState, setPwdState] = React.useState({ old: "", next: "", confirm: "" });

  const photoMutation = useMutation({
    mutationFn: async (file: File) => {
      const uploaded = await uploadPhoto(file);
      return bffFetch<unknown>("/api/hrm/me/photo", {
        method: "PUT",
        body: JSON.stringify({ photoId: uploaded.id }),
      });
    },
    onSuccess: () => {
      toast.success(t("photoUpdated"));
      qc.invalidateQueries({ queryKey: ["auth", "me"] });
    },
    onError: (err) => toast.error((err as Error).message),
  });

  const pwdMutation = useMutation({
    mutationFn: (payload: { oldPassword: string; newPassword: string }) =>
      bffFetch<unknown>("/api/hrm/me/change-password", {
        method: "POST",
        body: JSON.stringify(payload),
      }),
    onSuccess: () => {
      toast.success(t("passwordChanged"));
      setPwdState({ old: "", next: "", confirm: "" });
    },
    onError: (err) => toast.error((err as Error).message),
  });

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (!file.type.startsWith("image/")) {
      toast.error(t("photoMustBeImage"));
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      toast.error(t("photoTooLarge"));
      return;
    }
    photoMutation.mutate(file);
  };

  const submitPwd = (e: React.FormEvent) => {
    e.preventDefault();
    if (pwdState.next.length < 10) {
      toast.error(t("passwordTooShort"));
      return;
    }
    if (pwdState.next !== pwdState.confirm) {
      toast.error(t("passwordMismatch"));
      return;
    }
    pwdMutation.mutate({ oldPassword: pwdState.old, newPassword: pwdState.next });
  };

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
          <div className="mb-4 font-display text-base font-bold text-ink">{t("photoSectionTitle")}</div>
          <div className="flex items-center gap-4">
            <Avatar name={me.data.user.displayName} size="lg" />
            <div className="space-y-1">
              <Button
                type="button"
                variant="secondary"
                disabled={photoMutation.isPending}
                onClick={() => fileInputRef.current?.click()}
              >
                {photoMutation.isPending ? (
                  <Loader2 className="size-4 animate-spin" />
                ) : (
                  <Upload className="size-4" />
                )}
                {t("uploadPhoto")}
              </Button>
              <p className="text-[12px] text-ink-3">{t("photoHint")}</p>
            </div>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              className="hidden"
              onChange={handleFileSelect}
            />
          </div>
        </CardContent>
      </Card>

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
          <div className="mb-4 flex items-center gap-2 font-display text-base font-bold text-ink">
            <KeyRound className="size-4 text-brand-600" />
            {t("changePasswordTitle")}
          </div>
          <p className="mb-4 text-[12.5px] text-ink-3">{t("changePasswordHint")}</p>
          <form onSubmit={submitPwd} className="grid grid-cols-1 gap-3 sm:max-w-md">
            <div className="space-y-1.5">
              <Label htmlFor="oldPwd">{t("oldPassword")}</Label>
              <Input
                id="oldPwd"
                type="password"
                autoComplete="current-password"
                value={pwdState.old}
                onChange={(e) => setPwdState({ ...pwdState, old: e.target.value })}
                required
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="newPwd">{t("newPassword")}</Label>
              <Input
                id="newPwd"
                type="password"
                autoComplete="new-password"
                value={pwdState.next}
                onChange={(e) => setPwdState({ ...pwdState, next: e.target.value })}
                required
                minLength={10}
              />
              <p className="text-[11.5px] text-ink-3">{t("passwordPolicy")}</p>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="confirmPwd">{t("confirmPassword")}</Label>
              <Input
                id="confirmPwd"
                type="password"
                autoComplete="new-password"
                value={pwdState.confirm}
                onChange={(e) => setPwdState({ ...pwdState, confirm: e.target.value })}
                required
              />
              {pwdState.confirm && pwdState.next !== pwdState.confirm && (
                <div className="flex items-center gap-1.5 text-[11.5px] text-status-red-600">
                  <AlertTriangle className="size-3.5" />
                  {t("passwordMismatch")}
                </div>
              )}
              {pwdState.confirm && pwdState.next === pwdState.confirm && pwdState.next.length >= 10 && (
                <div className="flex items-center gap-1.5 text-[11.5px] text-status-green-600">
                  <CheckCircle2 className="size-3.5" />
                  {t("passwordMatch")}
                </div>
              )}
            </div>
            <div className="pt-2">
              <Button type="submit" disabled={pwdMutation.isPending}>
                {pwdMutation.isPending ? (
                  <Loader2 className="size-4 animate-spin" />
                ) : (
                  <Save className="size-4" />
                )}
                {t("savePassword")}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <div className="mb-4 font-display text-base font-bold text-ink">
            {tCommon("permissions") || "Permissions"} ({me.data.permissions.length})
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
