/* global React */
const { useState: useS_lv } = React;

function PageLeaves() {
  const { Icons, Avatar, PageHeader } = window;
  const [tab, setTab] = useS_lv('requests');

  const requests = [
    { id:'LR-0421', user:'Aminata Diallo', avatar:'orange', type:'Annuel', days:5, from:'18 mai 2026', to:'22 mai 2026', status:'pending', submitted:'il y a 2j' },
    { id:'LR-0420', user:'Joseph Mbarga', avatar:'blue', type:'Maladie', days:3, from:'13 mai 2026', to:'15 mai 2026', status:'pending', submitted:'il y a 12h' },
    { id:'LR-0419', user:'Sarah Nguemo', avatar:'violet', type:'Sans solde', days:2, from:'09 mai 2026', to:'10 mai 2026', status:'approved', submitted:'il y a 4j' },
    { id:'LR-0418', user:'Pierre Kouam', avatar:'amber', type:'Annuel', days:10, from:'25 mai 2026', to:'05 juin 2026', status:'pending', submitted:'il y a 6h' },
    { id:'LR-0417', user:'Marc Foga', avatar:'green', type:'Annuel', days:7, from:'14 juin 2026', to:'20 juin 2026', status:'approved', submitted:'il y a 1 sem.' },
    { id:'LR-0416', user:'Karine Djoumessi', avatar:'blue', type:'Maternité', days:98, from:'01 juin 2026', to:'06 sept. 2026', status:'approved', submitted:'il y a 2 sem.' },
    { id:'LR-0415', user:'Hervé Tankeu', avatar:'orange', type:'Annuel', days:5, from:'05 mai 2026', to:'09 mai 2026', status:'rejected', submitted:'il y a 1 sem.' },
  ];

  const statusBadge = s => s === 'approved' ? <span className="badge green">Approuvée</span>
    : s === 'pending' ? <span className="badge amber">En attente</span>
    : <span className="badge red">Refusée</span>;

  const days = ['L', 'M', 'M', 'J', 'V', 'S', 'D'];
  const month = [];
  for (let d = 1; d <= 31; d++) month.push(d);

  return (
    <div>
      <PageHeader
        uc="Activité"
        title="Congés"
        subtitle="Gestion des demandes, soldes et calendrier des absences"
        actions={
          <>
            <button className="btn btn-secondary"><Icons.cal size={14}/> Calendrier</button>
            <button className="btn btn-primary"><Icons.plus/> Nouvelle demande</button>
          </>
        }
      />

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'À approuver', v:'7', sub:'3 urgentes', tone:'amber' },
          { l:'En cours', v:'12', sub:'employés absents aujourd\'hui', tone:'blue' },
          { l:'Solde moyen', v:'18,4 j', sub:'sur 28 acquis', tone:'orange' },
          { l:'Taux d\'absentéisme', v:'3,4%', sub:'-0,8pt vs mois passé', tone:'green' },
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

      <div style={{display:'grid', gridTemplateColumns:'1.4fr 1fr', gap:16}}>
        {/* Requests */}
        <div className="card">
          <div className="card-head">
            <div>
              <div className="card-title">Demandes</div>
              <div className="muted" style={{fontSize:12, marginTop:2}}>{requests.length} demandes sur les 30 derniers jours</div>
            </div>
            <div className="row gap-2">
              <button className="chip active">Toutes</button>
              <button className="chip amber">En attente (3)</button>
              <button className="chip">Approuvées</button>
            </div>
          </div>
          <table className="t">
            <thead><tr><th>Employé</th><th>Type</th><th>Période</th><th>Jours</th><th>Statut</th><th></th></tr></thead>
            <tbody>
              {requests.map(r => (
                <tr key={r.id}>
                  <td>
                    <div className="row gap-2">
                      <Avatar name={r.user} color={r.avatar} size="sm"/>
                      <div>
                        <div style={{fontSize:13, fontWeight:600}}>{r.user}</div>
                        <div className="mono" style={{fontSize:10.5, color:'var(--ink-4)'}}>{r.id} · {r.submitted}</div>
                      </div>
                    </div>
                  </td>
                  <td>
                    <span className={"badge " + (r.type === 'Annuel' ? 'orange' : r.type === 'Maladie' ? 'red' : r.type === 'Maternité' ? 'violet' : 'gray')}>{r.type}</span>
                  </td>
                  <td>
                    <div style={{fontSize:12}}>{r.from}</div>
                    <div style={{fontSize:11, color:'var(--ink-3)'}}>→ {r.to}</div>
                  </td>
                  <td className="tabular" style={{fontWeight:600}}>{r.days}j</td>
                  <td>{statusBadge(r.status)}</td>
                  <td>
                    {r.status === 'pending' ? (
                      <div className="row gap-2">
                        <button className="btn btn-secondary btn-sm" style={{padding:'4px 8px'}}><Icons.x size={12}/></button>
                        <button className="btn btn-primary btn-sm"><Icons.check size={12}/></button>
                      </div>
                    ) : <button className="icon-btn" style={{width:28, height:28}}><Icons.chevR size={12}/></button>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Calendar */}
        <div className="card">
          <div className="card-head">
            <div>
              <div className="card-title">Calendrier · Mai 2026</div>
              <div className="muted" style={{fontSize:12, marginTop:2}}>12 absences planifiées</div>
            </div>
            <div className="row gap-2">
              <button className="icon-btn" style={{width:28, height:28}}><Icons.chevR size={12} style={{transform:'rotate(180deg)'}}/></button>
              <button className="icon-btn" style={{width:28, height:28}}><Icons.chevR size={12}/></button>
            </div>
          </div>
          <div style={{padding:18}}>
            <div style={{display:'grid', gridTemplateColumns:'repeat(7, 1fr)', gap:4, marginBottom:6}}>
              {days.map((d,i) => <div key={i} style={{fontSize:10, color:'var(--ink-3)', textAlign:'center', padding:'4px 0', fontWeight:600}}>{d}</div>)}
            </div>
            <div style={{display:'grid', gridTemplateColumns:'repeat(7, 1fr)', gap:4}}>
              {[null,null,null,null].map((_,i) => <div key={'p'+i}/>)}
              {month.map(d => {
                const isAbs = [5,6,7,8,9,13,14,15,18,19,20,21,22,25,26].includes(d);
                const isToday = d === 14;
                const isWk = (d + 4) % 7 < 2;
                return (
                  <div key={d} style={{
                    aspectRatio:'1', display:'grid', placeItems:'center',
                    borderRadius:8,
                    background: isToday ? 'var(--grad-orange)' : isAbs ? 'var(--orange-50)' : isWk ? 'var(--bg-soft)' : 'transparent',
                    color: isToday ? '#fff' : isWk ? 'var(--ink-4)' : 'var(--ink-2)',
                    fontSize:12, fontWeight: isToday ? 700 : 500, position:'relative'
                  }}>
                    {d}
                    {isAbs && !isToday && <span style={{position:'absolute', bottom:3, width:4, height:4, borderRadius:'50%', background:'var(--orange-500)'}}/>}
                  </div>
                );
              })}
            </div>
            <div className="divider"/>
            <div style={{fontSize:12, fontWeight:600, marginBottom:8}}>Cette semaine</div>
            {[
              { n:'Aminata D.', t:'Annuel', d:'18→22 mai', c:'orange' },
              { n:'Joseph M.', t:'Maladie', d:'13→15 mai', c:'blue' },
            ].map((a,i) => (
              <div key={i} className="row gap-2" style={{padding:'8px 0', borderTop: i>0 ? '1px solid var(--line-soft)' : 'none'}}>
                <Avatar name={a.n} color={a.c} size="sm"/>
                <div style={{flex:1}}>
                  <div style={{fontSize:12.5, fontWeight:600}}>{a.n}</div>
                  <div style={{fontSize:11, color:'var(--ink-3)'}}>{a.t} · {a.d}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Leave balances */}
      <div className="section-title">Soldes de congés par employé</div>
      <div className="card">
        <table className="t">
          <thead><tr><th>Employé</th><th>Annuel acquis</th><th>Annuel pris</th><th>Annuel restant</th><th>Maladie</th><th>RTT</th><th>Solde total (jours)</th></tr></thead>
          <tbody>
            {[
              { n:'Aminata Diallo', c:'orange', acq:28, taken:9.5, rest:18.5, mal:2, rtt:6 },
              { n:'Joseph Mbarga', c:'blue', acq:28, taken:14, rest:14, mal:3, rtt:5 },
              { n:'Marc Foga', c:'green', acq:30, taken:5, rest:25, mal:0, rtt:8 },
              { n:'Sarah Nguemo', c:'violet', acq:28, taken:2, rest:26, mal:1, rtt:4 },
              { n:'Estelle Bilong', c:'amber', acq:28, taken:0, rest:28, mal:0, rtt:0 },
            ].map((r,i) => (
              <tr key={i}>
                <td>
                  <div className="row gap-2">
                    <Avatar name={r.n} color={r.c} size="sm"/>
                    <span style={{fontSize:13, fontWeight:600}}>{r.n}</span>
                  </div>
                </td>
                <td className="tabular">{r.acq} j</td>
                <td className="tabular muted">{r.taken} j</td>
                <td>
                  <div className="row gap-2">
                    <span className="tabular" style={{fontWeight:600}}>{r.rest} j</span>
                    <div className="bar thin" style={{width:80}}><div style={{width: `${(r.rest/r.acq)*100}%`}}/></div>
                  </div>
                </td>
                <td className="tabular">{r.mal} j</td>
                <td className="tabular">{r.rtt} j</td>
                <td className="tabular" style={{fontWeight:600}}>{r.rest + r.rtt} j</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

window.PageLeaves = PageLeaves;
