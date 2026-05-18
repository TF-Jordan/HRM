export type Skill = {
  id: string;
  name: string;
  categorie: string | null;
  description: string | null;
};

export type EmployeeSkill = {
  id: string;
  employeeId: string;
  skillId: string;
  niveauActuel: number;
  niveauAttendu: number;
  dateEvaluation: string;
};

export type CreateSkillInput = {
  name: string;
  categorie?: string | null;
  description?: string | null;
};

export type CreateEmployeeSkillInput = {
  employeeId: string;
  skillId: string;
  niveauActuel: number;
  niveauAttendu: number;
  dateEvaluation: string;
};
