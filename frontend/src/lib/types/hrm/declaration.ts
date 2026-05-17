export type SocialDeclarationStatus = "DRAFT" | "GENERATED" | "SUBMITTED" | "ACKNOWLEDGED";

export type SocialDeclarationType = "CNPS" | "DIPE" | "IRPP_CAC" | "FNE" | "CFC";

export type SocialDeclarationFormat = "CSV" | "XML" | "EDI" | "PDF";

export type SocialDeclaration = {
  id: string;
  organizationId: string;
  type: SocialDeclarationType;
  periode: string;
  format: SocialDeclarationFormat;
  statut: SocialDeclarationStatus;
  fichierId: string | null;
  generatedAt: string | null;
  submittedAt: string | null;
};

export type CreateDeclarationInput = {
  type: SocialDeclarationType;
  periode: string;
  format: SocialDeclarationFormat;
};
