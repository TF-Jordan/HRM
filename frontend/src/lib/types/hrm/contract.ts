export type ContractType = "CDD" | "CDI" | "STAGE" | "INTERIM";
export type ContractStatus = "ACTIVE" | "EXPIRED" | "TERMINATED" | "RENEWED";

export type Contract = {
  id: string;
  employeeId: string;
  type: ContractType;
  dateDebut: string;
  dateFin: string | null;
  salaireBase: number;
  avantagesNature: number | null;
  periodeEssai: number | null;
  status: ContractStatus;
  motifFin: string | null;
  documentFileId: string | null;
};

export type AddContractInput = {
  type: ContractType;
  dateDebut: string;
  dateFin?: string | null;
  salaireBase: number;
  avantagesNature?: number | null;
  periodeEssai?: number | null;
};
