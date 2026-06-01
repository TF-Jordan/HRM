/* global React */

function PageBudget() {
  const { Icons, PageHeader, Donut, Line, Bars } = window;

  const depts = [
    { name:'Engineering', allocated:18.5, spent:12.8, pct:69, color:'#F97316' },
    { name:'Commercial', allocated:8.2, spent:4.6, pct:56, color:'#FB923C' },
    { name:'Opérations', allocated:6.8, spent:5.4, pct:79, color:'#FCD34D' },
    { name:'Support', allocated:3.2, spent:1.1, pct:34, color:'#34D399' },
    { name:'Finance & RH', allocated:2.8, spent:2.2, pct:79, color:'#60A5FA' },
    { name:'Direction', allocated:4.5, spent:1.8, pct:40, color:'#A78BFA' },
  ];

  return (
    <div>
      <PageHeader
        uc="Développement"
        title="Budget de formation"
        subtitle="Allocation, consommation et reste à dépenser · Année 2026"
        actions={
          <>
            <button className="btn btn-secondary"><Icons.download/> Rapport CNFPP</button>
            <button className="btn btn-primary"><Icons.plus/> Allouer un budget</button>
          </>
        }
      />

      {/* Hero KPI - budget overview */}
      <div className="card card-pad-lg" style={{
        background:'linear-gradient(135deg, #FFF4EB 0%, #FFFAF2 60%, #FFFFFF 100%)',
        border:'1px solid var(--orange-200)', marginBottom:20
      }}>
        <div className="row" style={{alignItems:'flex-start'}}>
          <div style={{flex:1}}>
            <div className="muted" style={{fontSize:11, textTransform:'uppercase', letterSpacing:'0.06em', fontWeight:700}}>Budget annuel 2026</div>
            <div style={{fontFamily:'Inter Tight', fontSize:48, fontWeight:800, letterSpacing:'-0.03em', lineHeight:1, marginTop:6}} className="tabular">44,0M <span style={{fontSize:24, color:'var(--ink-3)'}}>XAF</span></div>
            <div style={{fontSize:14, color:'var(--ink-2)', marginTop:8}}>
              <b className="tabular">27,9M</b> consommé · <b className="tabular">16,1M</b> disponible · <b>63%</b> utilisé
            </div>
            <div style={{marginTop:14, maxWidth:520}}>
              <div className="split-bar">
                <div style={{width:'63%', background:'var(--grad-orange)'}}>Consommé 63%</div>
                <div style={{width:'15%', background:'var(--orange-200)', color:'var(--ink-2)'}}>Engagé 15%</div>
                <div style={{width:'22%', background:'var(--bg-soft)', color:'var(--ink-2)'}}>Libre 22%</div>
              </div>
              <div className="muted" style={{fontSize:11, marginTop:6}}>Au 14 mai · à mi-année théorique 38% — vous êtes en avance 🎯</div>
            </div>
          </div>

          <div style={{position:'relative'}}>
            <Donut data={[
              { value:62, color:'#F97316' },
              { value:18, color:'#FB923C' },
              { value:8, color:'#FCD34D' },
              { value:12, color:'#34D399' },
            ]} size={180} thick={26}/>
            <div style={{position:'absolute', inset:0, display:'grid', placeItems:'center', textAlign:'center'}}>
              <div>
                <div style={{fontFamily:'Inter Tight', fontSize:26, fontWeight:800}} className="tabular">63%</div>
                <div style={{fontSize:11, color:'var(--ink-3)'}}>consommé</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div style={{display:'grid', gridTemplateColumns:'2fr 1fr', gap:16, marginBottom:20}}>
        <div className="card">
          <div className="card-head">
            <div>
              <div className="card-title">Consommation par département</div>
              <div className="muted" style={{fontSize:12, marginTop:2}}>Allocation vs dépensé en millions XAF</div>
            </div>
          </div>
          <div style={{padding:'18px 22px'}}>
            {depts.map((d,i) => (
              <div key={i} style={{marginBottom:16}}>
                <div className="row" style={{marginBottom:6}}>
                  <span style={{fontSize:13, fontWeight:600}}>{d.name}</span>
                  <span className="muted tabular" style={{marginLeft:'auto', fontSize:12}}>
                    <b style={{color:'var(--ink)'}}>{d.spent}M</b> / {d.allocated}M XAF
                  </span>
                </div>
                <div className="row gap-2">
                  <div className="bar" style={{flex:1, height:14}}>
                    <div style={{width: `${d.pct}%`, background: d.color}}/>
                  </div>
                  <span className="tabular" style={{fontSize:12, fontWeight:600, minWidth:42, textAlign:'right'}}>{d.pct}%</span>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="col gap-3">
          <div className="card card-pad">
            <div className="muted" style={{fontSize:11, textTransform:'uppercase', letterSpacing:'0.05em', fontWeight:600}}>Heures de formation</div>
            <div style={{fontFamily:'Inter Tight', fontSize:30, fontWeight:800, marginTop:6}} className="tabular">4 280 <span style={{fontSize:14, color:'var(--ink-3)'}}>h</span></div>
            <div style={{fontSize:12, color:'var(--ink-3)', marginTop:2}}>12,5h par employé · obj. 24h</div>
            <div style={{marginTop:8}}>
              <Bars data={[
                { label:'Jan', value: 320 },
                { label:'Fév', value: 280 },
                { label:'Mar', value: 410 },
                { label:'Avr', value: 380 },
                { label:'Mai', value: 290 },
              ]} h={80}/>
            </div>
          </div>

          <div className="card card-pad">
            <div className="card-title" style={{fontSize:14, marginBottom:12}}>Top formations</div>
            {[
              { n:'AWS Cloud Architect', h:380, c:'#F97316' },
              { n:'Leadership avancé', h:240, c:'#FB923C' },
              { n:'Sécurité info', h:480, c:'#34D399' },
              { n:'Design Thinking', h:96, c:'#60A5FA' },
            ].map((t,i) => (
              <div key={i} className="row" style={{padding:'6px 0', fontSize:12}}>
                <span style={{width:8, height:8, borderRadius:2, background:t.c}}/>
                <span style={{flex:1, marginLeft:8}}>{t.n}</span>
                <span className="tabular muted">{t.h}h</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-head">
          <div className="card-title">Détail des engagements</div>
          <div className="row gap-2">
            <button className="chip orange active">2026</button>
            <button className="chip">Q2</button>
            <button className="chip">Engagé</button>
          </div>
        </div>
        <table className="t">
          <thead><tr><th>Département</th><th>Manager</th><th>Allocation (M XAF)</th><th>Consommé</th><th>Engagé</th><th>Disponible</th><th>Inscrits</th><th>Heures réalisées</th></tr></thead>
          <tbody>
            {depts.map((d,i) => (
              <tr key={i}>
                <td><div className="row gap-2"><span style={{width:8, height:8, borderRadius:2, background:d.color}}/><span style={{fontSize:13, fontWeight:600}}>{d.name}</span></div></td>
                <td className="muted">{['Marc Foga','Hervé Tankeu','Estelle Bilong','Diane Tsoumou','Yannick Etoa','Faïsal Sab'][i]}</td>
                <td className="tabular" style={{fontWeight:700}}>{d.allocated}</td>
                <td className="tabular">{d.spent}</td>
                <td className="tabular muted">{(d.allocated * 0.12).toFixed(1)}</td>
                <td className="tabular" style={{color:'var(--green-600)', fontWeight:600}}>{(d.allocated - d.spent - d.allocated*0.12).toFixed(1)}</td>
                <td className="tabular">{[42,18,14,8,6,4][i]}</td>
                <td className="tabular">{[1240,560,820,260,180,140][i]}h</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

window.PageBudget = PageBudget;
