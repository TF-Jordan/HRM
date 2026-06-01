/* global React */

function PageTrainings() {
  const { Icons, Avatar, PageHeader } = window;

  const catalog = [
    { id:'TR-001', title:'Leadership & Management', cat:'Management', dur:'24h', mode:'Présentiel', price:'180K', enrolled:18, cap:24, color:'orange', upcoming:'12-14 juin 2026' },
    { id:'TR-002', title:'Architecture Cloud AWS', cat:'Tech', dur:'40h', mode:'Hybride', price:'420K', enrolled:12, cap:15, color:'blue', upcoming:'20-26 mai 2026' },
    { id:'TR-003', title:'Sécurité informatique', cat:'Tech', dur:'16h', mode:'En ligne', price:'95K', enrolled:34, cap:50, color:'green', upcoming:'Continue' },
    { id:'TR-004', title:'Vente B2B avancée', cat:'Commercial', dur:'32h', mode:'Présentiel', price:'240K', enrolled:14, cap:20, color:'violet', upcoming:'05-09 juin 2026' },
    { id:'TR-005', title:'Design Thinking', cat:'Innovation', dur:'12h', mode:'Présentiel', price:'120K', enrolled:8, cap:12, color:'amber', upcoming:'28-29 mai 2026' },
    { id:'TR-006', title:'Comptabilité IFRS', cat:'Finance', dur:'20h', mode:'En ligne', price:'150K', enrolled:6, cap:15, color:'teal', upcoming:'En continu' },
  ];

  const enrollments = [
    { emp:'Aminata Diallo', avc:'orange', training:'Architecture Cloud AWS', progress:60, status:'in-progress', start:'20 mai' },
    { emp:'Marc Foga', avc:'green', training:'Leadership & Management', progress:0, status:'enrolled', start:'12 juin' },
    { emp:'Sarah Nguemo', avc:'violet', training:'Design Thinking', progress:100, status:'completed', start:'14 avr' },
    { emp:'Joseph Mbarga', avc:'blue', training:'Vente B2B avancée', progress:0, status:'enrolled', start:'05 juin' },
    { emp:'Olivier Manga', avc:'green', training:'Sécurité informatique', progress:75, status:'in-progress', start:'En continu' },
  ];

  return (
    <div>
      <PageHeader
        uc="Développement"
        title="Formations"
        subtitle="Catalogue de formations et suivi des inscriptions"
        actions={
          <>
            <button className="btn btn-secondary"><Icons.cal size={14}/> Calendrier</button>
            <button className="btn btn-primary"><Icons.plus/> Créer une formation</button>
          </>
        }
      />

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'Formations actives', v:'24', sub:'92 employés inscrits', tone:'orange' },
          { l:'En cours', v:'28', sub:'12 finalisent ce mois', tone:'blue' },
          { l:'Complétées (2026)', v:'186', sub:'+34% vs 2025', tone:'green' },
          { l:'Heures de formation', v:'4 280h', sub:'12h / employé · YTD', tone:'violet' },
        ].map((k,i) => (
          <div key={i} className="kpi-mini">
            <div className="row" style={{justifyContent:'space-between'}}>
              <span className="kpi-mini-label">{k.l}</span>
              <span className={"sd " + k.tone}></span>
            </div>
            <div className="kpi-mini-value tabular">{k.v}</div>
            <div style={{fontSize:11, color:'var(--ink-3)'}}>{k.sub}</div>
          </div>
        ))}
      </div>

      <div className="row gap-2" style={{marginBottom:16}}>
        <button className="chip orange active">Toutes</button>
        <button className="chip">Tech (8)</button>
        <button className="chip">Management (5)</button>
        <button className="chip">Commercial (4)</button>
        <button className="chip">Finance (3)</button>
        <button className="chip">Innovation (4)</button>
      </div>

      {/* Catalog grid */}
      <div className="grid-3" style={{gap:14, marginBottom:24}}>
        {catalog.map(c => (
          <div key={c.id} className="card card-pad" style={{cursor:'pointer', transition:'all .15s', position:'relative', overflow:'hidden'}}>
            <div style={{
              height:80, marginLeft:-20, marginRight:-20, marginTop:-20, marginBottom:14,
              background: c.color === 'orange' ? 'linear-gradient(135deg, #FB923C, #EA580C)' :
                          c.color === 'blue' ? 'linear-gradient(135deg, #60A5FA, #2563EB)' :
                          c.color === 'green' ? 'linear-gradient(135deg, #34D399, #059669)' :
                          c.color === 'violet' ? 'linear-gradient(135deg, #A78BFA, #7C3AED)' :
                          c.color === 'amber' ? 'linear-gradient(135deg, #FCD34D, #D97706)' :
                          'linear-gradient(135deg, #2DD4BF, #0D9488)',
              position:'relative', display:'flex', alignItems:'flex-end', padding:'12px 20px'
            }}>
              <span className="badge" style={{background:'rgba(255,255,255,0.25)', color:'#fff', backdropFilter:'blur(6px)'}}>{c.cat}</span>
              <Icons.training size={48} style={{position:'absolute', right:12, top:14, color:'rgba(255,255,255,0.3)'}}/>
            </div>
            <div className="mono" style={{fontSize:10, color:'var(--ink-4)'}}>{c.id}</div>
            <div style={{fontSize:15, fontWeight:700, marginTop:2, letterSpacing:'-0.01em'}}>{c.title}</div>
            <div className="row gap-3" style={{marginTop:10, fontSize:11.5, color:'var(--ink-3)'}}>
              <span className="row gap-1"><Icons.clock size={12}/> {c.dur}</span>
              <span className="row gap-1"><Icons.briefcase size={12}/> {c.mode}</span>
            </div>
            <div style={{marginTop:12, paddingTop:12, borderTop:'1px solid var(--line-soft)'}}>
              <div className="row" style={{marginBottom:4}}>
                <span style={{fontSize:11, color:'var(--ink-3)'}}>Inscrits</span>
                <span className="tabular" style={{marginLeft:'auto', fontSize:12, fontWeight:600}}>{c.enrolled}/{c.cap}</span>
              </div>
              <div className="bar thin"><div style={{width: `${(c.enrolled/c.cap)*100}%`}}/></div>
            </div>
            <div className="row" style={{marginTop:12, alignItems:'center'}}>
              <div>
                <div style={{fontSize:10, color:'var(--ink-3)'}}>Session</div>
                <div style={{fontSize:12, fontWeight:600}}>{c.upcoming}</div>
              </div>
              <div style={{marginLeft:'auto', textAlign:'right'}}>
                <div style={{fontSize:10, color:'var(--ink-3)'}}>Coût</div>
                <div style={{fontSize:13, fontWeight:700}} className="tabular">{c.price} XAF</div>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Recent enrollments */}
      <div className="section-title">Inscriptions récentes</div>
      <div className="card">
        <table className="t">
          <thead><tr><th>Employé</th><th>Formation</th><th>Démarrage</th><th>Progression</th><th>Statut</th><th></th></tr></thead>
          <tbody>
            {enrollments.map((e,i) => (
              <tr key={i}>
                <td><div className="row gap-2"><Avatar name={e.emp} color={e.avc} size="sm"/><span style={{fontSize:13, fontWeight:600}}>{e.emp}</span></div></td>
                <td style={{fontSize:13}}>{e.training}</td>
                <td className="muted">{e.start}</td>
                <td>
                  <div className="row gap-2">
                    <div className="bar thin" style={{width:120}}><div style={{width: e.progress + '%', background: e.progress === 100 ? 'var(--green-500)' : 'var(--grad-orange)'}}/></div>
                    <span className="tabular" style={{fontSize:12, fontWeight:600}}>{e.progress}%</span>
                  </div>
                </td>
                <td>
                  {e.status === 'enrolled' && <span className="badge blue">Inscrit</span>}
                  {e.status === 'in-progress' && <span className="badge orange">En cours</span>}
                  {e.status === 'completed' && <span className="badge green">Validé</span>}
                </td>
                <td><button className="icon-btn" style={{width:28, height:28}}><Icons.chevR size={12}/></button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

window.PageTrainings = PageTrainings;
