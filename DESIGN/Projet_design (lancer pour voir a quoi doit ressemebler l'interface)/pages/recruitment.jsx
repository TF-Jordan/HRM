/* global React */

function PageRecruitment() {
  const { Icons, Avatar, PageHeader } = window;

  const offers = [
    { id:'JO-2026-007', title:'Tech Lead Mobile', dept:'Engineering', site:'Yaoundé HQ', salary:'1,2M–1,6M XAF', applications:24, interviews:5, days:18, status:'active' },
    { id:'JO-2026-008', title:'Data Engineer', dept:'Engineering', site:'Yaoundé HQ', salary:'900K–1,3M XAF', applications:18, interviews:3, days:11, status:'active' },
    { id:'JO-2026-009', title:'Account Manager · Centre', dept:'Commercial', site:'Yaoundé HQ', salary:'650K–900K XAF', applications:31, interviews:7, days:24, status:'active' },
    { id:'JO-2026-010', title:'Field Operations · Bafoussam', dept:'Opérations', site:'Bafoussam', salary:'380K–520K XAF', applications:42, interviews:9, days:14, status:'active' },
    { id:'JO-2026-005', title:'Designer UI (Stage)', dept:'Engineering', site:'Yaoundé HQ', salary:'150K XAF', applications:67, interviews:12, days:34, status:'filled' },
  ];

  const kanban = {
    'Nouveau': [
      { name:'Diane Nko', role:'Tech Lead Mobile', avatar:'violet', score:84, days:1 },
      { name:'Eric Mvondo', role:'Data Engineer', avatar:'blue', score:72, days:2 },
      { name:'Anne Ekani', role:'Account Manager', avatar:'orange', score:78, days:1 },
    ],
    'Présélectionné': [
      { name:'Brice Mengue', role:'Tech Lead Mobile', avatar:'green', score:88, days:5 },
      { name:'Linda Owono', role:'Designer UI', avatar:'amber', score:90, days:3 },
    ],
    'Entretien': [
      { name:'Bertrand Atangana', role:'Tech Lead Mobile', avatar:'orange', score:92, days:8, scheduled:'16 mai · 14h' },
      { name:'Pascale Eyenga', role:'Account Manager', avatar:'teal', score:86, days:6, scheduled:'17 mai · 10h' },
    ],
    'Offre': [
      { name:'Honoré Ndongo', role:'Data Engineer', avatar:'blue', score:94, days:12, offered:'1 100 000 XAF' },
    ],
    'Embauché': [
      { name:'Léa Ondoa', role:'Designer UI', avatar:'teal', score:91, days:21, started:'22 mai 2026' },
    ],
  };

  return (
    <div>
      <PageHeader
        uc="Capital humain"
        title="Recrutement"
        subtitle="Pipeline de talents, offres ouvertes et entretiens"
        actions={
          <>
            <button className="btn btn-secondary"><Icons.cal size={14}/> Entretiens</button>
            <button className="btn btn-primary"><Icons.plus/> Publier une offre</button>
          </>
        }
      />

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'Offres ouvertes', v:'12', sub:'182 candidatures', tone:'orange' },
          { l:'Candidatures en cours', v:'47', sub:'15 nouveaux ce mois', tone:'blue' },
          { l:'Entretiens cette sem.', v:'9', sub:'12 candidats actifs', tone:'violet' },
          { l:'Délai moyen', v:'18 j', sub:'objectif < 21 jours', tone:'green' },
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

      {/* Kanban pipeline */}
      <div className="card">
        <div className="card-head">
          <div>
            <div className="card-title">Pipeline candidats</div>
            <div className="muted" style={{fontSize:12, marginTop:2}}>Glisser-déposer pour faire évoluer un candidat dans le funnel</div>
          </div>
          <div className="row gap-2">
            <button className="chip orange active">Tous</button>
            <button className="chip">Engineering</button>
            <button className="chip">Commercial</button>
          </div>
        </div>
        <div style={{padding:18, display:'grid', gridTemplateColumns:'repeat(5, 1fr)', gap:12, overflowX:'auto'}}>
          {Object.entries(kanban).map(([col, cards]) => {
            const tone = col === 'Nouveau' ? 'gray' : col === 'Présélectionné' ? 'blue' : col === 'Entretien' ? 'orange' : col === 'Offre' ? 'amber' : 'green';
            return (
              <div key={col} style={{background:'var(--bg-dim)', borderRadius:12, padding:12, minWidth:220}}>
                <div className="row" style={{marginBottom:10}}>
                  <span className={"badge " + tone} style={{fontSize:11}}>{col}</span>
                  <span className="muted" style={{marginLeft:'auto', fontSize:11, fontWeight:600}}>{cards.length}</span>
                </div>
                {cards.map((c,i) => (
                  <div key={i} style={{background:'#fff', padding:12, borderRadius:10, border:'1px solid var(--line)', marginBottom:8}}>
                    <div className="row gap-2" style={{marginBottom:6}}>
                      <Avatar name={c.name} color={c.avatar} size="sm"/>
                      <div style={{flex:1, minWidth:0}}>
                        <div style={{fontSize:12.5, fontWeight:600, whiteSpace:'nowrap', overflow:'hidden', textOverflow:'ellipsis'}}>{c.name}</div>
                        <div style={{fontSize:10.5, color:'var(--ink-3)', whiteSpace:'nowrap', overflow:'hidden', textOverflow:'ellipsis'}}>{c.role}</div>
                      </div>
                    </div>
                    <div className="row" style={{marginTop:6}}>
                      <div style={{fontSize:10.5, color:'var(--ink-3)'}}>Match</div>
                      <div className="bar thin" style={{flex:1, margin:'0 8px'}}>
                        <div style={{width: c.score + '%', background: c.score >= 88 ? 'var(--green-500)' : 'var(--grad-orange)'}}/>
                      </div>
                      <span className="tabular" style={{fontSize:11, fontWeight:700}}>{c.score}</span>
                    </div>
                    {c.scheduled && <div style={{marginTop:6, fontSize:10.5, color:'var(--orange-600)', fontWeight:600}}>📅 {c.scheduled}</div>}
                    {c.offered && <div style={{marginTop:6, fontSize:10.5, color:'var(--ink-2)'}}>Offre: <b className="tabular">{c.offered}</b></div>}
                    {c.started && <div style={{marginTop:6, fontSize:10.5, color:'var(--green-600)', fontWeight:600}}>✓ Début {c.started}</div>}
                    <div style={{marginTop:6, fontSize:10, color:'var(--ink-4)'}}>il y a {c.days}j</div>
                  </div>
                ))}
                <button className="btn btn-ghost btn-sm" style={{width:'100%', justifyContent:'center'}}>+ Ajouter</button>
              </div>
            );
          })}
        </div>
      </div>

      {/* Offers list */}
      <div className="section-title">Offres ouvertes</div>
      <div className="card">
        <table className="t">
          <thead><tr><th>Réf.</th><th>Poste</th><th>Département</th><th>Site</th><th>Salaire</th><th>Candidatures</th><th>Entretiens</th><th>Ouverte depuis</th><th>Statut</th><th></th></tr></thead>
          <tbody>
            {offers.map(o => (
              <tr key={o.id}>
                <td className="mono" style={{fontSize:11, color:'var(--ink-3)'}}>{o.id}</td>
                <td style={{fontSize:13, fontWeight:600}}>{o.title}</td>
                <td><span className="tag">{o.dept}</span></td>
                <td className="muted">{o.site}</td>
                <td className="tabular" style={{fontSize:12.5}}>{o.salary}</td>
                <td>
                  <div className="row gap-2">
                    <span className="tabular" style={{fontWeight:600}}>{o.applications}</span>
                    <div className="bar thin" style={{width:60}}><div style={{width: Math.min(o.applications*2, 100) + '%'}}/></div>
                  </div>
                </td>
                <td className="tabular">{o.interviews}</td>
                <td className="muted tabular">{o.days} jours</td>
                <td>
                  <span className={"badge " + (o.status === 'active' ? 'orange' : 'green')}>
                    {o.status === 'active' ? 'Ouverte' : 'Pourvue'}
                  </span>
                </td>
                <td><button className="icon-btn" style={{width:28, height:28}}><Icons.chevR size={12}/></button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

window.PageRecruitment = PageRecruitment;
