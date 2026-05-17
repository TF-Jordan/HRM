export type DependentRelationship =
  | "SPOUSE"
  | "CHILD"
  | "PARENT"
  | "SIBLING"
  | "OTHER";

export type Dependent = {
  id: string;
  employeeId: string;
  nom: string;
  prenom: string;
  dateNaissance: string;
  lienParente: DependentRelationship;
  certificatFileId: string | null;
};

export type AddDependentInput = {
  nom: string;
  prenom: string;
  dateNaissance: string;
  lienParente: DependentRelationship;
};
