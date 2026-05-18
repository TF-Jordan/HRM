export type RhKpiSnapshot = {
  id: string;
  organizationId: string;
  periode: string;
  effectifTotal: number;
  effectifActif: number;
  tauxTurnover: string;
  tauxAbsenteisme: string;
  masseSalariale: string;
  couvertureCompetences: string;
};

export type CreateRhKpiSnapshotInput = {
  periode: string;
  effectifTotal: number;
  effectifActif: number;
  tauxTurnover: string;
  tauxAbsenteisme: string;
  masseSalariale: string;
  couvertureCompetences: string;
};
