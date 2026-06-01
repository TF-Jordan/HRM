/* global React */

function PageAnalytics() {
  const { Icons, Avatar, PageHeader, Line, Bars, Donut } = window;

  return (
    <div>
      <PageHeader
        uc="Pilotage"
        title="Analytics RH"
        subtitle="KPIs stratégiques, snapshots et benchmarks · Q1 2026"
        actions={
          <>
            <button className="btn btn-secondary"><Icons.cal size={14}/> Q1 2026</button>
            <button className="btn btn-secondary"><Icons.download/> Exporter PDF</button>
            <button className="btn btn-primary"><Icons.plus/> Snapshot</button>
          </>
        }
      />

      {/* Hero KPIs */}
      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'Coût employé annuel', v:'5,02M', sub:'XAF · -2% vs Q4', tone:'orange', icon:'payroll' },
          { l:'Productivité', v:'+8,4%', sub:'CA / ETP · vs T-1', tone:'green', icon:'trendUp' },
          { l:'Turnover annualisé', v:'9,2%', sub:'cible < 12%', tone:'blue', icon:'users' },
          { l:'Time to hire', v:'18 j', sub:'-3j vs Q4', tone:'violet', icon:'recruit' },
        ].map((k,i) => {
          const I = Icons[k.icon];
          return (
            <div key={i} className={"kpi " + k.tone}>
              <div className="row">
                <div className="kpi-icon"><I size={18}/></div>
              </div>
              <div className="kpi-label">{k.l}</div>
              <div className="kpi-value tabular">{k.v}</div>
              <div className="kpi-foot">{k.sub}</div>
            </div>
          );
        })}
      </div>

      {/* Headcount & demographics */}
      <div style={{display:'grid', gridTemplateColumns:'2fr 1fr', gap:16, marginBottom:20}}>
        <div className="card">
          <div className="card-head">
            <div className="card-title">Effectif & turnover · 12 derniers mois</div>
            <div className="row gap-2">
              <button className="chip orange active">Effectif</button>
              <button className="chip">Turnover</button>
              <button className="chip">Coût</button>
            </div>
          </div>
          <div style={{padding:'18px 22px'}}>
            <Line data={[298, 305, 310, 318, 322, 328, 332, 336, 338, 340, 341, 342]}
              labels={['Jui','Juil','Aoû','Sep','Oct','Nov','Déc','Jan','Fév','Mar','Avr','Mai']}
              color="#F97316" fill="#F97316" h={220}/>
          </div>
        </div>
        <div className="card">
          <div className="card-head"><div className="card-title">Pyramide des âges</div></div>
          <div style={{padding:18}}>
            {[
              { age:'< 25', m:18, f:12, pct:9 },
              { age:'25–34', m:62, f:48, pct:32 },
              { age:'35–44', m:54, f:41, pct:28 },
              { age:'45–54', m:38, f:24, pct:18 },
              { age:'55+', m:28, f:17, pct:13 },
            ].map((r,i) => (
              <div key={i} style={{marginBottom:10}}>
                <div className="row" style={{marginBottom:4, fontSize:11.5}}>
                  <span style={{color:'var(--ink-3)', fontWeight:600, width:50}}>{r.age}</span>
                  <span className="tabular muted">H {r.m}</span>
                  <span className="tabular" style={{marginLeft:'auto'}}>F {r.f}</span>
                </div>
                <div className="row gap-1">
                  <div style={{flex: r.m, height:14, background:'var(--orange-500)', borderRadius:'4px 0 0 4px', position:'relative'}}/>
                  <div style={{flex: r.f, height:14, background:'var(--blue-500)', borderRadius:'0 4px 4px 0'}}/>
                </div>
              </div>
            ))}
            <div className="divider"/>
            <div className="row gap-3" style={{fontSize:11.5}}>
              <span className="row gap-1"><div style={{width:10,height:10,background:'var(--orange-500)',borderRadius:2}}/> Hommes 200 (58%)</span>
              <span className="row gap-1"><div style={{width:10,height:10,background:'var(--blue-500)',borderRadius:2}}/> Femmes 142 (42%)</span>
            </div>
          </div>
        </div>
      </div>

      {/* Cost & engagement */}
      <div style={{display:'grid', gridTemplateColumns:'1fr 1fr 1fr', gap:16, marginBottom:20}}>
        <div className="card">
          <div className="card-head"><div className="card-title">Coût employé · trend</div></div>
          <div style={{padding:18}}>
            <Bars data={[
              { label:'Q1 25', value: 4.8, color:'#FFB066' },
              { label:'Q2 25', value: 4.85, color:'#FB923C' },
              { label:'Q3 25', value: 4.95, color:'#F97316' },
              { label:'Q4 25', value: 5.12, color:'#EA580C' },
              { label:'Q1 26', value: 5.02, color:'#C2410C' },
            ]} h={160}/>
            <div className="muted" style={{fontSize:11, textAlign:'center', marginTop:6}}>Coût total / ETP en millions XAF/an</div>
          </div>
        </div>
        <div className="card">
          <div className="card-head"><div className="card-title">Engagement · eNPS</div></div>
          <div style={{padding:18, textAlign:'center'}}>
            <div style={{fontFamily:'Inter Tight', fontSize:64, fontWeight:800, lineHeight:1, letterSpacing:'-0.03em'}} className="tabular">
              <span style={{color:'var(--green-500)'}}>+78</span>
            </div>
            <div style={{fontSize:12, color:'var(--ink-3)', marginTop:6}}>excellent (≥ 50) · sondage Q1</div>
            <div className="split-bar" style={{marginTop:18}}>
              <div style={{width:'82%', background:'var(--green-500)'}}>Promot. 82%</div>
              <div style={{width:'14%', background:'var(--amber-500)'}}>Pass. 14%</div>
              <div style={{width:'4%', background:'var(--red-500)'}}>Détr.</div>
            </div>
            <div className="row" style={{marginTop:18, fontSize:11, color:'var(--ink-3)'}}>
              <span>Réponses</span>
              <span style={{marginLeft:'auto'}} className="tabular"><b style={{color:'var(--ink)'}}>314</b> / 342 (92%)</span>
            </div>
          </div>
        </div>
        <div className="card">
          <div className="card-head"><div className="card-title">Top motifs de départ</div></div>
          <div style={{padding:18}}>
            {[
              { l:'Rémunération', n:8, pct:42, c:'#F97316' },
              { l:'Opportunité externe', n:5, pct:26, c:'#FB923C' },
              { l:'Management', n:3, pct:16, c:'#FCD34D' },
              { l:'Reconversion', n:2, pct:11, c:'#34D399' },
              { l:'Autre', n:1, pct:5, c:'#94A3B8' },
            ].map((r,i) => (
              <div key={i} style={{marginBottom:10}}>
                <div className="row" style={{marginBottom:4, fontSize:12}}>
                  <span>{r.l}</span>
                  <span className="muted tabular" style={{marginLeft:'auto'}}>{r.n} départs · {r.pct}%</span>
                </div>
                <div className="bar thin"><div style={{width: r.pct + '%', background:r.c}}/></div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Department deep dive */}
      <div className="card">
        <div className="card-head">
          <div>
            <div className="card-title">Performance par département</div>
            <div className="muted" style={{fontSize:12, marginTop:2}}>Comparatif Q1 2026</div>
          </div>
        </div>
        <table className="t">
          <thead><tr><th>Département</th><th>Effectif</th><th>Coût (M XAF/an)</th><th>Turnover</th><th>Absent.</th><th>Form. (h/pers)</th><th>Score perf.</th><th>eNPS</th></tr></thead>
          <tbody>
            {[
              { n:'Engineering', size:124, cost:684, tov:8.2, abs:2.8, form:18, perf:4.5, enps:84 },
              { n:'Opérations', size:78, cost:312, tov:14.4, abs:5.2, form:8, perf:4.0, enps:62 },
              { n:'Commercial', size:52, cost:286, tov:11.2, abs:3.4, form:14, perf:4.3, enps:78 },
              { n:'Support', size:36, cost:154, tov:6.4, abs:4.1, form:10, perf:4.1, enps:72 },
              { n:'Finance & RH', size:28, cost:142, tov:4.2, abs:2.2, form:22, perf:4.6, enps:88 },
              { n:'Direction', size:24, cost:198, tov:0, abs:1.4, form:28, perf:4.8, enps:92 },
            ].map((r,i) => (
              <tr key={i}>
                <td style={{fontSize:13, fontWeight:600}}>{r.n}</td>
                <td className="tabular">{r.size}</td>
                <td className="tabular" style={{fontWeight:600}}>{r.cost}</td>
                <td>
                  <span className={"badge " + (r.tov > 12 ? 'red' : r.tov > 8 ? 'amber' : 'green')}>{r.tov}%</span>
                </td>
                <td className="tabular">{r.abs}%</td>
                <td className="tabular">{r.form} h</td>
                <td>
                  <div className="row gap-2">
                    <span style={{fontWeight:700, fontFamily:'Inter Tight'}} className="tabular">{r.perf}</span>
                    <div className="bar thin" style={{width:60}}><div style={{width:`${(r.perf/5)*100}%`}}/></div>
                  </div>
                </td>
                <td>
                  <span style={{color: r.enps > 75 ? 'var(--green-600)' : r.enps > 50 ? 'var(--amber-600)' : 'var(--red-600)', fontWeight:700}} className="tabular">+{r.enps}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

window.PageAnalytics = PageAnalytics;
