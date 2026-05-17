export type MissionOrderStatus =
  | "DRAFT"
  | "APPROVED"
  | "IN_PROGRESS"
  | "COMPLETED"
  | "CANCELLED";

export type MissionOrder = {
  id: string;
  employeeId: string;
  destination: string;
  objet: string;
  dateDebut: string;
  dateFin: string;
  montantAvance: number;
  centreCout: string | null;
  status: MissionOrderStatus;
};

export type CreateMissionOrderInput = {
  employeeId: string;
  destination: string;
  objet: string;
  dateDebut: string;
  dateFin: string;
  montantAvance: number;
  centreCout?: string | null;
};
