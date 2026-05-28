/* global React */

function PageLoans() {
  const { Icons, Avatar, PageHeader, Donut, Line } = window;

  const loans = [
    { id:'AV-2026-088', emp:'Diane Tsoumou', avc:'violet', type:'Avance sur salaire', amount:'350 000', months:1, monthly:'350 000', remaining:'350 000', status:'pending' },
    { id:'PR-2026-012', emp:'Aminata Diallo', avc:'orange', type:'Prêt personnel', amount:'2 800 000', months:12, monthly:'245 000', remaining:'1 470 000', status:'active' },
    { id:'AV-2026-087', emp:'Pierre Kouam', avc:'amber', type:'Avance sur salaire', amount:'150 000', months:1, monthly:'150 000', remaining:'150 000', status:'approved' },
    { id:'PR-2025-034', emp:'Marc Foga', avc:'green', type:'Prêt logement', amount:'12 000 000', months:48, monthly:'278 000', remaining:'4 280 000', status:'active' },
    { id:'PR-2025-029', emp:'Joseph Mbarga', avc:'blue', type:'Prêt véhicule', amount:'5 500 000', months:24, monthly:'242 000', remaining:'2 940 000', status:'active' },
    { id:'AV-2026-086', emp:'Karine Djoumessi', avc:'blue', type:'Avance sur salaire', amount:'200 000', months:1, monthly:'200 000', remaining:'0', status:'repaid' },
  ];

  return (
    <div>
      <PageHeader
        uc="Rémunération"
        title="Avances & Prêts"
        subtitle="Gestion des avances de salaire et prêts au personnel"
        actions={
          <>
            <button className="btn btn-secondary"><Icons.download/> Tableau d'amortissement</button>
            <button className="btn btn-primary"><Icons.plus/> Nouvelle demande</button>
          </>
        }
      />

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'En attente', v:'3', sub:'à approuver · 700K XAF', tone:'amber' },
          { l:'Prêts actifs', v:'18', sub:'42,8M XAF en encours', tone:'orange' },
          { l:'Remboursement (mois)', v:'4,2M', sub:'XAF · prélèvement paie', tone:'green' },
          { l:'Taux d\'incidents', v:'0%', sub:'aucun défaut · 12 mois', tone:'blue' },
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
            <div className="card-title">Évolution de l'encours</div>
            <div className="row gap-2">
              <button className="chip orange active">12M</button>
              <button className="chip">YTD</button>
            </div>
          </div>
          <div style={{padding:'18px 22px'}}>
            <Line data={[38,40,41,42,42,43,43,44,43,43,42,42.8]}
              labels={['Jui','Juil','Aoû','Sep','Oct','Nov','Déc','Jan','Fév','Mar','Avr','Mai']}
              color="#F97316" fill="#F97316" h={200}/>
          </div>
        </div>

        <div className="card">
          <div className="card-head"><div className="card-title">Répartition</div></div>
          <div style={{padding:18, display:'flex', flexDirection:'column', alignItems:'center', gap:14}}>
            <div style={{position:'relative'}}>
              <Donut data={[
                { value:42, color:'#F97316' },
                { value:28, color:'#FB923C' },
                { value:18, color:'#FCD34D' },
                { value:12, color:'#34D399' },
              ]} size={150} thick={20}/>
              <div style={{position:'absolute', inset:0, display:'grid', placeItems:'center'}}>
                <div style={{textAlign:'center'}}>
                  <div style={{fontFamily:'Inter Tight', fontSize:20, fontWeight:800}} className="tabular">42,8M</div>
                  <div style={{fontSize:11, color:'var(--ink-3)'}}>XAF encours</div>
                </div>
              </div>
            </div>
            <div style={{width:'100%'}}>
              {[
                ['#F97316','Prêts logement','12,5M'],
                ['#FB923C','Prêts véhicule','9,8M'],
                ['#FCD34D','Prêts personnels','7,2M'],
                ['#34D399','Avances sur salaire','13,3M'],
              ].map((x,i) => (
                <div key={i} className="row" style={{padding:'4px 0', fontSize:12}}>
                  <span style={{width:10, height:10, borderRadius:3, background:x[0]}}/>
                  <span style={{flex:1, marginLeft:8}}>{x[1]}</span>
                  <span className="tabular" style={{fontWeight:600}}>{x[2]}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-head">
          <div className="card-title">Avances & prêts</div>
          <div className="row gap-2">
            <button className="chip orange active">Tous</button>
            <button className="chip">Avances</button>
            <button className="chip">Prêts</button>
            <button className="chip">En cours</button>
          </div>
        </div>
        <table className="t">
          <thead><tr><th>Réf.</th><th>Employé</th><th>Type</th><th>Montant total (XAF)</th><th>Échéance</th><th>Mensualité (XAF)</th><th>Restant dû (XAF)</th><th>Progression</th><th>Statut</th></tr></thead>
          <tbody>
            {loans.map(l => {
              const total = parseFloat(l.amount.replace(/\s/g, ''));
              const rem = parseFloat(l.remaining.replace(/\s/g, ''));
              const pct = total > 0 ? Math.round(((total-rem)/total) * 100) : 100;
              return (
                <tr key={l.id}>
                  <td className="mono" style={{fontSize:11, color:'var(--ink-3)'}}>{l.id}</td>
                  <td><div className="row gap-2"><Avatar name={l.emp} color={l.avc} size="sm"/><span style={{fontSize:13, fontWeight:600}}>{l.emp}</span></div></td>
                  <td>
                    <span className={"badge " + (l.type.includes('Avance') ? 'amber' : l.type.includes('logement') ? 'green' : l.type.includes('véhicule') ? 'blue' : 'violet')}>{l.type}</span>
                  </td>
                  <td className="tabular" style={{fontWeight:700}}>{l.amount}</td>
                  <td className="muted tabular">{l.months} mois</td>
                  <td className="tabular">{l.monthly}</td>
                  <td className="tabular" style={{color: rem > 0 ? 'var(--ink-2)' : 'var(--ink-4)'}}>{l.remaining}</td>
                  <td style={{minWidth:120}}>
                    <div className="row gap-2">
                      <div className="bar thin" style={{flex:1}}><div style={{width: pct + '%'}}/></div>
                      <span className="tabular" style={{fontSize:11, color:'var(--ink-3)', minWidth:32}}>{pct}%</span>
                    </div>
                  </td>
                  <td>
                    {l.status === 'pending' && <span className="badge amber">À approuver</span>}
                    {l.status === 'approved' && <span className="badge blue">Approuvé</span>}
                    {l.status === 'active' && <span className="badge orange">En cours</span>}
                    {l.status === 'repaid' && <span className="badge green">Remboursé</span>}
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

window.PageLoans = PageLoans;
