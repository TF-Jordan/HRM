/* global React */

function PageReviews() {
  const { Icons, Avatar, PageHeader, Donut } = window;

  const reviews = [
    { id:'EV-2026-Q1-0142', emp:'Aminata Diallo', avc:'orange', cycle:'Q1 2026', reviewer:'Marc Foga', score:4.6, status:'completed', date:'14 avr 2026' },
    { id:'EV-2026-Q1-0098', emp:'Joseph Mbarga', avc:'blue', cycle:'Q1 2026', reviewer:'Hervé Tankeu', score:4.2, status:'completed', date:'12 avr 2026' },
    { id:'EV-2026-Q1-0203', emp:'Sarah Nguemo', avc:'violet', cycle:'Q1 2026', reviewer:'Marc Foga', score:4.4, status:'pending', date:'16 mai 2026' },
    { id:'EV-2026-Q1-0021', emp:'Marc Foga', avc:'green', cycle:'Q1 2026', reviewer:'Faïsal Sab', score:4.8, status:'completed', date:'10 avr 2026' },
    { id:'EV-2026-Q1-0177', emp:'Pierre Kouam', avc:'amber', cycle:'Q1 2026', reviewer:'Estelle Bilong', score:3.8, status:'draft', date:'18 mai 2026' },
    { id:'EV-2026-Q1-0067', emp:'Olivier Manga', avc:'green', cycle:'Q1 2026', reviewer:'Marc Foga', score:4.5, status:'completed', date:'09 avr 2026' },
  ];

  return (
    <div>
      <PageHeader
        uc="Développement"
        title="Évaluations"
        subtitle="Cycle trimestriel · Objectifs, 360° et plans de carrière"
        actions={
          <>
            <button className="btn btn-secondary"><Icons.download/> Rapport campagne</button>
            <button className="btn btn-primary"><Icons.plus/> Lancer un cycle</button>
          </>
        }
      />

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'Cycle actif', v:'Q1 2026', sub:'89% complétées', tone:'orange' },
          { l:'À clôturer', v:'17', sub:'8 brouillons · 9 attente', tone:'amber' },
          { l:'Score moyen', v:'4,3 / 5', sub:'+0,2 vs Q4 2025', tone:'green' },
          { l:'Objectifs atteints', v:'78%', sub:'sur 540 objectifs OKR', tone:'violet' },
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

      <div style={{display:'grid', gridTemplateColumns:'2fr 1fr', gap:16, marginBottom:20}}>
        {/* Score distribution */}
        <div className="card">
          <div className="card-head">
            <div className="card-title">Distribution des scores · Q1 2026</div>
            <span className="badge orange">325 / 342 évaluations</span>
          </div>
          <div style={{padding:'18px 22px'}}>
            <div style={{display:'grid', gridTemplateColumns:'80px 1fr 60px', gap:14, rowGap:14}}>
              {[
                { l:'Exceptionnel', range:'4,7–5,0', n:42, pct:13, color:'var(--green-500)' },
                { l:'Excellent', range:'4,3–4,6', n:124, pct:38, color:'var(--orange-500)' },
                { l:'Solide', range:'3,8–4,2', n:108, pct:33, color:'var(--orange-300)' },
                { l:'À développer', range:'3,0–3,7', n:42, pct:13, color:'var(--amber-500)' },
                { l:'Insuffisant', range:'< 3,0', n:9, pct:3, color:'var(--red-500)' },
              ].map((r,i) => (
                <React.Fragment key={i}>
                  <div style={{fontSize:12.5, fontWeight:600}}>{r.l}</div>
                  <div>
                    <div className="bar" style={{height:24, position:'relative'}}>
                      <div style={{width: `${r.pct*2}%`, background: r.color, height:'100%', display:'flex', alignItems:'center', paddingLeft:10, color:'#fff', fontSize:11, fontWeight:700, borderRadius:999}}>
                        {r.n}
                      </div>
                    </div>
                    <div style={{fontSize:11, color:'var(--ink-4)', marginTop:3}} className="mono">{r.range}</div>
                  </div>
                  <div className="tabular" style={{fontSize:13, fontWeight:700, textAlign:'right'}}>{r.pct}%</div>
                </React.Fragment>
              ))}
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-head"><div className="card-title">Statut des évaluations</div></div>
          <div style={{padding:18, display:'flex', flexDirection:'column', alignItems:'center'}}>
            <div style={{position:'relative'}}>
              <Donut data={[
                { value:289, color:'#10B981' },
                { value:17, color:'#F59E0B' },
                { value:19, color:'#94A3B8' },
                { value:17, color:'#EF4444' },
              ]} size={160} thick={22}/>
              <div style={{position:'absolute', inset:0, display:'grid', placeItems:'center'}}>
                <div style={{textAlign:'center'}}>
                  <div style={{fontFamily:'Inter Tight', fontSize:22, fontWeight:800}}>89%</div>
                  <div style={{fontSize:11, color:'var(--ink-3)'}}>complétées</div>
                </div>
              </div>
            </div>
            <div style={{width:'100%', marginTop:16}}>
              {[['#10B981','Complétées','289'],['#F59E0B','En attente revue','17'],['#94A3B8','Brouillon','19'],['#EF4444','Non démarrées','17']].map((x,i) => (
                <div key={i} className="row" style={{padding:'4px 0', fontSize:12.5}}>
                  <span style={{width:10, height:10, borderRadius:3, background:x[0]}}/>
                  <span style={{flex:1, marginLeft:8}}>{x[1]}</span>
                  <span className="tabular" style={{fontWeight:600}}>{x[2]}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Sample 9-box */}
      <div className="card" style={{marginBottom:20}}>
        <div className="card-head">
          <div>
            <div className="card-title">9-Box · Performance vs Potentiel</div>
            <div className="muted" style={{fontSize:12, marginTop:2}}>Cartographie des talents · Q1 2026</div>
          </div>
        </div>
        <div style={{padding:18, display:'grid', gridTemplateColumns:'40px 1fr', gap:12}}>
          <div style={{display:'flex', flexDirection:'column-reverse', justifyContent:'space-around', alignItems:'center', fontSize:10, fontWeight:600, color:'var(--ink-3)', textTransform:'uppercase', letterSpacing:'0.06em'}}>
            <span style={{transform:'rotate(-90deg)', whiteSpace:'nowrap'}}>POTENTIEL →</span>
          </div>
          <div>
            <div style={{display:'grid', gridTemplateColumns:'repeat(3,1fr)', gap:8}}>
              {[
                { l:'Énigme', sub:'Pot. élevé / Perf. faible', n:8, color:'#FCD34D', tier:1 },
                { l:'Forte croissance', sub:'Pot. élevé / Perf. moyenne', n:24, color:'#34D399', tier:2 },
                { l:'Star ★', sub:'Pot. élevé / Perf. élevée', n:18, color:'#F97316', tier:3 },
                { l:'Dilemme', sub:'Pot. moyen / Perf. faible', n:14, color:'#FFE4CC', tier:1, ink:1 },
                { l:'Performant', sub:'Pot. moyen / Perf. moyenne', n:96, color:'#FFB066', tier:2, ink:1 },
                { l:'Performant senior', sub:'Pot. moyen / Perf. élevée', n:64, color:'#FB923C', tier:3 },
                { l:'Risque', sub:'Pot. faible / Perf. faible', n:9, color:'#FECACA', tier:1, ink:1 },
                { l:'Solide', sub:'Pot. faible / Perf. moyenne', n:42, color:'#FED7AA', tier:2, ink:1 },
                { l:'Expert', sub:'Pot. faible / Perf. élevée', n:42, color:'#FDBA74', tier:3, ink:1 },
              ].map((b,i) => (
                <div key={i} style={{
                  padding:'18px 16px', borderRadius:14, background:b.color, color: b.ink ? 'var(--ink)' : '#fff',
                  display:'flex', flexDirection:'column', justifyContent:'space-between', minHeight:100, position:'relative'
                }}>
                  <div>
                    <div style={{fontSize:13, fontWeight:700}}>{b.l}</div>
                    <div style={{fontSize:11, opacity:0.8, marginTop:2}}>{b.sub}</div>
                  </div>
                  <div style={{fontFamily:'Inter Tight', fontSize:28, fontWeight:800, marginTop:8}} className="tabular">{b.n}</div>
                </div>
              ))}
            </div>
            <div style={{textAlign:'center', fontSize:10, fontWeight:600, color:'var(--ink-3)', textTransform:'uppercase', letterSpacing:'0.06em', marginTop:10}}>PERFORMANCE →</div>
          </div>
        </div>
      </div>

      {/* Reviews table */}
      <div className="card">
        <div className="card-head">
          <div className="card-title">Évaluations · Q1 2026</div>
          <div className="row gap-2">
            <button className="chip orange active">Toutes</button>
            <button className="chip">Complétées</button>
            <button className="chip">En attente</button>
            <button className="chip">Brouillon</button>
          </div>
        </div>
        <table className="t">
          <thead><tr><th>Réf.</th><th>Employé</th><th>Évaluateur</th><th>Cycle</th><th>Score</th><th>Date</th><th>Statut</th></tr></thead>
          <tbody>
            {reviews.map(r => (
              <tr key={r.id}>
                <td className="mono" style={{fontSize:11, color:'var(--ink-3)'}}>{r.id}</td>
                <td><div className="row gap-2"><Avatar name={r.emp} color={r.avc} size="sm"/><span style={{fontSize:13, fontWeight:600}}>{r.emp}</span></div></td>
                <td>{r.reviewer}</td>
                <td><span className="tag">{r.cycle}</span></td>
                <td>
                  <div className="row gap-2">
                    <span style={{fontFamily:'Inter Tight', fontSize:16, fontWeight:700, letterSpacing:'-0.01em'}} className="tabular">{r.score}</span>
                    <div className="row gap-1">
                      {[1,2,3,4,5].map(i => (
                        <Icons.star key={i} size={10} style={{color: i <= Math.round(r.score) ? 'var(--orange-500)' : 'var(--bg-soft)'}}/>
                      ))}
                    </div>
                  </div>
                </td>
                <td className="muted">{r.date}</td>
                <td>
                  {r.status === 'completed' && <span className="badge green">Terminée</span>}
                  {r.status === 'pending' && <span className="badge amber">En attente</span>}
                  {r.status === 'draft' && <span className="badge gray">Brouillon</span>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

window.PageReviews = PageReviews;
