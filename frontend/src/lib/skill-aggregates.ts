import type {
  EmployeeSkillResponse,
  SkillResponse,
} from "@/server/ksm/modules/skills";

export type SkillAggregate = {
  skill: SkillResponse;
  evaluatedCount: number;
  avgActual: number;
  avgTarget: number;
  gap: number; // avgActual - avgTarget
  expertCount: number; // niveauActuel >= 5
};

/**
 * Build per-skill aggregates (average current/target, count of evaluations,
 * expert count) from a flat list of employee↔skill rows. Skills without any
 * mapping are still returned with zeros so the catalog stays exhaustive.
 */
export function buildSkillAggregates(
  skills: SkillResponse[],
  mappings: EmployeeSkillResponse[],
): SkillAggregate[] {
  const bySkill = new Map<string, EmployeeSkillResponse[]>();
  for (const m of mappings) {
    const list = bySkill.get(m.skillId) ?? [];
    list.push(m);
    bySkill.set(m.skillId, list);
  }
  return skills.map((s) => {
    const rows = bySkill.get(s.id) ?? [];
    const evaluatedCount = rows.length;
    const avgActual =
      evaluatedCount === 0
        ? 0
        : rows.reduce((acc, r) => acc + r.niveauActuel, 0) / evaluatedCount;
    const avgTarget =
      evaluatedCount === 0
        ? 0
        : rows.reduce((acc, r) => acc + r.niveauAttendu, 0) / evaluatedCount;
    const expertCount = rows.filter((r) => r.niveauActuel >= 5).length;
    return {
      skill: s,
      evaluatedCount,
      avgActual: round1(avgActual),
      avgTarget: round1(avgTarget),
      gap: round1(avgActual - avgTarget),
      expertCount,
    };
  });
}

function round1(n: number): number {
  return Math.round(n * 10) / 10;
}

/**
 * Pick a deterministic colour for a category name so the same category gets
 * the same chip tone everywhere.
 */
export function categoryTone(
  categorie: string | null | undefined,
):
  | "success"
  | "warning"
  | "danger"
  | "info"
  | "orange"
  | "violet"
  | "teal"
  | "gray" {
  if (!categorie) return "gray";
  const upper = categorie.toUpperCase();
  if (/TECH|IT|DEV|CLOUD/.test(upper)) return "info";
  if (/MGMT|MANAG|LEAD/.test(upper)) return "orange";
  if (/SOFT|COMM/.test(upper)) return "success";
  if (/LANG/.test(upper)) return "violet";
  if (/MET|BIZ|VENT/.test(upper)) return "warning";
  return "teal";
}
