/* global React */

function EmpPageReviews() {
  const { Icons, PageHeader, Donut } = window;

  return (
    <div>
      <PageHeader
        uc="Développement"
        title="Mes évaluations"
        subtitle="Cycle d'évaluation, objectifs OKR et plan de carrière"
      />

      {/* Current cycle banner */}
      <div className="card card-pad-lg" style={{
        background:'linear-gradient(135deg, var(--orange-50) 0%, #FFFAEC 60%, #fff 100%)',
        border:'1px solid var(--orange-200)', marginBottom:20
      }}>
        <div className="row" style={{alignItems:'flex-start'}}>
          <div style={{flex:1}}>
            <span className="badge orange">Cycle actif · Q1 2026</span>
            <div className="h-display" style={{fontSize:30, marginTop:8}}>Évaluation Q1 2026</div>
            <div style={{fontSize:13.5, color:'var(--ink-2)', marginTop:6, maxWidth:600}}>
              Entretien planifié avec Marc Foga le 16 mai à 14h en salle Tokyo. Pensez à compléter votre auto-évaluation avant.
            </div>

            <div className="stepper" style={{marginTop:18}}>
              <span className="step done">1 · Auto-éval ✓</span>
              <span className="step-arrow">→</span>
              <span className="step active">2 · Entretien manager</span>
              <span className="step-arrow">→</span>
              <span className="step">3 · Synthèse</span>
              <span className="step-arrow">→</span>
              <span className="step">4 · Signature</span>
            </div>
          </div>
          <div className="col gap-2">
            <button className="btn btn-primary"><Icons.edit size={14}/> Compléter mon auto-évaluation</button>
            <button className="btn btn-secondary"><Icons.cal size={14}/> Voir mon entretien</button>
          </div>
        </div>
      </div>

      <div style={{display:'grid', gridTemplateColumns:'2fr 1fr', gap:16, marginBottom:24}}>
        {/* Objectives */}
        <div className="card">
          <div className="card-head">
            <div>
              <div className="card-title">Mes objectifs · Q1 2026</div>
              <div className="muted" style={{fontSize:12, marginTop:2}}>5 objectifs OKR · 78% de réalisation</div>
            </div>
            <button className="btn btn-ghost btn-sm">Voir tous</button>
          </div>
          <div style={{padding:'18px 22px'}}>
            {[
              { title:'Livrer l\'app mobile v3.0 sur store', desc:'Release publique avec les 3 modules clients prioritaires', progress:95, status:'on-track', kr:'KR 1' },
              { title:'Réduire la dette technique de 40%', desc:'Migrer 12 modules vers la nouvelle architecture', progress:68, status:'on-track', kr:'KR 2' },
              { title:'Mentorer 2 développeurs juniors', desc:'Sarah & Olivier · sessions hebdomadaires + revues de code', progress:80, status:'on-track', kr:'KR 3' },
              { title:'Certification AWS Solutions Architect', desc:'Formation interne + examen prévu en juin 2026', progress:60, status:'on-track', kr:'KR 4' },
              { title:'Améliorer NPS app mobile à > 60', desc:'Actuellement à 52 · plan d\'action UX en cours', progress:42, status:'risk', kr:'KR 5' },
            ].map((o, i) => (
              <div key={i} style={{padding:'14px 0', borderBottom: i < 4 ? '1px solid var(--line-soft)' : 'none'}}>
                <div className="row" style={{marginBottom:6}}>
                  <span className="mono" style={{fontSize:10, color:'var(--orange-700)', background:'var(--orange-50)', padding:'1px 7px', borderRadius:999}}>{o.kr}</span>
                  <span style={{marginLeft:8, fontSize:13.5, fontWeight:700}}>{o.title}</span>
                  <span className={"badge " + (o.status === 'on-track' ? 'green' : o.status === 'risk' ? 'amber' : 'red')} style={{marginLeft:'auto'}}>
                    {o.status === 'on-track' ? 'En bonne voie' : o.status === 'risk' ? 'À surveiller' : 'En retard'}
                  </span>
                </div>
                <div style={{fontSize:12, color:'var(--ink-3)', marginBottom:8}}>{o.desc}</div>
                <div className="row gap-3">
                  <div className="bar thick" style={{flex:1}}>
                    <div style={{width: o.progress + '%', background: o.progress >= 80 ? 'var(--green-500)' : o.progress >= 50 ? 'var(--grad-orange)' : 'var(--amber-500)'}}/>
                  </div>
                  <span className="tabular" style={{fontSize:13, fontWeight:700, minWidth:42, textAlign:'right'}}>{o.progress}%</span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Performance overall */}
        <div className="col gap-3">
          <div className="card card-pad">
            <div className="card-title" style={{fontSize:14, marginBottom:14}}>Mon score global</div>
            <div style={{display:'flex', justifyContent:'center', position:'relative', marginBottom:14}}>
              <Donut data={[
                { value:4.6, color:'#F97316' },
                { value:0.4, color:'var(--bg-soft)' },
              ]} size={160} thick={20}/>
              <div style={{position:'absolute', inset:0, display:'grid', placeItems:'center', textAlign:'center'}}>
                <div>
                  <div style={{fontFamily:'Inter Tight', fontSize:38, fontWeight:800, letterSpacing:'-0.02em'}} className="tabular">4,6</div>
                  <div style={{fontSize:10, color:'var(--ink-3)'}}>sur 5,0 · Q4 25</div>
                </div>
              </div>
            </div>
            <div className="row gap-1" style={{justifyContent:'center', marginBottom:8}}>
              {[1,2,3,4,5].map(i => (
                <Icons.star key={i} size={14} style={{color: i <= 5 ? 'var(--orange-500)' : 'var(--bg-soft)'}}/>
              ))}
            </div>
            <div style={{textAlign:'center', fontSize:12.5, fontWeight:600, color:'var(--orange-700)'}}>Excellent</div>
            <div style={{textAlign:'center', fontSize:11, color:'var(--ink-3)', marginTop:4}}>top 12% de l'entreprise</div>
          </div>

          <div className="card card-pad">
            <div className="card-title" style={{fontSize:14, marginBottom:14}}>Mes 3 forces 💪</div>
            {[
              { l:'Excellence technique', s:4.9 },
              { l:'Esprit d\'équipe', s:4.7 },
              { l:'Sens des responsabilités', s:4.6 },
            ].map((s, i) => (
              <div key={i} style={{marginBottom:10}}>
                <div className="row" style={{marginBottom:4}}>
                  <span style={{fontSize:12.5, fontWeight:600}}>{s.l}</span>
                  <span className="tabular" style={{marginLeft:'auto', fontSize:12.5, color:'var(--orange-700)', fontWeight:700}}>{s.s}</span>
                </div>
                <div className="bar thin"><div style={{width: (s.s/5)*100 + '%'}}/></div>
              </div>
            ))}
          </div>

          <div className="card card-pad">
            <div className="card-title" style={{fontSize:14, marginBottom:14}}>À développer 🎯</div>
            {[
              { l:'Communication écrite', s:3.4 },
              { l:'Présentation publique', s:3.6 },
            ].map((s, i) => (
              <div key={i} style={{marginBottom:10}}>
                <div className="row" style={{marginBottom:4}}>
                  <span style={{fontSize:12.5, fontWeight:600}}>{s.l}</span>
                  <span className="tabular" style={{marginLeft:'auto', fontSize:12.5, color:'var(--ink-3)', fontWeight:700}}>{s.s}</span>
                </div>
                <div className="bar thin"><div style={{width: (s.s/5)*100 + '%', background:'var(--amber-500)'}}/></div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* History */}
      <div className="section-title">Historique des évaluations</div>
      <div className="card">
        <table className="t">
          <thead><tr><th>Cycle</th><th>Évaluateur</th><th>Score</th><th>Décision</th><th>Date entretien</th><th></th></tr></thead>
          <tbody>
            {[
              { c:'Q1 2026', e:'Marc Foga', s:'En cours', d:'À venir', dt:'16 mai 2026' },
              { c:'Q4 2025', e:'Marc Foga', s:'4,6 / 5', d:'Maintien en poste · bonus 12%', dt:'14 jan 2026' },
              { c:'Q3 2025', e:'Marc Foga', s:'4,5 / 5', d:'Promotion · Lead Mobile Eng.', dt:'10 oct 2025' },
              { c:'Q2 2025', e:'Marc Foga', s:'4,4 / 5', d:'Augmentation +8%', dt:'12 jul 2025' },
              { c:'Q1 2025', e:'Marc Foga', s:'4,3 / 5', d:'Plan de carrière validé', dt:'08 avr 2025' },
              { c:'Q4 2024', e:'Marc Foga', s:'4,2 / 5', d:'Bonus annuel', dt:'15 jan 2025' },
            ].map((r,i) => (
              <tr key={i}>
                <td><span className="tag">{r.c}</span></td>
                <td>{r.e}</td>
                <td>
                  {r.s === 'En cours' ? <span className="badge orange">En cours</span> : <span style={{fontWeight:700, fontFamily:'Inter Tight', fontSize:14}}>{r.s}</span>}
                </td>
                <td style={{fontSize:13}}>{r.d}</td>
                <td className="muted">{r.dt}</td>
                <td><button className="icon-btn" style={{width:28, height:28}}><Icons.chevR size={12}/></button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
window.EmpPageReviews = EmpPageReviews;
