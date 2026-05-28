/* global React */
const { useState } = React;

/* Icons */
const Icons = {
  dashboard: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="3" width="7" height="9" rx="1.5"/><rect x="14" y="3" width="7" height="5" rx="1.5"/><rect x="14" y="12" width="7" height="9" rx="1.5"/><rect x="3" y="16" width="7" height="5" rx="1.5"/></svg>,
  users: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><circle cx="9" cy="8" r="3.2"/><path d="M3 20c0-3.3 2.7-6 6-6s6 2.7 6 6"/><circle cx="17" cy="9" r="2.5"/><path d="M21 19c0-2.5-2-4.5-4-4.5"/></svg>,
  contract: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M14 3H7a2 2 0 00-2 2v14a2 2 0 002 2h10a2 2 0 002-2V8z"/><path d="M14 3v5h5M9 13h6M9 17h4"/></svg>,
  payroll: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="6" width="18" height="13" rx="2"/><circle cx="12" cy="12.5" r="2.6"/></svg>,
  leave: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M3 9h18M8 3v4M16 3v4"/></svg>,
  loan: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M2 9l10-6 10 6-10 6z"/><path d="M6 12v5c0 1 3 3 6 3s6-2 6-3v-5"/></svg>,
  training: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M22 9L12 4 2 9l10 5 10-5z"/><path d="M6 11.5v4c0 1.5 3 3 6 3s6-1.5 6-3v-4"/></svg>,
  review: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="9"/><circle cx="12" cy="12" r="5"/><circle cx="12" cy="12" r="1.5" fill="currentColor"/></svg>,
  recruit: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="6" width="18" height="14" rx="2"/><path d="M8 6V4a2 2 0 012-2h4a2 2 0 012 2v2M3 12h18"/></svg>,
  time: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg>,
  expense: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M5 3h11l3 3v15a1 1 0 01-1 1H5a1 1 0 01-1-1V4a1 1 0 011-1z"/><path d="M16 3v3h3M9 13h2.5a1.5 1.5 0 100-3H10a1.5 1.5 0 110-3h2.5"/></svg>,
  medical: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M12 21s-7-4.5-7-11a4 4 0 017-2.6A4 4 0 0119 10c0 6.5-7 11-7 11z"/><path d="M9 11h6M12 8v6"/></svg>,
  analytics: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M3 21V8M9 21V13M15 21v-9M21 21V4"/></svg>,
  declaration: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M5 3h14a1 1 0 011 1v17l-4-3-4 3-4-3-4 3V4a1 1 0 011-1z"/><path d="M8 8h8M8 12h6"/></svg>,
  mission: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M21 10c0 7-9 12-9 12S3 17 3 10a9 9 0 0118 0z"/><circle cx="12" cy="10" r="2.8"/></svg>,
  skill: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M12 2l2.4 5 5.6.5-4.2 3.8 1.3 5.7L12 14l-5.1 3 1.3-5.7L4 7.5 9.6 7z"/></svg>,
  settings: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 00.3 1.8l.1.1a2 2 0 11-2.9 2.9l-.1-.1a1.7 1.7 0 00-1.8-.3 1.7 1.7 0 00-1 1.5V21a2 2 0 11-4 0v-.1a1.7 1.7 0 00-1.1-1.5 1.7 1.7 0 00-1.8.3l-.1.1a2 2 0 11-2.9-2.9l.1-.1a1.7 1.7 0 00.3-1.8 1.7 1.7 0 00-1.5-1H3a2 2 0 110-4h.1a1.7 1.7 0 001.5-1.1 1.7 1.7 0 00-.3-1.8l-.1-.1a2 2 0 112.9-2.9l.1.1a1.7 1.7 0 001.8.3H9a1.7 1.7 0 001-1.5V3a2 2 0 114 0v.1a1.7 1.7 0 001 1.5 1.7 1.7 0 001.8-.3l.1-.1a2 2 0 112.9 2.9l-.1.1a1.7 1.7 0 00-.3 1.8V9a1.7 1.7 0 001.5 1H21a2 2 0 110 4h-.1a1.7 1.7 0 00-1.5 1z"/></svg>,
  search: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/></svg>,
  bell: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M6 8a6 6 0 0112 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10 21a2 2 0 004 0"/></svg>,
  logout: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M15 3h4a2 2 0 012 2v14a2 2 0 01-2 2h-4M10 17l5-5-5-5M15 12H3"/></svg>,
  plus: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M12 5v14M5 12h14"/></svg>,
  filter: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M3 5h18M6 12h12M10 19h4"/></svg>,
  download: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M12 3v12M7 10l5 5 5-5M5 21h14"/></svg>,
  upload: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M12 17V5M7 10l5-5 5 5M5 21h14"/></svg>,
  edit: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 113 3L7 19l-4 1 1-4z"/></svg>,
  more: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor"><circle cx="5" cy="12" r="1.5" fill="currentColor"/><circle cx="12" cy="12" r="1.5" fill="currentColor"/><circle cx="19" cy="12" r="1.5" fill="currentColor"/></svg>,
  chevR: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M9 5l7 7-7 7"/></svg>,
  chevD: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M5 9l7 7 7-7"/></svg>,
  check: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M5 12l4 4L19 7"/></svg>,
  x: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M6 6l12 12M18 6L6 18"/></svg>,
  trendUp: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M3 17l6-6 4 4 8-8M14 7h7v7"/></svg>,
  trendDown: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M3 7l6 6 4-4 8 8M14 17h7v-7"/></svg>,
  alert: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="9"/><path d="M12 8v5M12 16v.5"/></svg>,
  info: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="9"/><path d="M12 11v5M12 8v.5"/></svg>,
  cal: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M3 9h18M8 3v4M16 3v4"/></svg>,
  doc: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M14 3H7a2 2 0 00-2 2v14a2 2 0 002 2h10a2 2 0 002-2V8z"/><path d="M14 3v5h5"/></svg>,
  pieChart: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M12 2v10l9 3a10 10 0 11-9-13z"/></svg>,
  star: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l2.4 5 5.6.5-4.2 3.8 1.3 5.7L12 14l-5.1 3 1.3-5.7L4 7.5 9.6 7z"/></svg>,
  briefcase: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="7" width="18" height="13" rx="2"/><path d="M9 7V5a2 2 0 012-2h2a2 2 0 012 2v2M3 13h18"/></svg>,
  send: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M22 2L11 13M22 2l-7 20-4-9-9-4z"/></svg>,
  shield: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>,
  badge: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="8" r="4"/><path d="M5 21l3-7 4 2 4-2 3 7"/></svg>,
  print: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M6 9V3h12v6M6 18h-1a2 2 0 01-2-2v-5a2 2 0 012-2h14a2 2 0 012 2v5a2 2 0 01-2 2h-1"/><rect x="6" y="14" width="12" height="7"/></svg>,
  rocket: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M14 4c4 0 6 2 6 6 0 0-2 6-9 9l-3-3c3-7 6-9 6-9z"/><circle cx="14" cy="9" r="1.5" fill="currentColor"/><path d="M7 14l-4 1 1-4M9 19l-2 1 1-2"/></svg>,
  link: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M10 13a5 5 0 007 0l3-3a5 5 0 00-7-7l-1 1"/><path d="M14 11a5 5 0 00-7 0l-3 3a5 5 0 007 7l1-1"/></svg>,
  globe: (p={}) => <svg width={p.size||18} height={p.size||18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="9"/><path d="M3 12h18M12 3a14 14 0 010 18M12 3a14 14 0 000 18"/></svg>,
};
window.Icons = Icons;

/* Avatar */
const COLORS = ['orange','blue','green','violet','amber','teal'];
function Avatar({name='', size, color}) {
  const i = (name.charCodeAt(0) || 0) % COLORS.length;
  const initials = name.split(' ').map(n=>n[0]).join('').slice(0,2).toUpperCase();
  return <div className={"avatar "+(size||'')+" "+(color||COLORS[i])}>{initials}</div>;
}
window.Avatar = Avatar;

/* Sidebar */
const NAV = [
  { id: 'dashboard', label: 'Tableau de bord', icon: 'dashboard', section: 'Pilotage' },
  { id: 'analytics', label: 'Analytics RH', icon: 'analytics', section: 'Pilotage' },
  { id: 'employees', label: 'Employés', icon: 'users', section: 'Personnel', badge: 342 },
  { id: 'contracts', label: 'Contrats', icon: 'contract', section: 'Personnel' },
  { id: 'skills', label: 'Compétences', icon: 'skill', section: 'Personnel' },
  { id: 'recruitment', label: 'Recrutement', icon: 'recruit', section: 'Personnel', badge: 12 },
  { id: 'time', label: 'Temps & Présences', icon: 'time', section: 'Activité' },
  { id: 'leaves', label: 'Congés', icon: 'leave', section: 'Activité', badge: 7 },
  { id: 'missions', label: 'Ordres de mission', icon: 'mission', section: 'Activité' },
  { id: 'payroll', label: 'Paie', icon: 'payroll', section: 'Rémunération' },
  { id: 'loans', label: 'Avances & Prêts', icon: 'loan', section: 'Rémunération' },
  { id: 'expenses', label: 'Notes de frais', icon: 'expense', section: 'Rémunération', badge: 4 },
  { id: 'reviews', label: 'Évaluations', icon: 'review', section: 'Développement' },
  { id: 'trainings', label: 'Formations', icon: 'training', section: 'Développement' },
  { id: 'budget', label: 'Budget formation', icon: 'pieChart', section: 'Développement' },
  { id: 'medical', label: 'Suivi médical', icon: 'medical', section: 'Conformité' },
  { id: 'declarations', label: 'Déclarations', icon: 'declaration', section: 'Conformité' },
  { id: 'settings', label: 'Paramètres', icon: 'settings', section: 'Système' },
];
window.NAV = NAV;

function Sidebar({ active, onNav }) {
  const sections = [...new Set(NAV.map(n => n.section))];
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
        <React.Fragment key={sec}>
          <div className="nav-section-label">{sec}</div>
          {NAV.filter(n => n.section === sec).map(n => {
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
        <Avatar name="Faïsal Sab"/>
        <div style={{flex:1, minWidth:0}}>
          <div style={{fontSize:13, fontWeight:600}}>Faïsal Sab</div>
          <div style={{fontSize:11, color:'var(--ink-3)'}}>Admin RH</div>
        </div>
        <button className="icon-btn" style={{width:30,height:30}}><Icons.logout size={14}/></button>
      </div>
    </aside>
  );
}
window.Sidebar = Sidebar;

function Topbar() {
  return (
    <div className="topbar">
      <div className="search">
        <Icons.search size={16}/>
        <input placeholder="Rechercher un employé, contrat, bulletin, formation…"/>
        <kbd style={{fontSize:11, color:'var(--ink-3)', background:'var(--bg-soft)', padding:'2px 6px', borderRadius:6, border:'1px solid var(--line)'}}>⌘K</kbd>
      </div>
      <div className="row gap-2" style={{marginLeft:'auto'}}>
        <button className="icon-btn"><Icons.info size={16}/></button>
        <button className="icon-btn"><Icons.bell size={16}/><span className="dot"/></button>
        <div className="user-chip">
          <div style={{textAlign:'right'}}>
            <div className="user-chip-name">Faïsal Sab</div>
            <div className="user-chip-role">Admin RH · DRH</div>
          </div>
          <Avatar name="Faïsal Sab"/>
        </div>
      </div>
    </div>
  );
}
window.Topbar = Topbar;

function PageHeader({ uc, crumbs = [], title, subtitle, actions, children }) {
  return (
    <div className="page-header">
      <div>
        {(uc || crumbs.length > 0) && (
          <div className="breadcrumb">
            {uc && <span style={{fontFamily:'JetBrains Mono', fontSize:11, color:'var(--orange-700)', background:'var(--orange-50)', padding:'2px 8px', borderRadius:999, marginRight:8}}>{uc}</span>}
            {crumbs.map((c, i) => (
              <React.Fragment key={i}>
                <span style={i===crumbs.length-1?{color:'var(--ink-2)', fontWeight:600}:{}}>{c}</span>
                {i < crumbs.length - 1 && <span className="crumb-sep">›</span>}
              </React.Fragment>
            ))}
          </div>
        )}
        <h1 className="h-display">{title}</h1>
        {subtitle && <div className="page-subtitle">{subtitle}</div>}
        {children}
      </div>
      {actions && <div className="row gap-2">{actions}</div>}
    </div>
  );
}
window.PageHeader = PageHeader;

/* ================ Tiny chart components ================ */
function Donut({ data, size=160, thick=28 }) {
  const total = data.reduce((s,d)=>s+d.value,0);
  const r = (size-thick)/2;
  const c = 2 * Math.PI * r;
  let acc = 0;
  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
      <circle cx={size/2} cy={size/2} r={r} fill="none" stroke="var(--bg-soft)" strokeWidth={thick}/>
      {data.map((d,i)=>{
        const len = (d.value/total)*c;
        const dash = `${len} ${c-len}`;
        const off = c - acc; acc += len;
        return <circle key={i} cx={size/2} cy={size/2} r={r} fill="none"
          stroke={d.color} strokeWidth={thick} strokeDasharray={dash} strokeDashoffset={off}
          transform={`rotate(-90 ${size/2} ${size/2})`}/>
      })}
    </svg>
  );
}
window.Donut = Donut;

function Spark({ data, color='var(--orange-500)', h=40, w=120, fill }) {
  if (!data?.length) return null;
  const max = Math.max(...data), min = Math.min(...data);
  const step = w / (data.length - 1);
  const pts = data.map((v,i)=>{
    const x = i*step;
    const y = h - ((v-min)/(max-min||1))*(h-4) - 2;
    return [x,y];
  });
  const d = "M " + pts.map(p=>p.join(",")).join(" L ");
  const fillD = d + ` L ${w},${h} L 0,${h} Z`;
  return (
    <svg width={w} height={h} viewBox={`0 0 ${w} ${h}`}>
      {fill && <path d={fillD} fill={fill} opacity=".22"/>}
      <path d={d} stroke={color} strokeWidth="2" fill="none" strokeLinecap="round"/>
      <circle cx={pts[pts.length-1][0]} cy={pts[pts.length-1][1]} r="3" fill={color}/>
    </svg>
  );
}
window.Spark = Spark;

function Bars({data, h=140}) {
  const max = Math.max(...data.map(d=>d.value));
  return (
    <div style={{display:'flex',alignItems:'flex-end',gap:8,height:h,padding:'8px 0'}}>
      {data.map((d,i)=>(
        <div key={i} style={{flex:1, display:'flex', flexDirection:'column', gap:6, alignItems:'center'}}>
          <div style={{flex:1, width:'100%', display:'flex', alignItems:'flex-end'}}>
            <div style={{width:'100%', background: d.color||'var(--grad-orange)', borderRadius:'6px 6px 0 0',
              height:`${(d.value/max)*100}%`, minHeight:4}}/>
          </div>
          <div style={{fontSize:10, color:'var(--ink-3)', fontWeight:600}}>{d.label}</div>
        </div>
      ))}
    </div>
  );
}
window.Bars = Bars;

function Line({data, h=180, color='var(--orange-500)', labels=[], fill}) {
  const w=560, pL=32, pB=24, pT=10, pR=8;
  const max = Math.max(...data)*1.1, min = Math.min(...data)*0.9;
  const innerW=w-pL-pR, innerH=h-pT-pB;
  const step = innerW/(data.length-1);
  const pts = data.map((v,i)=>[pL+i*step, pT+innerH-((v-min)/(max-min))*innerH]);
  const d = "M " + pts.map(p=>p.join(",")).join(" L ");
  const fillD = d + ` L ${pts[pts.length-1][0]},${pT+innerH} L ${pts[0][0]},${pT+innerH} Z`;
  return (
    <svg viewBox={`0 0 ${w} ${h}`} width="100%" style={{maxHeight:h}}>
      {[0,1,2,3,4].map(i=>{
        const y = pT+(innerH/4)*i;
        return <line key={i} x1={pL} y1={y} x2={w-pR} y2={y} stroke="var(--line-soft)" strokeDasharray="3 3"/>
      })}
      {fill && <path d={fillD} fill={fill} opacity=".18"/>}
      <path d={d} stroke={color} strokeWidth="2.2" fill="none" strokeLinecap="round" strokeLinejoin="round"/>
      {pts.map((p,i)=><circle key={i} cx={p[0]} cy={p[1]} r="2.5" fill="white" stroke={color} strokeWidth="1.5"/>)}
      {labels.map((l,i)=><text key={i} x={pts[i][0]} y={h-6} textAnchor="middle" fontSize="9" fill="var(--ink-3)">{l}</text>)}
    </svg>
  );
}
window.Line = Line;
