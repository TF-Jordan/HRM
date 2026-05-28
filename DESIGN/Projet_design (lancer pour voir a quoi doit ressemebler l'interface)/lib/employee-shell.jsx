/* global React */
const { useState: useS_eshell } = React;

const EMP_ME = {
  id: 'EMP-0142',
  name: 'Aminata Diallo',
  firstname: 'Aminata',
  role: 'Lead Mobile Engineer',
  dept: 'Engineering',
  site: 'Yaoundé HQ',
  manager: 'Marc Foga',
  startDate: '14 fév 2022',
  tenure: '4a 3m',
  avatar: 'orange',
  email: 'a.diallo@rt-comops.com',
  phone: '+237 6 78 12 34 56',
  cnps: '110428937H',
  balance: { annual: 18.5, sick: 2, rtt: 6 },
  salary: { gross: 1245000, net: 1066960 },
};
window.EMP_ME = EMP_ME;

const EMP_NAV = [
  { id: 'home',       label: 'Accueil',          icon: 'dashboard', section: 'Espace personnel' },
  { id: 'profile',    label: 'Mon profil',       icon: 'users',     section: 'Espace personnel' },
  { id: 'documents',  label: 'Mes documents',    icon: 'doc',       section: 'Espace personnel' },

  { id: 'payslips',   label: 'Ma paie',          icon: 'payroll',   section: 'Rémunération' },
  { id: 'expenses',   label: 'Mes notes de frais', icon: 'expense', section: 'Rémunération', badge: 1 },
  { id: 'loans',      label: 'Avances & prêts',  icon: 'loan',      section: 'Rémunération' },

  { id: 'leaves',     label: 'Mes congés',       icon: 'leave',     section: 'Activité' },
  { id: 'time',       label: 'Mon temps',        icon: 'time',      section: 'Activité' },
  { id: 'missions',   label: 'Mes missions',     icon: 'mission',   section: 'Activité' },

  { id: 'trainings',  label: 'Mes formations',   icon: 'training',  section: 'Développement' },
  { id: 'reviews',    label: 'Mes évaluations',  icon: 'review',    section: 'Développement' },
  { id: 'skills',     label: 'Mes compétences',  icon: 'skill',     section: 'Développement' },

  { id: 'medical',    label: 'Suivi médical',    icon: 'medical',   section: 'Santé' },

  { id: 'directory',  label: 'Annuaire',         icon: 'briefcase', section: 'Entreprise' },
  { id: 'inbox',      label: 'Notifications',    icon: 'bell',      section: 'Entreprise', badge: 4 },
];
window.EMP_NAV = EMP_NAV;

function EmpSidebar({ active, onNav }) {
  const { Icons, Avatar } = window;
  const sections = [...new Set(EMP_NAV.map(n => n.section))];
  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <div className="sidebar-logo">H</div>
        <div>
          <div className="sidebar-brand-name">HR Core</div>
          <div className="sidebar-brand-sub">Mon espace</div>
        </div>
      </div>

      {/* Mini profile chip */}
      <div style={{
        padding:'12px', borderRadius:14,
        background:'linear-gradient(135deg, var(--orange-50) 0%, #fff 100%)',
        border:'1px solid var(--orange-100)', marginBottom:14,
        display:'flex', gap:10, alignItems:'center'
      }}>
        <Avatar name={EMP_ME.name} color={EMP_ME.avatar}/>
        <div style={{flex:1, minWidth:0}}>
          <div style={{fontSize:13, fontWeight:700, whiteSpace:'nowrap', overflow:'hidden', textOverflow:'ellipsis'}}>{EMP_ME.firstname} 👋</div>
          <div style={{fontSize:11, color:'var(--ink-3)'}} className="mono">{EMP_ME.id}</div>
        </div>
      </div>

      {sections.map(sec => (
        <React.Fragment key={sec}>
          <div className="nav-section-label">{sec}</div>
          {EMP_NAV.filter(n => n.section === sec).map(n => {
            const I = Icons[n.icon];
            return (
              <button key={n.id} onClick={() => onNav(n.id)}
                className={"nav-item" + (active === n.id ? " active" : "")}>
                <I size={17}/>
                <span>{n.label}</span>
                {n.badge && <span className="nav-badge">{n.badge}</span>}
              </button>
            );
          })}
        </React.Fragment>
      ))}

      <div className="sidebar-user">
        <Avatar name={EMP_ME.name} color={EMP_ME.avatar}/>
        <div style={{flex:1, minWidth:0}}>
          <div style={{fontSize:13, fontWeight:600, whiteSpace:'nowrap', overflow:'hidden', textOverflow:'ellipsis'}}>{EMP_ME.name}</div>
          <div style={{fontSize:11, color:'var(--ink-3)'}}>{EMP_ME.role}</div>
        </div>
        <button className="icon-btn" style={{width:30, height:30}} title="Déconnexion">
          <Icons.logout size={14}/>
        </button>
      </div>
    </aside>
  );
}
window.EmpSidebar = EmpSidebar;

function EmpTopbar({ goto }) {
  const { Icons, Avatar } = window;
  return (
    <div className="topbar">
      <div className="search">
        <Icons.search size={16}/>
        <input placeholder="Rechercher un collègue, un bulletin, une formation…"/>
        <kbd style={{fontSize:11, color:'var(--ink-3)', background:'var(--bg-soft)', padding:'2px 6px', borderRadius:6, border:'1px solid var(--line)'}}>⌘K</kbd>
      </div>
      <div className="row gap-2" style={{marginLeft:'auto'}}>
        <button className="btn btn-secondary btn-sm">
          <Icons.plus size={13}/> Nouvelle demande
        </button>
        <button className="icon-btn" title="Aide"><Icons.info size={16}/></button>
        <button className="icon-btn" onClick={() => goto('inbox')} title="Notifications"><Icons.bell size={16}/><span className="dot"/></button>
        <div className="user-chip">
          <div style={{textAlign:'right'}}>
            <div className="user-chip-name">{EMP_ME.name}</div>
            <div className="user-chip-role">{EMP_ME.role} · {EMP_ME.id}</div>
          </div>
          <Avatar name={EMP_ME.name} color={EMP_ME.avatar}/>
        </div>
      </div>
    </div>
  );
}
window.EmpTopbar = EmpTopbar;
