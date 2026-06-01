import type {
  DeclarationStatus,
  DeclarationType,
} from "@/server/ksm/modules/declarations";

export type BadgeTone =
  | "success"
  | "warning"
  | "danger"
  | "info"
  | "orange"
  | "violet"
  | "teal"
  | "gray";

export function declarationStatusTone(status: DeclarationStatus): BadgeTone {
  switch (status) {
    case "DRAFT":
      return "gray";
    case "GENERATED":
      return "info";
    case "SUBMITTED":
      return "orange";
    case "ACKNOWLEDGED":
      return "success";
  }
}

export function declarationTypeTone(type: DeclarationType): BadgeTone {
  switch (type) {
    case "CNPS":
      return "orange";
    case "DIPE":
      return "info";
    case "IRPP_CAC":
      return "violet";
  }
}

export const DECLARATION_TYPES: DeclarationType[] = ["CNPS", "DIPE", "IRPP_CAC"];
export const DECLARATION_FORMATS = ["PDF", "CSV", "XML"] as const;
