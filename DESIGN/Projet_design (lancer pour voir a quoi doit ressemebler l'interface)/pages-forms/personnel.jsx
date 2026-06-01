/* global React */
/* =================================================================
   Forms — PERSONNEL (Employee, Contract, Dependent, Skill, Onboarding)
   ================================================================= */
const { FF, Icons, Avatar } = window;

/* ────────────────────────────────────────────────
   1. CreateEmployee → CreateEmployeeCommand
   actorId, numCnps, categorie, echelon, dateEmbauche, departmentCode,
   modePaiement, compteBancaire, numMobileMoney, operateurMm,
   contractType, contractDateDebut, contractDateFin, salaireBase,
   avantagesNature, periodeEssai
   ──────────────────────────────────────────────── */
function FormCreateEmployee({ onBack }) {
  return (
    <FF.FormShell
      mapping="CreateEmployeeCommand"
      title="Créer un employé"
      subtitle="Onboarding administratif d'un nouveau collaborateur · génère également son premier contrat"
      meta="POST /api/hrm/employees"
      onCancel={onBack}
      submitLabel="Créer l'employé"
      sidebar={
        <>
          <div className="card card-pad" style={{background: 'linear-gradient(135deg, var(--orange-50) 0%, #fff 100%)', border: '1px solid var(--orange-200)'}}>
            <div style={{fontSize: 11, fontWeight: 700, color: 'var(--orange-700)', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 8}}>💡 Bon à savoir</div>
            <div style={{fontSize: 12.5, color: 'var(--ink-2)'}}>
              La création d'employé déclenche en cascade : génération du matricule, contrat initial, premier solde de congés (28 j) et création des comptes Microsoft 365 & VPN.
            </div>
          </div>
          <div className="card card-pad">
            <div style={{fontSize: 13, fontWeight: 700, marginBottom: 10}}>Étapes du processus</div>
            <div className="stepper" style={{flexDirection: 'column', alignItems: 'flex-start', gap: 8}}>
              <div className="step active">1 · Saisie administrative</div>
              <div className="step">2 · Validation DRH</div>
              <div className="step">3 · Onboarding planifié</div>
              <div className="step">4 · Premier jour</div>
            </div>
          </div>
        </>
      }
    >
      <FF.Section title="Identification administrative" sub="champs requis CNPS / DGI">
        <FF.Field label="N° CNPS" mapping="numCnps" required hint="Format : 12 chiffres + lettre · Ex. 110428937H">
          <FF.Input placeholder="110428937H" maxLength={13}/>
        </FF.Field>
        <FF.Field label="Date d'embauche" mapping="dateEmbauche" required>
          <FF.Input type="date" defaultValue="2026-05-22"/>
        </FF.Field>
        <FF.Field label="Catégorie (convention)" mapping="categorie" required hint="1 à 12 selon grille de la convention collective">
          <FF.Select defaultValue="11">
            <option value="1">1 — Manœuvre</option><option value="2">2</option><option value="3">3</option><option value="4">4</option>
            <option value="5">5</option><option value="6">6</option><option value="7">7</option><option value="8">8</option>
            <option value="9">9 — Agent de maîtrise</option><option value="10">10 — Cadre junior</option>
            <option value="11">11 — Cadre confirmé</option><option value="12">12 — Cadre supérieur</option>
          </FF.Select>
        </FF.Field>
        <FF.Field label="Échelon" mapping="echelon" required hint="A à E généralement">
          <FF.Select defaultValue="C">
            <option>A</option><option>B</option><option>C</option><option>D</option><option>E</option>
          </FF.Select>
        </FF.Field>
        <FF.Field label="Code département" mapping="departmentCode" required span={2}>
          <FF.Select>
            <option value="ENG">ENG — Engineering (124 personnes)</option>
            <option value="COM">COM — Commercial (52 personnes)</option>
            <option value="OPS">OPS — Opérations (78 personnes)</option>
            <option value="SUP">SUP — Support (36 personnes)</option>
            <option value="FIN">FIN — Finance & RH (28 personnes)</option>
            <option value="DIR">DIR — Direction (24 personnes)</option>
          </FF.Select>
        </FF.Field>
      </FF.Section>

      <FF.Section title="Premier contrat" sub="généré automatiquement à la création">
        <FF.Field label="Type de contrat" mapping="contractType" required span={2}>
          <FF.Radio
            value="CDI"
            options={[
              { value: 'CDI', label: 'CDI', sub: 'Indéterminé', tag: 'enum: CDI' },
              { value: 'CDD', label: 'CDD', sub: 'Avec terme', tag: 'enum: CDD' },
              { value: 'STAGE', label: 'Stage', sub: 'Convention', tag: 'enum: STAGE' },
              { value: 'INTERIM', label: 'Intérim', sub: 'Via agence', tag: 'enum: INTERIM' },
            ]}
          />
        </FF.Field>
        <FF.Field label="Date de début" mapping="contractDateDebut" required>
          <FF.Input type="date" defaultValue="2026-05-22"/>
        </FF.Field>
        <FF.Field label="Date de fin" mapping="contractDateFin" hint="Laisser vide pour CDI">
          <FF.Input type="date"/>
        </FF.Field>
        <FF.Field label="Salaire de base" mapping="salaireBase" required>
          <FF.Input type="number" suffix="XAF" placeholder="950 000"/>
        </FF.Field>
        <FF.Field label="Avantages en nature" mapping="avantagesNature" hint="Logement, véhicule, etc. (mensuel)">
          <FF.Input type="number" suffix="XAF" placeholder="0"/>
        </FF.Field>
        <FF.Field label="Période d'essai" mapping="periodeEssai" hint="Durée en jours (max 90j pour CDI cadre)" span={2}>
          <FF.Input type="number" suffix="jours" placeholder="90" defaultValue="90"/>
        </FF.Field>
      </FF.Section>

      <FF.Section title="Mode de paiement du salaire" sub="enum PaymentChannel">
        <FF.Field label="Mode de paiement" mapping="modePaiement" required span={2}>
          <FF.Radio
            value="MOBILE_MONEY"
            options={[
              { value: 'BANK_TRANSFER', label: 'Virement bancaire', tag: 'enum' },
              { value: 'MOBILE_MONEY', label: 'Mobile Money', tag: 'enum' },
              { value: 'CASH', label: 'Espèces', tag: 'enum' },
            ]}
          />
        </FF.Field>
        <FF.Field label="Compte bancaire (RIB)" mapping="compteBancaire" hint="24 chiffres · si virement">
          <FF.Input placeholder="10006 00012 00000123456 78" maxLength={29}/>
        </FF.Field>
        <FF.Field label="Numéro Mobile Money" mapping="numMobileMoney" hint="9 chiffres · format Cameroun">
          <FF.Input prefix="+237" placeholder="678 12 34 56" maxLength={11}/>
        </FF.Field>
        <FF.Field label="Opérateur Mobile Money" mapping="operateurMm" hint="enum MobileOperator" span={2}>
          <FF.Radio
            value="MTN"
            options={[
              { value: 'MTN', label: 'MTN MoMo', tag: 'enum: MTN' },
              { value: 'ORANGE', label: 'Orange Money', tag: 'enum: ORANGE' },
            ]}
          />
        </FF.Field>
      </FF.Section>
    </FF.FormShell>
  );
}

/* ────────────────────────────────────────────────
   2. UpdateEmployee → UpdateEmployeeCommand
   numCnps, categorie, echelon, departmentCode, modePaiement,
   compteBancaire, numMobileMoney, operateurMm
   ──────────────────────────────────────────────── */
function FormUpdateEmployee({ onBack }) {
  return (
    <FF.FormShell
      mapping="UpdateEmployeeCommand"
      title="Modifier un employé"
      subtitle="Mise à jour des données administratives · seuls les champs modifiables sont affichés"
      meta="PATCH /api/hrm/employees/{id}"
      onCancel={onBack}
      submitLabel="Mettre à jour"
      sidebar={
        <div className="card card-pad" style={{background: 'var(--bg-dim)'}}>
          <div style={{fontSize: 13, fontWeight: 700, marginBottom: 10}}>Employé en cours de modification</div>
          <div style={{display: 'flex', gap: 10, alignItems: 'center', padding: '10px', background: '#fff', borderRadius: 10, border: '1px solid var(--line)'}}>
            <Avatar name="Aminata Diallo" color="orange"/>
            <div>
              <div style={{fontSize: 13, fontWeight: 600}}>Aminata Diallo</div>
              <div className="mono" style={{fontSize: 11, color: 'var(--ink-3)'}}>EMP-0142</div>
            </div>
          </div>
          <div style={{fontSize: 11.5, color: 'var(--ink-3)', marginTop: 12}}>
            ⚠ Une modification du salaire requiert plutôt la création d'un avenant via le formulaire "Nouveau contrat".
          </div>
        </div>
      }
    >
      <FF.Section title="Identification administrative">
        <FF.Field label="N° CNPS" mapping="numCnps" required>
          <FF.Input defaultValue="110428937H"/>
        </FF.Field>
        <FF.Field label="Catégorie" mapping="categorie" required>
          <FF.Select><option>11 — Cadre confirmé</option></FF.Select>
        </FF.Field>
        <FF.Field label="Échelon" mapping="echelon" required>
          <FF.Select><option>C</option></FF.Select>
        </FF.Field>
        <FF.Field label="Code département" mapping="departmentCode" required>
          <FF.Select><option>ENG — Engineering</option></FF.Select>
        </FF.Field>
      </FF.Section>

      <FF.Section title="Mode de paiement">
        <FF.Field label="Mode de paiement" mapping="modePaiement" required span={2}>
          <FF.Radio value="MOBILE_MONEY" options={[
            { value: 'BANK_TRANSFER', label: 'Virement bancaire', tag: 'enum' },
            { value: 'MOBILE_MONEY', label: 'Mobile Money', tag: 'enum' },
            { value: 'CASH', label: 'Espèces', tag: 'enum' },
          ]}/>
        </FF.Field>
        <FF.Field label="Compte bancaire" mapping="compteBancaire">
          <FF.Input defaultValue="10006 00012 00000456245 62"/>
        </FF.Field>
        <FF.Field label="Numéro Mobile Money" mapping="numMobileMoney">
          <FF.Input prefix="+237" defaultValue="678 12 34 56"/>
        </FF.Field>
        <FF.Field label="Opérateur" mapping="operateurMm" required span={2}>
          <FF.Radio value="MTN" options={[
            { value: 'MTN', label: 'MTN MoMo', tag: 'enum: MTN' },
            { value: 'ORANGE', label: 'Orange Money', tag: 'enum: ORANGE' },
          ]}/>
        </FF.Field>
      </FF.Section>
    </FF.FormShell>
  );
}

/* ────────────────────────────────────────────────
   3. AddContract → AddContractCommand
   type, dateDebut, dateFin, salaireBase, avantagesNature, periodeEssai
   ──────────────────────────────────────────────── */
function FormAddContract({ onBack }) {
  return (
    <FF.FormShell
      mapping="AddContractCommand"
      title="Nouveau contrat / avenant"
      subtitle="Ajouter un contrat ou un avenant à un employé existant"
      meta="POST /api/hrm/employees/{id}/contracts"
      onCancel={onBack}
      submitLabel="Créer le contrat"
      sidebar={
        <div className="card card-pad">
          <div style={{fontSize: 13, fontWeight: 700, marginBottom: 10}}>Pour quel employé ?</div>
          <FF.EmployeePicker employee={{name: 'Aminata Diallo', id: 'EMP-0142', role: 'Lead Mobile Engineer', color: 'orange'}}/>
          <div style={{fontSize: 11.5, color: 'var(--ink-3)', marginTop: 12}}>
            Contrat actuel : <b style={{color: 'var(--ink-2)'}}>CDI · depuis 14 fév 2022</b><br/>
            Salaire actuel : <b className="tabular" style={{color: 'var(--ink-2)'}}>950 000 XAF</b>
          </div>
        </div>
      }
    >
      <FF.Section title="Caractéristiques du contrat">
        <FF.Field label="Type de contrat" mapping="type" required span={2}>
          <FF.Radio value="CDI" options={[
            { value: 'CDI', label: 'CDI', tag: 'enum' },
            { value: 'CDD', label: 'CDD', tag: 'enum' },
            { value: 'STAGE', label: 'Stage', tag: 'enum' },
            { value: 'INTERIM', label: 'Intérim', tag: 'enum' },
          ]}/>
        </FF.Field>
        <FF.Field label="Date de début" mapping="dateDebut" required>
          <FF.Input type="date" defaultValue="2026-06-01"/>
        </FF.Field>
        <FF.Field label="Date de fin" mapping="dateFin" hint="Vide = CDI">
          <FF.Input type="date"/>
        </FF.Field>
      </FF.Section>

      <FF.Section title="Rémunération">
        <FF.Field label="Salaire de base mensuel" mapping="salaireBase" required>
          <FF.Input type="number" suffix="XAF" defaultValue="1245000"/>
        </FF.Field>
        <FF.Field label="Avantages en nature mensuels" mapping="avantagesNature" hint="Logement, véhicule, etc.">
          <FF.Input type="number" suffix="XAF" defaultValue="120000"/>
        </FF.Field>
        <FF.Field label="Période d'essai" mapping="periodeEssai" hint="Durée en jours · 0 si avenant" span={2}>
          <FF.Input type="number" suffix="jours" defaultValue="0"/>
        </FF.Field>
      </FF.Section>
    </FF.FormShell>
  );
}

/* ────────────────────────────────────────────────
   4. AddDependent → AddDependentCommand
   nom, prenom, dateNaissance, lienParente
   ──────────────────────────────────────────────── */
function FormAddDependent({ onBack }) {
  return (
    <FF.FormShell
      mapping="AddDependentCommand"
      title="Ajouter une personne à charge"
      subtitle="Conjoint(e) ou enfant déclaré(e) pour calcul IRPP et droits CNPS"
      meta="POST /api/hrm/employees/{id}/dependents"
      onCancel={onBack}
      submitLabel="Ajouter"
      sidebar={
        <div className="card card-pad" style={{background: 'var(--orange-50)', border: '1px solid var(--orange-200)'}}>
          <div style={{fontSize: 13, fontWeight: 700, marginBottom: 6, color: 'var(--orange-700)'}}>📊 Impact fiscal</div>
          <div style={{fontSize: 12, color: 'var(--ink-2)'}}>
            Au Cameroun, chaque enfant à charge donne une déduction IRPP supplémentaire. Le conjoint sans revenu compte également comme part fiscale.
          </div>
        </div>
      }
    >
      <FF.Section cols={2}>
        <FF.Field label="Nom" mapping="nom" required>
          <FF.Input placeholder="Diallo"/>
        </FF.Field>
        <FF.Field label="Prénom" mapping="prenom" required>
          <FF.Input placeholder="Léa"/>
        </FF.Field>
        <FF.Field label="Date de naissance" mapping="dateNaissance" required>
          <FF.Input type="date"/>
        </FF.Field>
        <FF.Field label="Lien de parenté" mapping="lienParente" required>
          <FF.Select>
            <option>Conjoint(e)</option>
            <option>Enfant</option>
            <option>Parent à charge</option>
            <option>Autre</option>
          </FF.Select>
        </FF.Field>
      </FF.Section>
    </FF.FormShell>
  );
}

/* ────────────────────────────────────────────────
   5. CreateSkill → CreateSkillCommand
   name, categorie, description
   ──────────────────────────────────────────────── */
function FormCreateSkill({ onBack }) {
  return (
    <FF.FormShell
      mapping="CreateSkillCommand"
      title="Ajouter une compétence au référentiel"
      subtitle="Compétence du référentiel entreprise — assignable ensuite aux employés"
      meta="POST /api/hrm/skills"
      onCancel={onBack}
      submitLabel="Créer la compétence"
    >
      <FF.Section cols={2}>
        <FF.Field label="Nom de la compétence" mapping="name" required span={2}>
          <FF.Input placeholder="Ex. React Native"/>
        </FF.Field>
        <FF.Field label="Catégorie" mapping="categorie" required>
          <FF.Select>
            <option>Technique</option>
            <option>Management</option>
            <option>Métier</option>
            <option>Soft skill</option>
            <option>Langue</option>
          </FF.Select>
        </FF.Field>
        <FF.Field label="" mapping="" required span={1}>
          <div style={{visibility: 'hidden'}}></div>
        </FF.Field>
        <FF.Field label="Description" mapping="description" span={2} hint="Définition de la compétence et critères d'évaluation par niveau (1 à 5)">
          <FF.Textarea rows={5} placeholder="Capacité à concevoir et développer des applications mobiles cross-platform avec React Native. Niveau 5 = expert : peut former d'autres, contribuer à des libs open source…"/>
        </FF.Field>
      </FF.Section>
    </FF.FormShell>
  );
}

/* ────────────────────────────────────────────────
   6. CreateEmployeeSkill → CreateEmployeeSkillCommand
   employeeId, skillId, niveauActuel, niveauAttendu, dateEvaluation
   ──────────────────────────────────────────────── */
function FormAssignSkill({ onBack }) {
  const [actuel, setActuel] = React.useState(4);
  const [attendu, setAttendu] = React.useState(5);

  const LevelBar = ({ value, onChange }) => (
    <div style={{display: 'flex', gap: 6}}>
      {[1,2,3,4,5].map(lvl => (
        <button key={lvl} onClick={() => onChange && onChange(lvl)} style={{
          flex: 1, padding: '8px 0', borderRadius: 8, cursor: 'pointer',
          background: lvl <= value ? 'var(--orange-500)' : 'var(--bg-soft)',
          color: lvl <= value ? '#fff' : 'var(--ink-3)',
          border: 'none', fontSize: 12, fontWeight: 700, fontFamily: 'Inter Tight'
        }}>{lvl}</button>
      ))}
    </div>
  );

  return (
    <FF.FormShell
      mapping="CreateEmployeeSkillCommand"
      title="Évaluer une compétence d'employé"
      subtitle="Attribution / mise à jour du niveau d'une compétence pour un employé"
      meta="POST /api/hrm/employees/{id}/skills"
      onCancel={onBack}
      submitLabel="Enregistrer l'évaluation"
    >
      <FF.Section title="Sélection">
        <FF.Field label="Employé" mapping="employeeId" required>
          <FF.EmployeePicker employee={{name: 'Aminata Diallo', id: 'EMP-0142', role: 'Lead Mobile Eng.', color: 'orange'}}/>
        </FF.Field>
        <FF.Field label="Compétence" mapping="skillId" required>
          <FF.Picker icon={<Icons.skill size={14}/>} value="React Native" sub="Technique · 18 employés évalués"/>
        </FF.Field>
        <FF.Field label="Date d'évaluation" mapping="dateEvaluation" required span={2}>
          <FF.Input type="date" defaultValue="2026-05-14"/>
        </FF.Field>
      </FF.Section>

      <FF.Section title="Niveaux">
        <FF.Field label={`Niveau actuel : ${actuel} / 5`} mapping="niveauActuel" required>
          <LevelBar value={actuel} onChange={setActuel}/>
        </FF.Field>
        <FF.Field label={`Niveau attendu (cible) : ${attendu} / 5`} mapping="niveauAttendu" required>
          <LevelBar value={attendu} onChange={setAttendu}/>
        </FF.Field>
      </FF.Section>

      <div style={{padding: 16, background: 'var(--bg-dim)', borderRadius: 12, fontSize: 12.5}}>
        <div style={{fontWeight: 600, marginBottom: 6}}>Échelle de référence :</div>
        <div style={{color: 'var(--ink-3)'}}>
          <b>1</b> Notions · <b>2</b> Pratique encadrée · <b>3</b> Autonomie · <b>4</b> Maîtrise · <b>5</b> Expert (peut former)
        </div>
      </div>
    </FF.FormShell>
  );
}

/* ────────────────────────────────────────────────
   7. CreateOnboardingTask → CreateOnboardingTaskCommand
   employeeId, titre, description, assignedToPartyId, echeance
   ──────────────────────────────────────────────── */
function FormCreateOnboardingTask({ onBack }) {
  return (
    <FF.FormShell
      mapping="CreateOnboardingTaskCommand"
      title="Ajouter une tâche d'onboarding"
      subtitle="Tâche assignée à un membre de l'équipe pour préparer l'arrivée d'un nouvel employé"
      meta="POST /api/hrm/onboarding-tasks"
      onCancel={onBack}
      submitLabel="Créer la tâche"
    >
      <FF.Section title="Nouvel employé">
        <FF.Field label="Pour qui ?" mapping="employeeId" required span={2}>
          <FF.EmployeePicker employee={{name: 'Léa Ondoa', id: 'EMP-0234', role: 'Designer UI · Stage', color: 'teal'}} label="Nouvel arrivant"/>
        </FF.Field>
      </FF.Section>

      <FF.Section title="Détails de la tâche">
        <FF.Field label="Titre" mapping="titre" required span={2}>
          <FF.Input placeholder="Ex. Préparer le poste de travail (PC, badge, accès Slack)"/>
        </FF.Field>
        <FF.Field label="Description" mapping="description" span={2}>
          <FF.Textarea rows={4} placeholder="Détailler ce qu'il faut faire, où récupérer le matériel, qui contacter…"/>
        </FF.Field>
        <FF.Field label="Assignée à" mapping="assignedToPartyId" required>
          <FF.EmployeePicker employee={{name: 'Olivier Manga', id: 'EMP-0067', role: 'DevOps · IT support', color: 'green'}}/>
        </FF.Field>
        <FF.Field label="Échéance" mapping="echeance" required>
          <FF.Input type="date" defaultValue="2026-05-21" hint="Idéalement la veille de l'arrivée"/>
        </FF.Field>
      </FF.Section>
    </FF.FormShell>
  );
}

window.FormCreateEmployee = FormCreateEmployee;
window.FormUpdateEmployee = FormUpdateEmployee;
window.FormAddContract = FormAddContract;
window.FormAddDependent = FormAddDependent;
window.FormCreateSkill = FormCreateSkill;
window.FormAssignSkill = FormAssignSkill;
window.FormCreateOnboardingTask = FormCreateOnboardingTask;
