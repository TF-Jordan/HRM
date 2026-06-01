/* global React */
const { useState: useS_ct } = React;

function PageContracts() {
  const { Icons, Avatar, PageHeader, Donut } = window;

  const contracts = [
    { id:'CT-2022-0142', emp:'Aminata Diallo', avc:'orange', type:'CDI', position:'Lead Mobile Eng.', start:'14 fév 2022', end:'—', salary:'1 245 000', status:'active' },
    { id:'CT-2024-0177', emp:'Pierre Kouam', avc:'amber', type:'CDD', position:'Comptable', start:'15 jan 2026', end:'14 jul 2026', salary:'520 000', status:'trial' },
    { id:'CT-2026-0234', emp:'Léa Ondoa', avc:'teal', type:'Stage', position:'Designer UI', start:'22 mai 2026', end:'21 nov 2026', salary:'150 000', status:'pending' },
    { id:'CT-2021-0098', emp:'Joseph Mbarga', avc:'blue', type:'CDI', position:'Business Developer', start:'01 oct 2023', end:'—', salary:'780 000', status:'active' },
    { id:'CT-2026-0156', emp:'Yannick Etoa', avc:'orange', type:'CDI', position:'Talent Acquisition', start:'15 fév 2023', end:'—', salary:'620 000', status:'active' },
    { id:'CT-2025-0188', emp:'Hervé Tankeu', avc:'orange', type:'CDI', position:'Sales Manager', start:'01 sept 2022', end:'—', salary:'920 000', status:'active' },
  ];

  return (
    <div>
      <PageHeader
        uc="Capital humain"
        title="Contrats"
        subtitle="Suivi des contrats, avenants et fins de période d'essai"
        actions={
          <>
            <button className="btn btn-secondary"><Icons.download/> Exporter</button>
            <button className="btn btn-primary"><Icons.plus/> Nouveau contrat</button>
          </>
        }
      />

      <div style={{display:'grid', gridTemplateColumns:'2fr 1fr', gap:16, marginBottom:20}}>
        <div className="grid-3" style={{gap:14}}>
          {[
            { l:'Actifs', v:'342', sub:'sur 342 employés', tone:'green', icon:'check' },
            { l:'Période d\'essai', v:'12', sub:'3 fins ce mois', tone:'amber', icon:'alert' },
            { l:'Fins de CDD (90j)', v:'8', sub:'à anticiper', tone:'red', icon:'cal' },
            { l:'CDI', v:'278', sub:'81% de l\'effectif', tone:'orange', icon:'contract' },
            { l:'CDD', v:'42', sub:'12% · -3 vs T-1', tone:'blue', icon:'contract' },
            { l:'Stages', v:'10', sub:'7 prévus pour l\'été', tone:'violet', icon:'training' },
          ].map((k,i) => {
            const I = Icons[k.icon];
            return (
              <div key={i} className="card card-pad" style={{position:'relative'}}>
                <div className={"icon-tile " + k.tone} style={{marginBottom:10}}><I size={16}/></div>
                <div style={{fontFamily:'Inter Tight', fontSize:26, fontWeight:800}} className="tabular">{k.v}</div>
                <div style={{fontSize:12.5, fontWeight:600, color:'var(--ink-2)'}}>{k.l}</div>
                <div style={{fontSize:11, color:'var(--ink-3)', marginTop:2}}>{k.sub}</div>
              </div>
            );
          })}
        </div>

        <div className="card">
          <div className="card-head">
            <div className="card-title">Type de contrat</div>
          </div>
          <div style={{padding:18, display:'flex', flexDirection:'column', alignItems:'center', gap:14}}>
            <div style={{position:'relative'}}>
              <Donut data={[
                { value:278, color:'#F97316' },
                { value:42, color:'#3B82F6' },
                { value:10, color:'#8B5CF6' },
                { value:12, color:'#F59E0B' },
              ]} size={160} thick={22}/>
              <div style={{position:'absolute', inset:0, display:'grid', placeItems:'center', textAlign:'center'}}>
                <div>
                  <div style={{fontFamily:'Inter Tight', fontSize:22, fontWeight:800}} className="tabular">342</div>
                  <div style={{fontSize:11, color:'var(--ink-3)'}}>contrats</div>
                </div>
              </div>
            </div>
            <div style={{width:'100%'}}>
              {[['#F97316','CDI',278],['#F59E0B','Période essai',12],['#3B82F6','CDD',42],['#8B5CF6','Stage',10]].map((x,i) => (
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

      {/* Alerts */}
      <div className="card card-pad" style={{
        background:'linear-gradient(90deg, #FFF6E0 0%, #FFFAEA 100%)',
        border:'1px solid #FCD34D',
        marginBottom:16
      }}>
        <div className="row gap-3">
          <div className="icon-tile amber" style={{width:42, height:42}}><Icons.alert size={20}/></div>
          <div style={{flex:1}}>
            <div style={{fontSize:14, fontWeight:700}}>3 fins de période d'essai à valider ce mois-ci</div>
            <div style={{fontSize:12.5, color:'var(--ink-2)', marginTop:2}}>
              Pierre Kouam (15 mai), Léa Mballa (18 mai), Sébastien Owono (22 mai) — décision à prendre avant échéance.
            </div>
          </div>
          <button className="btn btn-dark">Voir la liste</button>
        </div>
      </div>

      {/* Table */}
      <div className="card">
        <div className="card-head">
          <div className="card-title">Tous les contrats</div>
          <div className="row gap-2">
            <button className="chip orange active">Tous</button>
            <button className="chip">CDI</button>
            <button className="chip">CDD</button>
            <button className="chip">Stage</button>
            <button className="chip">Essai</button>
          </div>
        </div>
        <table className="t">
          <thead>
            <tr><th>Réf.</th><th>Employé</th><th>Type</th><th>Poste</th><th>Début</th><th>Fin</th><th>Salaire (XAF)</th><th>Statut</th><th></th></tr>
          </thead>
          <tbody>
            {contracts.map(c => (
              <tr key={c.id}>
                <td className="mono" style={{fontSize:11, color:'var(--ink-3)'}}>{c.id}</td>
                <td>
                  <div className="row gap-2">
                    <Avatar name={c.emp} color={c.avc} size="sm"/>
                    <span style={{fontSize:13, fontWeight:600}}>{c.emp}</span>
                  </div>
                </td>
                <td>
                  <span className={"badge " + (c.type === 'CDI' ? 'green' : c.type === 'CDD' ? 'amber' : 'blue')}>{c.type}</span>
                </td>
                <td style={{fontSize:13}}>{c.position}</td>
                <td className="muted">{c.start}</td>
                <td className={c.end === '—' ? 'muted' : ''}>{c.end}</td>
                <td className="tabular" style={{fontWeight:600}}>{c.salary}</td>
                <td>
                  {c.status === 'active' && <span className="badge green">Actif</span>}
                  {c.status === 'trial' && <span className="badge amber">Période d'essai</span>}
                  {c.status === 'pending' && <span className="badge blue">En attente</span>}
                </td>
                <td>
                  <div className="row gap-2">
                    <button className="icon-btn" style={{width:28, height:28}}><Icons.download size={12}/></button>
                    <button className="icon-btn" style={{width:28, height:28}}><Icons.more size={12}/></button>
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

window.PageContracts = PageContracts;
