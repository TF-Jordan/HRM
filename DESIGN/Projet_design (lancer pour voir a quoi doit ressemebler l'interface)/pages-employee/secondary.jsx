/* global React */

function EmpPageMissions() {
  const { Icons, PageHeader } = window;

  return (
    <div>
      <PageHeader
        uc="Activité"
        title="Mes missions"
        subtitle="Déplacements professionnels passés, en cours et à venir"
        actions={<button className="btn btn-primary"><Icons.plus/> Demander un ordre</button>}
      />

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'À venir', v:'1', sub:'Lagos · 22 mai', tone:'orange' },
          { l:'En attente', v:'1', sub:'à valider par Marc', tone:'amber' },
          { l:'Cette année', v:'8', sub:'4 365 km parcourus', tone:'blue' },
          { l:'Indemnités YTD', v:'2,4M', sub:'XAF cumulés', tone:'green' },
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

      {/* Upcoming hero */}
      <div className="card card-pad-lg" style={{
        background:'linear-gradient(135deg, #FFFAF2 0%, #fff 100%)',
        border:'1px solid var(--orange-200)', marginBottom:20
      }}>
        <div className="row" style={{alignItems:'flex-start'}}>
          <div style={{flex:1}}>
            <span className="badge orange">Prochaine mission · dans 8 jours</span>
            <div className="h-display" style={{fontSize:28, marginTop:8}}>Lagos · Africa Mobile Summit</div>
            <div style={{fontSize:13, color:'var(--ink-2)', marginTop:4}}>22 → 25 mai 2026 · 4 jours · MO-2026-0086</div>

            <div className="row gap-4" style={{marginTop:18, flexWrap:'wrap'}}>
              <div>
                <div style={{fontSize:11, color:'var(--ink-3)', textTransform:'uppercase', fontWeight:600}}>Indemnité</div>
                <div className="tabular" style={{fontSize:18, fontWeight:800, fontFamily:'Inter Tight'}}>920 000 XAF</div>
              </div>
              <div>
                <div style={{fontSize:11, color:'var(--ink-3)', textTransform:'uppercase', fontWeight:600}}>Vol</div>
                <div style={{fontSize:13, fontWeight:600}}>YAO → LOS · 22 mai 08h15</div>
              </div>
              <div>
                <div style={{fontSize:11, color:'var(--ink-3)', textTransform:'uppercase', fontWeight:600}}>Hôtel</div>
                <div style={{fontSize:13, fontWeight:600}}>Eko Atlantic · 3 nuits</div>
              </div>
            </div>
          </div>
          <div className="col gap-2">
            <button className="btn btn-secondary"><Icons.doc size={14}/> Voir l'ordre signé</button>
            <button className="btn btn-secondary"><Icons.download size={14}/> Mes billets</button>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-head">
          <div className="card-title">Mes missions</div>
          <div className="row gap-2">
            <button className="chip orange active">Toutes</button>
            <button className="chip">À venir (1)</button>
            <button className="chip">Terminées</button>
          </div>
        </div>
        <table className="t">
          <thead><tr><th>Réf.</th><th>Destination</th><th>Objet</th><th>Période</th><th>Indemnité (XAF)</th><th>Statut</th></tr></thead>
          <tbody>
            {[
              { id:'MO-2026-0086', dest:'Lagos (Ng)', purpose:'Africa Mobile Summit', period:'22-25 mai', amount:'920 000', status:'upcoming' },
              { id:'MO-2026-0072', dest:'Douala', purpose:'Audit infra back-office', period:'04-05 avr', amount:'85 000', status:'done' },
              { id:'MO-2026-0058', dest:'Garoua', purpose:'Déploiement Field Ops', period:'12-14 mar', amount:'180 000', status:'done' },
              { id:'MO-2025-0234', dest:'Paris (Fr)', purpose:'Salon Vivatech', period:'15-19 jun 25', amount:'1 850 000', status:'done' },
              { id:'MO-2025-0198', dest:'Douala', purpose:'Formation interne équipe Mobile', period:'02-04 mai 25', amount:'95 000', status:'done' },
            ].map(m => (
              <tr key={m.id}>
                <td className="mono" style={{fontSize:11, color:'var(--ink-3)'}}>{m.id}</td>
                <td>
                  <div className="row gap-2">
                    <Icons.mission size={14} style={{color:'var(--orange-500)'}}/>
                    <span style={{fontSize:13, fontWeight:600}}>{m.dest}</span>
                  </div>
                </td>
                <td style={{fontSize:13}}>{m.purpose}</td>
                <td className="muted">{m.period}</td>
                <td className="tabular" style={{fontWeight:700}}>{m.amount}</td>
                <td>
                  {m.status === 'upcoming' && <span className="badge orange">À venir</span>}
                  {m.status === 'done' && <span className="badge green">Terminée</span>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
window.EmpPageMissions = EmpPageMissions;

/* ---------------- Loans ---------------- */
function EmpPageLoans() {
  const { Icons, PageHeader } = window;

  return (
    <div>
      <PageHeader
        uc="Rémunération"
        title="Mes avances & prêts"
        subtitle="Demandes, échéanciers et historique de remboursement"
        actions={<button className="btn btn-primary"><Icons.plus/> Nouvelle demande</button>}
      />

      {/* Active loan hero */}
      <div className="card card-pad-lg" style={{
        background:'linear-gradient(135deg, #1A150E 0%, #2D2520 100%)',
        color:'#fff', marginBottom:20, position:'relative', overflow:'hidden'
      }}>
        <div style={{position:'absolute', top:-100, right:-100, width:400, height:400,
          background:'radial-gradient(circle, rgba(249,115,22,0.3) 0%, transparent 70%)'}}/>
        <div className="row" style={{alignItems:'flex-start', position:'relative'}}>
          <div style={{flex:1}}>
            <div className="row gap-2">
              <span className="badge orange" style={{background:'rgba(249,115,22,0.2)', color:'#FFB066'}}>Prêt actif</span>
              <span className="mono" style={{fontSize:11, opacity:0.6}}>PR-2026-012</span>
            </div>
            <div className="h-display" style={{fontSize:28, color:'#fff', marginTop:8}}>Prêt personnel · 2,8M XAF</div>
            <div style={{fontSize:13, opacity:0.7, marginTop:4}}>Démarré en jan 2026 · 12 mensualités · taux 4%</div>

            <div style={{marginTop:24, marginBottom:20}}>
              <div className="row" style={{marginBottom:8}}>
                <span style={{fontSize:12, opacity:0.7}}>Remboursé</span>
                <span className="tabular" style={{marginLeft:'auto', fontSize:13, fontWeight:700}}>1 330 000 / 2 800 000 XAF</span>
              </div>
              <div className="bar thick" style={{background:'rgba(255,255,255,0.2)'}}>
                <div style={{width:'48%', background:'#FFB066'}}/>
              </div>
            </div>

            <div className="row gap-4" style={{flexWrap:'wrap'}}>
              <div>
                <div style={{fontSize:11, opacity:0.6, textTransform:'uppercase', letterSpacing:'0.05em'}}>Mensualité</div>
                <div className="tabular" style={{fontSize:20, fontWeight:800, fontFamily:'Inter Tight'}}>245 000 <span style={{fontSize:12, opacity:0.6}}>XAF</span></div>
              </div>
              <div>
                <div style={{fontSize:11, opacity:0.6, textTransform:'uppercase', letterSpacing:'0.05em'}}>Restant dû</div>
                <div className="tabular" style={{fontSize:20, fontWeight:800, fontFamily:'Inter Tight', color:'#FFB066'}}>1 470 000 <span style={{fontSize:12, opacity:0.6, color:'#fff'}}>XAF</span></div>
              </div>
              <div>
                <div style={{fontSize:11, opacity:0.6, textTransform:'uppercase', letterSpacing:'0.05em'}}>Échéances restantes</div>
                <div className="tabular" style={{fontSize:20, fontWeight:800, fontFamily:'Inter Tight'}}>6 / 12</div>
              </div>
            </div>
          </div>
          <button className="btn" style={{background:'#fff', color:'var(--ink)'}}><Icons.download size={14}/> Tableau d'amortissement</button>
        </div>
      </div>

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'Demandes en attente', v:'1', sub:'Avance 350K · soumis aujourd\'hui', tone:'amber' },
          { l:'Capacité disponible', v:'1,06M', sub:'XAF · max 1 mois salaire', tone:'green' },
          { l:'Cumul emprunté', v:'5,8M', sub:'XAF depuis 2022', tone:'blue' },
          { l:'Taux d\'incidents', v:'0', sub:'aucun · merci !', tone:'orange' },
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

      <div className="card">
        <div className="card-head"><div className="card-title">Historique de mes demandes</div></div>
        <table className="t">
          <thead><tr><th>Réf.</th><th>Type</th><th>Montant (XAF)</th><th>Échéance</th><th>Restant dû (XAF)</th><th>Statut</th></tr></thead>
          <tbody>
            {[
              { id:'AV-2026-088', t:'Avance sur salaire', amount:'350 000', m:'1 mois', rest:'350 000', status:'pending' },
              { id:'PR-2026-012', t:'Prêt personnel', amount:'2 800 000', m:'12 mois', rest:'1 470 000', status:'active' },
              { id:'AV-2025-211', t:'Avance sur salaire', amount:'500 000', m:'1 mois', rest:'0', status:'repaid' },
              { id:'PR-2024-008', t:'Prêt formation', amount:'1 500 000', m:'18 mois', rest:'0', status:'repaid' },
              { id:'AV-2024-145', t:'Avance sur salaire', amount:'250 000', m:'1 mois', rest:'0', status:'repaid' },
            ].map(l => (
              <tr key={l.id}>
                <td className="mono" style={{fontSize:11, color:'var(--ink-3)'}}>{l.id}</td>
                <td><span className={"badge " + (l.t.includes('Avance') ? 'amber' : 'orange')}>{l.t}</span></td>
                <td className="tabular" style={{fontWeight:700}}>{l.amount}</td>
                <td className="muted">{l.m}</td>
                <td className="tabular">{l.rest}</td>
                <td>
                  {l.status === 'pending' && <span className="badge amber">En attente</span>}
                  {l.status === 'active' && <span className="badge orange">En cours</span>}
                  {l.status === 'repaid' && <span className="badge green">Remboursé</span>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
window.EmpPageLoans = EmpPageLoans;

/* ---------------- Skills ---------------- */
function EmpPageSkills() {
  const { Icons, PageHeader } = window;

  return (
    <div>
      <PageHeader
        uc="Développement"
        title="Mes compétences"
        subtitle="Référentiel, niveaux et plan de développement personnalisé"
      />

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'Compétences acquises', v:'12', sub:'+2 sur 12 mois', tone:'orange' },
          { l:'Expertises (★★★★★)', v:'3', sub:'React Native, Architecture, AWS', tone:'green' },
          { l:'Score moyen', v:'4,2', sub:'/ 5,0 · top 15% équipe', tone:'blue' },
          { l:'Plan en cours', v:'2', sub:'compétences à développer', tone:'violet' },
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

      <div className="row gap-2" style={{marginBottom:16}}>
        <button className="chip orange active">Toutes (12)</button>
        <button className="chip">Tech (7)</button>
        <button className="chip">Management (2)</button>
        <button className="chip">Soft (3)</button>
      </div>

      {/* Skills grid */}
      <div className="grid-3" style={{gap:14, marginBottom:24}}>
        {[
          { name:'React Native', cat:'Tech', level:5, expert:true, since:'2021', last:'mai 2026' },
          { name:'Architecture mobile', cat:'Tech', level:5, expert:true, since:'2020', last:'mai 2026' },
          { name:'AWS Cloud', cat:'Tech', level:4, since:'2023', last:'sep 2023', training:'En cours' },
          { name:'TypeScript', cat:'Tech', level:4, since:'2022', last:'mar 2026' },
          { name:'Flutter', cat:'Tech', level:3, since:'2024' },
          { name:'Leadership', cat:'Management', level:3, since:'2025', training:'Recommandée' },
          { name:'Mentoring', cat:'Management', level:4, since:'2023', last:'avr 2026' },
          { name:'Anglais', cat:'Langues', level:3, since:'—' },
          { name:'Présentation', cat:'Soft', level:3, since:'2024', training:'À développer' },
        ].map((s, i) => (
          <div key={i} className="card card-pad clickable">
            <div className="row" style={{marginBottom:8}}>
              <span className="tag">{s.cat}</span>
              {s.expert && <span className="badge orange" style={{marginLeft:'auto', fontSize:10}}>★ Expert</span>}
            </div>
            <div style={{fontSize:15, fontWeight:700, marginBottom:8}}>{s.name}</div>
            <div className="row gap-1" style={{marginBottom:8}}>
              {[1,2,3,4,5].map(lvl => (
                <div key={lvl} style={{
                  flex:1, height:8, borderRadius:2,
                  background: lvl <= s.level ? (s.expert ? 'var(--orange-500)' : 'var(--orange-400)') : 'var(--bg-soft)'
                }}/>
              ))}
              <span className="tabular" style={{fontSize:12, fontWeight:700, marginLeft:6, minWidth:24}}>{s.level}/5</span>
            </div>
            <div className="row gap-3" style={{fontSize:11, color:'var(--ink-3)'}}>
              <span>Depuis {s.since}</span>
              {s.last && <span>· éval. {s.last}</span>}
            </div>
            {s.training && (
              <div style={{marginTop:10, padding:'6px 10px', background:'var(--violet-50)', color:'var(--violet-600)', borderRadius:8, fontSize:11, fontWeight:600}}>
                🎯 {s.training}
              </div>
            )}
          </div>
        ))}
      </div>

      {/* Development plan */}
      <div className="section-title">Mon plan de développement</div>
      <div className="card">
        <table className="t">
          <thead><tr><th>Compétence</th><th>Niveau actuel</th><th>Niveau cible</th><th>Écart</th><th>Action proposée</th><th>Échéance</th></tr></thead>
          <tbody>
            {[
              { s:'AWS Cloud', c:4, t:5, gap:1, a:'Formation TR-002 + certification', d:'Q3 2026' },
              { s:'Leadership', c:3, t:4, gap:1, a:'Formation TR-001 · juin 2026', d:'Q4 2026' },
              { s:'Présentation publique', c:3, t:4, gap:1, a:'Coaching individuel · 6 séances', d:'Q3 2026' },
              { s:'Anglais B2', c:3, t:4, gap:1, a:'Cours hebdo en ligne · 6 mois', d:'2027' },
            ].map((r,i) => (
              <tr key={i}>
                <td style={{fontSize:13, fontWeight:600}}>{r.s}</td>
                <td className="tabular">{r.c}/5</td>
                <td className="tabular" style={{color:'var(--orange-600)', fontWeight:700}}>{r.t}/5</td>
                <td><span className="badge amber">+{r.gap}</span></td>
                <td style={{fontSize:13}}>{r.a}</td>
                <td className="muted">{r.d}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
window.EmpPageSkills = EmpPageSkills;

/* ---------------- Medical ---------------- */
function EmpPageMedical() {
  const { Icons, PageHeader } = window;

  return (
    <div>
      <PageHeader
        uc="Santé"
        title="Mon suivi médical"
        subtitle="Visites médicales, certificats et déclarations"
        actions={<button className="btn btn-primary"><Icons.upload size={14}/> Déposer un certificat</button>}
      />

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'Statut d\'aptitude', v:'Apte', sub:'sans restriction · 14 avr 2026', tone:'green' },
          { l:'Prochaine visite', v:'14 avr', sub:'2027 · annuelle', tone:'orange' },
          { l:'Certificats 2026', v:'1', sub:'maladie · janvier', tone:'red' },
          { l:'Mutuelle', v:'Active', sub:'Activa Santé · 100%', tone:'blue' },
        ].map((k,i) => (
          <div key={i} className="kpi-mini">
            <div className="row" style={{justifyContent:'space-between'}}>
              <span className="kpi-mini-label">{k.l}</span>
              <span className={"sd " + k.tone}></span>
            </div>
            <div className="kpi-mini-value tabular" style={{fontSize: k.v.length > 4 ? 18 : 24}}>{k.v}</div>
            <div style={{fontSize:11, color:'var(--ink-3)'}}>{k.sub}</div>
          </div>
        ))}
      </div>

      {/* Next visit banner */}
      <div className="card card-pad-lg" style={{
        background:'linear-gradient(135deg, #FFF1E6 0%, #FFF8F2 60%, #fff 100%)',
        border:'1px solid var(--orange-200)', marginBottom:20
      }}>
        <div className="row gap-4">
          <div className="icon-tile orange" style={{width:54, height:54}}><Icons.medical size={26}/></div>
          <div style={{flex:1}}>
            <div className="row gap-2">
              <span className="badge orange">À planifier</span>
              <span style={{fontSize:12, color:'var(--ink-3)'}}>Échéance dans 11 mois</span>
            </div>
            <div style={{fontSize:18, fontWeight:700, marginTop:6}}>Visite médicale annuelle obligatoire</div>
            <div style={{fontSize:13, color:'var(--ink-2)', marginTop:2}}>
              Dr. Nkoa · clinique Saint-Vincent, Yaoundé · environ 1h
            </div>
          </div>
          <div className="col gap-2">
            <button className="btn btn-primary"><Icons.cal size={14}/> Prendre rendez-vous</button>
            <button className="btn btn-secondary"><Icons.info size={14}/> En savoir plus</button>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-head">
          <div className="card-title">Mes visites & certificats</div>
        </div>
        <table className="t">
          <thead><tr><th>Réf.</th><th>Type</th><th>Date</th><th>Médecin</th><th>Résultat</th><th>Document</th></tr></thead>
          <tbody>
            {[
              { id:'MV-2026-0042', type:'Visite annuelle', date:'14 avr 2026', doc:'Dr. Nkoa', result:'Apte', cat:'visit' },
              { id:'MC-2026-0012', type:'Certificat maladie', date:'08 jan 2026', doc:'Dr. Owono', result:'2 jours arrêt', cat:'cert' },
              { id:'MV-2025-0038', type:'Visite annuelle', date:'10 mar 2025', doc:'Dr. Nkoa', result:'Apte', cat:'visit' },
              { id:'MV-2024-0021', type:'Visite spécifique', date:'05 jun 2024', doc:'Dr. Tchoungui', result:'Apte', cat:'visit' },
              { id:'MV-2022-0001', type:'Visite embauche', date:'14 fév 2022', doc:'Dr. Mballa', result:'Apte', cat:'visit' },
            ].map(r => (
              <tr key={r.id}>
                <td className="mono" style={{fontSize:11, color:'var(--ink-3)'}}>{r.id}</td>
                <td>
                  <span className={"badge " + (r.cat === 'visit' ? 'orange' : 'red')}>{r.type}</span>
                </td>
                <td className="muted">{r.date}</td>
                <td className="muted">{r.doc}</td>
                <td>
                  {r.result === 'Apte' ? <span className="badge green">Apte</span> : <span className="badge red">{r.result}</span>}
                </td>
                <td>
                  <button className="btn btn-secondary btn-sm"><Icons.download size={12}/> PDF</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
window.EmpPageMedical = EmpPageMedical;
