'use client';
import { useState } from 'react';
import { Icons } from '../../components/Icons';
import Avatar from '../../components/Avatar';
import PageHeader from '../../components/PageHeader';

const sections = [
  { id: 'company', label: 'Entreprise', icon: 'briefcase' },
  { id: 'org', label: 'Organisation', icon: 'users' },
  { id: 'payroll', label: 'Paramètres paie', icon: 'payroll' },
  { id: 'leaves', label: 'Politique de congés', icon: 'leave' },
  { id: 'workflows', label: "Workflows d'approbation", icon: 'shield' },
  { id: 'users', label: 'Utilisateurs & rôles', icon: 'shield' },
  { id: 'integrations', label: 'Intégrations', icon: 'link' },
  { id: 'notifications', label: 'Notifications', icon: 'bell' },
  { id: 'audit', label: "Journal d'audit", icon: 'doc' },
];

export default function Settings() {
  const [section, setSection] = useState('company');

  return (
    <div>
      <PageHeader
        uc="Système"
        title="Paramètres"
        subtitle="Configuration de l'entreprise, des rôles et des intégrations"
      />

      <div style={{ display: 'grid', gridTemplateColumns: '240px 1fr', gap: 20 }}>
        <div className="card" style={{ padding: 8, alignSelf: 'flex-start', position: 'sticky', top: 88 }}>
          {sections.map(s => {
            const I = Icons[s.icon] || Icons.settings;
            return (
              <button key={s.id} onClick={() => setSection(s.id)} style={{
                width: '100%', padding: '10px 12px', borderRadius: 10, textAlign: 'left',
                background: section === s.id ? 'var(--orange-50)' : 'transparent',
                color: section === s.id ? 'var(--orange-700)' : 'var(--ink-2)',
                fontWeight: section === s.id ? 600 : 500, fontSize: 13,
                display: 'flex', alignItems: 'center', gap: 10, marginBottom: 2
              }}>
                <I size={15} /> {s.label}
              </button>
            );
          })}
        </div>

        {section === 'company' && <CompanySettings />}
        {section === 'org' && <OrgSettings />}
        {section === 'payroll' && <PayrollSettings />}
        {section === 'leaves' && <LeaveSettings />}
        {section === 'workflows' && <WorkflowSettings />}
        {section === 'users' && <UserRolesSettings />}
        {section === 'integrations' && <IntegrationsSettings />}
        {section === 'notifications' && <NotificationsSettings />}
        {section === 'audit' && <AuditLog />}
      </div>
    </div>
  );
}

function CompanySettings() {
  return (
    <div className="col gap-3">
      <div className="card">
        <div className="card-head">
          <div className="card-title">Informations légales</div>
          <button className="btn btn-primary btn-sm"><Icons.edit size={13} /> Modifier</button>
        </div>
        <div style={{ padding: '18px 22px' }}>
          <div className="row gap-4" style={{ marginBottom: 24 }}>
            <div style={{ width: 80, height: 80, borderRadius: 16, background: 'var(--grad-orange)', display: 'grid', placeItems: 'center', color: '#fff', fontWeight: 800, fontSize: 36, fontFamily: 'Inter Tight' }}>R</div>
            <div>
              <div style={{ fontSize: 18, fontWeight: 700, fontFamily: 'Inter Tight' }}>RT-Comops SARL</div>
              <div style={{ fontSize: 13, color: 'var(--ink-3)' }}>Société à responsabilité limitée · 342 collaborateurs · 6 sites</div>
              <div className="row gap-2" style={{ marginTop: 6 }}>
                <span className="badge green">Active</span>
                <span className="badge orange">Conformité 100%</span>
              </div>
            </div>
          </div>
          <div className="grid-3" style={{ gap: 18 }}>
            {[
              ['Raison sociale', 'RT-Comops SARL'],
              ['Forme juridique', 'SARL au capital de 50 000 000 XAF'],
              ['RCCM', 'RCCM/Y/2018/B/12345'],
              ['NIU', 'M122001234567'],
              ['Adresse siège', 'BP 1234 Yaoundé · Quartier Bastos'],
              ['Téléphone', '+237 6 99 12 34 56'],
              ['Email contact', 'contact@rt-comops.com'],
              ['Site web', 'www.rt-comops.com'],
              ['N° CNPS employeur', 'EMP-23456789'],
            ].map(([l, v], i) => (
              <div key={i}>
                <div style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--ink-3)', fontWeight: 600 }}>{l}</div>
                <div style={{ fontSize: 13, marginTop: 3 }}>{v}</div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-head">
          <div className="card-title">Sites & établissements</div>
          <button className="btn btn-secondary btn-sm"><Icons.plus size={13} /> Ajouter un site</button>
        </div>
        <table className="t">
          <thead><tr><th>Site</th><th>Adresse</th><th>Type</th><th>Effectif</th><th>Manager</th><th></th></tr></thead>
          <tbody>
            {[
              { s: 'Yaoundé HQ', a: 'BP 1234 · Bastos', t: 'Siège', e: 182, m: 'Faïsal Sab' },
              { s: 'Douala', a: 'Akwa Bonanjo · 5e étage', t: 'Agence', e: 84, m: 'Hervé Tankeu' },
              { s: 'Bafoussam', a: 'Marché B', t: 'Antenne', e: 42, m: 'Karine Djoumessi' },
              { s: 'Garoua', a: 'Plateau', t: 'Antenne', e: 24, m: 'Patrick Mbo' },
              { s: 'Bertoua', a: 'Centre-ville', t: 'Antenne', e: 10, m: '—' },
            ].map((r, i) => (
              <tr key={i}>
                <td style={{ fontSize: 13, fontWeight: 600 }}>{r.s}</td>
                <td className="muted">{r.a}</td>
                <td><span className="tag">{r.t}</span></td>
                <td className="tabular">{r.e}</td>
                <td>{r.m}</td>
                <td><button className="icon-btn" style={{ width: 28, height: 28 }}><Icons.edit size={12} /></button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function NodeCard({ name, role, color, size }) {
  return (
    <div style={{
      padding: size === 'lg' ? '14px 18px' : '10px 14px',
      background: '#fff', borderRadius: 14, border: '1px solid var(--line)', boxShadow: 'var(--shadow-card)',
      display: 'flex', alignItems: 'center', gap: 10, minWidth: 160
    }}>
      <Avatar name={name} color={color} size={size} />
      <div>
        <div style={{ fontSize: size === 'lg' ? 14 : 12.5, fontWeight: 700 }}>{name}</div>
        <div style={{ fontSize: 11, color: 'var(--ink-3)' }}>{role}</div>
      </div>
    </div>
  );
}

function OrgSettings() {
  return (
    <div className="card">
      <div className="card-head"><div className="card-title">Organigramme</div></div>
      <div style={{ padding: 30, background: 'linear-gradient(180deg, #FFFAF2, #fff)' }}>
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 24 }}>
          <NodeCard name="Faïsal Sab" role="CEO · DRH" color="orange" size="lg" />
          <div style={{ width: 2, height: 24, background: 'var(--line-strong)' }} />
          <div style={{ display: 'flex', gap: 24, alignItems: 'flex-start' }}>
            {[
              { n: 'Marc Foga', r: 'Dir. Technique', c: 'green', kids: ['Engineering 124', 'IT/Infra 12'] },
              { n: 'Hervé Tankeu', r: 'Dir. Commercial', c: 'blue', kids: ['Sales 36', 'BD 16'] },
              { n: 'Estelle Bilong', r: 'Dir. Financière', c: 'amber', kids: ['Compta 12', 'Paie 8'] },
              { n: 'Diane Tsoumou', r: 'Dir. Opérations', c: 'violet', kids: ['Field Ops 52', 'Support 26'] },
            ].map((x, i) => (
              <div key={i} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 14 }}>
                <div style={{ width: 2, height: 24, background: 'var(--line-strong)', marginTop: -24 }} />
                <NodeCard name={x.n} role={x.r} color={x.c} />
                <div style={{ display: 'flex', flexDirection: 'column', gap: 6, marginTop: 8 }}>
                  {x.kids.map((k, j) => (
                    <div key={j} style={{ padding: '6px 12px', background: '#fff', border: '1px solid var(--line)', borderRadius: 8, fontSize: 11, fontWeight: 600 }}>{k}</div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

function PayrollSettings() {
  return (
    <div className="col gap-3">
      <div className="card">
        <div className="card-head"><div className="card-title">Cotisations & taux applicables</div></div>
        <table className="t">
          <thead><tr><th>Cotisation</th><th>Base</th><th>Part salariale</th><th>Part patronale</th><th>Plafond</th></tr></thead>
          <tbody>
            {[
              ['CNPS · Vieillesse (PVID)', 'Salaire brut', '4,2%', '4,2%', '750 000 XAF'],
              ['CNPS · Allocations familiales', 'Salaire brut', '—', '7,0%', '750 000 XAF'],
              ['CNPS · Accidents travail', 'Salaire brut', '—', '1,75%', '—'],
              ['IRPP (barème progressif)', 'Salaire imposable', 'Barème', '—', '—'],
              ['CAC · Centimes addit. communaux', 'Sur IRPP', '10%', '—', '—'],
              ['CFC · Crédit Foncier', 'Salaire brut', '1,0%', '1,5%', '—'],
              ['Redevance audiovisuelle', 'Fixe', '750–13 000 XAF', '—', '—'],
              ['FNE (Fonds Emploi)', 'Salaire brut', '—', '1,0%', '—'],
            ].map((r, i) => (
              <tr key={i}>
                <td style={{ fontSize: 13, fontWeight: 600 }}>{r[0]}</td>
                <td className="muted">{r[1]}</td>
                <td className="tabular">{r[2]}</td>
                <td className="tabular">{r[3]}</td>
                <td className="tabular muted">{r[4]}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="card">
        <div className="card-head"><div className="card-title">Calendrier de paie</div></div>
        <div className="grid-3" style={{ padding: 18, gap: 14 }}>
          {[
            ['Fréquence', 'Mensuelle'],
            ['Date de cut-off', '25 du mois'],
            ['Date de paiement', 'Dernier jour ouvré'],
            ['Devise', 'XAF (Franc CFA)'],
            ['Approbation', 'DRH → DG'],
            ['Canal de paiement', 'Virement bancaire + Mobile Money'],
          ].map(([l, v], i) => (
            <div key={i} className="field">
              <label>{l}</label>
              <input className="input" defaultValue={v} />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function LeaveSettings() {
  return (
    <div className="card">
      <div className="card-head"><div className="card-title">Types de congés & accruals</div></div>
      <table className="t">
        <thead><tr><th>Type</th><th>Acquisition</th><th>Plafond annuel</th><th>Délai prévis.</th><th>Justif. requis</th><th>Reporté ?</th></tr></thead>
        <tbody>
          {[
            ['Congé annuel payé', '1,5 j / mois', '18 j (+ majorations)', '15 jours', 'Non', '50%'],
            ['Maladie', '—', 'Selon CMA', '24h', 'Certificat médical', 'Non'],
            ['Maternité', '—', '98 jours', '3 mois avant', 'Certificat', '—'],
            ['Paternité', '—', '3 jours', '—', 'Acte naissance', '—'],
            ['Mariage', '—', '4 jours', '—', 'Acte mariage', '—'],
            ['Décès parent', '—', '3 jours', '—', 'Acte décès', '—'],
            ['Sans solde', '—', '15 jours', '7 jours', 'Lettre', '—'],
          ].map((r, i) => (
            <tr key={i}>
              <td style={{ fontSize: 13, fontWeight: 600 }}>{r[0]}</td>
              <td>{r[1]}</td><td className="tabular">{r[2]}</td><td>{r[3]}</td><td>{r[4]}</td><td>{r[5]}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function WorkflowSettings() {
  const workflows = [
    { name: 'Demande de congé', steps: ['Manager direct', 'DRH'], levels: 2 },
    { name: 'Note de frais', steps: ['Manager direct', 'Contrôle gestion', 'DRH'], levels: 3 },
    { name: 'Avance / prêt', steps: ['DRH', 'Direction financière', 'DG (>1M XAF)'], levels: 3 },
    { name: 'Ordre de mission', steps: ['Manager direct', 'DRH'], levels: 2 },
    { name: 'Recrutement', steps: ['RH', 'Manager hiring', 'DG (cadre)'], levels: 3 },
  ];
  return (
    <div className="col gap-3">
      {workflows.map((w, i) => (
        <div key={i} className="card card-pad">
          <div className="row" style={{ marginBottom: 12 }}>
            <div className="card-title" style={{ fontSize: 14 }}>{w.name}</div>
            <span className="badge gray" style={{ marginLeft: 8 }}>{w.levels} niveaux</span>
            <button className="btn btn-ghost btn-sm" style={{ marginLeft: 'auto' }}><Icons.edit size={13} /></button>
          </div>
          <div className="stepper">
            {w.steps.map((s, j) => (
              <span key={j} style={{ display: 'inline-flex', alignItems: 'center', gap: 8 }}>
                <span className="step done">{s}</span>
                {j < w.steps.length - 1 && <span className="step-arrow">→</span>}
              </span>
            ))}
            <span className="step-arrow">→</span>
            <span className="step" style={{ background: 'var(--green-50)', color: 'var(--green-600)', borderColor: 'transparent' }}>Validé</span>
          </div>
        </div>
      ))}
    </div>
  );
}

function UserRolesSettings() {
  const users = [
    { n: 'Faïsal Sab', c: 'orange', e: 'f.sab@rt-comops.com', r: 'Super admin', p: 'Toutes (RWX)', last: 'il y a 5 min' },
    { n: 'Marc Foga', c: 'green', e: 'm.foga@rt-comops.com', r: 'Manager', p: 'Engineering · R+W', last: 'il y a 2h' },
    { n: 'Estelle Bilong', c: 'amber', e: 'e.bilong@rt-comops.com', r: 'Paie', p: 'Paie + Finance · R+W', last: 'il y a 1j' },
    { n: 'Yannick Etoa', c: 'orange', e: 'y.etoa@rt-comops.com', r: 'Recruteur', p: 'Recrutement · R+W', last: 'il y a 3h' },
    { n: 'Diane Tsoumou', c: 'violet', e: 'd.tsoumou@rt-comops.com', r: 'Manager', p: 'Ops + Support · R+W', last: 'il y a 12h' },
  ];
  return (
    <div className="card">
      <div className="card-head">
        <div className="card-title">Utilisateurs & rôles</div>
        <button className="btn btn-primary btn-sm"><Icons.plus size={13} /> Inviter</button>
      </div>
      <table className="t">
        <thead><tr><th>Utilisateur</th><th>Email</th><th>Rôle</th><th>Permissions</th><th>Dernière connexion</th><th>Statut</th></tr></thead>
        <tbody>
          {users.map((u, i) => (
            <tr key={i}>
              <td><div className="row gap-2"><Avatar name={u.n} color={u.c} size="sm" /><span style={{ fontSize: 13, fontWeight: 600 }}>{u.n}</span></div></td>
              <td className="muted">{u.e}</td>
              <td><span className={"badge " + (u.r === 'Super admin' ? 'orange' : u.r === 'Manager' ? 'blue' : u.r === 'Paie' ? 'green' : 'violet')}>{u.r}</span></td>
              <td className="muted" style={{ fontSize: 12 }}>{u.p}</td>
              <td className="muted" style={{ fontSize: 12 }}>{u.last}</td>
              <td><span className="badge green">Actif</span></td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function IntegrationsSettings() {
  const integ = [
    { n: 'CNPS · Téléservices', d: 'Soumission DSN & paiement cotisations', status: 'connected', color: 'orange', logo: 'C' },
    { n: 'Afriland First Bank', d: 'Virements bancaires automatisés', status: 'connected', color: 'green', logo: 'A' },
    { n: 'MTN Mobile Money', d: 'Paiement salaires & remboursements', status: 'connected', color: 'amber', logo: 'M' },
    { n: 'Orange Money', d: 'Paiement salaires & remboursements', status: 'connected', color: 'orange', logo: 'O' },
    { n: 'Microsoft 365 / Outlook', d: 'SSO + agenda + emails RH', status: 'connected', color: 'blue', logo: 'M' },
    { n: 'Slack', d: 'Notifications RH dans #people', status: 'disconnected', color: 'violet', logo: 'S' },
    { n: 'LinkedIn Talent', d: 'Sourcing candidats & publication offres', status: 'connected', color: 'blue', logo: 'L' },
    { n: 'DGI · Téléservices', d: 'IRPP et autres déclarations fiscales', status: 'connected', color: 'red', logo: 'D' },
  ];
  return (
    <div className="grid-2" style={{ gap: 14 }}>
      {integ.map((x, i) => (
        <div key={i} className="card card-pad">
          <div className="row" style={{ marginBottom: 10 }}>
            <div className={"icon-tile " + x.color} style={{ width: 42, height: 42, fontFamily: 'Inter Tight', fontWeight: 800, fontSize: 18 }}>{x.logo}</div>
            <div style={{ flex: 1, marginLeft: 12 }}>
              <div style={{ fontSize: 14, fontWeight: 700 }}>{x.n}</div>
              <div style={{ fontSize: 11.5, color: 'var(--ink-3)' }}>{x.d}</div>
            </div>
            {x.status === 'connected' ? <span className="badge green">Connecté</span> : <span className="badge gray">Déconnecté</span>}
          </div>
          <div className="row gap-2" style={{ marginTop: 8 }}>
            <button className="btn btn-secondary btn-sm">Configurer</button>
            {x.status === 'connected' ? <button className="btn btn-ghost btn-sm">Tester</button> : <button className="btn btn-primary btn-sm">Connecter</button>}
          </div>
        </div>
      ))}
    </div>
  );
}

function NotificationsSettings() {
  return (
    <div className="card">
      <div className="card-head"><div className="card-title">Préférences de notifications</div></div>
      <table className="t">
        <thead><tr><th>Évènement</th><th>Email</th><th>Mobile</th><th>Slack</th><th>SMS</th></tr></thead>
        <tbody>
          {[
            ['Nouvelle demande de congé', '✓', '✓', '✓', '—'],
            ['Note de frais à approuver', '✓', '✓', '✓', '—'],
            ['Bulletin de paie disponible', '✓', '✓', '—', '✓'],
            ['Échéance contrat / essai', '✓', '—', '✓', '—'],
            ['Visite médicale à planifier', '✓', '—', '—', '—'],
            ['Déclaration sociale due', '✓', '—', '✓', '✓'],
            ['Onboarding employé', '✓', '✓', '✓', '—'],
          ].map((r, i) => (
            <tr key={i}>
              <td style={{ fontSize: 13, fontWeight: 600 }}>{r[0]}</td>
              {[1, 2, 3, 4].map(j => (
                <td key={j}>
                  {r[j] === '✓' ? <span className="badge green">Activé</span> :
                   r[j] === '—' ? <span className="muted">—</span> : ''}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function AuditLog() {
  const logs = [
    { d: '14/05 16:42', u: 'Faïsal Sab', c: 'orange', a: 'Approbation', r: 'NF-2026-0245', ip: '196.43.x.x' },
    { d: '14/05 14:18', u: 'Estelle Bilong', c: 'amber', a: 'Run paie', r: 'PR-2026-05 · Calcul', ip: '196.43.x.x' },
    { d: '14/05 11:32', u: 'Yannick Etoa', c: 'orange', a: 'Création offre', r: 'JO-2026-010', ip: '196.42.x.x' },
    { d: '14/05 09:14', u: 'Marc Foga', c: 'green', a: 'Approbation', r: 'LR-0419', ip: '192.168.x.x' },
    { d: '13/05 17:54', u: 'Faïsal Sab', c: 'orange', a: 'Modification', r: 'EMP-0142 · Salaire', ip: '196.43.x.x' },
    { d: '13/05 15:22', u: 'Système', c: 'gray', a: 'Génération', r: 'Bulletins mai (×342)', ip: '—' },
  ];
  return (
    <div className="card">
      <div className="card-head">
        <div className="card-title">Journal d'audit</div>
        <span className="muted" style={{ fontSize: 12 }}>Toutes les actions sont tracées · conservation 7 ans</span>
      </div>
      <table className="t">
        <thead><tr><th>Date</th><th>Utilisateur</th><th>Action</th><th>Ressource</th><th>IP</th></tr></thead>
        <tbody>
          {logs.map((l, i) => (
            <tr key={i}>
              <td className="mono muted" style={{ fontSize: 11.5 }}>{l.d}</td>
              <td>
                <div className="row gap-2">
                  {l.u !== 'Système' ? <Avatar name={l.u} color={l.c} size="sm" /> : <div className="icon-tile gray" style={{ width: 26, height: 26 }}><Icons.shield size={12} /></div>}
                  <span style={{ fontSize: 13 }}>{l.u}</span>
                </div>
              </td>
              <td><span className={"badge " + (l.a === 'Approbation' ? 'green' : l.a === 'Modification' ? 'amber' : l.a === 'Création offre' ? 'orange' : 'blue')}>{l.a}</span></td>
              <td className="mono" style={{ fontSize: 11.5 }}>{l.r}</td>
              <td className="muted mono" style={{ fontSize: 11.5 }}>{l.ip}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
