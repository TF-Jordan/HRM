/* global React */
/* =================================================================
   Forms — ACTIVITÉ & RÉMUNÉRATION
   ================================================================= */
const { FF: FF_a, Icons: Ic_a, Avatar: Av_a } = window;

/* ────────────────────────────────────────────────
   8. SubmitLeave → SubmitLeaveCommand
   employeeId, type, dateDebut, dateFin, motif, justificatifFileId
   ──────────────────────────────────────────────── */
function FormSubmitLeave({ onBack }) {
  return (
    <FF_a.FormShell
      mapping="SubmitLeaveCommand"
      title="Demande de congé"
      subtitle="Demande soumise pour validation manager puis DRH"
      meta="POST /api/hrm/leave-requests"
      onCancel={onBack}
      submitLabel="Soumettre la demande"
      draftLabel="Enregistrer brouillon"
      sidebar={
        <>
          <div className="card card-pad" style={{background: 'linear-gradient(135deg, var(--orange-50) 0%, #fff 100%)', border: '1px solid var(--orange-200)'}}>
            <div style={{fontSize: 11, fontWeight: 700, color: 'var(--orange-700)', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 8}}>Récapitulatif</div>
            <div style={{fontFamily: 'Inter Tight', fontSize: 34, fontWeight: 800, color: 'var(--orange-700)'}} className="tabular">5 jours</div>
            <div style={{fontSize: 12.5, color: 'var(--ink-2)', marginTop: 4}}>18 → 22 mai 2026 · Annuel</div>
            <div className="divider"/>
            <div style={{fontSize: 12.5, display: 'flex', justifyContent: 'space-between', padding: '3px 0'}}>
              <span style={{color: 'var(--ink-3)'}}>Solde avant</span><b className="tabular">18,5 j</b>
            </div>
            <div style={{fontSize: 12.5, display: 'flex', justifyContent: 'space-between', padding: '3px 0'}}>
              <span style={{color: 'var(--ink-3)'}}>Demande</span><b className="tabular" style={{color: 'var(--orange-600)'}}>– 5 j</b>
            </div>
            <div style={{fontSize: 13, display: 'flex', justifyContent: 'space-between', padding: '8px 0 0', borderTop: '1px solid var(--orange-200)', marginTop: 4}}>
              <b>Solde après</b><b className="tabular" style={{fontFamily: 'Inter Tight', fontSize: 17}}>13,5 j</b>
            </div>
          </div>
          <div className="card card-pad">
            <div style={{fontSize: 13, fontWeight: 700, marginBottom: 10}}>Workflow</div>
            <div className="stepper" style={{flexDirection: 'column', alignItems: 'flex-start', gap: 8}}>
              <div className="step done">1 · Vous (soumission)</div>
              <div className="step active">2 · Marc Foga (manager)</div>
              <div className="step">3 · DRH (validation finale)</div>
            </div>
          </div>
        </>
      }
    >
      <FF_a.Section title="Employé">
        <FF_a.Field label="Employé concerné" mapping="employeeId" required span={2}>
          <FF_a.EmployeePicker employee={{name: 'Aminata Diallo', id: 'EMP-0142', role: 'Lead Mobile Engineer', color: 'orange'}}/>
        </FF_a.Field>
      </FF_a.Section>

      <FF_a.Section title="Type & période">
        <FF_a.Field label="Type de congé" mapping="type" required span={2}>
          <FF_a.Radio value="ANNUAL" options={[
            { value: 'ANNUAL', label: 'Annuel', tag: 'enum' },
            { value: 'SICK', label: 'Maladie', tag: 'enum' },
            { value: 'MATERNITY', label: 'Maternité', tag: 'enum' },
            { value: 'PATERNITY', label: 'Paternité', tag: 'enum' },
            { value: 'UNPAID', label: 'Sans solde', tag: 'enum' },
            { value: 'SPECIAL', label: 'Spécial', tag: 'enum' },
          ]}/>
        </FF_a.Field>
        <FF_a.Field label="Date de début" mapping="dateDebut" required>
          <FF_a.Input type="date" defaultValue="2026-05-18"/>
        </FF_a.Field>
        <FF_a.Field label="Date de fin" mapping="dateFin" required>
          <FF_a.Input type="date" defaultValue="2026-05-22"/>
        </FF_a.Field>
      </FF_a.Section>

      <FF_a.Section title="Justificatif">
        <FF_a.Field label="Motif" mapping="motif" hint="Optionnel pour congé annuel · obligatoire pour congé spécial / sans solde" span={2}>
          <FF_a.Textarea placeholder="Vacances en famille à Limbé"/>
        </FF_a.Field>
        <FF_a.Field label="Justificatif (fichier)" mapping="justificatifFileId" hint="Obligatoire pour maladie / maternité / mariage · UUID FilePort" span={2}>
          <FF_a.FileDrop hint="certif. médical, acte de mariage…"/>
        </FF_a.Field>
      </FF_a.Section>
    </FF_a.FormShell>
  );
}

/* ────────────────────────────────────────────────
   9. RequestLoanAdvance → RequestLoanAdvanceCommand
   employeeId, montant, nbEcheances, motif
   ──────────────────────────────────────────────── */
function FormRequestLoanAdvance({ onBack }) {
  const [montant, setMontant] = React.useState(2800000);
  const [nbEch, setNbEch] = React.useState(12);
  const mensualite = Math.round(montant / nbEch);

  return (
    <FF_a.FormShell
      mapping="RequestLoanAdvanceCommand"
      title="Demande d'avance ou de prêt"
      subtitle="Avance sur salaire (1 échéance) ou prêt amortissable (> 1 échéance)"
      meta="POST /api/hrm/loan-advances"
      onCancel={onBack}
      submitLabel="Soumettre la demande"
      sidebar={
        <>
          <div className="card card-pad" style={{background: 'linear-gradient(135deg, #1A150E 0%, #2D2520 100%)', color: '#fff'}}>
            <div style={{fontSize: 11, opacity: 0.7, textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 8}}>Simulation</div>
            <div style={{fontFamily: 'Inter Tight', fontSize: 24, fontWeight: 800}} className="tabular">{mensualite.toLocaleString('fr-FR').replace(/,/g, ' ')} <span style={{fontSize: 12, opacity: 0.6}}>XAF/mois</span></div>
            <div style={{fontSize: 12, opacity: 0.7, marginTop: 4}}>Mensualité prélevée sur le salaire</div>
            <div style={{marginTop: 14, fontSize: 12}}>
              <div style={{display: 'flex', justifyContent: 'space-between', padding: '3px 0'}}>
                <span style={{opacity: 0.7}}>Capital</span><span className="tabular">{montant.toLocaleString('fr-FR').replace(/,/g, ' ')}</span>
              </div>
              <div style={{display: 'flex', justifyContent: 'space-between', padding: '3px 0'}}>
                <span style={{opacity: 0.7}}>Échéances</span><span className="tabular">{nbEch} mois</span>
              </div>
              <div style={{display: 'flex', justifyContent: 'space-between', padding: '3px 0'}}>
                <span style={{opacity: 0.7}}>Taux interne</span><span className="tabular">4,0%</span>
              </div>
            </div>
          </div>
          <div className="card card-pad" style={{background: 'var(--amber-50)', border: '1px solid var(--amber-500)'}}>
            <div style={{fontSize: 12.5, color: 'var(--ink-2)'}}>
              ⚖️ Plafond légal : la mensualité ne doit pas dépasser <b>33% du salaire net</b> (politique Yowyob).
            </div>
          </div>
        </>
      }
    >
      <FF_a.Section title="Demandeur">
        <FF_a.Field label="Employé" mapping="employeeId" required span={2}>
          <FF_a.EmployeePicker employee={{name: 'Aminata Diallo', id: 'EMP-0142', role: 'Lead Mobile Engineer', color: 'orange'}}/>
        </FF_a.Field>
      </FF_a.Section>

      <FF_a.Section title="Conditions financières">
        <FF_a.Field label="Montant demandé" mapping="montant" required>
          <FF_a.Input type="number" suffix="XAF" value={montant} onChange={e => setMontant(+e.target.value || 0)}/>
        </FF_a.Field>
        <FF_a.Field label="Nombre d'échéances" mapping="nbEcheances" required hint="1 = avance · > 1 = prêt amortissable">
          <FF_a.Input type="number" suffix="mois" value={nbEch} onChange={e => setNbEch(+e.target.value || 1)} min={1} max={48}/>
        </FF_a.Field>
        <FF_a.Field label="Motif de la demande" mapping="motif" required span={2} hint="Sera revu par le comité prêt si > 5M XAF">
          <FF_a.Textarea rows={4} placeholder="Achat de véhicule pour mobilité professionnelle"/>
        </FF_a.Field>
      </FF_a.Section>
    </FF_a.FormShell>
  );
}

/* ────────────────────────────────────────────────
   10. CreateExpenseReport → CreateExpenseReportCommand
   employeeId, periode, motif
   ──────────────────────────────────────────────── */
function FormCreateExpenseReport({ onBack }) {
  return (
    <FF_a.FormShell
      mapping="CreateExpenseReportCommand"
      title="Nouvelle note de frais"
      subtitle="Création de l'enveloppe — vous ajouterez ensuite les lignes individuelles"
      meta="POST /api/hrm/expense-reports"
      onCancel={onBack}
      submitLabel="Créer la note"
      sidebar={
        <div className="card card-pad" style={{background: 'var(--bg-dim)'}}>
          <div style={{fontSize: 13, fontWeight: 700, marginBottom: 10}}>Ensuite ?</div>
          <div style={{fontSize: 12.5, color: 'var(--ink-2)'}}>
            Après création, vous pourrez ajouter les <b>lignes de dépense</b> (formulaire AddExpenseLine) une par une, avec justificatifs scannés.
          </div>
        </div>
      }
    >
      <FF_a.Section>
        <FF_a.Field label="Employé" mapping="employeeId" required span={2}>
          <FF_a.EmployeePicker employee={{name: 'Aminata Diallo', id: 'EMP-0142', role: 'Lead Mobile Engineer', color: 'orange'}}/>
        </FF_a.Field>
        <FF_a.Field label="Période" mapping="periode" required hint="Mois ou plage couverte par la note · format YYYY-MM ou libre">
          <FF_a.Input placeholder="2026-05" defaultValue="2026-05"/>
        </FF_a.Field>
        <FF_a.Field label="" mapping="" span={1}><div style={{visibility: 'hidden'}}/></FF_a.Field>
        <FF_a.Field label="Motif / Objet de la note" mapping="motif" required span={2}>
          <FF_a.Textarea rows={3} placeholder="Mission Lagos · Africa Mobile Summit 22-25 mai"/>
        </FF_a.Field>
      </FF_a.Section>
    </FF_a.FormShell>
  );
}

/* ────────────────────────────────────────────────
   11. AddExpenseLine → AddExpenseLineCommand
   expenseReportId, description, montant, categorie, justificatifFileId
   ──────────────────────────────────────────────── */
function FormAddExpenseLine({ onBack }) {
  return (
    <FF_a.FormShell
      mapping="AddExpenseLineCommand"
      title="Ajouter une ligne de dépense"
      subtitle="Ligne individuelle rattachée à une note de frais existante"
      meta="POST /api/hrm/expense-reports/{id}/lines"
      onCancel={onBack}
      submitLabel="Ajouter la ligne"
      sidebar={
        <div className="card card-pad">
          <div style={{fontSize: 13, fontWeight: 700, marginBottom: 10}}>Note rattachée</div>
          <div style={{padding: 12, background: 'var(--bg-dim)', borderRadius: 10}}>
            <div className="mono" style={{fontSize: 11, color: 'var(--ink-3)'}}>NF-2026-0245</div>
            <div style={{fontSize: 13, fontWeight: 600, marginTop: 2}}>Mission Lagos · Africa Mobile Summit</div>
            <div style={{fontSize: 11, color: 'var(--ink-3)', marginTop: 4}}>Total actuel · <b className="tabular" style={{color: 'var(--ink-2)'}}>363 500 XAF</b> · 3 lignes</div>
          </div>
        </div>
      }
    >
      <FF_a.Section>
        <FF_a.Field label="Note de frais" mapping="expenseReportId" required span={2}>
          <FF_a.Picker icon={<Ic_a.expense size={14}/>} value="NF-2026-0245 — Mission Lagos" sub="Aminata Diallo · brouillon · 3 lignes"/>
        </FF_a.Field>
        <FF_a.Field label="Description" mapping="description" required span={2}>
          <FF_a.Input placeholder="Ex. Vol Yaoundé → Lagos (aller)"/>
        </FF_a.Field>
        <FF_a.Field label="Catégorie" mapping="categorie" required>
          <FF_a.Select>
            <option>Transport</option>
            <option>Hébergement</option>
            <option>Repas</option>
            <option>Matériel</option>
            <option>Conférence / formation</option>
            <option>Autre</option>
          </FF_a.Select>
        </FF_a.Field>
        <FF_a.Field label="Montant TTC" mapping="montant" required>
          <FF_a.Input type="number" suffix="XAF" placeholder="42 500"/>
        </FF_a.Field>
        <FF_a.Field label="Justificatif scanné" mapping="justificatifFileId" required hint="Reçu / facture · UUID FilePort · OCR appliqué automatiquement" span={2}>
          <FF_a.FileDrop accept="PDF, JPG, PNG, HEIC" maxMb={10}/>
        </FF_a.Field>
      </FF_a.Section>
    </FF_a.FormShell>
  );
}

/* ────────────────────────────────────────────────
   12. CreateMissionOrder → CreateMissionOrderCommand
   employeeId, destination, objet, dateDebut, dateFin, montantAvance, centreCout
   ──────────────────────────────────────────────── */
function FormCreateMissionOrder({ onBack }) {
  return (
    <FF_a.FormShell
      mapping="CreateMissionOrderCommand"
      title="Nouvel ordre de mission"
      subtitle="Déplacement professionnel · avance versée avant départ"
      meta="POST /api/hrm/mission-orders"
      onCancel={onBack}
      submitLabel="Créer l'ordre"
      sidebar={
        <div className="card card-pad" style={{background: 'linear-gradient(135deg, var(--orange-50) 0%, #fff 100%)'}}>
          <div style={{fontSize: 11, fontWeight: 700, color: 'var(--orange-700)', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 8}}>📅 Cycle</div>
          <div style={{fontSize: 12.5, color: 'var(--ink-2)'}}>
            1. Création OM<br/>
            2. Validation manager + DRH<br/>
            3. Versement avance<br/>
            4. Mission effectuée<br/>
            5. Note de frais en retour avec justificatifs
          </div>
        </div>
      }
    >
      <FF_a.Section title="Voyageur">
        <FF_a.Field label="Employé" mapping="employeeId" required span={2}>
          <FF_a.EmployeePicker employee={{name: 'Aminata Diallo', id: 'EMP-0142', role: 'Lead Mobile Engineer', color: 'orange'}}/>
        </FF_a.Field>
      </FF_a.Section>

      <FF_a.Section title="Destination & objet">
        <FF_a.Field label="Destination" mapping="destination" required hint="Ville, pays">
          <FF_a.Input icon={<Ic_a.mission size={14}/>} placeholder="Lagos, Nigeria"/>
        </FF_a.Field>
        <FF_a.Field label="Centre de coût" mapping="centreCout" required hint="Pour imputation comptable">
          <FF_a.Select>
            <option>CC-ENG-MOB · Engineering Mobile</option>
            <option>CC-COM-DOA · Commercial Douala</option>
            <option>CC-OPS-FLD · Field Operations</option>
            <option>CC-RH-GEN · Ressources Humaines</option>
          </FF_a.Select>
        </FF_a.Field>
        <FF_a.Field label="Objet de la mission" mapping="objet" required span={2}>
          <FF_a.Textarea rows={3} placeholder="Participation Africa Mobile Summit · networking partenaires écosystème mobile en Afrique de l'Ouest"/>
        </FF_a.Field>
      </FF_a.Section>

      <FF_a.Section title="Période & avance">
        <FF_a.Field label="Date de départ" mapping="dateDebut" required>
          <FF_a.Input type="date" defaultValue="2026-05-22"/>
        </FF_a.Field>
        <FF_a.Field label="Date de retour" mapping="dateFin" required>
          <FF_a.Input type="date" defaultValue="2026-05-25"/>
        </FF_a.Field>
        <FF_a.Field label="Montant de l'avance" mapping="montantAvance" required span={2} hint="Estimation des frais à couvrir · sera régularisé par la note de frais retour">
          <FF_a.Input type="number" suffix="XAF" placeholder="920 000"/>
        </FF_a.Field>
      </FF_a.Section>
    </FF_a.FormShell>
  );
}

/* ────────────────────────────────────────────────
   13. CreateTimesheet → CreateTimesheetCommand
   employeeId, periode, heuresNormales, heuresSupplementaires,
   heuresNuit, heuresWeekend, absencesNonJustifiees
   ──────────────────────────────────────────────── */
function FormCreateTimesheet({ onBack }) {
  return (
    <FF_a.FormShell
      mapping="CreateTimesheetCommand"
      title="Saisir une feuille de temps"
      subtitle="Pour la période en cours · entrera dans le calcul de la paie"
      meta="POST /api/hrm/timesheets"
      onCancel={onBack}
      submitLabel="Soumettre la feuille"
      draftLabel="Brouillon"
      sidebar={
        <div className="card card-pad" style={{background: 'var(--bg-dim)'}}>
          <div style={{fontSize: 13, fontWeight: 700, marginBottom: 10}}>Impact sur la paie</div>
          <div style={{fontSize: 12, color: 'var(--ink-2)'}}>
            <b>HS</b> majorées à 125% (jusqu'à 8h/sem) puis 150%<br/>
            <b>Nuit</b> majoration 50%<br/>
            <b>Week-end</b> majoration 100%<br/>
            <b>Absences NJ</b> déduites du salaire au prorata
          </div>
        </div>
      }
    >
      <FF_a.Section title="Période">
        <FF_a.Field label="Employé" mapping="employeeId" required>
          <FF_a.EmployeePicker employee={{name: 'Aminata Diallo', id: 'EMP-0142', role: 'Lead Mobile Eng.', color: 'orange'}}/>
        </FF_a.Field>
        <FF_a.Field label="Période" mapping="periode" required hint="Format YYYY-MM">
          <FF_a.Input placeholder="2026-05" defaultValue="2026-05"/>
        </FF_a.Field>
      </FF_a.Section>

      <FF_a.Section title="Décompte horaire" sub="BigDecimal · 2 décimales acceptées">
        <FF_a.Field label="Heures normales" mapping="heuresNormales" required hint="Temps travail standard contractuel">
          <FF_a.Input type="number" step="0.25" suffix="h" placeholder="173.33" defaultValue="173.33"/>
        </FF_a.Field>
        <FF_a.Field label="Heures supplémentaires" mapping="heuresSupplementaires" hint="Au-delà de 40h/sem">
          <FF_a.Input type="number" step="0.25" suffix="h" defaultValue="12"/>
        </FF_a.Field>
        <FF_a.Field label="Heures de nuit" mapping="heuresNuit" hint="22h → 6h">
          <FF_a.Input type="number" step="0.25" suffix="h" defaultValue="0"/>
        </FF_a.Field>
        <FF_a.Field label="Heures de week-end" mapping="heuresWeekend" hint="Samedi / Dimanche">
          <FF_a.Input type="number" step="0.25" suffix="h" defaultValue="4"/>
        </FF_a.Field>
        <FF_a.Field label="Absences non justifiées" mapping="absencesNonJustifiees" hint="En heures · déduites du brut" span={2}>
          <FF_a.Input type="number" step="0.25" suffix="h" defaultValue="0"/>
        </FF_a.Field>
      </FF_a.Section>
    </FF_a.FormShell>
  );
}

window.FormSubmitLeave = FormSubmitLeave;
window.FormRequestLoanAdvance = FormRequestLoanAdvance;
window.FormCreateExpenseReport = FormCreateExpenseReport;
window.FormAddExpenseLine = FormAddExpenseLine;
window.FormCreateMissionOrder = FormCreateMissionOrder;
window.FormCreateTimesheet = FormCreateTimesheet;
