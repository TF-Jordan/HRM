/* global React */

function PageTime() {
  const { Icons, Avatar, PageHeader, Line, Bars } = window;

  const hours = ['8','9','10','11','12','13','14','15','16','17','18'];
  const today = [
    { n:'Aminata Diallo', c:'orange', in:'08:24', status:'present', hrs:'7h22', breaks:'0h45', notes:'En réunion' },
    { n:'Joseph Mbarga', c:'blue', in:'07:58', status:'present', hrs:'8h12', breaks:'1h00', notes:'Visite client' },
    { n:'Sarah Nguemo', c:'violet', in:'09:12', status:'present', hrs:'6h48', breaks:'0h30', notes:'Télétravail' },
    { n:'Pierre Kouam', c:'amber', in:'—', status:'absent', hrs:'—', breaks:'—', notes:'Maladie' },
    { n:'Marc Foga', c:'green', in:'08:00', status:'present', hrs:'8h00', breaks:'1h00', notes:'' },
    { n:'Karine D.', c:'blue', in:'08:34', status:'late', hrs:'7h10', breaks:'0h45', notes:'Trafic' },
    { n:'Olivier Manga', c:'green', in:'08:05', status:'present', hrs:'7h55', breaks:'1h00', notes:'' },
  ];

  return (
    <div>
      <PageHeader
        uc="Activité"
        title="Temps & Présences"
        subtitle="Suivi des feuilles de temps, pointages et heures supplémentaires"
        actions={
          <>
            <button className="btn btn-secondary"><Icons.cal size={14}/> Semaine</button>
            <button className="btn btn-primary"><Icons.plus/> Saisir manuellement</button>
          </>
        }
      />

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'Présents aujourd\'hui', v:'298/342', sub:'87% · 6 retards', tone:'green' },
          { l:'Absents', v:'44', sub:'31 congé · 8 maladie · 5 mission', tone:'red' },
          { l:'Heures sup. (mois)', v:'1 248 h', sub:'+12% vs avril', tone:'orange' },
          { l:'Temps moyen / jour', v:'8h12', sub:'cible 8h00', tone:'blue' },
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
        <div className="card">
          <div className="card-head">
            <div>
              <div className="card-title">Pointages d'aujourd'hui</div>
              <div className="muted" style={{fontSize:12, marginTop:2}}>14 mai 2026 · 16:42 actualisé il y a 2 min</div>
            </div>
            <div className="row gap-2">
              <button className="chip orange active">Tous</button>
              <button className="chip">Présents (298)</button>
              <button className="chip">Retards (6)</button>
              <button className="chip">Absents (44)</button>
            </div>
          </div>
          <table className="t">
            <thead><tr><th>Employé</th><th>Arrivée</th><th>Heures travaillées</th><th>Pauses</th><th>Statut</th><th>Note</th></tr></thead>
            <tbody>
              {today.map((r,i) => (
                <tr key={i}>
                  <td><div className="row gap-2"><Avatar name={r.n} color={r.c} size="sm"/><span style={{fontSize:13, fontWeight:600}}>{r.n}</span></div></td>
                  <td className="mono tabular" style={{fontWeight:600}}>{r.in}</td>
                  <td className="mono tabular">{r.hrs}</td>
                  <td className="mono tabular muted">{r.breaks}</td>
                  <td>
                    {r.status === 'present' && <span className="badge green">Présent</span>}
                    {r.status === 'absent' && <span className="badge red">Absent</span>}
                    {r.status === 'late' && <span className="badge amber">Retard</span>}
                  </td>
                  <td className="muted" style={{fontSize:12}}>{r.notes}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="card">
          <div className="card-head">
            <div className="card-title">Tendance hebdo</div>
          </div>
          <div style={{padding:18}}>
            <Bars data={[
              { label:'Lun', value: 290 },
              { label:'Mar', value: 312 },
              { label:'Mer', value: 298 },
              { label:'Jeu', value: 304 },
              { label:'Ven', value: 285, color:'var(--orange-500)' },
              { label:'Sam', value: 96, color:'var(--bg-soft)' },
              { label:'Dim', value: 12, color:'var(--bg-soft)' },
            ]} h={180}/>
            <div className="divider"/>
            <div className="row gap-3" style={{fontSize:12}}>
              <div className="row gap-1"><div style={{width:10,height:10,background:'var(--orange-500)',borderRadius:3}}/> Présents</div>
              <div className="row gap-1"><div style={{width:10,height:10,background:'var(--bg-soft)',borderRadius:3}}/> Week-end</div>
            </div>
          </div>
        </div>
      </div>

      {/* Timesheets */}
      <div className="section-title">Feuilles de temps · Semaine 20 (11→17 mai 2026)</div>
      <div className="card">
        <table className="t">
          <thead>
            <tr>
              <th>Employé</th>
              <th>Lun</th><th>Mar</th><th>Mer</th><th>Jeu</th><th>Ven</th><th>Sam</th><th>Dim</th>
              <th>Total</th><th>Statut</th>
            </tr>
          </thead>
          <tbody>
            {[
              { n:'Aminata Diallo', c:'orange', d:[8.2,8.4,7.9,8.5,7.8,0,0], status:'pending' },
              { n:'Joseph Mbarga', c:'blue', d:[8.1,8.3,9.0,8.2,8.0,2.5,0], status:'submitted' },
              { n:'Sarah Nguemo', c:'violet', d:[7.5,8.0,8.0,8.0,7.0,0,0], status:'approved' },
              { n:'Marc Foga', c:'green', d:[9.0,8.5,8.5,9.0,8.0,0,0], status:'approved' },
              { n:'Olivier Manga', c:'green', d:[8.0,8.2,8.1,8.0,7.5,4.0,0], status:'pending' },
            ].map((r,i) => {
              const total = r.d.reduce((s,x)=>s+x,0).toFixed(1);
              return (
                <tr key={i}>
                  <td><div className="row gap-2"><Avatar name={r.n} color={r.c} size="sm"/><span style={{fontSize:13, fontWeight:600}}>{r.n}</span></div></td>
                  {r.d.map((h, j) => {
                    const isWk = j > 4;
                    return <td key={j} className="tabular" style={{fontWeight: h > 8 ? 700 : 400, color: h > 8 ? 'var(--orange-600)' : isWk && h === 0 ? 'var(--ink-4)' : 'var(--ink-2)'}}>{h ? h + 'h' : '—'}</td>;
                  })}
                  <td className="tabular" style={{fontWeight:700}}>{total}h</td>
                  <td>
                    {r.status === 'approved' && <span className="badge green">Validée</span>}
                    {r.status === 'pending' && <span className="badge amber">À valider</span>}
                    {r.status === 'submitted' && <span className="badge blue">Soumise</span>}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}

window.PageTime = PageTime;
