import { addDays, differenceInYears, isAfter, isBefore } from "date-fns";
import { z } from "zod";

type RefineCtx = z.RefinementCtx;

/**
 * Validate chronology between two date fields in the same form.
 * To be used inside .superRefine() on a Zod object schema.
 */
export function dateRangeRefinement(
  startField: string,
  endField: string,
  options: {
    allowEqual?: boolean;
    maxDurationDays?: number;
    errorMessageKey?: string;
  } = {},
) {
  const { allowEqual = true, maxDurationDays, errorMessageKey } = options;
  return (data: Record<string, unknown>, ctx: RefineCtx) => {
    const start = data[startField] as Date | undefined;
    const end = data[endField] as Date | undefined;
    if (!start || !end) return;

    const isInvalid = allowEqual ? isBefore(end, start) : !isAfter(end, start);
    if (isInvalid) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: errorMessageKey ?? "validation.dateRange.endBeforeStart",
        path: [endField],
      });
    }

    if (maxDurationDays) {
      const maxEnd = addDays(start, maxDurationDays);
      if (isAfter(end, maxEnd)) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: "validation.dateRange.durationExceeded",
          path: [endField],
        });
      }
    }
  };
}

export function minAgeRefinement(birthField: string, referenceField: string, minYears: number) {
  return (data: Record<string, unknown>, ctx: RefineCtx) => {
    const birth = data[birthField] as Date | undefined;
    const ref = data[referenceField] as Date | undefined;
    if (!birth || !ref) return;

    if (differenceInYears(ref, birth) < minYears) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "validation.minAge",
        path: [referenceField],
      });
    }
  };
}

export function notInPastRefinement(field: string, errorKey = "validation.date.notInPast") {
  return (data: Record<string, unknown>, ctx: RefineCtx) => {
    const value = data[field] as Date | undefined;
    if (!value) return;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    if (isBefore(value, today)) {
      ctx.addIssue({ code: z.ZodIssueCode.custom, message: errorKey, path: [field] });
    }
  };
}

export function notInFutureRefinement(field: string, errorKey = "validation.date.notInFuture") {
  return (data: Record<string, unknown>, ctx: RefineCtx) => {
    const value = data[field] as Date | undefined;
    if (!value) return;
    if (isAfter(value, new Date())) {
      ctx.addIssue({ code: z.ZodIssueCode.custom, message: errorKey, path: [field] });
    }
  };
}
