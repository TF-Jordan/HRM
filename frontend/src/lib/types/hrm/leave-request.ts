export type LeaveRequestStatus = "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED";

export type LeaveRequestType =
  | "ANNUAL"
  | "SICK"
  | "MATERNITY"
  | "PATERNITY"
  | "UNPAID"
  | "SPECIAL";

export type LeaveRequest = {
  id: string;
  employeeId: string;
  type: LeaveRequestType;
  dateDebut: string;
  dateFin: string;
  nbJours: number;
  status: LeaveRequestStatus;
  motif: string | null;
  valideurPartyId: string | null;
  valideurDisplayName: string | null;
  dateValidation: string | null;
  commentaireValideur: string | null;
  justificatifFileId: string | null;
};

export type SubmitLeaveInput = {
  employeeId: string;
  type: LeaveRequestType;
  dateDebut: string;
  dateFin: string;
  motif?: string | null;
  justificatifFileId?: string | null;
};
