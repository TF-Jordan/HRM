/* global React */
const { useState: useS_ehome } = React;

function EmpPageHome({ goto }) {
  const { Icons, Avatar, PageHeader, Donut, Spark, Line, EMP_ME } = window;

  const now = new Date();
  const hour = now.getHours();
  const greeting = hour < 12 ? 'Bonjour' : hour < 18 ? 'Bon après-midi' : 'Bonsoir';

  const quickActions = [
    { icon:'leave', label:'Demander un congé', tone:'orange', sub:'18,5j disponibles', go:'leaves' },
    { icon:'expense', label:'Saisir une note de frais', tone:'amber', sub:'remboursement sous 3j', go:'expenses' },
    { icon:'time', label:'Pointer / déclarer mes heures', tone:'blue', sub:'semaine 20', go:'time' },
    { icon:'loan', label:'Demander une avance', tone:'green', sub:'jusqu\'à 1 mois de salaire', go:'loans' },
    { icon:'training', label:'M\'inscrire à une formation', tone:'violet', sub:'24 disponibles', go:'trainings' },
    { icon:'doc', label:'Télécharger mes documents', tone:'teal', sub:'contrats, attestations', go:'documents' },
  ];

  return (
    <div>
      {/* Hero — personalized welcome */}
      <div className="card card-pad-lg" style={{
        background:'linear-gradient(135deg, #FFF1E6 0%, #FFFBF5 60%, #FFFFFF 100%)',
        border:'1px solid var(--orange-200)',
        marginBottom:24,
        position:'relative',
        overflow:'hidden'
      }}>
        <div style={{
          position:'absolute', top:-80, right:-80, width:300, height:300,
          background:'var(--grad-orange)', borderRadius:'50%', opacity:0.08, filter:'blur(40px)'
        }}/>
        <div className="row" style={{alignItems:'flex-start', position:'relative'}}>
          <div style={{flex:1}}>
            <div className="row gap-2" style={{marginBottom:6}}>
              <span className="badge orange" style={{textTransform:'none', letterSpacing:0}}>Mardi 14 mai 2026 · semaine 20</span>
              <span className="badge green">En ligne</span>
            </div>
            <div className="h-display" style={{fontSize:36}}>{greeting}, {EMP_ME.firstname}.</div>
            <div style={{fontSize:14.5, color:'var(--ink-2)', marginTop:8, maxWidth:520}}>
              Vous avez <b style={{color:'var(--orange-700)'}}>2 actions à compléter</b> aujourd'hui.
              Votre bulletin de mai sera disponible dans 14 jours.
            </div>
            <div className="row gap-2" style={{marginTop:18}}>
              <button className="btn btn-primary" onClick={() => goto('leaves')}><Icons.leave size={14}/> Demander un congé</button>
              <button className="btn btn-secondary" onClick={() => goto('time')}><Icons.time size={14}/> Pointer maintenant</button>
            </div>
          </div>

          {/* Today's check-in card */}
          <div style={{
            padding:20, borderRadius:16, background:'#fff', border:'1px solid var(--line)',
            boxShadow:'var(--shadow-md)', minWidth:280
          }}>
            <div className="row" style={{marginBottom:14}}>
              <div className="icon-tile green" style={{width:32, height:32}}><Icons.check size={14}/></div>
              <div style={{marginLeft:10}}>
                <div style={{fontSize:11, color:'var(--ink-3)', textTransform:'uppercase', fontWeight:600, letterSpacing:'0.06em'}}>Pointage du jour</div>
                <div style={{fontSize:13, fontWeight:700}}>En cours · 7h22 travaillées</div>
              </div>
            </div>
            <div style={{padding:'12px 14px', background:'var(--bg-dim)', borderRadius:10}}>
              <div className="row" style={{fontSize:12.5, marginBottom:4}}>
                <span style={{color:'var(--ink-3)'}}>Arrivée</span>
                <span className="tabular" style={{marginLeft:'auto', fontWeight:600}}>08:24</span>
              </div>
              <div className="row" style={{fontSize:12.5, marginBottom:4}}>
                <span style={{color:'var(--ink-3)'}}>Pause déjeuner</span>
                <span className="tabular" style={{marginLeft:'auto'}}>45 min</span>
              </div>
              <div className="row" style={{fontSize:12.5}}>
                <span style={{color:'var(--ink-3)'}}>Heure actuelle</span>
                <span className="tabular" style={{marginLeft:'auto', fontWeight:700, color:'var(--orange-600)'}}>16:46</span>
              </div>
            </div>
            <button className="btn btn-dark btn-sm" style={{width:'100%', justifyContent:'center', marginTop:12}}>
              <Icons.x size={12}/> Déclarer mon départ
            </button>
          </div>
        </div>
      </div>

      {/* Quick stats — what's mine right now */}
      <div className="grid-4" style={{marginBottom:24}}>
        {[
          { l:'Solde congés', v:'18,5', u:'jours', sub:'sur 28 acquis', tone:'orange', icon:'leave' },
          { l:'Heures du mois', v:'142', u:'/ 173h', sub:'+12h sup.', tone:'blue', icon:'time' },
          { l:'Net du mois', v:'1,07', u:'M XAF', sub:'estimation mai', tone:'green', icon:'payroll' },
          { l:'Formations', v:'1', u:'en cours', sub:'AWS · 60% complété', tone:'violet', icon:'training' },
        ].map((k,i) => {
          const I = Icons[k.icon];
          return (
            <div key={i} className={"kpi " + k.tone}>
              <div className="row">
                <div className="kpi-icon"><I size={18}/></div>
              </div>
              <div>
                <div className="kpi-label">{k.l}</div>
                <div className="row" style={{alignItems:'baseline', gap:6}}>
                  <span className="kpi-value tabular">{k.v}</span>
                  <span style={{fontSize:12, opacity:0.9}}>{k.u}</span>
                </div>
                <div className="kpi-foot">{k.sub}</div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Quick actions grid */}
      <div className="section-title">Actions rapides</div>
      <div className="grid-3" style={{gap:14, marginBottom:24}}>
        {quickActions.map((a, i) => {
          const I = Icons[a.icon];
          return (
            <button key={i} onClick={() => goto(a.go)} className="card card-pad clickable" style={{
              display:'flex', alignItems:'center', gap:14, textAlign:'left', cursor:'pointer'
            }}>
              <div className={"icon-tile " + a.tone} style={{width:46, height:46}}><I size={20}/></div>
              <div style={{flex:1}}>
                <div style={{fontSize:14, fontWeight:700}}>{a.label}</div>
                <div style={{fontSize:12, color:'var(--ink-3)', marginTop:2}}>{a.sub}</div>
              </div>
              <Icons.chevR size={14} style={{color:'var(--ink-4)'}}/>
            </button>
          );
        })}
      </div>

      <div style={{display:'grid', gridTemplateColumns:'1.4fr 1fr', gap:16, marginBottom:24}}>
        {/* My recent requests */}
        <div className="card">
          <div className="card-head">
            <div>
              <div className="card-title">Mes demandes récentes</div>
              <div className="muted" style={{fontSize:12, marginTop:2}}>Statut en temps réel</div>
            </div>
            <button className="btn btn-ghost btn-sm">Tout voir <Icons.chevR size={12}/></button>
          </div>
          <div>
            {[
              { type:'leave', tone:'amber', icon:'leave', title:'Congé annuel · 18→22 mai', meta:'5 jours · soumis il y a 2j', status:'pending', step:1 },
              { type:'expense', tone:'green', icon:'expense', title:'Note de frais · Conférence Lagos', meta:'546 800 XAF · 11 lignes', status:'approved', step:2 },
              { type:'loan', tone:'orange', icon:'loan', title:'Avance sur salaire', meta:'350 000 XAF · soumis aujourd\'hui', status:'pending', step:1 },
              { type:'training', tone:'violet', icon:'training', title:'AWS Cloud Architect', meta:'Inscription validée · début 20 mai', status:'approved', step:2 },
              { type:'mission', tone:'blue', icon:'mission', title:'Mission Lagos · Africa Mobile Summit', meta:'22→25 mai · 920 000 XAF', status:'approved', step:2 },
            ].map((r, i) => {
              const I = Icons[r.icon];
              return (
                <div key={i} className="lrow">
                  <div className={"icon-tile " + r.tone}><I size={16}/></div>
                  <div style={{flex:1, minWidth:0}}>
                    <div style={{fontSize:13.5, fontWeight:600}}>{r.title}</div>
                    <div style={{fontSize:11.5, color:'var(--ink-3)'}}>{r.meta}</div>
                  </div>
                  <div className="stepper" style={{gap:4}}>
                    <span className={"step " + (r.step >= 1 ? 'done' : '')} style={{fontSize:10, padding:'3px 8px'}}>Soumis</span>
                    <span className={"step " + (r.step >= 2 ? 'done' : r.status === 'pending' ? 'active' : '')} style={{fontSize:10, padding:'3px 8px'}}>Validé</span>
                    <span className={"step " + (r.step >= 3 ? 'done' : '')} style={{fontSize:10, padding:'3px 8px'}}>Clôturé</span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* My next events */}
        <div className="card">
          <div className="card-head">
            <div className="card-title">Mes prochains rendez-vous</div>
            <button className="icon-btn" style={{width:28, height:28}}><Icons.cal size={14}/></button>
          </div>
          <div style={{padding:8}}>
            {[
              { d:'16', m:'mai', tag:'Entretien', tagColor:'orange', title:'Évaluation Q1 avec Marc Foga', sub:'14h00 · Salle Tokyo · 1h', highlight:true },
              { d:'18', m:'mai', tag:'Congé', tagColor:'amber', title:'Début de congé annuel', sub:'5 jours · retour 23 mai' },
              { d:'20', m:'mai', tag:'Formation', tagColor:'violet', title:'Démarrage AWS Cloud', sub:'40h · hybride · 6 semaines' },
              { d:'22', m:'mai', tag:'Mission', tagColor:'blue', title:'Vol Lagos · Africa Mobile Summit', sub:'08h15 · départ Yaoundé Nsimalen' },
              { d:'28', m:'mai', tag:'Paie', tagColor:'green', title:'Bulletin de mai disponible', sub:'Virement prévu sur MTN MoMo' },
            ].map((u, i) => (
              <div key={i} className="row" style={{padding:'10px 12px', gap:12, alignItems:'flex-start', borderRadius:10,
                background: u.highlight ? 'linear-gradient(135deg, var(--orange-50) 0%, #fff 100%)' : 'transparent',
                border: u.highlight ? '1px solid var(--orange-200)' : 'none', marginBottom:4}}>
                <div style={{
                  width:44, padding:6, textAlign:'center', borderRadius:10,
                  background: u.highlight ? 'var(--grad-orange)' : '#fff',
                  color: u.highlight ? '#fff' : 'var(--ink)',
                  border: u.highlight ? 'none' : '1px solid var(--line)',
                  flexShrink: 0
                }}>
                  <div style={{fontFamily:'Inter Tight', fontWeight:800, fontSize:16, lineHeight:1}}>{u.d}</div>
                  <div style={{fontSize:9, textTransform:'uppercase', letterSpacing:'0.06em', opacity:.85, marginTop:2}}>{u.m}</div>
                </div>
                <div style={{flex:1, minWidth:0}}>
                  <span className={"badge " + u.tagColor} style={{fontSize:10}}>{u.tag}</span>
                  <div style={{fontSize:13, fontWeight:600, marginTop:4}}>{u.title}</div>
                  <div style={{fontSize:11, color:'var(--ink-3)'}}>{u.sub}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* My leave balance + payroll trend */}
      <div style={{display:'grid', gridTemplateColumns:'1fr 1.4fr', gap:16, marginBottom:24}}>
        <div className="card">
          <div className="card-head"><div className="card-title">Mes soldes de congés</div></div>
          <div style={{padding:'20px 22px'}}>
            <div style={{position:'relative', display:'flex', justifyContent:'center', marginBottom:18}}>
              <Donut data={[
                { value:18.5, color:'#F97316' },
                { value:9.5, color:'var(--bg-soft)' },
              ]} size={180} thick={26}/>
              <div style={{position:'absolute', inset:0, display:'grid', placeItems:'center', textAlign:'center'}}>
                <div>
                  <div style={{fontFamily:'Inter Tight', fontSize:36, fontWeight:800, letterSpacing:'-0.025em'}} className="tabular">18,5</div>
                  <div style={{fontSize:11, color:'var(--ink-3)'}}>jours restants</div>
                </div>
              </div>
            </div>
            <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:10}}>
              {[
                ['Congé annuel', '18,5 j', 'sur 28', 'orange'],
                ['Maladie', '2 j', 'utilisés', 'red'],
                ['RTT', '6 j', 'restants', 'amber'],
                ['Compte épargne', '4 j', 'cumulés', 'violet'],
              ].map(([l, v, s, c], i) => (
                <div key={i} style={{padding:10, background:'var(--bg-dim)', borderRadius:10}}>
                  <div className="row gap-2" style={{marginBottom:2}}>
                    <span className={"sd " + c}></span>
                    <span style={{fontSize:11, color:'var(--ink-3)', fontWeight:600}}>{l}</span>
                  </div>
                  <div className="row" style={{alignItems:'baseline'}}>
                    <span style={{fontFamily:'Inter Tight', fontSize:18, fontWeight:700}} className="tabular">{v}</span>
                    <span style={{fontSize:11, color:'var(--ink-3)', marginLeft:6}}>{s}</span>
                  </div>
                </div>
              ))}
            </div>
            <button className="btn btn-primary btn-sm" style={{width:'100%', justifyContent:'center', marginTop:14}} onClick={() => goto('leaves')}>
              <Icons.plus size={13}/> Demander un congé
            </button>
          </div>
        </div>

        <div className="card">
          <div className="card-head">
            <div>
              <div className="card-title">Évolution de mon net</div>
              <div className="muted" style={{fontSize:12, marginTop:2}}>12 derniers bulletins · XAF</div>
            </div>
            <span className="badge green"><Icons.trendUp size={11}/> +12% YoY</span>
          </div>
          <div style={{padding:'18px 22px'}}>
            <Line data={[895, 910, 935, 942, 958, 965, 982, 998, 1015, 1028, 1042, 1067]}
              labels={['Jui','Juil','Aoû','Sep','Oct','Nov','Déc','Jan','Fév','Mar','Avr','Mai']}
              color="#F97316" fill="#F97316" h={200}/>
            <div className="row gap-3" style={{marginTop:14, paddingTop:12, borderTop:'1px solid var(--line-soft)', flexWrap:'wrap'}}>
              <span style={{fontSize:12}}>Net mois en cours <b className="tabular">1 066 960 XAF</b></span>
              <span style={{fontSize:12, color:'var(--ink-3)'}}>· Cumul 2026 <b className="tabular" style={{color:'var(--ink-2)'}}>4 962 000</b> XAF</span>
              <button className="btn btn-secondary btn-sm" style={{marginLeft:'auto'}} onClick={() => goto('payslips')}>Voir bulletins</button>
            </div>
          </div>
        </div>
      </div>

      {/* Team birthdays + announcements */}
      <div style={{display:'grid', gridTemplateColumns:'1fr 1.4fr', gap:16}}>
        <div className="card">
          <div className="card-head">
            <div className="card-title">🎂 Anniversaires & arrivées</div>
          </div>
          <div style={{padding:8}}>
            {[
              { type:'bday', who:'Sarah Nguemo', sub:'Aujourd\'hui · 32 ans', tag:'🎉', color:'violet' },
              { type:'arrival', who:'Léa Ondoa', sub:'Arrive le 22 mai · Designer UI', tag:'👋', color:'teal' },
              { type:'bday', who:'Olivier Manga', sub:'Demain · 41 ans', tag:'🎂', color:'green' },
              { type:'work', who:'Pierre Kouam', sub:'1 an dans l\'entreprise · 15 mai', tag:'⭐', color:'amber' },
            ].map((it, i) => (
              <div key={i} className="lrow" style={{padding:'10px 14px'}}>
                <Avatar name={it.who} color={it.color} size="sm"/>
                <div style={{flex:1, minWidth:0}}>
                  <div style={{fontSize:13, fontWeight:600}}>{it.who}</div>
                  <div style={{fontSize:11, color:'var(--ink-3)'}}>{it.sub}</div>
                </div>
                <span style={{fontSize:18}}>{it.tag}</span>
              </div>
            ))}
          </div>
        </div>

        <div className="card">
          <div className="card-head">
            <div className="card-title">📣 Annonces de l'entreprise</div>
            <button className="btn btn-ghost btn-sm">Tout voir</button>
          </div>
          <div style={{padding:'8px'}}>
            {[
              { tag:'RH', tagColor:'orange', title:'Nouveau plan formation 2026', sub:'24 formations disponibles dont 8 en ligne', date:'il y a 2j' },
              { tag:'Direction', tagColor:'violet', title:'Résultats Q1 2026 · +28% de croissance', sub:'Marc Foga partage les chiffres et les objectifs Q2', date:'il y a 4j' },
              { tag:'Bien-être', tagColor:'green', title:'Activités sportives à Yaoundé HQ', sub:'Inscription au tournoi de football inter-services', date:'il y a 1 sem.' },
              { tag:'IT', tagColor:'blue', title:'Maintenance VPN dimanche 18 mai', sub:'Coupure prévue de 02h à 04h', date:'il y a 1 sem.' },
            ].map((a, i) => (
              <div key={i} style={{padding:14, borderBottom: i < 3 ? '1px solid var(--line-soft)' : 'none', cursor:'pointer'}}>
                <div className="row gap-2" style={{marginBottom:4}}>
                  <span className={"badge " + a.tagColor} style={{fontSize:10}}>{a.tag}</span>
                  <span style={{fontSize:11, color:'var(--ink-4)', marginLeft:'auto'}}>{a.date}</span>
                </div>
                <div style={{fontSize:13.5, fontWeight:600}}>{a.title}</div>
                <div style={{fontSize:12, color:'var(--ink-3)', marginTop:2}}>{a.sub}</div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
window.EmpPageHome = EmpPageHome;
