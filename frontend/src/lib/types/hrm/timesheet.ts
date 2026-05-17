export type TimesheetStatus = "DRAFT" | "SUBMITTED" | "VALIDATED";

export type TimesheetEntry = {
  date: string;
  projet: string | null;
  heuresNormales: number;
  heuresSupplementaires: number;
  description?: string | null;
};

export type Timesheet = {
  id: string;
  employeeId: string;
  periode: string;
  status: TimesheetStatus;
  totalHeures: number;
  totalHeuresSup: number;
  entries: TimesheetEntry[];
  validateurId: string | null;
};

export type CreateTimesheetInput = {
  employeeId: string;
  periode: string;
  entries: TimesheetEntry[];
};
