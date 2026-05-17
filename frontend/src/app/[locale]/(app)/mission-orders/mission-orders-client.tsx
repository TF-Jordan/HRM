"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { useEmployees } from "@/hooks/modules/useEmployees";
import { useEmployeeMissions, useCreateMission } from "@/hooks/modules/useMissions";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { Link } from "@/i18n/navigation";
import {
  createMissionSchema,
  type CreateMissionFormValues,
} from "@/lib/validation/hrm/mission.schema";

export function MissionOrdersClient() {
  const t = useTranslations("manager.missions");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const employees = useEmployees();
  const [selected, setSelected] = React.useState<string>("");
  const missions = useEmployeeMissions(selected || undefined);
  const create = useCreateMission();
  const [open, setOpen] = React.useState(false);

  const form = useForm<CreateMissionFormValues>({
    resolver: zodResolver(createMissionSchema),
    defaultValues: {
      employeeId: "",
      destination: "",
      objet: "",
      dateDebut: new Date().toISOString().slice(0, 10),
      dateFin: new Date().toISOString().slice(0, 10),
      montantAvance: 0,
      centreCout: "",
    },
  });

  React.useEffect(() => {
    if (employees.data && !selected && employees.data.length > 0) {
      setSelected(employees.data[0]!.id);
    }
  }, [employees.data, selected]);

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-21"
        crumbs={[{ label: tNav("items.missionOrders") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <Button onClick={() => setOpen(true)} disabled={!employees.data?.length}>
            <Plus className="size-4" />
            {t("newButton")}
          </Button>
        }
      />

      <Card>
        <CardContent className="flex items-center gap-3">
          <Label className="shrink-0">Employé</Label>
          <Select value={selected} onValueChange={setSelected}>
            <SelectTrigger className="max-w-md">
              <SelectValue placeholder="Sélectionner" />
            </SelectTrigger>
            <SelectContent>
              {employees.data?.map((e) => (
                <SelectItem key={e.id} value={e.id}>
                  {e.matricule} — {e.actorDisplayName}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </CardContent>
      </Card>

      {missions.isLoading && <Skeleton className="h-32 w-full" />}

      {!missions.isLoading && missions.data && missions.data.length === 0 && (
        <Card>
          <CardContent className="py-8 text-center text-sm text-ink-3">{t("empty")}</CardContent>
        </Card>
      )}

      {!missions.isLoading && missions.data && missions.data.length > 0 && (
        <Card>
          <CardContent className="p-0">
            <table className="w-full border-separate border-spacing-0">
              <thead>
                <tr>
                  {[
                    t("table.destination"),
                    t("table.objet"),
                    t("table.dateDebut"),
                    t("table.dateFin"),
                    t("table.montantAvance"),
                    t("table.status"),
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
                {missions.data.map((m) => (
                  <tr key={m.id} className="cursor-pointer hover:bg-brand-50/40">
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] font-medium text-ink">
                      <Link href={`/mission-orders/${m.id}` as never} className="hover:text-brand-700">
                        {m.destination}
                      </Link>
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2">{m.objet}</td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">{fmt.date(m.dateDebut)}</td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">{fmt.date(m.dateFin)}</td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular text-right">{fmt.money(m.montantAvance)}</td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px]"><StatusBadge kind="mission" status={m.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardContent>
        </Card>
      )}

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("newButton")}</DialogTitle>
          </DialogHeader>
          <form
            onSubmit={form.handleSubmit((values) =>
              create.mutate(
                {
                  ...values,
                  montantAvance: Number(values.montantAvance),
                  centreCout: values.centreCout ?? null,
                } as never,
                {
                  onSuccess: () => {
                    toast.success(t("form.submit"));
                    setOpen(false);
                    form.reset();
                  },
                  onError: (err) => toast.error((err as Error).message),
                },
              ),
            )}
            className="space-y-4"
          >
            <div className="space-y-1.5">
              <Label htmlFor="missionEmployee">Employé</Label>
              <Select
                value={form.watch("employeeId")}
                onValueChange={(v) => form.setValue("employeeId", v)}
              >
                <SelectTrigger id="missionEmployee">
                  <SelectValue placeholder="—" />
                </SelectTrigger>
                <SelectContent>
                  {employees.data?.map((e) => (
                    <SelectItem key={e.id} value={e.id}>
                      {e.matricule} — {e.actorDisplayName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label htmlFor="destination">{t("form.destination")}</Label>
                <Input id="destination" {...form.register("destination")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="centreCout">{t("form.centreCout")}</Label>
                <Input id="centreCout" {...form.register("centreCout")} />
              </div>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="objet">{t("form.objet")}</Label>
              <Input id="objet" {...form.register("objet")} />
            </div>
            <div className="grid grid-cols-3 gap-3">
              <div className="space-y-1.5">
                <Label htmlFor="missionDebut">{t("form.dateDebut")}</Label>
                <Input id="missionDebut" type="date" {...form.register("dateDebut")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="missionFin">{t("form.dateFin")}</Label>
                <Input id="missionFin" type="date" {...form.register("dateFin")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="montantAvance">{t("form.montantAvance")}</Label>
                <Input id="montantAvance" type="number" min={0} {...form.register("montantAvance")} />
              </div>
            </div>
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => setOpen(false)}>
                Annuler
              </Button>
              <Button type="submit" disabled={create.isPending}>
                {create.isPending && <Loader2 className="size-4 animate-spin" />}
                {t("form.submit")}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
