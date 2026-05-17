export type ReviewStatus = "DRAFT" | "SUBMITTED" | "ACKNOWLEDGED" | "FINALIZED";

export type Review = {
  id: string;
  organizationId: string;
  employeeId: string;
  evaluateurPartyId: string;
  evaluateurDisplayName: string;
  periode: string;
  noteGlobale: number | null;
  commentaires: string | null;
  planAction: string | null;
  status: ReviewStatus;
};

export type ReviewObjective = {
  id: string;
  reviewId: string;
  description: string;
  poids: number;
  noteAtteinte: number | null;
  commentaire: string | null;
};

export type CreateReviewInput = {
  employeeId: string;
  evaluateurPartyId: string;
  evaluateurDisplayName: string;
  periode: string;
};

export type SubmitReviewInput = {
  noteGlobale: number;
  commentaires: string;
  planAction?: string | null;
};

export type AddObjectiveInput = {
  description: string;
  poids: number;
};

export type EvaluateObjectiveInput = {
  noteAtteinte: number;
  commentaire?: string | null;
};
