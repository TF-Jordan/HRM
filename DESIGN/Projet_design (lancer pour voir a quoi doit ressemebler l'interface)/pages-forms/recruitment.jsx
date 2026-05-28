/* global React */
/* =================================================================
   Forms — RECRUTEMENT & DÉVELOPPEMENT
   ================================================================= */
const { FF: FF_r, Icons: Ic_r } = window;

/* ────────────────────────────────────────────────
   14. CreateJobOffer → CreateJobOfferCommand
   agencyId, poste, departement, localisation, competencesRequises, dateLimite, packageSalarial
   ──────────────────────────────────────────────── */
function FormCreateJobOffer({ onBack }) {
  return (
    <FF_r.FormShell
      mapping="CreateJobOfferCommand"
      title="Publier une offre d'emploi"
      subtitle="Création d'une offre qui sera publiée sur les canaux choisis"
      meta="POST /api/hrm/job-offers"
      onCancel={onBack}
      submitLabel="Publier l'offre"
      draftLabel="Enregistrer brouillon"
      sidebar={
        <div className="card card-pad" style={{background: 'var(--orange-50)', border: '1px solid var(--orange-200)'}}>
          <div style={{fontSize: 11, fontWeight: 700, color: 'var(--orange-700)', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 8}}>📡 Diffusion</div>
          <div style={{fontSize: 12.5, color: 'var(--ink-2)'}}>
            À la publication, l'offre est poussée vers :<br/>
            ✓ LinkedIn Talent<br/>
            ✓ Site carrière interne<br/>
            ✓ Yowyob Talents Network<br/>
            ✓ Programme cooptation
          </div>
        </div>
      }
    >
      <FF_r.Section title="Affectation">
        <FF_r.Field label="Agence / établissement" mapping="agencyId" required span={2}>
          <FF_r.Picker icon={<Ic_r.building size={14}/>} value="Yaoundé HQ" sub="Siège · 182 personnes · code YHQ"/>
        </FF_r.Field>
      </FF_r.Section>

      <FF_r.Section title="Poste à pourvoir">
        <FF_r.Field label="Intitulé du poste" mapping="poste" required span={2}>
          <FF_r.Input placeholder="Ex. Tech Lead Mobile React Native"/>
        </FF_r.Field>
        <FF_r.Field label="Département" mapping="departement" required>
          <FF_r.Select>
            <option>Engineering</option>
            <option>Commercial</option>
            <option>Opérations</option>
            <option>Support</option>
            <option>Finance & RH</option>
            <option>Direction</option>
          </FF_r.Select>
        </FF_r.Field>
        <FF_r.Field label="Localisation" mapping="localisation" required hint="Ville · format remote possible">
          <FF_r.Input icon={<Ic_r.briefcase size={14}/>} placeholder="Yaoundé · hybride 3j/sem"/>
        </FF_r.Field>
      </FF_r.Section>

      <FF_r.Section title="Détails de l'offre">
        <FF_r.Field label="Compétences requises" mapping="competencesRequises" required span={2} hint="Liste séparée par virgules · 5 à 8 compétences clés">
          <FF_r.Textarea rows={4} placeholder="React Native, TypeScript, Architecture mobile, Leadership équipe, AWS, CI/CD, méthodes agiles"/>
        </FF_r.Field>
        <FF_r.Field label="Date limite de candidature" mapping="dateLimite" required>
          <FF_r.Input type="date" defaultValue="2026-06-30"/>
        </FF_r.Field>
        <FF_r.Field label="Package salarial" mapping="packageSalarial" required hint="Fourchette en XAF + avantages">
          <FF_r.Input placeholder="1,2M – 1,6M XAF + bonus 15%"/>
        </FF_r.Field>
      </FF_r.Section>
    </FF_r.FormShell>
  );
}

/* ────────────────────────────────────────────────
   15. CreateApplication → CreateApplicationCommand
   jobOfferId, candidatNom, candidatPrenom, candidatEmail, candidatTelephone, cvFileId, lettreMotivationFileId
   ──────────────────────────────────────────────── */
function FormCreateApplication({ onBack }) {
  return (
    <FF_r.FormShell
      mapping="CreateApplicationCommand"
      title="Saisir une candidature"
      subtitle="Création manuelle d'une candidature externe (sourcing direct, cooptation, événement)"
      meta="POST /api/hrm/applications"
      onCancel={onBack}
      submitLabel="Enregistrer la candidature"
    >
      <FF_r.Section title="Offre ciblée">
        <FF_r.Field label="Offre d'emploi" mapping="jobOfferId" required span={2}>
          <FF_r.Picker icon={<Ic_r.recruit size={14}/>} value="JO-2026-007 — Tech Lead Mobile" sub="Engineering · Yaoundé HQ · ouverte depuis 18j · 24 candidatures"/>
        </FF_r.Field>
      </FF_r.Section>

      <FF_r.Section title="Identité du candidat">
        <FF_r.Field label="Nom" mapping="candidatNom" required>
          <FF_r.Input placeholder="Atangana"/>
        </FF_r.Field>
        <FF_r.Field label="Prénom" mapping="candidatPrenom" required>
          <FF_r.Input placeholder="Bertrand"/>
        </FF_r.Field>
        <FF_r.Field label="Email" mapping="candidatEmail" required>
          <FF_r.Input type="email" icon={<Ic_r.mail size={14}/>} placeholder="b.atangana@email.com"/>
        </FF_r.Field>
        <FF_r.Field label="Téléphone" mapping="candidatTelephone" required>
          <FF_r.Input icon={<Ic_r.phone size={14}/>} prefix="+237" placeholder="6 78 12 34 56"/>
        </FF_r.Field>
      </FF_r.Section>

      <FF_r.Section title="Documents joints">
        <FF_r.Field label="CV" mapping="cvFileId" required hint="UUID FilePort · format PDF privilégié">
          <FF_r.FileDrop accept="PDF, DOC, DOCX" hint="CV obligatoire"/>
        </FF_r.Field>
        <FF_r.Field label="Lettre de motivation" mapping="lettreMotivationFileId" hint="Optionnel">
          <FF_r.FileDrop accept="PDF, DOC, DOCX"/>
        </FF_r.Field>
      </FF_r.Section>
    </FF_r.FormShell>
  );
}

/* ────────────────────────────────────────────────
   16. ScheduleInterview → ScheduleInterviewCommand
   applicationId, type (InterviewType), dateHeure (Instant), lieu, interviewerPartyId, interviewerDisplayName
   ──────────────────────────────────────────────── */
function FormScheduleInterview({ onBack }) {
  return (
    <FF_r.FormShell
      mapping="ScheduleInterviewCommand"
      title="Planifier un entretien"
      subtitle="Convoque le candidat et notifie le recruteur (créneau Outlook/Google synchronisé)"
      meta="POST /api/hrm/interviews"
      onCancel={onBack}
      submitLabel="Planifier"
    >
      <FF_r.Section title="Candidat">
        <FF_r.Field label="Candidature" mapping="applicationId" required span={2}>
          <FF_r.Picker icon={<Ic_r.users size={14}/>} value="Bertrand Atangana · Tech Lead Mobile" sub="JO-2026-007 · match 92% · à l'étape Présélectionné"/>
        </FF_r.Field>
      </FF_r.Section>

      <FF_r.Section title="Type & créneau">
        <FF_r.Field label="Type d'entretien" mapping="type" required span={2}>
          <FF_r.Radio value="TECHNICAL" options={[
            { value: 'PHONE_SCREEN', label: 'Pré-qualif. tél.', sub: '15 min', tag: 'enum' },
            { value: 'TECHNICAL', label: 'Technique', sub: '60-90 min', tag: 'enum' },
            { value: 'CULTURAL', label: 'Culturel', sub: '45 min', tag: 'enum' },
            { value: 'FINAL', label: 'Final (DG)', sub: '30 min', tag: 'enum' },
          ]}/>
        </FF_r.Field>
        <FF_r.Field label="Date et heure" mapping="dateHeure" required hint="Instant ISO 8601">
          <FF_r.Input type="datetime-local" defaultValue="2026-05-16T14:00"/>
        </FF_r.Field>
        <FF_r.Field label="Lieu" mapping="lieu" required hint="Salle, ou lien Teams/Meet">
          <FF_r.Input icon={<Ic_r.briefcase size={14}/>} placeholder="Salle Tokyo · Yaoundé HQ"/>
        </FF_r.Field>
      </FF_r.Section>

      <FF_r.Section title="Recruteur / évaluateur">
        <FF_r.Field label="Personne en charge (Party)" mapping="interviewerPartyId" required>
          <FF_r.EmployeePicker employee={{name: 'Marc Foga', id: 'EMP-0021', role: 'Dir. Technique', color: 'green'}}/>
        </FF_r.Field>
        <FF_r.Field label="Nom affiché" mapping="interviewerDisplayName" required hint="Tel qu'il apparaîtra dans la convocation">
          <FF_r.Input defaultValue="Marc Foga"/>
        </FF_r.Field>
      </FF_r.Section>
    </FF_r.FormShell>
  );
}

/* ────────────────────────────────────────────────
   17. PlanTraining → PlanTrainingCommand
   agencyId, intitule, organisme, dateDebut, dateFin, cout, nbPlaces, lieu
   ──────────────────────────────────────────────── */
function FormPlanTraining({ onBack }) {
  return (
    <FF_r.FormShell
      mapping="PlanTrainingCommand"
      title="Planifier une formation"
      subtitle="Création d'une session de formation · les employés s'y inscriront ensuite"
      meta="POST /api/hrm/trainings"
      onCancel={onBack}
      submitLabel="Planifier"
      sidebar={
        <div className="card card-pad">
          <div style={{fontSize: 13, fontWeight: 700, marginBottom: 10}}>Budget formation</div>
          <div style={{padding: 10, background: 'var(--bg-dim)', borderRadius: 10}}>
            <div style={{fontSize: 11, color: 'var(--ink-3)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em'}}>Disponible 2026</div>
            <div style={{fontFamily: 'Inter Tight', fontSize: 22, fontWeight: 800, marginTop: 4}} className="tabular">16,1M XAF</div>
            <div className="bar thin" style={{marginTop: 8}}><div style={{width: '63%'}}/></div>
            <div style={{fontSize: 11, color: 'var(--ink-3)', marginTop: 4}}>63% du budget annuel consommé</div>
          </div>
        </div>
      }
    >
      <FF_r.Section title="Établissement">
        <FF_r.Field label="Agence" mapping="agencyId" required span={2}>
          <FF_r.Picker icon={<Ic_r.building size={14}/>} value="Yaoundé HQ" sub="Siège"/>
        </FF_r.Field>
      </FF_r.Section>

      <FF_r.Section title="Formation">
        <FF_r.Field label="Intitulé" mapping="intitule" required span={2}>
          <FF_r.Input placeholder="Architecture Cloud AWS"/>
        </FF_r.Field>
        <FF_r.Field label="Organisme formateur" mapping="organisme" required>
          <FF_r.Input placeholder="AWS Training · ou nom du formateur interne"/>
        </FF_r.Field>
        <FF_r.Field label="Lieu" mapping="lieu" required>
          <FF_r.Input icon={<Ic_r.briefcase size={14}/>} placeholder="Salle Sydney + visio"/>
        </FF_r.Field>
      </FF_r.Section>

      <FF_r.Section title="Planning & capacité">
        <FF_r.Field label="Date de début" mapping="dateDebut" required>
          <FF_r.Input type="date" defaultValue="2026-05-20"/>
        </FF_r.Field>
        <FF_r.Field label="Date de fin" mapping="dateFin" required>
          <FF_r.Input type="date" defaultValue="2026-05-26"/>
        </FF_r.Field>
        <FF_r.Field label="Nombre de places" mapping="nbPlaces" required>
          <FF_r.Input type="number" suffix="places" defaultValue="15"/>
        </FF_r.Field>
        <FF_r.Field label="Coût total" mapping="cout" required hint="Sera imputé sur le budget formation">
          <FF_r.Input type="number" suffix="XAF" defaultValue="420000"/>
        </FF_r.Field>
      </FF_r.Section>
    </FF_r.FormShell>
  );
}

/* ────────────────────────────────────────────────
   18. EnrollTraining → EnrollTrainingCommand
   trainingId, employeeId
   ──────────────────────────────────────────────── */
function FormEnrollTraining({ onBack }) {
  return (
    <FF_r.FormShell
      mapping="EnrollTrainingCommand"
      title="Inscrire un employé à une formation"
      subtitle="Crée un TrainingEnrollment · décompté de la capacité"
      meta="POST /api/hrm/training-enrollments"
      onCancel={onBack}
      submitLabel="Confirmer l'inscription"
    >
      <FF_r.Section>
        <FF_r.Field label="Formation" mapping="trainingId" required span={2}>
          <FF_r.Picker icon={<Ic_r.training size={14}/>} value="TR-002 — Architecture Cloud AWS" sub="20-26 mai 2026 · 12/15 inscrits · 420K XAF/place"/>
        </FF_r.Field>
        <FF_r.Field label="Employé" mapping="employeeId" required span={2}>
          <FF_r.EmployeePicker employee={{name: 'Aminata Diallo', id: 'EMP-0142', role: 'Lead Mobile Engineer', color: 'orange'}}/>
        </FF_r.Field>
      </FF_r.Section>

      <div style={{padding: 14, background: 'var(--green-50)', border: '1px solid var(--green-500)', borderRadius: 12, fontSize: 12.5, color: 'var(--ink-2)'}}>
        ✅ Il reste <b>3 places disponibles</b> sur cette session.<br/>
        L'employé sera notifié par email et son agenda sera mis à jour automatiquement.
      </div>
    </FF_r.FormShell>
  );
}

/* ────────────────────────────────────────────────
   19. CreateTrainingBudget → CreateTrainingBudgetCommand
   organizationId, agencyId, annee, montantAlloue
   ──────────────────────────────────────────────── */
function FormCreateTrainingBudget({ onBack }) {
  return (
    <FF_r.FormShell
      mapping="CreateTrainingBudgetCommand"
      title="Allouer un budget formation"
      subtitle="Enveloppe annuelle dédiée à la formation pour une agence donnée"
      meta="POST /api/hrm/training-budgets"
      onCancel={onBack}
      submitLabel="Créer le budget"
    >
      <FF_r.Section title="Périmètre">
        <FF_r.Field label="Organisation" mapping="organizationId" required>
          <FF_r.Picker value="RT-Comops SARL" sub="Identifiant Yowyob · multi-agence"/>
        </FF_r.Field>
        <FF_r.Field label="Agence" mapping="agencyId" required>
          <FF_r.Select>
            <option>Yaoundé HQ — siège</option>
            <option>Douala — agence</option>
            <option>Bafoussam — antenne</option>
            <option>Garoua — antenne</option>
          </FF_r.Select>
        </FF_r.Field>
      </FF_r.Section>

      <FF_r.Section title="Allocation">
        <FF_r.Field label="Année" mapping="annee" required>
          <FF_r.Input type="number" defaultValue="2026"/>
        </FF_r.Field>
        <FF_r.Field label="Montant alloué" mapping="montantAlloue" required hint="Budget total à dépenser dans l'année">
          <FF_r.Input type="number" suffix="XAF" placeholder="18 500 000"/>
        </FF_r.Field>
      </FF_r.Section>
    </FF_r.FormShell>
  );
}

/* ────────────────────────────────────────────────
   20. CreateReview → CreateReviewCommand
   employeeId, evaluateurPartyId, evaluateurDisplayName, periode
   ──────────────────────────────────────────────── */
function FormCreateReview({ onBack }) {
  return (
    <FF_r.FormShell
      mapping="CreateReviewCommand"
      title="Démarrer une évaluation"
      subtitle="Lance le cycle d'évaluation · les objectifs seront ajoutés ensuite"
      meta="POST /api/hrm/reviews"
      onCancel={onBack}
      submitLabel="Démarrer"
    >
      <FF_r.Section>
        <FF_r.Field label="Employé évalué" mapping="employeeId" required span={2}>
          <FF_r.EmployeePicker employee={{name: 'Aminata Diallo', id: 'EMP-0142', role: 'Lead Mobile Engineer', color: 'orange'}}/>
        </FF_r.Field>
        <FF_r.Field label="Évaluateur (Party)" mapping="evaluateurPartyId" required>
          <FF_r.EmployeePicker employee={{name: 'Marc Foga', id: 'EMP-0021', role: 'Dir. Technique · manager direct', color: 'green'}}/>
        </FF_r.Field>
        <FF_r.Field label="Nom affiché évaluateur" mapping="evaluateurDisplayName" required>
          <FF_r.Input defaultValue="Marc Foga"/>
        </FF_r.Field>
        <FF_r.Field label="Période évaluée" mapping="periode" required hint="Format YYYY-QN ou YYYY-MM" span={2}>
          <FF_r.Input placeholder="2026-Q1" defaultValue="2026-Q1"/>
        </FF_r.Field>
      </FF_r.Section>
    </FF_r.FormShell>
  );
}

/* ────────────────────────────────────────────────
   21. AddObjective → AddObjectiveCommand
   reviewId, description, poids (BigDecimal)
   ──────────────────────────────────────────────── */
function FormAddObjective({ onBack }) {
  return (
    <FF_r.FormShell
      mapping="AddObjectiveCommand"
      title="Ajouter un objectif (KR)"
      subtitle="Key Result rattaché à une évaluation en cours · pondéré par % de poids"
      meta="POST /api/hrm/reviews/{id}/objectives"
      onCancel={onBack}
      submitLabel="Ajouter l'objectif"
      sidebar={
        <div className="card card-pad" style={{background: 'var(--bg-dim)'}}>
          <div style={{fontSize: 13, fontWeight: 700, marginBottom: 10}}>💡 Méthode OKR</div>
          <div style={{fontSize: 12, color: 'var(--ink-2)'}}>
            La somme des poids des KR d'une évaluation doit faire <b>100%</b>.<br/><br/>
            Cycle actuel · 4 KR déjà ajoutés (75%) · il reste 25% à attribuer.
          </div>
        </div>
      }
    >
      <FF_r.Section>
        <FF_r.Field label="Évaluation rattachée" mapping="reviewId" required span={2}>
          <FF_r.Picker icon={<Ic_r.review size={14}/>} value="EV-2026-Q1-0142 — Aminata Diallo" sub="Cycle Q1 2026 · évaluateur Marc Foga"/>
        </FF_r.Field>
        <FF_r.Field label="Description de l'objectif" mapping="description" required span={2} hint="Formulation SMART : Spécifique, Mesurable, Atteignable, Réaliste, Temporel">
          <FF_r.Textarea rows={4} placeholder="Améliorer le NPS de l'app mobile à > 60 d'ici fin Q1 2026 (actuellement 52)"/>
        </FF_r.Field>
        <FF_r.Field label="Poids" mapping="poids" required hint="% sur 100 · BigDecimal" span={2}>
          <FF_r.Input type="number" min="0" max="100" suffix="%" placeholder="25" defaultValue="25"/>
        </FF_r.Field>
      </FF_r.Section>
    </FF_r.FormShell>
  );
}

window.FormCreateJobOffer = FormCreateJobOffer;
window.FormCreateApplication = FormCreateApplication;
window.FormScheduleInterview = FormScheduleInterview;
window.FormPlanTraining = FormPlanTraining;
window.FormEnrollTraining = FormEnrollTraining;
window.FormCreateTrainingBudget = FormCreateTrainingBudget;
window.FormCreateReview = FormCreateReview;
window.FormAddObjective = FormAddObjective;
