/* global React */
const { useState: useState_emp } = React;

function PageEmployees({ goto }) {
  const { Icons, Avatar, PageHeader, Spark } = window;
  const [view, setView] = useState_emp('list'); // 'list' | 'detail'
  const [selected, setSelected] = useState_emp(null);

  const employees = [
    { id:'EMP-0142', name:'Aminata Diallo', email:'a.diallo@rt-comops.com', role:'Lead Mobile Engineer', dept:'Engineering', site:'Yaoundé HQ', contract:'CDI', status:'Actif', tenure:'4a 3m', avatar:'orange' },
    { id:'EMP-0098', name:'Joseph Mbarga', email:'j.mbarga@rt-comops.com', role:'Business Developer', dept:'Commercial', site:'Douala', contract:'CDI', status:'Actif', tenure:'2a 7m', avatar:'blue' },
    { id:'EMP-0203', name:'Sarah Nguemo', email:'s.nguemo@rt-comops.com', role:'UX Designer', dept:'Engineering', site:'Yaoundé HQ', contract:'CDI', status:'Actif', tenure:'1a 2m', avatar:'violet' },
    { id:'EMP-0177', name:'Pierre Kouam', email:'p.kouam@rt-comops.com', role:'Comptable', dept:'Finance & RH', site:'Yaoundé HQ', contract:'CDD', status:'Période d\'essai', tenure:'4m', avatar:'amber' },
    { id:'EMP-0021', name:'Marc Foga', email:'m.foga@rt-comops.com', role:'Directeur Technique', dept:'Direction', site:'Yaoundé HQ', contract:'CDI', status:'Actif', tenure:'6a 1m', avatar:'green' },
    { id:'EMP-0234', name:'Léa Ondoa', email:'l.ondoa@rt-comops.com', role:'Designer UI', dept:'Engineering', site:'Yaoundé HQ', contract:'Stage', status:'Pré-onboarding', tenure:'—', avatar:'teal' },
    { id:'EMP-0156', name:'Yannick Etoa', email:'y.etoa@rt-comops.com', role:'Talent Acquisition', dept:'Finance & RH', site:'Douala', contract:'CDI', status:'Actif', tenure:'3a 4m', avatar:'orange' },
    { id:'EMP-0089', name:'Diane Tsoumou', email:'d.tsoumou@rt-comops.com', role:'Support Tier 2', dept:'Support', site:'Douala', contract:'CDI', status:'Actif', tenure:'2a 11m', avatar:'violet' },
    { id:'EMP-0254', name:'Karine Djoumessi', email:'k.djoumessi@rt-comops.com', role:'Field Operations', dept:'Opérations', site:'Bafoussam', contract:'CDI', status:'Actif', tenure:'5m', avatar:'blue' },
    { id:'EMP-0067', name:'Olivier Manga', email:'o.manga@rt-comops.com', role:'DevOps Engineer', dept:'Engineering', site:'Yaoundé HQ', contract:'CDI', status:'Actif', tenure:'4a 9m', avatar:'green' },
    { id:'EMP-0119', name:'Estelle Bilong', email:'e.bilong@rt-comops.com', role:'Chargée de paie', dept:'Finance & RH', site:'Yaoundé HQ', contract:'CDI', status:'Congé maternité', tenure:'7a 2m', avatar:'amber' },
    { id:'EMP-0188', name:'Hervé Tankeu', email:'h.tankeu@rt-comops.com', role:'Sales Manager', dept:'Commercial', site:'Douala', contract:'CDI', status:'Actif', tenure:'3a 8m', avatar:'orange' },
  ];

  if (view === 'detail' && selected) {
    return <EmployeeDetail emp={selected} onBack={() => setView('list')}/>;
  }

  return (
    <div>
      <PageHeader
        uc="Capital humain"
        title="Employés"
        subtitle="342 collaborateurs · 6 départements · 4 sites"
        actions={
          <>
            <button className="btn btn-secondary"><Icons.upload/> Importer</button>
            <button className="btn btn-secondary"><Icons.download/> Exporter</button>
            <button className="btn btn-primary"><Icons.plus/> Ajouter</button>
          </>
        }
      />

      {/* KPI mini cards */}
      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'Effectif total', v:'342', sub:'+12 ce mois', tone:'green' },
          { l:'CDI', v:'278', sub:'81% de l\'effectif', tone:'orange' },
          { l:'CDD & Stages', v:'52', sub:'8 fins de contrat (90j)', tone:'amber' },
          { l:'En période d\'essai', v:'12', sub:'3 à valider ce mois', tone:'blue' },
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

      {/* Filters */}
      <div className="card" style={{padding:16, marginBottom:16}}>
        <div className="row gap-3" style={{flexWrap:'wrap'}}>
          <div className="row gap-2" style={{flex:1, minWidth:280, padding:'8px 12px', background:'#fff', border:'1px solid var(--line)', borderRadius:10}}>
            <Icons.search size={14} style={{color:'var(--ink-3)'}}/>
            <input style={{border:'none', outline:'none', flex:1, fontSize:13}} placeholder="Nom, ID, email, poste…"/>
          </div>
          <button className="chip orange active">Tous (342)</button>
          <button className="chip">Engineering (124)</button>
          <button className="chip">Opérations (78)</button>
          <button className="chip">Commercial (52)</button>
          <button className="chip">+ 3 autres</button>
          <div style={{width:1, height:24, background:'var(--line)'}}/>
          <button className="btn btn-secondary btn-sm"><Icons.filter size={14}/> Plus de filtres</button>
        </div>
      </div>

      {/* Table */}
      <div className="card" style={{overflow:'hidden'}}>
        <table className="t">
          <thead>
            <tr>
              <th style={{width:36}}><input type="checkbox"/></th>
              <th>Employé</th>
              <th>Poste</th>
              <th>Département</th>
              <th>Site</th>
              <th>Contrat</th>
              <th>Statut</th>
              <th>Ancienneté</th>
              <th style={{width:50}}></th>
            </tr>
          </thead>
          <tbody>
            {employees.map(e => {
              const statusColor = e.status === 'Actif' ? 'green' :
                e.status === 'Période d\'essai' ? 'amber' :
                e.status === 'Pré-onboarding' ? 'blue' :
                e.status === 'Congé maternité' ? 'violet' : 'gray';
              return (
                <tr key={e.id} onClick={() => { setSelected(e); setView('detail'); }} style={{cursor:'pointer'}}>
                  <td><input type="checkbox" onClick={ev => ev.stopPropagation()}/></td>
                  <td>
                    <div className="row gap-2">
                      <Avatar name={e.name} color={e.avatar}/>
                      <div>
                        <div style={{fontSize:13.5, fontWeight:600}}>{e.name}</div>
                        <div style={{fontSize:11, color:'var(--ink-3)'}} className="mono">{e.id} · {e.email}</div>
                      </div>
                    </div>
                  </td>
                  <td>{e.role}</td>
                  <td><span className="tag">{e.dept}</span></td>
                  <td><span className="muted">{e.site}</span></td>
                  <td>
                    <span className={"badge " + (e.contract === 'CDI' ? 'green' : e.contract === 'CDD' ? 'amber' : 'blue')}>{e.contract}</span>
                  </td>
                  <td><span className={"badge " + statusColor}>{e.status}</span></td>
                  <td><span className="muted">{e.tenure}</span></td>
                  <td>
                    <button className="icon-btn" style={{width:28, height:28}} onClick={ev => ev.stopPropagation()}>
                      <Icons.more size={14}/>
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
        <div className="row" style={{padding:'14px 22px', borderTop:'1px solid var(--line-soft)', fontSize:12, color:'var(--ink-3)'}}>
          <span>Affichage de <b className="tabular" style={{color:'var(--ink)'}}>1–12</b> sur <b className="tabular" style={{color:'var(--ink)'}}>342</b> employés</span>
          <div className="row gap-2" style={{marginLeft:'auto'}}>
            <button className="chip">‹ Préc.</button>
            <button className="chip orange active">1</button>
            <button className="chip">2</button>
            <button className="chip">3</button>
            <span style={{padding:'0 4px'}}>…</span>
            <button className="chip">29</button>
            <button className="chip">Suiv. ›</button>
          </div>
        </div>
      </div>
    </div>
  );
}

function EmployeeDetail({ emp, onBack }) {
  const { Icons, Avatar, PageHeader, Donut, Spark } = window;
  const [tab, setTab] = useState_emp('overview');

  const tabs = [
    { id:'overview', label:'Vue d\'ensemble' },
    { id:'contract', label:'Contrats' },
    { id:'payroll', label:'Paie' },
    { id:'leaves', label:'Congés' },
    { id:'skills', label:'Compétences' },
    { id:'reviews', label:'Évaluations' },
    { id:'trainings', label:'Formations' },
    { id:'documents', label:'Documents' },
  ];

  return (
    <div>
      <div className="row gap-2" style={{marginBottom:14}}>
        <button className="btn btn-ghost btn-sm" onClick={onBack}><Icons.chevR size={14} style={{transform:'rotate(180deg)'}}/> Retour</button>
        <span className="muted" style={{fontSize:12}}>Employés / {emp.name}</span>
      </div>

      {/* Hero card */}
      <div className="card card-pad-lg" style={{
        background:'linear-gradient(135deg, #FFFAF2 0%, #FFFFFF 60%)',
        border:'1px solid var(--orange-200)',
        marginBottom:20,
        position:'relative',
        overflow:'hidden'
      }}>
        <div style={{
          position:'absolute', top:-60, right:-60, width:240, height:240,
          background:'var(--grad-orange)', borderRadius:'50%', opacity:0.08, filter:'blur(20px)'
        }}/>
        <div className="row gap-4" style={{alignItems:'flex-start'}}>
          <div style={{position:'relative'}}>
            <Avatar name={emp.name} size="xl" color={emp.avatar}/>
            <div style={{position:'absolute', bottom:0, right:0, width:18, height:18, borderRadius:'50%',
              background:'var(--green-500)', border:'3px solid #fff'}}/>
          </div>
          <div style={{flex:1}}>
            <div className="row gap-2" style={{marginBottom:4}}>
              <span className="mono" style={{fontSize:11, color:'var(--orange-700)', background:'var(--orange-50)', padding:'2px 8px', borderRadius:999}}>{emp.id}</span>
              <span className={"badge " + (emp.contract === 'CDI' ? 'green' : 'amber')}>{emp.contract}</span>
              <span className="badge green">{emp.status}</span>
            </div>
            <div className="h-display" style={{fontSize:30}}>{emp.name}</div>
            <div style={{fontSize:14, color:'var(--ink-2)', marginTop:4}}>{emp.role} · {emp.dept}</div>
            <div className="row gap-4" style={{marginTop:16, flexWrap:'wrap'}}>
              <span className="row gap-2 muted" style={{fontSize:12.5}}><Icons.cal size={14}/> Embauche · 14 fév 2022</span>
              <span className="row gap-2 muted" style={{fontSize:12.5}}><Icons.briefcase size={14}/> {emp.site}</span>
              <span className="row gap-2 muted" style={{fontSize:12.5}}><Icons.users size={14}/> Manager · Marc Foga</span>
              <span className="row gap-2 muted" style={{fontSize:12.5}}><Icons.shield size={14}/> CNPS · 110428937H</span>
            </div>
          </div>
          <div className="col gap-2">
            <button className="btn btn-primary"><Icons.send size={14}/> Message</button>
            <button className="btn btn-secondary"><Icons.edit size={14}/> Modifier</button>
            <button className="btn btn-secondary"><Icons.more size={14}/> Plus d'actions</button>
          </div>
        </div>

        <div className="grid-4" style={{marginTop:24, gap:14}}>
          {[
            { l:'Salaire brut', v:'1 245 000', u:'XAF/mois', spark:[1100,1150,1170,1200,1245] },
            { l:'Solde congés', v:'18,5', u:'jours · acquis 28' },
            { l:'Performance', v:'4,6', u:'/ 5 · Q1 2026', delta:'+0,3' },
            { l:'Formations 2026', v:'3', u:'complétées · 1 en cours' },
          ].map((s, i) => (
            <div key={i} style={{background:'#fff', padding:14, borderRadius:12, border:'1px solid var(--line)'}}>
              <div className="muted" style={{fontSize:11, textTransform:'uppercase', letterSpacing:'0.04em', fontWeight:600}}>{s.l}</div>
              <div className="row" style={{alignItems:'baseline', gap:6, marginTop:6}}>
                <span style={{fontFamily:'Inter Tight', fontSize:22, fontWeight:800, letterSpacing:'-0.02em'}} className="tabular">{s.v}</span>
                {s.delta && <span className="badge green">+0,3</span>}
              </div>
              <div style={{fontSize:11, color:'var(--ink-3)', marginTop:2}}>{s.u}</div>
              {s.spark && <div style={{marginTop:6}}><Spark data={s.spark} color="#F97316" h={28} w={140} fill="#F97316"/></div>}
            </div>
          ))}
        </div>
      </div>

      {/* Tabs */}
      <div className="row gap-2" style={{marginBottom:20, borderBottom:'1px solid var(--line-soft)'}}>
        {tabs.map(t => (
          <button key={t.id} onClick={() => setTab(t.id)} style={{
            padding:'10px 14px',
            borderBottom: tab === t.id ? '2px solid var(--orange-500)' : '2px solid transparent',
            fontSize:13.5, fontWeight: tab === t.id ? 700 : 500,
            color: tab === t.id ? 'var(--ink)' : 'var(--ink-3)'
          }}>{t.label}</button>
        ))}
      </div>

      {tab === 'overview' && <OverviewTab emp={emp}/>}
      {tab !== 'overview' && (
        <div className="card card-pad-lg" style={{textAlign:'center', padding:50, color:'var(--ink-3)'}}>
          Contenu de l'onglet « {tabs.find(t=>t.id===tab).label} » disponible bientôt.
        </div>
      )}
    </div>
  );
}

function OverviewTab({ emp }) {
  const { Icons, Avatar } = window;
  return (
    <div style={{display:'grid', gridTemplateColumns:'2fr 1fr', gap:16}}>
      {/* Left col */}
      <div className="col gap-3">
        {/* Onboarding / personnel */}
        <div className="card">
          <div className="card-head">
            <div className="card-title">Informations personnelles</div>
            <button className="btn btn-ghost btn-sm"><Icons.edit size={13}/> Modifier</button>
          </div>
          <div className="grid-3" style={{padding:'18px 22px', gap:18}}>
            {[
              ['Date de naissance','12 mars 1988 · 38 ans'],
              ['Nationalité','Camerounaise'],
              ['Pièce d\'identité','CNI · 1100428937'],
              ['Téléphone','+237 6 78 12 34 56'],
              ['Email perso.','aminata.d@gmail.com'],
              ['Adresse','Quartier Bastos, Yaoundé'],
              ['Situation familiale','Mariée · 2 enfants'],
              ['Banque','Afriland First · 04562...'],
              ['Mobile money','MTN MoMo · *34 56'],
            ].map(([l,v], i) => (
              <div key={i}>
                <div className="muted" style={{fontSize:11, textTransform:'uppercase', letterSpacing:'0.04em', fontWeight:600}}>{l}</div>
                <div style={{fontSize:13, marginTop:3, color:'var(--ink-2)'}}>{v}</div>
              </div>
            ))}
          </div>
        </div>

        {/* Timeline */}
        <div className="card">
          <div className="card-head">
            <div className="card-title">Chronologie de carrière</div>
            <button className="btn btn-ghost btn-sm">Voir tout</button>
          </div>
          <div style={{padding:18, paddingLeft:30}}>
            {[
              { d:'jan 2026', t:'Promotion · Lead Mobile Engineer', desc:'Augmentation de 12% · Manager: Marc Foga', tone:'orange', icon:'star' },
              { d:'mai 2024', t:'Évaluation annuelle · Excellent (4.6/5)', desc:'Bonus performance · Plan de carrière révisé', tone:'green', icon:'review' },
              { d:'sept 2023', t:'Formation · Architecture Mobile avancée', desc:'AWS · 32h · certifiée', tone:'blue', icon:'training' },
              { d:'fév 2022', t:'Embauche · Senior Mobile Engineer', desc:'CDI · Yaoundé HQ', tone:'violet', icon:'briefcase' },
            ].map((it, i, arr) => {
              const I = Icons[it.icon];
              return (
                <div key={i} className="row" style={{gap:14, paddingBottom:18, alignItems:'flex-start', position:'relative'}}>
                  {i < arr.length - 1 && <div style={{position:'absolute', left:15, top:32, bottom:0, width:2, background:'var(--line)'}}/>}
                  <div className={"icon-tile " + it.tone} style={{position:'relative', zIndex:1}}><I size={14}/></div>
                  <div style={{flex:1}}>
                    <div className="row gap-2"><span className="muted" style={{fontSize:11, textTransform:'uppercase', letterSpacing:'0.06em', fontWeight:600}}>{it.d}</span></div>
                    <div style={{fontSize:13.5, fontWeight:600, marginTop:2}}>{it.t}</div>
                    <div style={{fontSize:12, color:'var(--ink-3)', marginTop:2}}>{it.desc}</div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Right col */}
      <div className="col gap-3">
        {/* Manager / Team */}
        <div className="card card-pad">
          <div className="row" style={{marginBottom:12}}>
            <span className="card-title" style={{fontSize:14}}>Hiérarchie</span>
          </div>
          <div className="row gap-3" style={{padding:'12px', background:'var(--bg-dim)', borderRadius:10}}>
            <Avatar name="Marc Foga" color="green" size="lg"/>
            <div style={{flex:1}}>
              <div style={{fontSize:13.5, fontWeight:600}}>Marc Foga</div>
              <div style={{fontSize:11, color:'var(--ink-3)'}}>Directeur Technique · Manager direct</div>
            </div>
            <button className="icon-btn" style={{width:30,height:30}}><Icons.send size={13}/></button>
          </div>
          <div style={{margin:'12px 0', textAlign:'center', color:'var(--ink-4)'}}>↓</div>
          <div className="row gap-3" style={{padding:'12px', background:'var(--orange-50)', borderRadius:10, border:'1px solid var(--orange-200)'}}>
            <Avatar name={emp.name} color={emp.avatar} size="lg"/>
            <div style={{flex:1}}>
              <div style={{fontSize:13.5, fontWeight:700}}>{emp.name}</div>
              <div style={{fontSize:11, color:'var(--ink-3)'}}>{emp.role}</div>
            </div>
          </div>
          <div style={{margin:'12px 0', textAlign:'center', color:'var(--ink-4)'}}>↓ Manage 4 personnes</div>
          <div className="avstack">
            <Avatar name="Sarah N." color="violet"/>
            <Avatar name="Olivier M." color="green"/>
            <Avatar name="Karine D." color="blue"/>
            <Avatar name="Hervé T." color="amber"/>
          </div>
        </div>

        {/* Compétences */}
        <div className="card card-pad">
          <div className="row" style={{marginBottom:14}}>
            <span className="card-title" style={{fontSize:14}}>Compétences clés</span>
            <button className="btn btn-ghost btn-sm" style={{marginLeft:'auto'}}>+ Ajouter</button>
          </div>
          {[
            { name:'React Native', level:5, max:5 },
            { name:'Flutter', level:4, max:5 },
            { name:'AWS', level:4, max:5 },
            { name:'Leadership', level:3, max:5 },
            { name:'Architecture', level:5, max:5 },
          ].map((s,i) => (
            <div key={i} style={{marginBottom:10}}>
              <div className="row" style={{justifyContent:'space-between', marginBottom:4}}>
                <span style={{fontSize:13, fontWeight:500}}>{s.name}</span>
                <span className="row gap-1">
                  {[...Array(s.max)].map((_,j) => (
                    <span key={j} style={{
                      width:6, height:12, borderRadius:2,
                      background: j < s.level ? 'var(--orange-500)' : 'var(--bg-soft)'
                    }}/>
                  ))}
                </span>
              </div>
            </div>
          ))}
        </div>

        {/* Documents */}
        <div className="card card-pad">
          <div className="row" style={{marginBottom:12}}>
            <span className="card-title" style={{fontSize:14}}>Documents</span>
            <button className="btn btn-ghost btn-sm" style={{marginLeft:'auto'}}><Icons.upload size={13}/></button>
          </div>
          {[
            { n:'Contrat CDI signé.pdf', s:'2,4 MB', icon:'contract', tone:'green' },
            { n:'CNI scan.pdf', s:'1,1 MB', icon:'badge', tone:'blue' },
            { n:'Certif. Médical 2026.pdf', s:'480 KB', icon:'medical', tone:'red' },
            { n:'Avenant promotion.pdf', s:'620 KB', icon:'contract', tone:'orange' },
          ].map((d,i) => {
            const I = Icons[d.icon];
            return (
              <div key={i} className="row gap-2" style={{padding:'8px 0', borderBottom: i<3 ? '1px solid var(--line-soft)' : 'none'}}>
                <div className={"icon-tile " + d.tone} style={{width:30, height:30}}><I size={13}/></div>
                <div style={{flex:1, minWidth:0}}>
                  <div style={{fontSize:12.5, fontWeight:500, whiteSpace:'nowrap', overflow:'hidden', textOverflow:'ellipsis'}}>{d.n}</div>
                  <div style={{fontSize:11, color:'var(--ink-3)'}}>{d.s}</div>
                </div>
                <button className="icon-btn" style={{width:26, height:26}}><Icons.download size={12}/></button>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

window.PageEmployees = PageEmployees;
