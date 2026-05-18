export type AptitudeResult = "APTE" | "APTE_AVEC_RESTRICTIONS" | "INAPTE_TEMPORAIRE";

export type MedicalVisit = {
  id: string;
  employeeId: string;
  dateVisite: string;
  medecin: string;
  resultatAptitude: AptitudeResult;
  restrictions: string | null;
  prochaineEcheance: string | null;
  certificatFileId: string | null;
};

export type CreateMedicalVisitInput = {
  employeeId: string;
  dateVisite: string;
  medecin: string;
  resultatAptitude: AptitudeResult;
  restrictions?: string | null;
  prochaineEcheance?: string | null;
  certificatFileId?: string | null;
};

export type MedicalCertificate = {
  id: string;
  employeeId: string;
  typeCertificat: string;
  dateEmission: string;
  dateExpiration: string | null;
  statut: string;
  fichierId: string | null;
};

export type CreateMedicalCertificateInput = {
  employeeId: string;
  typeCertificat: string;
  dateEmission: string;
  dateExpiration?: string | null;
  statut: string;
  fichierId?: string | null;
};
