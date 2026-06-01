/* global React */

function PageMedical() {
  const { Icons, Avatar, PageHeader, Donut } = window;

  const visits = [
    { id:'MV-2026-0042', emp:'Aminata Diallo', avc:'orange', type:'Annuelle', date:'14 avr 2026', next:'14 avr 2027', result:'Apte', doctor:'Dr. Nkoa' },
    { id:'MV-2026-0041', emp:'Pierre Kouam', avc:'amber', type:'Embauche', date:'15 jan 2026', next:'15 jan 2027', result:'Apte', doctor:'Dr. Mballa' },
    { id:'MV-2026-0040', emp:'Joseph Mbarga', avc:'blue', type:'Annuelle', date:'10 mar 2026', next:'10 mar 2027', result:'Apte avec restrictions', doctor:'Dr. Nkoa' },
    { id:'MV-2026-0039', emp:'Marc Foga', avc:'green', type:'Spécifique', date:'02 fév 2026', next:'02 fév 2027', result:'Apte', doctor:'Dr. Tchoungui' },
    { id:'MV-2026-0038', emp:'Karine Djoumessi', avc:'blue', type:'Reprise', date:'18 jan 2026', next:'18 jan 2027', result:'Apte', doctor:'Dr. Mballa' },
  ];

  const certs = [
    { id:'MC-2026-0058', emp:'Joseph Mbarga', avc:'blue', type:'Maladie', from:'13 mai 2026', to:'15 mai 2026', days:3, doctor:'Dr. Owono · CMA Yaoundé' },
    { id:'MC-2026-0057', emp:'Estelle Bilong', avc:'amber', type:'Maternité', from:'01 juin 2026', to:'06 sept 2026', days:98, doctor:'Dr. Tcheumegne · Maternité Mvog-Ada' },
    { id:'MC-2026-0056', emp:'Léa Mballa', avc:'violet', type:'Maladie', from:'08 mai 2026', to:'10 mai 2026', days:3, doctor:'Dr. Eyenga' },
  ];

  return (
    <div>
      <PageHeader
        uc="Conformité"
        title="Suivi médical"
        subtitle="Visites médicales, certificats et déclarations d'accidents de travail"
        actions={
          <>
            <button className="btn btn-secondary"><Icons.download/> Registre médical</button>
            <button className="btn btn-primary"><Icons.plus/> Planifier une visite</button>
          </>
        }
      />

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'À planifier (90j)', v:'18', sub:'7 expirations < 30j', tone:'amber' },
          { l:'Visites Q2', v:'42', sub:'planifiées', tone:'orange' },
          { l:'Certificats actifs', v:'12', sub:'3 maternité · 8 maladie', tone:'blue' },
          { l:'Aptitudes restrictions', v:'4', sub:'à suivre', tone:'red' },
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

      {/* Alert */}
      <div className="card card-pad" style={{
        background:'linear-gradient(90deg, #FFF6E0 0%, #FFFAEA 100%)',
        border:'1px solid #FCD34D', marginBottom:20
      }}>
        <div className="row gap-3">
          <div className="icon-tile amber" style={{width:42, height:42}}><Icons.alert size={20}/></div>
          <div style={{flex:1}}>
            <div style={{fontSize:14, fontWeight:700}}>7 visites médicales expirent dans moins de 30 jours</div>
            <div style={{fontSize:12.5, color:'var(--ink-2)', marginTop:2}}>
              Obligation légale code du travail · à planifier auprès du médecin du travail agréé
            </div>
          </div>
          <button className="btn btn-dark">Planifier maintenant</button>
        </div>
      </div>

      <div style={{display:'grid', gridTemplateColumns:'2fr 1fr', gap:16, marginBottom:20}}>
        <div className="card">
          <div className="card-head">
            <div className="card-title">Visites médicales récentes</div>
            <div className="row gap-2">
              <button className="chip orange active">Toutes</button>
              <button className="chip">Annuelles</button>
              <button className="chip">Embauche</button>
              <button className="chip">Reprise</button>
            </div>
          </div>
          <table className="t">
            <thead><tr><th>Réf.</th><th>Employé</th><th>Type</th><th>Date</th><th>Prochaine</th><th>Résultat</th><th>Médecin</th></tr></thead>
            <tbody>
              {visits.map(v => (
                <tr key={v.id}>
                  <td className="mono" style={{fontSize:11, color:'var(--ink-3)'}}>{v.id}</td>
                  <td><div className="row gap-2"><Avatar name={v.emp} color={v.avc} size="sm"/><span style={{fontSize:13, fontWeight:600}}>{v.emp}</span></div></td>
                  <td><span className={"badge " + (v.type === 'Annuelle' ? 'orange' : v.type === 'Embauche' ? 'green' : v.type === 'Reprise' ? 'blue' : 'violet')}>{v.type}</span></td>
                  <td className="muted">{v.date}</td>
                  <td className="muted">{v.next}</td>
                  <td>
                    {v.result === 'Apte' && <span className="badge green">Apte</span>}
                    {v.result.includes('restrictions') && <span className="badge amber">Apte · restrictions</span>}
                  </td>
                  <td className="muted" style={{fontSize:12}}>{v.doctor}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="card">
          <div className="card-head"><div className="card-title">Statut d'aptitude</div></div>
          <div style={{padding:18, display:'flex', flexDirection:'column', alignItems:'center'}}>
            <div style={{position:'relative'}}>
              <Donut data={[
                { value:324, color:'#10B981' },
                { value:14, color:'#F59E0B' },
                { value:4, color:'#EF4444' },
              ]} size={160} thick={22}/>
              <div style={{position:'absolute', inset:0, display:'grid', placeItems:'center'}}>
                <div style={{textAlign:'center'}}>
                  <div style={{fontFamily:'Inter Tight', fontSize:24, fontWeight:800}}>95%</div>
                  <div style={{fontSize:11, color:'var(--ink-3)'}}>aptes</div>
                </div>
              </div>
            </div>
            <div style={{width:'100%', marginTop:16}}>
              {[['#10B981','Aptes sans réserve','324'],['#F59E0B','Aptes avec restrictions','14'],['#EF4444','Inaptes temporaires','4']].map((x,i) => (
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

      <div className="section-title">Certificats médicaux en cours</div>
      <div className="card">
        <table className="t">
          <thead><tr><th>Réf.</th><th>Employé</th><th>Type</th><th>Période</th><th>Durée</th><th>Médecin</th><th>Statut</th></tr></thead>
          <tbody>
            {certs.map(c => (
              <tr key={c.id}>
                <td className="mono" style={{fontSize:11, color:'var(--ink-3)'}}>{c.id}</td>
                <td><div className="row gap-2"><Avatar name={c.emp} color={c.avc} size="sm"/><span style={{fontSize:13, fontWeight:600}}>{c.emp}</span></div></td>
                <td><span className={"badge " + (c.type === 'Maternité' ? 'violet' : 'red')}>{c.type}</span></td>
                <td className="muted">{c.from} → {c.to}</td>
                <td className="tabular" style={{fontWeight:600}}>{c.days} j</td>
                <td className="muted" style={{fontSize:12}}>{c.doctor}</td>
                <td><span className="badge orange">En cours</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

window.PageMedical = PageMedical;
