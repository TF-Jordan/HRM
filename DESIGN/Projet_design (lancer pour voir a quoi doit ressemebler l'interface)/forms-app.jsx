/* global React, ReactDOM */
const { useState: useS_fapp } = React;

const FORM_GROUPS = [
  {
    label: 'Capital humain',
    forms: [
      { id: 'create-employee', label: 'Créer un employé', mapping: 'CreateEmployeeCommand', icon: 'users' },
      { id: 'update-employee', label: 'Modifier un employé', mapping: 'UpdateEmployeeCommand', icon: 'edit' },
      { id: 'add-contract', label: 'Nouveau contrat / avenant', mapping: 'AddContractCommand', icon: 'contract' },
      { id: 'add-dependent', label: 'Personne à charge', mapping: 'AddDependentCommand', icon: 'users' },
      { id: 'create-skill', label: 'Compétence (référentiel)', mapping: 'CreateSkillCommand', icon: 'skill' },
      { id: 'assign-skill', label: 'Évaluer compétence d\'un employé', mapping: 'CreateEmployeeSkillCommand', icon: 'star' },
      { id: 'onboarding-task', label: 'Tâche d\'onboarding', mapping: 'CreateOnboardingTaskCommand', icon: 'check' },
    ],
  },
  {
    label: 'Activité',
    forms: [
      { id: 'submit-leave', label: 'Demande de congé', mapping: 'SubmitLeaveCommand', icon: 'leave' },
      { id: 'create-mission', label: 'Ordre de mission', mapping: 'CreateMissionOrderCommand', icon: 'mission' },
      { id: 'create-timesheet', label: 'Feuille de temps', mapping: 'CreateTimesheetCommand', icon: 'time' },
    ],
  },
  {
    label: 'Rémunération',
    forms: [
      { id: 'request-loan', label: 'Avance / prêt', mapping: 'RequestLoanAdvanceCommand', icon: 'loan' },
      { id: 'create-expense-report', label: 'Note de frais (entête)', mapping: 'CreateExpenseReportCommand', icon: 'expense' },
      { id: 'add-expense-line', label: 'Ligne de dépense', mapping: 'AddExpenseLineCommand', icon: 'plus' },
    ],
  },
  {
    label: 'Recrutement',
    forms: [
      { id: 'create-joboffer', label: 'Publier une offre', mapping: 'CreateJobOfferCommand', icon: 'recruit' },
      { id: 'create-application', label: 'Saisir une candidature', mapping: 'CreateApplicationCommand', icon: 'users' },
      { id: 'schedule-interview', label: 'Planifier un entretien', mapping: 'ScheduleInterviewCommand', icon: 'cal' },
    ],
  },
  {
    label: 'Développement',
    forms: [
      { id: 'plan-training', label: 'Planifier formation', mapping: 'PlanTrainingCommand', icon: 'training' },
      { id: 'enroll-training', label: 'Inscrire un employé', mapping: 'EnrollTrainingCommand', icon: 'training' },
      { id: 'create-training-budget', label: 'Budget de formation', mapping: 'CreateTrainingBudgetCommand', icon: 'pieChart' },
      { id: 'create-review', label: 'Démarrer une évaluation', mapping: 'CreateReviewCommand', icon: 'review' },
      { id: 'add-objective', label: 'Ajouter un objectif (KR)', mapping: 'AddObjectiveCommand', icon: 'star' },
    ],
  },
  {
    label: 'Conformité',
    forms: [
      { id: 'medical-visit', label: 'Visite médicale', mapping: 'CreateMedicalVisitCommand', icon: 'medical' },
      { id: 'medical-cert', label: 'Certificat médical', mapping: 'CreateMedicalCertificateCommand', icon: 'doc' },
      { id: 'social-decl', label: 'Déclaration sociale', mapping: 'CreateSocialDeclarationCommand', icon: 'declaration' },
      { id: 'kpi-snapshot', label: 'Snapshot KPI RH', mapping: 'CreateRhKpiSnapshotCommand', icon: 'analytics' },
    ],
  },
];

const FORM_COMPONENTS = {
  'create-employee':    () => window.FormCreateEmployee,
  'update-employee':    () => window.FormUpdateEmployee,
  'add-contract':       () => window.FormAddContract,
  'add-dependent':      () => window.FormAddDependent,
  'create-skill':       () => window.FormCreateSkill,
  'assign-skill':       () => window.FormAssignSkill,
  'onboarding-task':    () => window.FormCreateOnboardingTask,
  'submit-leave':       () => window.FormSubmitLeave,
  'create-mission':     () => window.FormCreateMissionOrder,
  'create-timesheet':   () => window.FormCreateTimesheet,
  'request-loan':       () => window.FormRequestLoanAdvance,
  'create-expense-report': () => window.FormCreateExpenseReport,
  'add-expense-line':   () => window.FormAddExpenseLine,
  'create-joboffer':    () => window.FormCreateJobOffer,
  'create-application': () => window.FormCreateApplication,
  'schedule-interview': () => window.FormScheduleInterview,
  'plan-training':      () => window.FormPlanTraining,
  'enroll-training':    () => window.FormEnrollTraining,
  'create-training-budget': () => window.FormCreateTrainingBudget,
  'create-review':      () => window.FormCreateReview,
  'add-objective':      () => window.FormAddObjective,
  'medical-visit':      () => window.FormCreateMedicalVisit,
  'medical-cert':       () => window.FormCreateMedicalCertificate,
  'social-decl':        () => window.FormCreateSocialDeclaration,
  'kpi-snapshot':       () => window.FormCreateRhKpiSnapshot,
};

function FormsCatalog({ active, onSelect }) {
  const { Icons } = window;
  return (
    <aside style={{
      width: 296,
      background: 'linear-gradient(180deg, #FFFFFF 0%, #FCF8EF 100%)',
      borderRight: '1px solid var(--line)',
      padding: '18px 12px 18px 18px',
      position: 'sticky', top: 0, height: '100vh',
      overflowY: 'auto',
      flexShrink: 0
    }}>
      <a href="index.html" style={{
        display: 'flex', alignItems: 'center', gap: 12, padding: '8px 10px 16px',
        borderBottom: '1px solid var(--line-soft)', marginBottom: 12, textDecoration: 'none'
      }}>
        <div className="sidebar-logo">H</div>
        <div>
          <div className="sidebar-brand-name">HR Core</div>
          <div className="sidebar-brand-sub">25 formulaires</div>
        </div>
        <Icons.chevR size={14} style={{marginLeft: 'auto', color: 'var(--ink-4)'}}/>
      </a>

      <div style={{padding: '6px 10px', marginBottom: 10}}>
        <div style={{
          padding: '8px 12px', background: 'var(--bg-soft)', borderRadius: 10,
          fontSize: 11.5, color: 'var(--ink-3)', display: 'flex', alignItems: 'center', gap: 8
        }}>
          <Icons.info size={14}/>
          <span>Chaque formulaire mappe 1:1 sur un <b style={{color: 'var(--ink-2)'}}>Command record</b> backend.</span>
        </div>
      </div>

      {FORM_GROUPS.map(group => (
        <div key={group.label}>
          <div className="nav-section-label">{group.label}</div>
          {group.forms.map(f => {
            const I = Icons[f.icon] || Icons.doc;
            const isActive = active === f.id;
            return (
              <button key={f.id} onClick={() => onSelect(f.id)} className={"nav-item" + (isActive ? " active" : "")}
                style={{paddingTop: 10, paddingBottom: 10, lineHeight: 1.3}}>
                <I size={16}/>
                <div style={{flex: 1, textAlign: 'left'}}>
                  <div style={{fontSize: 13, fontWeight: isActive ? 600 : 500}}>{f.label}</div>
                  <div className="mono" style={{
                    fontSize: 10, marginTop: 2,
                    color: isActive ? 'rgba(255,255,255,0.7)' : 'var(--ink-4)'
                  }}>{f.mapping}</div>
                </div>
              </button>
            );
          })}
        </div>
      ))}
    </aside>
  );
}

function FormsApp() {
  const [active, setActive] = useS_fapp('create-employee');
  const getComp = FORM_COMPONENTS[active];
  const Comp = getComp ? getComp() : null;

  return (
    <div style={{display: 'flex', minHeight: '100vh'}}>
      <FormsCatalog active={active} onSelect={setActive}/>
      <div className="main" style={{flex: 1, minWidth: 0}}>
        <div className="topbar">
          <div className="search">
            <window.Icons.search size={16}/>
            <input placeholder="Rechercher un formulaire ou un champ…"/>
          </div>
          <div className="row gap-2" style={{marginLeft: 'auto'}}>
            <a href="index.html" className="btn btn-secondary btn-sm">← Vue Admin</a>
            <a href="employee.html" className="btn btn-secondary btn-sm">Vue Employé</a>
          </div>
        </div>
        <div className="page" data-screen-label={active}>
          {Comp ? <Comp onBack={() => {}}/> : <div className="card card-pad-lg" style={{textAlign:'center', color:'var(--ink-3)', padding:60}}>Formulaire introuvable</div>}
        </div>
      </div>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(<FormsApp/>);
