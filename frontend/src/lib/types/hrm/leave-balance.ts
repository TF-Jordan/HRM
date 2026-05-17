export type LeaveType =
  | "ANNUAL"
  | "SICK"
  | "MATERNITY"
  | "PATERNITY"
  | "UNPAID"
  | "SPECIAL";

export type LeaveBalance = {
  id: string;
  employeeId: string;
  type: LeaveType;
  acquis: number;
  pris: number;
  soldeRestant: number;
  annee: number;
};
