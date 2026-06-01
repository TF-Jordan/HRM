/* global React */
/* =================================================================
   Forms — CONFORMITÉ (Medical + Social + KPI)
   ================================================================= */
const { FF: FF_c, Icons: Ic_c } = window;

/* ────────────────────────────────────────────────
   22. CreateMedicalVisit → CreateMedicalVisitCommand
   employeeId, dateVisite, medecin, resultatAptitude (AptitudeResult),
   restrictions, prochaineEcheance, certificatFileId
   ──────────────────────────────────────────────── */
function FormCreateMedicalVisit({ onBack }) {
  return (
    <FF_c.FormShell
      mapping="CreateMedicalVisitCommand"
      title="Enregistrer une visite médicale"
      subtitle="Visite du travail · embauche, annuelle, reprise ou spécifique"
      meta="POST /api/hrm/medical-visits"
      onCancel={onBack}
      submitLabel="Enregistrer la visite"
      sidebar={
        <div className="card card-pad" style={{background: 'var(--red-50)', border: '1px solid var(--red-500)'}}>
          <div style={{fontSize: 11, fontWeight: 700, color: 'var(--red-600)', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 8}}>⚖️ Obligation légale</div>
          <div style={{fontSize: 12.5, color: 'var(--ink-2)'}}>
            Visite médicale annuelle obligatoire pour tous les salariés (Code du travail camerounais, art. L.142).
            <br/><br/>L'employeur en supporte le coût.
          </div>
        </div>
      }
    >
      <FF_c.Section title="Patient">
        <FF_c.Field label="Employé" mapping="employeeId" required span={2}>
          <FF_c.EmployeePicker employee={{name: 'Aminata Diallo', id: 'EMP-0142', role: 'Lead Mobile Engineer', color: 'orange'}}/>
        </FF_c.Field>
      </FF_c.Section>

      <FF_c.Section title="Visite">
        <FF_c.Field label="Date de visite" mapping="dateVisite" required>
          <FF_c.Input type="date" defaultValue="2026-05-22"/>
        </FF_c.Field>
        <FF_c.Field label="Médecin du travail" mapping="medecin" required>
          <FF_c.Input icon={<Ic_c.medical size={14}/>} placeholder="Dr. Nkoa · CMA Yaoundé"/>
        </FF_c.Field>
        <FF_c.Field label="Résultat d'aptitude" mapping="resultatAptitude" required span={2}>
          <FF_c.Radio value="FIT" options={[
            { value: 'FIT', label: 'Apte', sub: 'sans réserve', tag: 'enum: FIT' },
            { value: 'FIT_WITH_RESTRICTIONS', label: 'Apte avec restrictions', sub: 'aménagement de poste', tag: 'enum: FIT_WITH_RESTRICTIONS' },
            { value: 'UNFIT_TEMPORARY', label: 'Inapte temporaire', sub: 'délai à fixer', tag: 'enum: UNFIT_TEMPORARY' },
            { value: 'UNFIT_PERMANENT', label: 'Inapte définitif', sub: 'reclassement requis', tag: 'enum: UNFIT_PERMANENT' },
          ]}/>
        </FF_c.Field>
        <FF_c.Field label="Restrictions / aménagements" mapping="restrictions" span={2} hint="Si apte avec restrictions ou inapte temporaire">
          <FF_c.Textarea rows={3} placeholder="Ex. Éviter le port de charges &gt; 10kg · Limiter le temps écran à 6h/jour"/>
        </FF_c.Field>
        <FF_c.Field label="Prochaine échéance" mapping="prochaineEcheance" required hint="Date de la prochaine visite obligatoire">
          <FF_c.Input type="date" defaultValue="2027-05-22"/>
        </FF_c.Field>
        <FF_c.Field label="Certificat médical scanné" mapping="certificatFileId" hint="UUID FilePort">
          <FF_c.FileDrop accept="PDF, JPG" maxMb={5}/>
        </FF_c.Field>
      </FF_c.Section>
    </FF_c.FormShell>
  );
}

/* ────────────────────────────────────────────────
   23. CreateMedicalCertificate → CreateMedicalCertificateCommand
   employeeId, typeCertificat, dateEmission, dateExpiration, statut, fichierId
   ──────────────────────────────────────────────── */
function FormCreateMedicalCertificate({ onBack }) {
  return (
    <FF_c.FormShell
      mapping="CreateMedicalCertificateCommand"
      title="Déposer un certificat médical"
      subtitle="Maladie, maternité, accident du travail ou autre certificat"
      meta="POST /api/hrm/medical-certificates"
      onCancel={onBack}
      submitLabel="Déposer le certificat"
    >
      <FF_c.Section title="Concerné">
        <FF_c.Field label="Employé" mapping="employeeId" required span={2}>
          <FF_c.EmployeePicker employee={{name: 'Joseph Mbarga', id: 'EMP-0098', role: 'Business Developer', color: 'blue'}}/>
        </FF_c.Field>
      </FF_c.Section>

      <FF_c.Section title="Certificat">
        <FF_c.Field label="Type de certificat" mapping="typeCertificat" required span={2}>
          <FF_c.Radio value="SICK_LEAVE" options={[
            { value: 'SICK_LEAVE', label: 'Arrêt maladie', sub: 'délivré par médecin' },
            { value: 'MATERNITY', label: 'Maternité', sub: 'pré + post natal' },
            { value: 'WORK_ACCIDENT', label: 'Accident travail', sub: 'déclaration CNPS' },
            { value: 'OTHER', label: 'Autre', sub: 'longue maladie…' },
          ]}/>
        </FF_c.Field>
        <FF_c.Field label="Date d'émission" mapping="dateEmission" required>
          <FF_c.Input type="date" defaultValue="2026-05-13"/>
        </FF_c.Field>
        <FF_c.Field label="Date d'expiration" mapping="dateExpiration" required hint="Fin de la période couverte">
          <FF_c.Input type="date" defaultValue="2026-05-15"/>
        </FF_c.Field>
        <FF_c.Field label="Statut" mapping="statut" required>
          <FF_c.Select>
            <option>EN_ATTENTE — à valider DRH</option>
            <option>VALIDE — accepté</option>
            <option>EXPIRE — passé</option>
            <option>REJETE — non conforme</option>
          </FF_c.Select>
        </FF_c.Field>
        <FF_c.Field label="Fichier scanné" mapping="fichierId" required>
          <FF_c.FileDrop accept="PDF, JPG, PNG" maxMb={5} hint="Certificat lisible"/>
        </FF_c.Field>
      </FF_c.Section>
    </FF_c.FormShell>
  );
}

/* ────────────────────────────────────────────────
   24. CreateSocialDeclaration → CreateSocialDeclarationCommand
   organizationId, type, periode, format
   ──────────────────────────────────────────────── */
function FormCreateSocialDeclaration({ onBack }) {
  return (
    <FF_c.FormShell
      mapping="CreateSocialDeclarationCommand"
      title="Préparer une déclaration sociale"
      subtitle="DSN CNPS, IRPP DGI, CFC… génération automatique à partir du dernier run paie"
      meta="POST /api/hrm/social-declarations"
      onCancel={onBack}
      submitLabel="Générer la déclaration"
      sidebar={
        <div className="card card-pad" style={{background: 'linear-gradient(135deg, var(--orange-50) 0%, #FFFAEC 100%)', border: '1px solid var(--orange-200)'}}>
          <div style={{fontSize: 11, fontWeight: 700, color: 'var(--orange-700)', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 8}}>📅 Échéances 2026</div>
          <div style={{fontSize: 12, color: 'var(--ink-2)'}}>
            <b>DSN CNPS</b> · le 15 du mois suivant<br/>
            <b>IRPP DGI</b> · le 15 du mois suivant<br/>
            <b>CFC</b> · le 15 du mois suivant<br/>
            <b>Taxe communale</b> · le 15 du mois suivant
          </div>
        </div>
      }
    >
      <FF_c.Section title="Organisation">
        <FF_c.Field label="Organisation" mapping="organizationId" required span={2}>
          <FF_c.Picker value="RT-Comops SARL" sub="NIU M122001234567"/>
        </FF_c.Field>
      </FF_c.Section>

      <FF_c.Section title="Déclaration">
        <FF_c.Field label="Type" mapping="type" required span={2}>
          <FF_c.Radio value="CNPS" options={[
            { value: 'CNPS', label: 'DSN CNPS', sub: 'cotisations sociales', tag: 'enum' },
            { value: 'IRPP', label: 'IRPP', sub: 'impôt revenu', tag: 'enum' },
            { value: 'CFC', label: 'CFC', sub: 'Crédit Foncier', tag: 'enum' },
            { value: 'TAXE_COMMUNALE', label: 'Taxe communale', sub: 'TCS + RAV', tag: 'enum' },
            { value: 'FNE', label: 'FNE', sub: 'Fonds Emploi', tag: 'enum' },
          ]}/>
        </FF_c.Field>
        <FF_c.Field label="Période" mapping="periode" required hint="Format YYYY-MM">
          <FF_c.Input placeholder="2026-05" defaultValue="2026-05"/>
        </FF_c.Field>
        <FF_c.Field label="Format de sortie" mapping="format" required hint="XML pour télédéclaration · PDF pour archivage">
          <FF_c.Radio value="XML" options={[
            { value: 'XML', label: 'XML', sub: 'Télédéclaration' },
            { value: 'PDF', label: 'PDF', sub: 'Archivage' },
            { value: 'CSV', label: 'CSV', sub: 'Tableur' },
          ]}/>
        </FF_c.Field>
      </FF_c.Section>
    </FF_c.FormShell>
  );
}

/* ────────────────────────────────────────────────
   25. CreateRhKpiSnapshot → CreateRhKpiSnapshotCommand
   organizationId, periode, effectifTotal, effectifActif, tauxTurnover,
   tauxAbsenteisme, masseSalariale, couvertureCompetences
   ──────────────────────────────────────────────── */
function FormCreateRhKpiSnapshot({ onBack }) {
  return (
    <FF_c.FormShell
      mapping="CreateRhKpiSnapshotCommand"
      title="Snapshot des KPI RH"
      subtitle="Capture périodique des indicateurs clés · pour tableaux de bord et reporting board"
      meta="POST /api/hrm/rh-kpi-snapshots"
      onCancel={onBack}
      submitLabel="Enregistrer le snapshot"
      sidebar={
        <div className="card card-pad" style={{background: 'var(--bg-dim)'}}>
          <div style={{fontSize: 13, fontWeight: 700, marginBottom: 10}}>🤖 Auto-calcul</div>
          <div style={{fontSize: 12, color: 'var(--ink-2)'}}>
            Tous les champs peuvent être pré-remplis par le moteur RhKpiService à partir des données du système.
            <br/><br/>Cliquez sur "Auto-remplir" en haut de chaque section.
          </div>
        </div>
      }
    >
      <FF_c.Section title="Périmètre">
        <FF_c.Field label="Organisation" mapping="organizationId" required>
          <FF_c.Picker value="RT-Comops SARL"/>
        </FF_c.Field>
        <FF_c.Field label="Période" mapping="periode" required hint="YYYY-QN ou YYYY-MM">
          <FF_c.Input placeholder="2026-Q1" defaultValue="2026-Q1"/>
        </FF_c.Field>
      </FF_c.Section>

      <FF_c.Section title="Indicateurs effectif">
        <FF_c.Field label="Effectif total" mapping="effectifTotal" required hint="Tous statuts confondus">
          <FF_c.Input type="number" defaultValue="342"/>
        </FF_c.Field>
        <FF_c.Field label="Effectif actif" mapping="effectifActif" required hint="Hors congés longs / suspendus">
          <FF_c.Input type="number" defaultValue="338"/>
        </FF_c.Field>
        <FF_c.Field label="Taux de turnover" mapping="tauxTurnover" required hint="Annualisé en %">
          <FF_c.Input type="number" step="0.1" suffix="%" defaultValue="9.2"/>
        </FF_c.Field>
        <FF_c.Field label="Taux d'absentéisme" mapping="tauxAbsenteisme" required hint="Sur la période">
          <FF_c.Input type="number" step="0.1" suffix="%" defaultValue="3.4"/>
        </FF_c.Field>
      </FF_c.Section>

      <FF_c.Section title="Indicateurs financiers & RH">
        <FF_c.Field label="Masse salariale" mapping="masseSalariale" required hint="Sur la période · BigDecimal">
          <FF_c.Input type="number" suffix="XAF" defaultValue="142850000"/>
        </FF_c.Field>
        <FF_c.Field label="Couverture compétences" mapping="couvertureCompetences" required hint="% des compétences cibles couvertes par au moins 1 expert">
          <FF_c.Input type="number" step="0.1" suffix="%" defaultValue="78.4"/>
        </FF_c.Field>
      </FF_c.Section>
    </FF_c.FormShell>
  );
}

window.FormCreateMedicalVisit = FormCreateMedicalVisit;
window.FormCreateMedicalCertificate = FormCreateMedicalCertificate;
window.FormCreateSocialDeclaration = FormCreateSocialDeclaration;
window.FormCreateRhKpiSnapshot = FormCreateRhKpiSnapshot;
