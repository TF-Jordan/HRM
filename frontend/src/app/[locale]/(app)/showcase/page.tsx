import {
  AlertTriangle,
  CalendarRange,
  CheckCircle2,
  Coins,
  FileBadge,
  GraduationCap,
  HeartPulse,
  Plus,
  TrendingUp,
  Users,
  Wallet,
} from "lucide-react";
import { useTranslations } from "next-intl";
import { setRequestLocale } from "next-intl/server";

import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Chip } from "@/components/ui/chip";
import { Column, DataTable } from "@/components/ui/data-table";
import { IconTile } from "@/components/ui/icon-tile";
import { Field, Input } from "@/components/ui/input";
import { KpiCard, KpiMini } from "@/components/ui/kpi-card";
import { ProgressBar } from "@/components/ui/progress-bar";
import { SectionTitle } from "@/components/ui/section-title";
import { StatusPill } from "@/components/ui/status-pill";
import { WorkflowStepper } from "@/components/ui/workflow-stepper";
import { formatMoney } from "@/lib/format";

type DemoRow = {
  id: string;
  name: string;
  role: string;
  department: string;
  status: "active" | "leave" | "suspended";
  salary: number;
};

const DEMO_ROWS: DemoRow[] = [
  {
    id: "1",
    name: "Jean Dupont",
    role: "Développeur Senior",
    department: "Informatique",
    status: "active",
    salary: 850_000,
  },
  {
    id: "2",
    name: "Marie Ngo",
    role: "Comptable principale",
    department: "Finances",
    status: "active",
    salary: 720_000,
  },
  {
    id: "3",
    name: "Paul Mbarga",
    role: "Chef de projet",
    department: "Opérations",
    status: "leave",
    salary: 1_100_000,
  },
  {
    id: "4",
    name: "Aline Toko",
    role: "Designer UX",
    department: "Produit",
    status: "active",
    salary: 680_000,
  },
];

export default async function ShowcasePage({ params }: PageProps<"/[locale]/showcase">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <ShowcaseContent />;
}

function ShowcaseContent() {
  const t = useTranslations("design.showcase");
  const tCommon = useTranslations("common");
  const localeStr = useTranslations()("common.appName") ? "fr" : "fr"; // not used for now

  const columns: Column<DemoRow>[] = [
    {
      key: "employee",
      header: t("table.employee"),
      cell: (row) => (
        <div className="flex items-center gap-3">
          <Avatar
            name={row.name}
            size="md"
            tone={(["orange", "blue", "green", "violet", "amber", "teal"] as const)[
              row.id.charCodeAt(0) % 6
            ]}
          />
          <div>
            <div className="font-semibold text-ink">{row.name}</div>
            <div className="text-[11.5px] text-ink-3">EMP-2024-{row.id.padStart(4, "0")}</div>
          </div>
        </div>
      ),
    },
    {
      key: "role",
      header: t("table.role"),
      cell: (row) => row.role,
    },
    {
      key: "department",
      header: t("table.department"),
      cell: (row) => row.department,
    },
    {
      key: "status",
      header: t("table.status"),
      cell: (row) =>
        row.status === "active" ? (
          <Badge tone="success">{tCommon("status.active")}</Badge>
        ) : row.status === "leave" ? (
          <Badge tone="info">{tCommon("status.pending")}</Badge>
        ) : (
          <Badge tone="warning">{tCommon("status.suspended")}</Badge>
        ),
    },
    {
      key: "salary",
      header: t("table.salary"),
      cell: (row) => (
        <span className="font-mono-tabular font-semibold text-ink">
          {formatMoney(row.salary, { locale: "fr" })}
        </span>
      ),
      className: "text-right",
      headClassName: "text-right",
    },
  ];

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: tCommon("appName") }, { label: t("title") }]}
        title={<>{t("title")}</>}
        subtitle={t("subtitle")}
        actions={
          <>
            <Button variant="secondary">
              <CalendarRange className="h-4 w-4" />
              Janvier 2024
            </Button>
            <Button variant="primary">
              <Plus className="h-4 w-4" />
              {tCommon("actions.create")}
            </Button>
          </>
        }
      />

      {/* KPIs principaux */}
      <SectionTitle>{t("kpiTitle")}</SectionTitle>
      <div className="mb-8 grid grid-cols-4 gap-4">
        <KpiCard
          tone="orange"
          icon={Users}
          label={t("kpi.headcount")}
          value="248"
          foot={
            <>
              <TrendingUp className="h-3.5 w-3.5" /> {t("kpi.headcountFoot")}
            </>
          }
        />
        <KpiCard
          tone="dark"
          icon={CalendarRange}
          label={t("kpi.leaves")}
          value="14"
          foot={t("kpi.leavesFoot")}
        />
        <KpiCard
          tone="amber"
          icon={Wallet}
          label={t("kpi.payroll")}
          value={formatMoney(48_250_000, { withCurrency: false, locale: "fr" })}
          foot={t("kpi.payrollFoot")}
        />
        <KpiCard
          tone="violet"
          icon={AlertTriangle}
          label={t("kpi.alerts")}
          value="6"
          foot={t("kpi.alertsFoot")}
        />
      </div>

      {/* KPI Mini */}
      <div className="mb-10 grid grid-cols-5 gap-3">
        <KpiMini icon={Coins} label="Avances en cours" value="12" delta="3 à approuver" />
        <KpiMini icon={GraduationCap} label="Formations" value="8" delta="2 en cours" />
        <KpiMini icon={HeartPulse} label="Visites médicales" value="124" delta="ce mois" />
        <KpiMini icon={FileBadge} label="CDD expirants" value="4" delta="< 30 jours" />
        <KpiMini icon={CheckCircle2} label="Conformité" value="98%" delta="+2 pts" />
      </div>

      {/* Boutons */}
      <SectionTitle>{t("buttons")}</SectionTitle>
      <Card className="mb-8">
        <CardContent>
          <div className="flex flex-wrap items-center gap-3">
            <Button variant="primary">{t("primary")}</Button>
            <Button variant="secondary">{t("secondary")}</Button>
            <Button variant="ghost">{t("ghost")}</Button>
            <Button variant="dark">{t("dark")}</Button>
            <Button variant="primary" size="sm">
              {t("small")}
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Workflow + Status */}
      <div className="mb-8 grid grid-cols-2 gap-4">
        <Card>
          <CardHeader>
            <CardTitle>{t("stepperTitle")}</CardTitle>
          </CardHeader>
          <CardContent>
            <WorkflowStepper
              steps={[
                { key: "submit", label: t("step.submitted"), state: "done" },
                { key: "manager", label: t("step.manager"), state: "done" },
                { key: "hr", label: t("step.hr"), state: "active" },
                { key: "paid", label: t("step.paid"), state: "pending" },
              ]}
            />
            <div className="mt-6 flex flex-wrap gap-2">
              <Chip>Tous</Chip>
              <Chip active>Actifs</Chip>
              <Chip active tone="orange">
                En attente
              </Chip>
              <Chip>Résiliés</Chip>
            </div>
            <div className="mt-6">
              <ProgressBar value={72} />
              <p className="mt-2 text-[12px] text-ink-3">Budget formation consommé : 72%</p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>{t("statusTitle")}</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap items-center gap-2">
              <Badge tone="success">{tCommon("status.active")}</Badge>
              <Badge tone="warning">{tCommon("status.pending")}</Badge>
              <Badge tone="danger">{tCommon("status.rejected")}</Badge>
              <Badge tone="info">{tCommon("status.submitted")}</Badge>
              <Badge tone="orange">{tCommon("status.draft")}</Badge>
              <Badge tone="violet">Approuvé</Badge>
              <Badge tone="teal">Validé</Badge>
              <Badge tone="gray">{tCommon("status.cancelled")}</Badge>
            </div>
            <div className="mt-5 flex flex-wrap items-center gap-2">
              <StatusPill tone="orange">En attente</StatusPill>
              <StatusPill tone="success">Validé</StatusPill>
              <StatusPill tone="danger">Rejeté</StatusPill>
              <StatusPill tone="dark">Brouillon</StatusPill>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Icon tiles */}
      <SectionTitle>{t("iconTilesTitle")}</SectionTitle>
      <Card className="mb-8">
        <CardContent>
          <div className="flex flex-wrap items-center gap-4">
            <IconTile icon={Users} tone="orange" />
            <IconTile icon={Wallet} tone="success" />
            <IconTile icon={CalendarRange} tone="info" />
            <IconTile icon={GraduationCap} tone="violet" />
            <IconTile icon={AlertTriangle} tone="warning" />
            <IconTile icon={HeartPulse} tone="danger" />
            <IconTile icon={Coins} tone="teal" />
            <IconTile icon={FileBadge} tone="gray" />
          </div>
        </CardContent>
      </Card>

      {/* Form */}
      <SectionTitle>Formulaires</SectionTitle>
      <Card className="mb-8">
        <CardContent>
          <div className="grid grid-cols-2 gap-4">
            <Field label="Email">
              <Input type="email" placeholder="jean.dupont@example.com" />
            </Field>
            <Field label="Téléphone" hint="Format international préféré">
              <Input placeholder="+237 6 99 00 00 00" />
            </Field>
          </div>
        </CardContent>
      </Card>

      {/* Table */}
      <SectionTitle>{t("tableTitle")}</SectionTitle>
      <DataTable columns={columns} data={DEMO_ROWS} rowKey={(r) => r.id} />
    </>
  );
}
