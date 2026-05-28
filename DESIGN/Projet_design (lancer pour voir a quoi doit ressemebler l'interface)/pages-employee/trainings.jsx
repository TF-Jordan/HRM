/* global React */

function EmpPageTrainings() {
  const { Icons, PageHeader } = window;

  return (
    <div>
      <PageHeader
        uc="Développement"
        title="Mes formations"
        subtitle="Catalogue, inscriptions et certifications obtenues"
        actions={
          <button className="btn btn-secondary"><Icons.download/> Mes attestations</button>
        }
      />

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'En cours', v:'1', sub:'AWS · 60% complété', tone:'orange' },
          { l:'À démarrer', v:'2', sub:'cette semaine', tone:'blue' },
          { l:'Complétées 2026', v:'3', sub:'42h cumulées', tone:'green' },
          { l:'Score moyen', v:'18 / 20', sub:'sur évaluations', tone:'violet' },
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

      {/* In progress */}
      <div className="section-title">En cours</div>
      <div className="card" style={{marginBottom:24, overflow:'hidden'}}>
        <div style={{
          padding:'24px 26px',
          background:'linear-gradient(135deg, #60A5FA 0%, #2563EB 100%)',
          color:'#fff', position:'relative', overflow:'hidden'
        }}>
          <div style={{position:'absolute', top:-60, right:-60, width:240, height:240,
            background:'radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 70%)'}}/>
          <div className="row" style={{alignItems:'flex-start', position:'relative'}}>
            <div style={{flex:1}}>
              <div className="row gap-2">
                <span className="badge" style={{background:'rgba(255,255,255,0.2)', color:'#fff'}}>Tech · 40h</span>
                <span className="mono" style={{fontSize:11, opacity:0.7}}>TR-002</span>
              </div>
              <div className="h-display" style={{fontSize:28, color:'#fff', marginTop:8}}>Architecture Cloud AWS</div>
              <div style={{fontSize:13, opacity:0.85, marginTop:4}}>Formateur · Patrick Mbo (AWS certified) · Hybride · démarré le 20 mai</div>

              <div style={{marginTop:18}}>
                <div className="row" style={{marginBottom:6}}>
                  <span style={{fontSize:12, opacity:0.85}}>Progression</span>
                  <span className="tabular" style={{marginLeft:'auto', fontSize:13, fontWeight:700}}>24 / 40 h · 60%</span>
                </div>
                <div className="bar thick" style={{background:'rgba(255,255,255,0.2)'}}>
                  <div style={{width:'60%', background:'#fff'}}/>
                </div>
              </div>

              <div className="row gap-4" style={{marginTop:20}}>
                <span className="row gap-2" style={{fontSize:12.5}}><Icons.cal size={14}/> Prochaine session · 16 mai à 14h</span>
                <span className="row gap-2" style={{fontSize:12.5}}><Icons.briefcase size={14}/> Salle Sydney + visioconférence</span>
              </div>
            </div>
            <button className="btn" style={{background:'#fff', color:'var(--ink)'}}>Rejoindre la session</button>
          </div>
        </div>

        <div className="grid-4" style={{padding:18, gap:14}}>
          {[
            { l:'Compétence visée', v:'AWS Architecture' },
            { l:'Investissement', v:'420 000 XAF' },
            { l:'Demi-journées restantes', v:'8 / 20' },
            { l:'Évaluation', v:'À l\'issue · obligatoire' },
          ].map((s, i) => (
            <div key={i}>
              <div style={{fontSize:11, color:'var(--ink-3)', fontWeight:600, textTransform:'uppercase', letterSpacing:'0.05em'}}>{s.l}</div>
              <div style={{fontSize:13.5, fontWeight:600, marginTop:4}}>{s.v}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Catalog */}
      <div className="section-title">Catalogue disponible</div>
      <div className="row gap-2" style={{marginBottom:16}}>
        <button className="chip orange active">Toutes (24)</button>
        <button className="chip">Tech (8)</button>
        <button className="chip">Management (5)</button>
        <button className="chip">Soft skills (6)</button>
        <button className="chip">Langues (5)</button>
        <button className="chip" style={{marginLeft:'auto'}}><Icons.star size={11}/> Recommandées</button>
      </div>

      <div className="grid-3" style={{gap:14, marginBottom:24}}>
        {[
          { title:'Leadership avancé', cat:'Management', dur:'24h', mode:'Présentiel', date:'12-14 juin', color:'orange', enrolled:false, recommended:true, why:'Recommandé par votre manager' },
          { title:'TypeScript Mastery', cat:'Tech', dur:'32h', mode:'En ligne', date:'En continu', color:'blue', enrolled:true, status:'À démarrer · 25 mai' },
          { title:'Design Thinking', cat:'Innovation', dur:'12h', mode:'Présentiel', date:'28-29 mai', color:'amber', enrolled:true, status:'Inscrit · 28 mai' },
          { title:'Anglais B2 conversationnel', cat:'Langues', dur:'48h', mode:'En ligne', date:'En continu', color:'violet', enrolled:false },
          { title:'React Native avancé', cat:'Tech', dur:'24h', mode:'Hybride', date:'10-12 juin', color:'teal', enrolled:false, recommended:true, why:'Aligné avec vos objectifs Q2' },
          { title:'Gestion du stress au travail', cat:'Bien-être', dur:'8h', mode:'En ligne', date:'En continu', color:'green', enrolled:false },
        ].map((c, i) => (
          <div key={i} className="card card-pad clickable" style={{position:'relative', overflow:'hidden'}}>
            {c.recommended && (
              <div style={{position:'absolute', top:12, right:12, fontSize:10, fontWeight:700, color:'var(--orange-600)',
                background:'var(--orange-50)', padding:'2px 8px', borderRadius:999, letterSpacing:'0.04em'}}>★ POUR VOUS</div>
            )}
            <div style={{
              height:64, marginLeft:-22, marginRight:-22, marginTop:-22, marginBottom:14,
              background: c.color === 'orange' ? 'linear-gradient(135deg, #FB923C, #EA580C)' :
                          c.color === 'blue' ? 'linear-gradient(135deg, #60A5FA, #2563EB)' :
                          c.color === 'green' ? 'linear-gradient(135deg, #34D399, #059669)' :
                          c.color === 'violet' ? 'linear-gradient(135deg, #A78BFA, #7C3AED)' :
                          c.color === 'amber' ? 'linear-gradient(135deg, #FCD34D, #D97706)' :
                          'linear-gradient(135deg, #2DD4BF, #0D9488)',
              display:'flex', alignItems:'flex-end', padding:'12px 22px'
            }}>
              <span className="badge" style={{background:'rgba(255,255,255,0.25)', color:'#fff'}}>{c.cat}</span>
            </div>
            <div style={{fontSize:15, fontWeight:700}}>{c.title}</div>
            <div className="row gap-3" style={{marginTop:10, fontSize:11.5, color:'var(--ink-3)'}}>
              <span className="row gap-1"><Icons.time size={12}/> {c.dur}</span>
              <span className="row gap-1"><Icons.briefcase size={12}/> {c.mode}</span>
              <span className="row gap-1"><Icons.cal size={12}/> {c.date}</span>
            </div>
            {c.why && <div style={{marginTop:10, padding:'6px 10px', background:'var(--orange-50)', borderRadius:8, fontSize:11.5, color:'var(--orange-700)'}}>💡 {c.why}</div>}
            <div className="row" style={{marginTop:14}}>
              {c.enrolled ? (
                <span className="badge green">{c.status}</span>
              ) : (
                <button className="btn btn-primary btn-sm">M'inscrire</button>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Completed */}
      <div className="section-title">Mon historique de formations</div>
      <div className="card">
        <table className="t">
          <thead><tr><th>Formation</th><th>Date</th><th>Durée</th><th>Score</th><th>Certif.</th><th></th></tr></thead>
          <tbody>
            {[
              { n:'Architecture Mobile avancée', d:'23 sept 2023', h:'32h', score:'18/20', cert:true },
              { n:'Sécurité informatique', d:'12 mar 2024', h:'16h', score:'17/20', cert:true },
              { n:'Leadership niveau 1', d:'05 fév 2025', h:'24h', score:'19/20', cert:true },
              { n:'Méthodes agiles', d:'18 nov 2025', h:'12h', score:'—', cert:false },
            ].map((r,i) => (
              <tr key={i}>
                <td style={{fontSize:13.5, fontWeight:600}}>{r.n}</td>
                <td className="muted">{r.d}</td>
                <td className="tabular">{r.h}</td>
                <td>
                  {r.score !== '—' ? <span style={{fontSize:13, fontWeight:700, color:'var(--orange-700)'}} className="tabular">{r.score}</span> : <span className="muted">—</span>}
                </td>
                <td>{r.cert ? <span className="badge green">Validée</span> : <span className="badge gray">Participation</span>}</td>
                <td>
                  <div className="row gap-2">
                    {r.cert && <button className="icon-btn" style={{width:28, height:28}}><Icons.download size={12}/></button>}
                    <button className="icon-btn" style={{width:28, height:28}}><Icons.chevR size={12}/></button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
window.EmpPageTrainings = EmpPageTrainings;
