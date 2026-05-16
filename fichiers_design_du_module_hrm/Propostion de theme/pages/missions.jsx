/* global React */

function PageMissions() {
  const { Icons, Avatar, PageHeader } = window;

  const missions = [
    { id:'MO-2026-0089', emp:'Joseph Mbarga', avc:'blue', dest:'Douala', purpose:'Client meeting · Orange Cameroun', from:'14 mai', to:'15 mai', days:2, allowance:'85 000', status:'in-progress' },
    { id:'MO-2026-0088', emp:'Hervé Tankeu', avc:'orange', dest:'Bafoussam', purpose:'Audit point de vente Ouest', from:'16 mai', to:'18 mai', days:3, allowance:'120 000', status:'approved' },
    { id:'MO-2026-0087', emp:'Yannick Etoa', avc:'orange', dest:'Paris (Fr)', purpose:'Salon recrutement Tech 2026', from:'02 jun', to:'08 jun', days:7, allowance:'1 850 000', status:'pending' },
    { id:'MO-2026-0086', emp:'Aminata Diallo', avc:'orange', dest:'Lagos (Ng)', purpose:'Africa Mobile Summit', from:'22 mai', to:'25 mai', days:4, allowance:'920 000', status:'approved' },
    { id:'MO-2026-0085', emp:'Karine Djoumessi', avc:'blue', dest:'Garoua', purpose:'Déploiement Field Ops', from:'08 mai', to:'12 mai', days:5, allowance:'180 000', status:'completed' },
    { id:'MO-2026-0084', emp:'Olivier Manga', avc:'green', dest:'Bafoussam', purpose:'Migration infra région Ouest', from:'05 mai', to:'07 mai', days:3, allowance:'95 000', status:'completed' },
  ];

  return (
    <div>
      <PageHeader
        uc="Activité"
        title="Ordres de mission"
        subtitle="Déplacements professionnels, indemnités et frais associés"
        actions={
          <>
            <button className="btn btn-secondary"><Icons.download/> Exporter</button>
            <button className="btn btn-primary"><Icons.plus/> Nouvel ordre</button>
          </>
        }
      />

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'En cours', v:'8', sub:'5 nationales · 3 internat.', tone:'orange' },
          { l:'À valider', v:'4', sub:'2 urgentes', tone:'amber' },
          { l:'Ce mois', v:'27', sub:'+18% vs avril', tone:'blue' },
          { l:'Coût indemnités', v:'4,2M', sub:'XAF · mai 2026', tone:'green' },
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
            <div className="card-title">Missions en cours et à venir</div>
            <div className="row gap-2">
              <button className="chip orange active">Toutes</button>
              <button className="chip">Nationales</button>
              <button className="chip">Internationales</button>
            </div>
          </div>
          <table className="t">
            <thead><tr><th>Référence</th><th>Employé</th><th>Destination & objet</th><th>Période</th><th>Indemnité (XAF)</th><th>Statut</th></tr></thead>
            <tbody>
              {missions.map(m => (
                <tr key={m.id}>
                  <td className="mono" style={{fontSize:11, color:'var(--ink-3)'}}>{m.id}</td>
                  <td><div className="row gap-2"><Avatar name={m.emp} color={m.avc} size="sm"/><span style={{fontSize:13, fontWeight:600}}>{m.emp}</span></div></td>
                  <td>
                    <div className="row gap-2">
                      <Icons.mission size={14} style={{color:'var(--orange-500)'}}/>
                      <div>
                        <div style={{fontSize:13, fontWeight:600}}>{m.dest}</div>
                        <div style={{fontSize:11, color:'var(--ink-3)'}}>{m.purpose}</div>
                      </div>
                    </div>
                  </td>
                  <td>
                    <div style={{fontSize:12}}>{m.from} → {m.to}</div>
                    <div style={{fontSize:11, color:'var(--ink-3)'}} className="tabular">{m.days} jours</div>
                  </td>
                  <td className="tabular" style={{fontWeight:600}}>{m.allowance}</td>
                  <td>
                    {m.status === 'pending' && <span className="badge amber">À valider</span>}
                    {m.status === 'approved' && <span className="badge blue">Approuvée</span>}
                    {m.status === 'in-progress' && <span className="badge orange">En cours</span>}
                    {m.status === 'completed' && <span className="badge green">Terminée</span>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="card">
          <div className="card-head"><div className="card-title">Carte des destinations</div></div>
          <div style={{padding:18}}>
            {/* Simple "map" placeholder with cities */}
            <div style={{
              background:'linear-gradient(180deg, #FFF4EB 0%, #FFE4CC 100%)',
              borderRadius:12, padding:20, position:'relative', height:200, marginBottom:14, overflow:'hidden'
            }}>
              <svg viewBox="0 0 300 200" style={{position:'absolute', inset:0}}>
                <defs>
                  <radialGradient id="rg1"><stop offset="0%" stopColor="#F97316" stopOpacity="0.8"/><stop offset="100%" stopColor="#F97316" stopOpacity="0"/></radialGradient>
                </defs>
                <circle cx="120" cy="100" r="32" fill="url(#rg1)"/>
                <circle cx="180" cy="140" r="22" fill="url(#rg1)"/>
                <circle cx="80" cy="90" r="18" fill="url(#rg1)"/>
                <circle cx="220" cy="60" r="14" fill="url(#rg1)"/>
              </svg>
              <div style={{position:'absolute', left:'40%', top:'50%', display:'flex', alignItems:'center', gap:6}}>
                <Icons.mission size={20} style={{color:'var(--orange-700)'}}/>
                <div>
                  <div style={{fontSize:12, fontWeight:700, color:'var(--ink)'}}>Yaoundé HQ</div>
                  <div style={{fontSize:10, color:'var(--ink-3)'}}>4 missions actives</div>
                </div>
              </div>
            </div>
            <div style={{fontSize:12.5, fontWeight:600, marginBottom:8}}>Top destinations · Mai</div>
            {[
              { c:'Douala', n:8, p:'30%' },
              { c:'Bafoussam', n:5, p:'19%' },
              { c:'Garoua', n:4, p:'15%' },
              { c:'Paris (Fr)', n:2, p:'7%' },
              { c:'Lagos (Ng)', n:2, p:'7%' },
            ].map((d,i) => (
              <div key={i} className="row" style={{padding:'6px 0', fontSize:12.5, borderBottom: i<4 ? '1px solid var(--line-soft)' : 'none'}}>
                <Icons.mission size={12} style={{color:'var(--ink-3)'}}/>
                <span style={{marginLeft:6, flex:1}}>{d.c}</span>
                <span className="tabular" style={{fontWeight:600}}>{d.n}</span>
                <span className="muted tabular" style={{marginLeft:8, width:32, textAlign:'right'}}>{d.p}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

window.PageMissions = PageMissions;
