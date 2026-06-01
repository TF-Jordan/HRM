/* global React */
const { useState: useS_ets, useEffect: useE_ets } = React;

function EmpPageTime() {
  const { Icons, PageHeader, Bars } = window;
  const [clockedIn, setClockedIn] = useS_ets(true);
  const [now, setNow] = useS_ets('16:46');

  return (
    <div>
      <PageHeader
        uc="Activité"
        title="Mon temps"
        subtitle="Pointage, feuilles de temps et heures supplémentaires"
        actions={
          <button className="btn btn-primary"><Icons.plus/> Saisir une journée</button>
        }
      />

      {/* Clock-in hero */}
      <div className="card card-pad-lg" style={{
        background:'linear-gradient(135deg, #1A150E 0%, #2D2520 100%)',
        color:'#fff', marginBottom:20, position:'relative', overflow:'hidden'
      }}>
        <div style={{position:'absolute', top:-100, right:-50, width:400, height:400,
          background:'radial-gradient(circle, rgba(249,115,22,0.25) 0%, transparent 70%)'}}/>
        <div className="row" style={{alignItems:'center', position:'relative'}}>
          <div style={{flex:1}}>
            <div className="row gap-2">
              <span className="badge green" style={{background:'rgba(16,185,129,0.2)', color:'#34D399'}}>● En activité</span>
              <span style={{fontSize:13, opacity:0.7}}>Mardi 14 mai 2026 · semaine 20</span>
            </div>
            <div className="h-display" style={{fontSize:64, color:'#fff', marginTop:8, fontFamily:'JetBrains Mono', fontWeight:600, letterSpacing:'-0.025em'}}>{now}</div>
            <div style={{fontSize:14, opacity:0.7, marginTop:4}}>7h22 travaillées · Arrivée à 08:24 · 45 min de pause</div>
          </div>
          <div className="col gap-3">
            <button className={"btn " + (clockedIn ? "btn-dark" : "btn-primary")}
              style={clockedIn ? {background:'#fff', color:'var(--ink)', padding:'14px 28px'} : {padding:'14px 28px'}}
              onClick={() => setClockedIn(!clockedIn)}>
              {clockedIn ? <><Icons.x size={16}/> Déclarer mon départ</> : <><Icons.check size={16}/> Pointer mon arrivée</>}
            </button>
            <button className="btn" style={{background:'rgba(255,255,255,0.1)', color:'#fff', border:'1px solid rgba(255,255,255,0.2)', justifyContent:'center'}}>
              ☕ Démarrer une pause
            </button>
          </div>
        </div>
      </div>

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'Cette semaine', v:'37,5 h', sub:'sur 40h prévues', tone:'orange' },
          { l:'Ce mois', v:'142 h', sub:'sur 173h · 82%', tone:'blue' },
          { l:'Heures sup. (mois)', v:'12 h', sub:'majorées 125%', tone:'amber' },
          { l:'Retards (mois)', v:'2', sub:'15 min cumulées', tone:'red' },
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
        {/* Weekly timesheet */}
        <div className="card">
          <div className="card-head">
            <div>
              <div className="card-title">Ma semaine · 11 → 17 mai</div>
              <div className="muted" style={{fontSize:12, marginTop:2}}>Brouillon · à soumettre vendredi 17 mai</div>
            </div>
            <div className="row gap-2">
              <button className="icon-btn" style={{width:28, height:28}}><Icons.chevR size={12} style={{transform:'rotate(180deg)'}}/></button>
              <button className="icon-btn" style={{width:28, height:28}}><Icons.chevR size={12}/></button>
            </div>
          </div>
          <div style={{padding:18}}>
            {[
              { d:'Lun 11', in:'08:18', out:'17:32', total:8.2, breaks:'45 min', status:'recorded', notes:'En réunion (10h-12h)' },
              { d:'Mar 12', in:'08:05', out:'17:48', total:8.4, breaks:'1h00', status:'recorded', notes:'' },
              { d:'Mer 13', in:'08:32', out:'17:15', total:7.9, breaks:'45 min', status:'recorded', notes:'Télétravail' },
              { d:'Jeu 14', in:'08:24', out:'—', total:7.2, breaks:'45 min', status:'live', notes:'En cours · jusqu\'à 17h30 prévu' },
              { d:'Ven 15', in:'—', out:'—', total:0, breaks:'—', status:'planned', notes:'Prévu 7h' },
            ].map((day, i) => (
              <div key={i} className="row gap-3" style={{padding:'14px 12px', borderBottom: i < 4 ? '1px solid var(--line-soft)' : 'none', borderRadius:8,
                background: day.status === 'live' ? 'var(--orange-50)' : 'transparent',
                marginBottom: day.status === 'live' ? 4 : 0
              }}>
                <div style={{width:60, fontSize:13, fontWeight:700}}>
                  {day.d}
                  {day.status === 'live' && <div style={{fontSize:10, color:'var(--orange-600)', fontWeight:600, marginTop:2}}>● Live</div>}
                </div>
                <div style={{flex:1}}>
                  <div className="row gap-3">
                    <div>
                      <div style={{fontSize:10, color:'var(--ink-3)', textTransform:'uppercase', fontWeight:600}}>Arrivée</div>
                      <div className="mono tabular" style={{fontSize:13, fontWeight:600}}>{day.in}</div>
                    </div>
                    <div>
                      <div style={{fontSize:10, color:'var(--ink-3)', textTransform:'uppercase', fontWeight:600}}>Départ</div>
                      <div className="mono tabular" style={{fontSize:13, fontWeight:600}}>{day.out}</div>
                    </div>
                    <div>
                      <div style={{fontSize:10, color:'var(--ink-3)', textTransform:'uppercase', fontWeight:600}}>Pauses</div>
                      <div className="mono tabular" style={{fontSize:13, color:'var(--ink-3)'}}>{day.breaks}</div>
                    </div>
                    <div style={{marginLeft:'auto', textAlign:'right'}}>
                      <div style={{fontSize:10, color:'var(--ink-3)', textTransform:'uppercase', fontWeight:600}}>Total</div>
                      <div style={{fontSize:18, fontFamily:'Inter Tight', fontWeight:800}} className="tabular">
                        {day.total > 0 ? day.total + 'h' : '—'}
                      </div>
                    </div>
                  </div>
                  {day.notes && <div style={{fontSize:11.5, color:'var(--ink-3)', marginTop:6, fontStyle:'italic'}}>{day.notes}</div>}
                </div>
              </div>
            ))}

            <div className="row" style={{padding:'14px 12px', borderTop:'2px solid var(--ink)', marginTop:8, background:'var(--bg-dim)', borderRadius:10}}>
              <div style={{fontSize:14, fontWeight:700}}>Total semaine</div>
              <div style={{marginLeft:'auto', fontSize:22, fontFamily:'Inter Tight', fontWeight:800}} className="tabular">31,7 h <span style={{fontSize:13, color:'var(--ink-3)', fontWeight:400}}>/ 40h</span></div>
            </div>

            <div className="row gap-2" style={{marginTop:14}}>
              <button className="btn btn-secondary" style={{flex:1, justifyContent:'center'}}><Icons.edit size={13}/> Modifier</button>
              <button className="btn btn-primary" style={{flex:1, justifyContent:'center'}}><Icons.send size={13}/> Soumettre la feuille</button>
            </div>
          </div>
        </div>

        {/* Monthly chart */}
        <div className="card">
          <div className="card-head"><div className="card-title">Mes heures · mai</div></div>
          <div style={{padding:18}}>
            <Bars data={[
              { label:'S18', value: 38 },
              { label:'S19', value: 41 },
              { label:'S20', value: 32, color:'var(--orange-500)' },
              { label:'S21', value: 35 },
              { label:'S22', value: 0, color:'var(--bg-soft)' },
            ]} h={140}/>
            <div className="divider"/>
            <div className="row gap-2" style={{fontSize:12, justifyContent:'space-between', padding:'2px 0'}}>
              <span style={{color:'var(--ink-3)'}}>Moyenne hebdo</span>
              <b className="tabular">36,5 h</b>
            </div>
            <div className="row gap-2" style={{fontSize:12, justifyContent:'space-between', padding:'2px 0'}}>
              <span style={{color:'var(--ink-3)'}}>Heures sup. mois</span>
              <b className="tabular" style={{color:'var(--orange-600)'}}>+12 h</b>
            </div>
            <div className="row gap-2" style={{fontSize:12, justifyContent:'space-between', padding:'2px 0'}}>
              <span style={{color:'var(--ink-3)'}}>Rémunération sup.</span>
              <b className="tabular">~ 59 000 XAF</b>
            </div>
          </div>
        </div>
      </div>

      <div className="section-title">Historique des feuilles de temps</div>
      <div className="card">
        <table className="t">
          <thead><tr><th>Semaine</th><th>Heures normales</th><th>Heures sup.</th><th>Total</th><th>Statut</th><th>Validée par</th><th></th></tr></thead>
          <tbody>
            {[
              { w:'S20 · 11-17 mai 2026', n:'31,7h', s:'0h', t:'31,7h', st:'draft', by:'—' },
              { w:'S19 · 04-10 mai 2026', n:'40h', s:'3h', t:'43h', st:'approved', by:'Marc Foga' },
              { w:'S18 · 27 avr - 3 mai', n:'35h', s:'5h', t:'40h', st:'approved', by:'Marc Foga' },
              { w:'S17 · 20-26 avr 2026', n:'38h', s:'2h', t:'40h', st:'approved', by:'Marc Foga' },
              { w:'S16 · 13-19 avr 2026', n:'30h', s:'0h', t:'30h', st:'approved', by:'Marc Foga' },
            ].map((r,i) => (
              <tr key={i}>
                <td style={{fontSize:13, fontWeight:600}}>{r.w}</td>
                <td className="tabular">{r.n}</td>
                <td className="tabular" style={{color:'var(--orange-600)', fontWeight:600}}>{r.s}</td>
                <td className="tabular" style={{fontWeight:700}}>{r.t}</td>
                <td>
                  {r.st === 'draft' && <span className="badge gray">Brouillon</span>}
                  {r.st === 'approved' && <span className="badge green">Validée</span>}
                </td>
                <td className="muted">{r.by}</td>
                <td><button className="icon-btn" style={{width:28, height:28}}><Icons.chevR size={12}/></button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
window.EmpPageTime = EmpPageTime;
