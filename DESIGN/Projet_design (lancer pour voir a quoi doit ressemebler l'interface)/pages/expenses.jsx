/* global React */

function PageExpenses() {
  const { Icons, Avatar, PageHeader, Bars } = window;

  const reports = [
    { id:'NF-2026-0245', emp:'Joseph Mbarga', avc:'blue', title:'Mission Douala · Orange Cameroun', lines:8, total:'124 500', status:'pending', date:'12 mai 2026' },
    { id:'NF-2026-0244', emp:'Hervé Tankeu', avc:'orange', title:'Tournée Ouest · 3 villes', lines:14, total:'287 200', status:'pending', date:'11 mai 2026' },
    { id:'NF-2026-0243', emp:'Karine Djoumessi', avc:'blue', title:'Repas équipe Garoua', lines:5, total:'68 400', status:'approved', date:'10 mai 2026' },
    { id:'NF-2026-0242', emp:'Aminata Diallo', avc:'orange', title:'Conférence Mobile Summit', lines:11, total:'546 800', status:'pending', date:'09 mai 2026' },
    { id:'NF-2026-0241', emp:'Yannick Etoa', avc:'orange', title:'Salons recrutement (×3)', lines:9, total:'195 700', status:'paid', date:'06 mai 2026' },
    { id:'NF-2026-0240', emp:'Olivier Manga', avc:'green', title:'Achats matériel infra', lines:6, total:'412 350', status:'rejected', date:'04 mai 2026' },
  ];

  return (
    <div>
      <PageHeader
        uc="Rémunération"
        title="Notes de frais"
        subtitle="Saisie, validation et remboursement des dépenses professionnelles"
        actions={
          <>
            <button className="btn btn-secondary"><Icons.download/> Exporter</button>
            <button className="btn btn-primary"><Icons.plus/> Nouvelle note</button>
          </>
        }
      />

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'À approuver', v:'4', sub:'3 urgentes · 1,1M XAF', tone:'amber' },
          { l:'Approuvées (mois)', v:'42', sub:'4,8M XAF', tone:'green' },
          { l:'Délai moyen', v:'2,3 j', sub:'cible 3j', tone:'blue' },
          { l:'Refusées', v:'3', sub:'manque de justificatifs', tone:'red' },
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
            <div className="card-title">Notes en attente de validation</div>
            <div className="row gap-2">
              <button className="chip orange active">À traiter (4)</button>
              <button className="chip">Approuvées</button>
              <button className="chip">Payées</button>
            </div>
          </div>
          <table className="t">
            <thead><tr><th>Réf.</th><th>Auteur · Objet</th><th>Lignes</th><th>Montant (XAF)</th><th>Date</th><th>Statut</th><th></th></tr></thead>
            <tbody>
              {reports.map(r => (
                <tr key={r.id}>
                  <td className="mono" style={{fontSize:11, color:'var(--ink-3)'}}>{r.id}</td>
                  <td>
                    <div className="row gap-2">
                      <Avatar name={r.emp} color={r.avc} size="sm"/>
                      <div>
                        <div style={{fontSize:13, fontWeight:600}}>{r.emp}</div>
                        <div style={{fontSize:11, color:'var(--ink-3)'}}>{r.title}</div>
                      </div>
                    </div>
                  </td>
                  <td className="muted tabular">{r.lines}</td>
                  <td className="tabular" style={{fontWeight:700}}>{r.total}</td>
                  <td className="muted">{r.date}</td>
                  <td>
                    {r.status === 'pending' && <span className="badge amber">En attente</span>}
                    {r.status === 'approved' && <span className="badge blue">Approuvée</span>}
                    {r.status === 'paid' && <span className="badge green">Payée</span>}
                    {r.status === 'rejected' && <span className="badge red">Refusée</span>}
                  </td>
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

        <div className="card">
          <div className="card-head"><div className="card-title">Catégories · Mai 2026</div></div>
          <div style={{padding:18}}>
            <Bars data={[
              { label:'Transp.', value: 1850, color:'#F97316' },
              { label:'Repas', value: 980, color:'#FB923C' },
              { label:'Héberg.', value: 1240, color:'#FCD34D' },
              { label:'Matér.', value: 620, color:'#34D399' },
              { label:'Autres', value: 410, color:'#A78BFA' },
            ]} h={160}/>
            <div style={{fontSize:11, color:'var(--ink-4)', textAlign:'center', marginTop:6}}>en milliers XAF</div>
            <div className="divider"/>
            <div style={{fontSize:12, color:'var(--ink-2)', display:'flex', justifyContent:'space-between', padding:'4px 0'}}>
              <span>Total mois</span><b className="tabular">5 100 000 XAF</b>
            </div>
            <div style={{fontSize:12, color:'var(--ink-2)', display:'flex', justifyContent:'space-between', padding:'4px 0'}}>
              <span>Budget alloué</span><b className="tabular">6 500 000 XAF</b>
            </div>
            <div className="bar thin" style={{marginTop:8}}><div style={{width:'78%'}}/></div>
            <div className="muted" style={{fontSize:11, marginTop:4}}>78% utilisé · 1,4M restant</div>
          </div>
        </div>
      </div>

      {/* Expense detail preview */}
      <div className="section-title">Aperçu · NF-2026-0245 — Joseph Mbarga</div>
      <div className="card card-pad-lg">
        <div className="row" style={{marginBottom:18}}>
          <div>
            <div className="row gap-2">
              <Avatar name="Joseph Mbarga" color="blue" size="lg"/>
              <div>
                <div style={{fontSize:16, fontWeight:700}}>Mission Douala · Orange Cameroun</div>
                <div style={{fontSize:12, color:'var(--ink-3)'}}>Soumise le 12 mai 2026 par Joseph Mbarga · EMP-0098</div>
              </div>
            </div>
          </div>
          <div className="row gap-2" style={{marginLeft:'auto'}}>
            <span className="badge amber">En attente DRH</span>
            <button className="btn btn-secondary btn-sm"><Icons.x size={13}/> Refuser</button>
            <button className="btn btn-primary btn-sm"><Icons.check size={13}/> Approuver</button>
          </div>
        </div>

        <table className="t">
          <thead><tr><th>Date</th><th>Catégorie</th><th>Libellé</th><th>Justificatif</th><th>TTC (XAF)</th><th>TVA</th></tr></thead>
          <tbody>
            {[
              { d:'12 mai 2026', cat:'Transport', l:'Vol Yaoundé → Douala (aller)', j:true, m:'42 500', tva:'19,25%' },
              { d:'12 mai 2026', cat:'Repas', l:'Déjeuner client · Le Méridien', j:true, m:'28 400', tva:'19,25%' },
              { d:'12 mai 2026', cat:'Transport', l:'Taxi aéroport → hôtel', j:false, m:'8 000', tva:'—' },
              { d:'13 mai 2026', cat:'Hébergement', l:'Nuit Hôtel Akwa Palace', j:true, m:'32 000', tva:'19,25%' },
              { d:'13 mai 2026', cat:'Transport', l:'Vol Douala → Yaoundé (retour)', j:true, m:'13 600', tva:'19,25%' },
            ].map((x,i) => (
              <tr key={i}>
                <td className="muted">{x.d}</td>
                <td><span className="tag">{x.cat}</span></td>
                <td style={{fontSize:13}}>{x.l}</td>
                <td>
                  {x.j ? <span className="badge green">Joint</span> : <span className="badge red">Manquant</span>}
                </td>
                <td className="tabular" style={{fontWeight:600}}>{x.m}</td>
                <td className="muted tabular">{x.tva}</td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr style={{background:'var(--bg-dim)'}}>
              <td colSpan="4" style={{padding:'12px 16px', fontWeight:700, fontSize:13}}>Total à rembourser</td>
              <td className="tabular" style={{fontWeight:800, fontFamily:'Inter Tight', fontSize:16}}>124 500</td>
              <td></td>
            </tr>
          </tfoot>
        </table>
      </div>
    </div>
  );
}

window.PageExpenses = PageExpenses;
