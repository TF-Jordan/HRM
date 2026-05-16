/* global React */

function PageDeclarations() {
  const { Icons, PageHeader } = window;

  const decls = [
    { id:'DSN-2026-05', type:'DSN mensuelle · CNPS', period:'Mai 2026', amount:'42 850 000', dueDate:'19 mai 2026', status:'pending', overdue:5, daysLeft:5 },
    { id:'DSN-2026-04', type:'DSN mensuelle · CNPS', period:'Avril 2026', amount:'41 220 000', dueDate:'19 avr 2026', status:'submitted', submittedDate:'18 avr 2026' },
    { id:'IRPP-2026-04', type:'IRPP · DGI', period:'Avril 2026', amount:'12 480 000', dueDate:'15 mai 2026', status:'submitted', submittedDate:'12 mai 2026' },
    { id:'TC-2026-04', type:'Taxe communale + redevance', period:'Avril 2026', amount:'820 000', dueDate:'15 mai 2026', status:'submitted', submittedDate:'14 mai 2026' },
    { id:'CFC-2026-04', type:'CFC · Crédit Foncier', period:'Avril 2026', amount:'2 145 000', dueDate:'15 mai 2026', status:'submitted', submittedDate:'13 mai 2026' },
    { id:'DSN-2026-03', type:'DSN mensuelle · CNPS', period:'Mars 2026', amount:'40 540 000', dueDate:'19 mar 2026', status:'paid', paidDate:'18 mar 2026' },
    { id:'IRPP-2026-03', type:'IRPP · DGI', period:'Mars 2026', amount:'12 110 000', dueDate:'15 avr 2026', status:'paid', paidDate:'14 avr 2026' },
  ];

  return (
    <div>
      <PageHeader
        uc="Conformité"
        title="Déclarations sociales & fiscales"
        subtitle="CNPS, IRPP, CFC, Taxe communale et autres obligations"
        actions={
          <>
            <button className="btn btn-secondary"><Icons.download/> Télécharger archives</button>
            <button className="btn btn-primary"><Icons.send size={14}/> Soumettre une déclaration</button>
          </>
        }
      />

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'À soumettre', v:'3', sub:'1 urgente · DSN mai', tone:'red' },
          { l:'Soumises (mois)', v:'4', sub:'délai moyen 3,2 j avant', tone:'green' },
          { l:'Montant déclaré', v:'58,1M', sub:'XAF · mai 2026', tone:'orange' },
          { l:'Conformité YTD', v:'100%', sub:'0 retard depuis 18 mois', tone:'blue' },
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

      {/* Urgent banner */}
      <div className="card card-pad" style={{
        background:'linear-gradient(90deg, #FEECEC 0%, #FFF4F4 100%)',
        border:'1px solid #FCA5A5', marginBottom:20
      }}>
        <div className="row gap-3">
          <div className="icon-tile red" style={{width:42, height:42}}><Icons.alert size={20}/></div>
          <div style={{flex:1}}>
            <div style={{fontSize:14, fontWeight:700}}>DSN Mai 2026 · échéance dans 5 jours (19 mai)</div>
            <div style={{fontSize:12.5, color:'var(--ink-2)', marginTop:2}}>
              42 850 000 XAF à déclarer · 342 salariés · à finaliser après run paie mai
            </div>
          </div>
          <button className="btn btn-secondary">Voir le calcul</button>
          <button className="btn btn-primary">Préparer maintenant</button>
        </div>
      </div>

      <div className="card">
        <div className="card-head">
          <div className="card-title">Toutes les déclarations</div>
          <div className="row gap-2">
            <button className="chip orange active">Toutes</button>
            <button className="chip">CNPS</button>
            <button className="chip">DGI / IRPP</button>
            <button className="chip">CFC</button>
            <button className="chip">À soumettre (3)</button>
          </div>
        </div>
        <table className="t">
          <thead><tr><th>Réf.</th><th>Déclaration</th><th>Période</th><th>Montant (XAF)</th><th>Échéance</th><th>Statut</th><th></th></tr></thead>
          <tbody>
            {decls.map(d => {
              const overdue = d.status === 'pending';
              return (
                <tr key={d.id}>
                  <td className="mono" style={{fontSize:11, color:'var(--ink-3)'}}>{d.id}</td>
                  <td>
                    <div className="row gap-2">
                      <div className={"icon-tile " + (d.type.includes('CNPS') ? 'orange' : d.type.includes('IRPP') ? 'blue' : d.type.includes('CFC') ? 'violet' : 'amber')} style={{width:32, height:32}}>
                        <Icons.declaration size={14}/>
                      </div>
                      <div>
                        <div style={{fontSize:13, fontWeight:600}}>{d.type}</div>
                        <div style={{fontSize:11, color:'var(--ink-3)'}}>342 salariés</div>
                      </div>
                    </div>
                  </td>
                  <td>{d.period}</td>
                  <td className="tabular" style={{fontWeight:700}}>{d.amount}</td>
                  <td>
                    <div style={{fontSize:12}}>{d.dueDate}</div>
                    {overdue && <div style={{fontSize:11, color:'var(--orange-600)', fontWeight:600}}>dans {d.daysLeft} j</div>}
                    {d.submittedDate && <div style={{fontSize:11, color:'var(--green-600)'}}>soumise {d.submittedDate}</div>}
                    {d.paidDate && <div style={{fontSize:11, color:'var(--ink-3)'}}>payée {d.paidDate}</div>}
                  </td>
                  <td>
                    {d.status === 'pending' && <span className="badge red">À soumettre</span>}
                    {d.status === 'submitted' && <span className="badge blue">Soumise</span>}
                    {d.status === 'paid' && <span className="badge green">Payée</span>}
                  </td>
                  <td>
                    <div className="row gap-2">
                      <button className="icon-btn" style={{width:28, height:28}}><Icons.download size={12}/></button>
                      <button className="icon-btn" style={{width:28, height:28}}><Icons.chevR size={12}/></button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Calendar compliance */}
      <div className="section-title">Calendrier des obligations 2026</div>
      <div className="card card-pad">
        <div style={{display:'grid', gridTemplateColumns:'repeat(12, 1fr)', gap:4}}>
          {['Jan','Fév','Mar','Avr','Mai','Jun','Jul','Aoû','Sep','Oct','Nov','Déc'].map((m, i) => {
            const isPast = i < 4;
            const isCurrent = i === 4;
            return (
              <div key={i} style={{textAlign:'center'}}>
                <div style={{fontSize:11, fontWeight:600, color:'var(--ink-3)', marginBottom:6}}>{m}</div>
                <div style={{
                  padding:'12px 6px', borderRadius:10,
                  background: isPast ? 'var(--green-50)' : isCurrent ? 'var(--orange-500)' : 'var(--bg-soft)',
                  color: isCurrent ? '#fff' : isPast ? 'var(--green-600)' : 'var(--ink-3)',
                  border: isCurrent ? 'none' : '1px solid var(--line)'
                }}>
                  <div style={{fontSize:18, fontWeight:700}}>{isPast ? '✓' : isCurrent ? '!' : '—'}</div>
                  <div style={{fontSize:10, marginTop:2}}>{isPast ? '4/4' : isCurrent ? '1/4' : '0/4'}</div>
                </div>
              </div>
            );
          })}
        </div>
        <div className="row gap-3" style={{marginTop:16, fontSize:11}}>
          <span className="row gap-1"><div className="sd green"></div> Soumis</span>
          <span className="row gap-1"><div className="sd orange"></div> En cours</span>
          <span className="row gap-1"><div className="sd gray"></div> À venir</span>
        </div>
      </div>
    </div>
  );
}

window.PageDeclarations = PageDeclarations;
