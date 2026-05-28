/* global React */
const { useState: useS_dsh } = React;
const { Icons: Ic_d, Avatar: Av_d } = window;

const DOC_NAV = [
  { id: 'dashboard',   label: 'Tableau de bord',     icon: 'dashboard', section: 'Pratique' },
  { id: 'today',       label: "Consultations du jour", icon: 'cal',     section: 'Pratique', badge: 6 },
  { id: 'schedule',    label: 'Planning',            icon: 'time',      section: 'Pratique' },
  { id: 'patients',    label: 'Patients',            icon: 'users',     section: 'Dossiers', badge: 342 },
  { id: 'visits',      label: 'Visites médicales',   icon: 'medical',   section: 'Dossiers' },
  { id: 'certificates',label: 'Certificats',         icon: 'doc',       section: 'Dossiers', badge: 3 },
  { id: 'restrictions',label: 'Aptitudes & restrictions', icon: 'shield', section: 'Suivi' },
  { id: 'alerts',      label: 'Échéances & alertes', icon: 'alert',     section: 'Suivi', badge: 7 },
  { id: 'reports',     label: 'Rapports & export',   icon: 'analytics', section: 'Reporting' },
];

function DocSidebar({ active, onNav }) {
  const sections = [...new Set(DOC_NAV.map(n => n.section))];
  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <div className="sidebar-logo" style={{background:'linear-gradient(135deg, #ff9d52, #f26b0f)'}}>⚕</div>
        <div>
          <div className="sidebar-brand-name">HR Core · Santé</div>
          <div className="sidebar-brand-sub">Médecine du travail</div>
        </div>
      </div>

      <div style={{
        padding:'12px', borderRadius:14,
        background:'linear-gradient(135deg, var(--orange-50) 0%, #fff 100%)',
        border:'1px solid var(--orange-100)', marginBottom:14,
        display:'flex', gap:10, alignItems:'center'
      }}>
        <Av_d name="Dr Nkoa" color="green"/>
        <div style={{flex:1, minWidth:0}}>
          <div style={{fontSize:13, fontWeight:700}}>Dr. M. Nkoa</div>
          <div style={{fontSize:11, color:'var(--ink-3)'}}>Médecin du travail · CMA Yaoundé</div>
        </div>
      </div>

      {sections.map(sec => (
        <React.Fragment key={sec}>
          <div className="nav-section-label">{sec}</div>
          {DOC_NAV.filter(n => n.section === sec).map(n => {
            const I = Ic_d[n.icon];
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
        <Av_d name="Dr Nkoa" color="green"/>
        <div style={{flex:1, minWidth:0}}>
          <div style={{fontSize:13, fontWeight:600}}>Dr. Nkoa</div>
          <div style={{fontSize:11, color:'var(--ink-3)'}}>Agrément CNPS · 2018</div>
        </div>
        <button className="icon-btn" style={{width:30, height:30}}><Ic_d.logout size={14}/></button>
      </div>
    </aside>
  );
}

function DocTopbar() {
  return (
    <div className="topbar">
      <div className="search">
        <Ic_d.search size={16}/>
        <input placeholder="Rechercher un patient, visite, certificat…"/>
      </div>
      <div className="row gap-2" style={{marginLeft:'auto'}}>
        <a href="index.html" className="btn btn-secondary btn-sm">Vue Admin</a>
        <a href="employee.html" className="btn btn-secondary btn-sm">Vue Employé</a>
        <button className="btn btn-primary btn-sm"><Ic_d.plus size={13}/> Nouvelle consultation</button>
        <button className="icon-btn"><Ic_d.bell size={16}/><span className="dot"/></button>
        <div className="user-chip">
          <div style={{textAlign:'right'}}>
            <div className="user-chip-name">Dr M. Nkoa</div>
            <div className="user-chip-role">CMA Yaoundé · Médecin agréé</div>
          </div>
          <Av_d name="Dr Nkoa" color="green"/>
        </div>
      </div>
    </div>
  );
}

window.DocSidebar = DocSidebar;
window.DocTopbar = DocTopbar;
