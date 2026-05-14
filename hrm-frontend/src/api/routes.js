const BASE = '/api/v1/hrm';

const API = {
  // ─── Employés ────────────────────────────────────────────────
  employees: {
    list:         () => `${BASE}/employees`,           // GET    — Liste paginée des employés
    get:          (id) => `${BASE}/employees/${id}`,   // GET    — Détail d'un employé
    create:       () => `${BASE}/employees`,           // POST   — Créer un employé
    update:       (id) => `${BASE}/employees/${id}`,   // PUT    — Modifier un employé
    delete:       (id) => `${BASE}/employees/${id}`,   // DELETE — Supprimer (soft) un employé
    activate:     (id) => `${BASE}/employees/${id}/activate`,   // POST — Activer
    deactivate:   (id) => `${BASE}/employees/${id}/deactivate`, // POST — Désactiver
    search:       () => `${BASE}/employees/search`,    // GET    — Recherche full-text
    byDepartment: (dept) => `${BASE}/employees/department/${dept}`, // GET — Par département
    stats:        () => `${BASE}/employees/stats`,     // GET    — Statistiques effectif
  },

  // ─── Contrats ────────────────────────────────────────────────
  contracts: {
    list:         () => `${BASE}/contracts`,
    get:          (id) => `${BASE}/contracts/${id}`,
    create:       () => `${BASE}/contracts`,
    update:       (id) => `${BASE}/contracts/${id}`,
    terminate:    (id) => `${BASE}/contracts/${id}/terminate`,   // POST — Résiliation
    renew:        (id) => `${BASE}/contracts/${id}/renew`,       // POST — Renouvellement
    amendments:   (id) => `${BASE}/contracts/${id}/amendments`,  // GET  — Avenants
    createAmendment: (id) => `${BASE}/contracts/${id}/amendments`, // POST — Nouvel avenant
    expiring:     () => `${BASE}/contracts/expiring`,            // GET  — Contrats expirant bientôt
    trialEnding:  () => `${BASE}/contracts/trial-ending`,        // GET  — Fins de période d'essai
    stats:        () => `${BASE}/contracts/stats`,
  },

  // ─── Compétences ────────────────────────────────────────────
  skills: {
    list:            () => `${BASE}/skills`,
    get:             (id) => `${BASE}/skills/${id}`,
    create:          () => `${BASE}/skills`,
    update:          (id) => `${BASE}/skills/${id}`,
    categories:      () => `${BASE}/skills/categories`,        // GET  — Catégories
    employeeSkills:  (empId) => `${BASE}/employees/${empId}/skills`, // GET  — Compétences d'un employé
    assess:          (empId) => `${BASE}/employees/${empId}/skills`, // POST — Évaluer
    gapAnalysis:     () => `${BASE}/skills/gap-analysis`,      // GET  — Analyse des écarts
    experts:         (skillId) => `${BASE}/skills/${skillId}/experts`, // GET — Experts par compétence
  },

  // ─── Recrutement ────────────────────────────────────────────
  recruitment: {
    offers:          () => `${BASE}/recruitment/offers`,
    getOffer:        (id) => `${BASE}/recruitment/offers/${id}`,
    createOffer:     () => `${BASE}/recruitment/offers`,
    updateOffer:     (id) => `${BASE}/recruitment/offers/${id}`,
    closeOffer:      (id) => `${BASE}/recruitment/offers/${id}/close`,
    applications:    (offerId) => `${BASE}/recruitment/offers/${offerId}/applications`,
    getApplication:  (id) => `${BASE}/recruitment/applications/${id}`,
    updateStage:     (id) => `${BASE}/recruitment/applications/${id}/stage`, // PUT — Changer l'étape pipeline
    scheduleInterview: (id) => `${BASE}/recruitment/applications/${id}/interview`, // POST
    stats:           () => `${BASE}/recruitment/stats`,
  },

  // ─── Temps & Présences ──────────────────────────────────────
  time: {
    clockIn:       () => `${BASE}/time/clock-in`,               // POST — Pointage entrée
    clockOut:      () => `${BASE}/time/clock-out`,               // POST — Pointage sortie
    today:         () => `${BASE}/time/today`,                   // GET  — Pointages du jour
    timesheets:    () => `${BASE}/time/timesheets`,              // GET  — Feuilles de temps
    getTimesheet:  (id) => `${BASE}/time/timesheets/${id}`,
    submitTimesheet: (id) => `${BASE}/time/timesheets/${id}/submit`, // POST
    approveTimesheet:(id) => `${BASE}/time/timesheets/${id}/approve`, // POST
    overtime:      () => `${BASE}/time/overtime`,                // GET  — Heures supplémentaires
    stats:         () => `${BASE}/time/stats`,
  },

  // ─── Congés ─────────────────────────────────────────────────
  leaves: {
    requests:      () => `${BASE}/leaves/requests`,            // GET  — Demandes
    get:           (id) => `${BASE}/leaves/requests/${id}`,
    create:        () => `${BASE}/leaves/requests`,            // POST — Nouvelle demande
    approve:       (id) => `${BASE}/leaves/requests/${id}/approve`, // POST
    reject:        (id) => `${BASE}/leaves/requests/${id}/reject`,  // POST
    cancel:        (id) => `${BASE}/leaves/requests/${id}/cancel`,  // POST
    balances:      () => `${BASE}/leaves/balances`,            // GET  — Soldes par employé
    balanceFor:    (empId) => `${BASE}/leaves/balances/${empId}`,
    calendar:      () => `${BASE}/leaves/calendar`,            // GET  — Calendrier absences
    types:         () => `${BASE}/leaves/types`,               // GET  — Types de congés
    stats:         () => `${BASE}/leaves/stats`,
  },

  // ─── Ordres de mission ──────────────────────────────────────
  missions: {
    list:          () => `${BASE}/missions`,
    get:           (id) => `${BASE}/missions/${id}`,
    create:        () => `${BASE}/missions`,
    update:        (id) => `${BASE}/missions/${id}`,
    approve:       (id) => `${BASE}/missions/${id}/approve`,
    reject:        (id) => `${BASE}/missions/${id}/reject`,
    complete:      (id) => `${BASE}/missions/${id}/complete`,
    expenses:      (id) => `${BASE}/missions/${id}/expenses`,  // GET — Frais liés
    stats:         () => `${BASE}/missions/stats`,
  },

  // ─── Paie ───────────────────────────────────────────────────
  payroll: {
    runs:          () => `${BASE}/payroll/runs`,               // GET  — Cycles de paie
    getRun:        (id) => `${BASE}/payroll/runs/${id}`,
    createRun:     () => `${BASE}/payroll/runs`,               // POST — Nouveau cycle
    calculateRun:  (id) => `${BASE}/payroll/runs/${id}/calculate`,  // POST — Calculer
    validateRun:   (id) => `${BASE}/payroll/runs/${id}/validate`,   // POST — Valider DRH
    approveRun:    (id) => `${BASE}/payroll/runs/${id}/approve`,    // POST — Approuver DG
    executeRun:    (id) => `${BASE}/payroll/runs/${id}/execute`,    // POST — Exécuter paiement
    payslips:      (runId) => `${BASE}/payroll/runs/${runId}/payslips`, // GET — Bulletins
    getPayslip:    (id) => `${BASE}/payroll/payslips/${id}`,
    downloadPayslip:(id) => `${BASE}/payroll/payslips/${id}/pdf`,   // GET — PDF
    elements:      () => `${BASE}/payroll/elements`,            // GET — Éléments de paie
    createElement: () => `${BASE}/payroll/elements`,            // POST
    stats:         () => `${BASE}/payroll/stats`,
  },

  // ─── Avances & Prêts ───────────────────────────────────────
  loans: {
    list:          () => `${BASE}/loans`,
    get:           (id) => `${BASE}/loans/${id}`,
    create:        () => `${BASE}/loans`,
    approve:       (id) => `${BASE}/loans/${id}/approve`,
    reject:        (id) => `${BASE}/loans/${id}/reject`,
    repayments:    (id) => `${BASE}/loans/${id}/repayments`,    // GET — Échéancier
    recordPayment: (id) => `${BASE}/loans/${id}/repayments`,    // POST — Enregistrer remboursement
    stats:         () => `${BASE}/loans/stats`,
  },

  // ─── Notes de frais ────────────────────────────────────────
  expenses: {
    reports:       () => `${BASE}/expenses/reports`,
    getReport:     (id) => `${BASE}/expenses/reports/${id}`,
    createReport:  () => `${BASE}/expenses/reports`,
    updateReport:  (id) => `${BASE}/expenses/reports/${id}`,
    submitReport:  (id) => `${BASE}/expenses/reports/${id}/submit`,
    approveReport: (id) => `${BASE}/expenses/reports/${id}/approve`,
    rejectReport:  (id) => `${BASE}/expenses/reports/${id}/reject`,
    lines:         (reportId) => `${BASE}/expenses/reports/${reportId}/lines`, // GET/POST — Lignes
    stats:         () => `${BASE}/expenses/stats`,
  },

  // ─── Évaluations ───────────────────────────────────────────
  reviews: {
    campaigns:     () => `${BASE}/reviews/campaigns`,          // GET  — Campagnes
    getCampaign:   (id) => `${BASE}/reviews/campaigns/${id}`,
    createCampaign:() => `${BASE}/reviews/campaigns`,          // POST — Lancer un cycle
    reviews:       () => `${BASE}/reviews`,                    // GET  — Toutes les évaluations
    get:           (id) => `${BASE}/reviews/${id}`,
    create:        () => `${BASE}/reviews`,
    update:        (id) => `${BASE}/reviews/${id}`,
    submit:        (id) => `${BASE}/reviews/${id}/submit`,
    objectives:    (empId) => `${BASE}/employees/${empId}/objectives`, // GET
    nineBox:       () => `${BASE}/reviews/nine-box`,           // GET — Matrice 9-box
    stats:         () => `${BASE}/reviews/stats`,
  },

  // ─── Formations ────────────────────────────────────────────
  trainings: {
    catalog:       () => `${BASE}/trainings`,
    get:           (id) => `${BASE}/trainings/${id}`,
    create:        () => `${BASE}/trainings`,
    update:        (id) => `${BASE}/trainings/${id}`,
    sessions:      (id) => `${BASE}/trainings/${id}/sessions`,  // GET/POST
    enrollments:   () => `${BASE}/trainings/enrollments`,       // GET — Inscriptions
    enroll:        (id) => `${BASE}/trainings/${id}/enroll`,    // POST
    complete:      (enrollId) => `${BASE}/trainings/enrollments/${enrollId}/complete`, // POST
    stats:         () => `${BASE}/trainings/stats`,
  },

  // ─── Budget formation ──────────────────────────────────────
  budget: {
    overview:      () => `${BASE}/budget/training`,            // GET  — Vue d'ensemble
    allocations:   () => `${BASE}/budget/training/allocations`, // GET — Par département
    allocate:      () => `${BASE}/budget/training/allocations`, // POST — Allouer
    consumption:   () => `${BASE}/budget/training/consumption`, // GET — Consommation
    stats:         () => `${BASE}/budget/training/stats`,
  },

  // ─── Suivi médical ─────────────────────────────────────────
  medical: {
    visits:        () => `${BASE}/medical/visits`,
    getVisit:      (id) => `${BASE}/medical/visits/${id}`,
    createVisit:   () => `${BASE}/medical/visits`,
    updateVisit:   (id) => `${BASE}/medical/visits/${id}`,
    certificates:  () => `${BASE}/medical/certificates`,       // GET — Certificats médicaux
    createCert:    () => `${BASE}/medical/certificates`,
    expiring:      () => `${BASE}/medical/visits/expiring`,    // GET — Visites à planifier
    accidents:     () => `${BASE}/medical/accidents`,          // GET/POST — Accidents de travail
    stats:         () => `${BASE}/medical/stats`,
  },

  // ─── Déclarations sociales & fiscales ──────────────────────
  declarations: {
    list:          () => `${BASE}/declarations`,
    get:           (id) => `${BASE}/declarations/${id}`,
    create:        () => `${BASE}/declarations`,
    submit:        (id) => `${BASE}/declarations/${id}/submit`, // POST — Soumettre
    markPaid:      (id) => `${BASE}/declarations/${id}/pay`,    // POST — Marquer payée
    generate:      () => `${BASE}/declarations/generate`,       // POST — Générer (CNPS, IRPP, etc.)
    calendar:      () => `${BASE}/declarations/calendar`,       // GET  — Calendrier obligations
    stats:         () => `${BASE}/declarations/stats`,
  },

  // ─── Paramètres ────────────────────────────────────────────
  settings: {
    company:       () => `${BASE}/settings/company`,           // GET/PUT — Infos entreprise
    sites:         () => `${BASE}/settings/sites`,             // GET/POST — Sites
    getSite:       (id) => `${BASE}/settings/sites/${id}`,
    org:           () => `${BASE}/settings/organization`,      // GET — Organigramme
    payrollConfig: () => `${BASE}/settings/payroll`,           // GET/PUT — Config paie
    leavePolicy:   () => `${BASE}/settings/leave-policy`,      // GET/PUT — Politique congés
    workflows:     () => `${BASE}/settings/workflows`,         // GET — Workflows d'approbation
    updateWorkflow:(id) => `${BASE}/settings/workflows/${id}`, // PUT
    users:         () => `${BASE}/settings/users`,             // GET/POST — Utilisateurs
    getUser:       (id) => `${BASE}/settings/users/${id}`,
    roles:         () => `${BASE}/settings/roles`,             // GET — Rôles
    integrations:  () => `${BASE}/settings/integrations`,      // GET
    notifications: () => `${BASE}/settings/notifications`,     // GET/PUT
    audit:         () => `${BASE}/settings/audit-log`,         // GET — Journal d'audit
  },

  // ─── Analytics / KPI ───────────────────────────────────────
  analytics: {
    dashboard:     () => `${BASE}/analytics/dashboard`,        // GET — KPIs tableau de bord
    headcount:     () => `${BASE}/analytics/headcount`,        // GET — Évolution effectif
    demographics:  () => `${BASE}/analytics/demographics`,     // GET — Pyramide des âges, genre
    costPerEmployee:()=> `${BASE}/analytics/cost-per-employee`,// GET — Coût par ETP
    turnover:      () => `${BASE}/analytics/turnover`,         // GET — Turnover & motifs départ
    absenteeism:   () => `${BASE}/analytics/absenteeism`,      // GET — Taux d'absentéisme
    engagement:    () => `${BASE}/analytics/engagement`,       // GET — eNPS, satisfaction
    departmentPerf:() => `${BASE}/analytics/department-performance`, // GET — Perf. par département
    kpiSnapshot:   () => `${BASE}/analytics/kpi-snapshot`,     // POST — Sauvegarder snapshot
  },
};

export default API;
