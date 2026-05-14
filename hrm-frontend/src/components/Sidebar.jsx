'use client';
import { usePathname, useRouter } from 'next/navigation';
import { Icons } from './Icons';
import Avatar from './Avatar';

const NAV = [
  { id: 'dashboard',   label: 'Tableau de bord',  icon: 'dashboard',   path: '/',             section: 'Pilotage' },
  { id: 'analytics',   label: 'Analytics RH',     icon: 'analytics',   path: '/analytics',    section: 'Pilotage' },
  { id: 'employees',   label: 'Employés',         icon: 'users',       path: '/employees',    section: 'Personnel', badge: 342 },
  { id: 'contracts',   label: 'Contrats',         icon: 'contract',    path: '/contracts',    section: 'Personnel' },
  { id: 'skills',      label: 'Compétences',      icon: 'skill',       path: '/skills',       section: 'Personnel' },
  { id: 'recruitment', label: 'Recrutement',      icon: 'recruit',     path: '/recruitment',  section: 'Personnel', badge: 12 },
  { id: 'time',        label: 'Temps & Présences',icon: 'time',        path: '/time',         section: 'Activité' },
  { id: 'leaves',      label: 'Congés',           icon: 'leave',       path: '/leaves',       section: 'Activité', badge: 7 },
  { id: 'missions',    label: 'Ordres de mission',icon: 'mission',     path: '/missions',     section: 'Activité' },
  { id: 'payroll',     label: 'Paie',             icon: 'payroll',     path: '/payroll',      section: 'Rémunération' },
  { id: 'loans',       label: 'Avances & Prêts',  icon: 'loan',        path: '/loans',        section: 'Rémunération' },
  { id: 'expenses',    label: 'Notes de frais',   icon: 'expense',     path: '/expenses',     section: 'Rémunération', badge: 4 },
  { id: 'reviews',     label: 'Évaluations',      icon: 'review',      path: '/reviews',      section: 'Développement' },
  { id: 'trainings',   label: 'Formations',       icon: 'training',    path: '/trainings',    section: 'Développement' },
  { id: 'budget',      label: 'Budget formation', icon: 'pieChart',    path: '/budget',       section: 'Développement' },
  { id: 'medical',     label: 'Suivi médical',    icon: 'medical',     path: '/medical',      section: 'Conformité' },
  { id: 'declarations',label: 'Déclarations',     icon: 'declaration', path: '/declarations', section: 'Conformité' },
  { id: 'settings',    label: 'Paramètres',       icon: 'settings',    path: '/settings',     section: 'Système' },
];

export default function Sidebar() {
  const pathname = usePathname();
  const router = useRouter();
  const sections = [...new Set(NAV.map(n => n.section))];

  const isActive = (path) => path === '/' ? pathname === '/' : pathname.startsWith(path);

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <div className="sidebar-logo">H</div>
        <div>
          <div className="sidebar-brand-name">HR Core</div>
          <div className="sidebar-brand-sub">RT-Comops</div>
        </div>
      </div>
      {sections.map(sec => (
        <div key={sec}>
          <div className="nav-section-label">{sec}</div>
          {NAV.filter(n => n.section === sec).map(n => {
            const I = Icons[n.icon];
            return (
              <button key={n.id} onClick={() => router.push(n.path)}
                className={"nav-item" + (isActive(n.path) ? " active" : "")}>
                {I && <I size={17} />}
                <span>{n.label}</span>
                {n.badge && <span className="nav-badge">{n.badge}</span>}
              </button>
            );
          })}
        </div>
      ))}
      <div className="sidebar-user">
        <Avatar name="Faïsal Sab" />
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ fontSize: 13, fontWeight: 600 }}>Faïsal Sab</div>
          <div style={{ fontSize: 11, color: 'var(--ink-3)' }}>Admin RH</div>
        </div>
        <button className="icon-btn" style={{ width: 30, height: 30 }}>
          <Icons.logout size={14} />
        </button>
      </div>
    </aside>
  );
}
