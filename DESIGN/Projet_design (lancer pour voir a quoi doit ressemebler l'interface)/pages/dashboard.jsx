/* global React */
const { useState } = React;

function PageDashboard({ goto }) {
  const { Icons, Avatar, PageHeader, Donut, Spark, Line } = window;

  const kpis = [
    { label: 'Effectif total', value: '342', delta: '+12', deltaType: 'up', sub: '8 entrées · 3 départs (mois)', icon: 'users', tone: 'orange' },
    { label: 'Masse salariale', value: '142,8M', sub: 'XAF · mai 2026', icon: 'payroll', tone: 'dark' },
    { label: 'Taux d\'absentéisme', value: '3,4%', delta: '-0,8pt', deltaType: 'down', sub: 'sur 30 derniers jours', icon: 'leave', tone: 'amber' },
    { label: 'Engagement', value: '78', sub: 'eNPS · sondage Q1', icon: 'star', tone: 'violet' },
  ];

  const headcountData = [298, 305, 310, 318, 322, 328, 332, 336, 338, 340, 341, 342];
  const monthLabels = ['Jui','Juil','Aoû','Sep','Oct','Nov','Déc','Jan','Fév','Mar','Avr','Mai'];

  const departments = [
    { name: 'Engineering', value: 124, color: '#F97316' },
    { name: 'Opérations', value: 78, color: '#FB923C' },
    { name: 'Commercial', value: 52, color: '#FCD34D' },
    { name: 'Support', value: 36, color: '#34D399' },
    { name: 'Finance & RH', value: 28, color: '#60A5FA' },
    { name: 'Direction', value: 24, color: '#A78BFA' },
  ];

  const todoList = [
    { type: 'leave', user: 'Aminata Diallo', meta: '5 jours · Congé annuel · 18→22 mai', urgency: 'now' },
    { type: 'expense', user: 'Joseph Mbarga', meta: '124 500 XAF · Déplacement Yaoundé', urgency: 'now' },
    { type: 'review', user: 'Sarah Nguemo', meta: 'Évaluation 360° · échéance dans 2j', urgency: 'soon' },
    { type: 'contract', user: 'Pierre Kouam', meta: 'CDD arrive à terme le 12/06/2026', urgency: 'soon' },
    { type: 'loan', user: 'Diane Tsoumou', meta: 'Demande d\'avance · 350 000 XAF', urgency: 'now' },
    { type: 'medical', user: 'Marc Foga', meta: 'Visite médicale annuelle dans 7j', urgency: 'later' },
  ];

  const upcoming = [
    { d: '14', m: 'mai', title: 'Run paie · mai 2026', tag: 'Paie', tagColor: 'orange', sub: '342 employés · estimation 142,8M XAF', cta: 'Lancer' },
    { d: '16', m: 'mai', title: 'Comité de recrutement', tag: 'Recrutement', tagColor: 'blue', sub: '7 candidatures en lice · Tech Lead Mobile', cta: 'Voir' },
    { d: '19', m: 'mai', title: 'Échéance déclaration CNPS', tag: 'Conformité', tagColor: 'red', sub: 'DSN mai · à finaliser', cta: 'Préparer' },
    { d: '22', m: 'mai', title: 'Onboarding Léa Ondoa', tag: 'Personnel', tagColor: 'violet', sub: 'Designer UI · Yaoundé HQ', cta: 'Préparer' },
  ];

  const recentActivity = [
    { who: 'Aminata Diallo', what: 'a soumis une demande de congé', when: 'il y a 12 min', icon: 'leave', color: 'orange' },
    { who: 'Vous', what: 'avez approuvé une note de frais (Karine D.)', when: 'il y a 38 min', icon: 'expense', color: 'green' },
    { who: 'Système Paie', what: 'a calculé les bulletins de mai (brouillon)', when: 'il y a 1h', icon: 'payroll', color: 'amber' },
    { who: 'Marc Foga', what: 'a complété sa formation "Sécurité info"', when: 'il y a 2h', icon: 'training', color: 'violet' },
    { who: 'Yannick Etoa', what: 'a publié l\'offre d\'emploi Data Engineer', when: 'il y a 3h', icon: 'recruit', color: 'blue' },
  ];

  return (
    <div>
      <PageHeader
        uc="DRH · Vue d'ensemble"
        title="Bonjour, Faïsal"
        subtitle="6 actions vous attendent aujourd'hui · 4 échéances cette semaine"
        actions={
          <>
            <button className="btn btn-secondary"><Icons.download/> Exporter le rapport</button>
            <button className="btn btn-primary"><Icons.plus/> Nouvel employé</button>
          </>
        }
      />

      {/* KPI hero row */}
      <div className="grid-4">
        {kpis.map((k, i) => {
          const I = Icons[k.icon];
          return (
            <div key={i} className={"kpi " + k.tone}>
              <div className="row">
                <div className="kpi-icon"><I size={18}/></div>
                <div style={{marginLeft:'auto'}}>
                  {k.delta && (
                    <span className="badge" style={{background:'rgba(255,255,255,0.18)', color:'#fff'}}>
                      {k.deltaType==='up' ? <Icons.trendUp size={11}/> : <Icons.trendDown size={11}/>}
                      {k.delta}
                    </span>
                  )}
                </div>
              </div>
              <div>
                <div className="kpi-label">{k.label}</div>
                <div className="kpi-value tabular">{k.value}</div>
                <div className="kpi-foot">{k.sub}</div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Headcount chart + departments donut */}
      <div style={{display:'grid', gridTemplateColumns:'2fr 1fr', gap:16, marginTop:16}}>
        <div className="card">
          <div className="card-head">
            <div>
              <div className="card-title">Évolution de l'effectif</div>
              <div className="muted" style={{fontSize:12, marginTop:2}}>12 derniers mois · entrées vs départs</div>
            </div>
            <div className="row gap-2">
              <span className="badge orange">Effectif total</span>
              <button className="chip orange active">12M</button>
              <button className="chip">YTD</button>
              <button className="chip">All</button>
            </div>
          </div>
          <div style={{padding:'18px 22px'}}>
            <Line data={headcountData} labels={monthLabels} color="#F97316" fill="#F97316" h={220}/>
            <div className="row gap-4" style={{marginTop:16, paddingTop:14, borderTop:'1px solid var(--line-soft)'}}>
              <div className="row gap-2"><div className="sd green"></div><span style={{fontSize:12}}><b>+44</b> entrées</span></div>
              <div className="row gap-2"><div className="sd red"></div><span style={{fontSize:12}}><b>-9</b> départs</span></div>
              <div className="row gap-2"><div className="sd orange"></div><span style={{fontSize:12}}>Turnover <b>2,4%</b></span></div>
              <div style={{marginLeft:'auto', fontSize:12, color:'var(--ink-3)'}}>Croissance nette <b style={{color:'var(--green-600)'}}>+14,8%</b></div>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-head">
            <div className="card-title">Répartition par dpt</div>
            <button className="icon-btn" style={{width:28, height:28}}><Icons.more size={14}/></button>
          </div>
          <div style={{padding:18, display:'flex', flexDirection:'column', alignItems:'center', gap:16}}>
            <div style={{position:'relative'}}>
              <Donut data={departments} size={180} thick={26}/>
              <div style={{position:'absolute', inset:0, display:'grid', placeItems:'center', textAlign:'center'}}>
                <div>
                  <div style={{fontSize:26, fontWeight:800, letterSpacing:'-0.02em', fontFamily:'Inter Tight'}}>342</div>
                  <div style={{fontSize:11, color:'var(--ink-3)'}}>Total</div>
                </div>
              </div>
            </div>
            <div style={{width:'100%', display:'flex', flexDirection:'column', gap:8}}>
              {departments.map((d, i) => (
                <div key={i} className="row" style={{fontSize:12.5}}>
                  <span style={{width:8, height:8, borderRadius:2, background:d.color}}/>
                  <span>{d.name}</span>
                  <span style={{marginLeft:'auto', color:'var(--ink-3)'}} className="tabular">{d.value}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* To-do + Calendar */}
      <div style={{display:'grid', gridTemplateColumns:'1.2fr 1fr', gap:16, marginTop:16}}>
        <div className="card">
          <div className="card-head">
            <div>
              <div className="card-title">À traiter</div>
              <div className="muted" style={{fontSize:12, marginTop:2}}>Approbations & demandes en attente</div>
            </div>
            <div className="row gap-2">
              <button className="chip active">Tout ({todoList.length})</button>
              <button className="chip">Urgent (3)</button>
            </div>
          </div>
          <div>
            {todoList.map((t, i) => {
              const meta = {
                leave:    { tag:'Congé',      tone:'amber',  icon:'leave' },
                expense:  { tag:'Note de frais', tone:'green', icon:'expense' },
                review:   { tag:'Évaluation', tone:'violet', icon:'review' },
                contract: { tag:'Contrat',    tone:'blue',   icon:'contract' },
                loan:     { tag:'Avance',     tone:'orange', icon:'loan' },
                medical:  { tag:'Médical',    tone:'red',    icon:'medical' },
              }[t.type];
              const I = Icons[meta.icon];
              return (
                <div key={i} className="lrow" style={{padding:'14px 22px'}}>
                  <div className={"icon-tile " + meta.tone}><I size={16}/></div>
                  <div style={{flex:1, minWidth:0}}>
                    <div style={{fontSize:13.5, fontWeight:600}}>
                      {t.user} <span className="muted" style={{fontWeight:400}}>· {meta.tag}</span>
                    </div>
                    <div style={{fontSize:12, color:'var(--ink-3)'}}>{t.meta}</div>
                  </div>
                  {t.urgency === 'now' && <span className="badge red">Aujourd'hui</span>}
                  {t.urgency === 'soon' && <span className="badge amber">Cette semaine</span>}
                  {t.urgency === 'later' && <span className="badge gray">Plus tard</span>}
                  <div className="row gap-2" style={{marginLeft:12}}>
                    <button className="btn btn-secondary btn-sm">Détails</button>
                    <button className="btn btn-primary btn-sm"><Icons.check size={14}/></button>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        <div className="card">
          <div className="card-head">
            <div className="card-title">Prochaines échéances</div>
            <div className="row gap-2">
              <button className="icon-btn" style={{width:28, height:28}}><Icons.cal size={14}/></button>
              <button className="icon-btn" style={{width:28, height:28}}><Icons.more size={14}/></button>
            </div>
          </div>
          <div style={{padding:8}}>
            {upcoming.map((u, i) => (
              <div key={i} className="row" style={{padding:'14px', gap:14, alignItems:'flex-start', borderRadius:12,
                background: i===0 ? 'linear-gradient(135deg, #FFF4EB 0%, #FFF8F2 100%)' : 'transparent',
                border: i===0 ? '1px solid var(--orange-200)' : 'none', marginBottom: 4}}>
                <div style={{
                  width:48, padding:'6px', textAlign:'center', borderRadius:10,
                  background: i===0 ? 'var(--grad-orange)' : '#fff',
                  color: i===0 ? '#fff' : 'var(--ink)',
                  border: i===0 ? 'none' : '1px solid var(--line)',
                  flexShrink: 0
                }}>
                  <div style={{fontFamily:'Inter Tight', fontWeight:800, fontSize:18, lineHeight:1}}>{u.d}</div>
                  <div style={{fontSize:9, textTransform:'uppercase', letterSpacing:'0.06em', opacity:.8}}>{u.m}</div>
                </div>
                <div style={{flex:1, minWidth:0}}>
                  <div className="row gap-2">
                    <span className={"badge " + u.tagColor}>{u.tag}</span>
                  </div>
                  <div style={{fontSize:13.5, fontWeight:600, marginTop:6}}>{u.title}</div>
                  <div style={{fontSize:12, color:'var(--ink-3)', marginTop:2}}>{u.sub}</div>
                </div>
                <button className={i===0 ? "btn btn-dark btn-sm" : "btn btn-secondary btn-sm"}>{u.cta}</button>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Quick actions + Recent activity */}
      <div style={{display:'grid', gridTemplateColumns:'1fr 1.4fr', gap:16, marginTop:16}}>
        <div className="card">
          <div className="card-head">
            <div className="card-title">Actions rapides</div>
          </div>
          <div className="grid-2" style={{padding:18, gap:12}}>
            {[
              { icon:'users', label:'Ajouter un employé', tone:'orange' },
              { icon:'payroll', label:'Lancer la paie', tone:'amber' },
              { icon:'leave', label:'Calendrier congés', tone:'violet' },
              { icon:'recruit', label:'Publier une offre', tone:'blue' },
              { icon:'training', label:'Planifier formation', tone:'green' },
              { icon:'declaration', label:'Déclaration sociale', tone:'red' },
            ].map((a, i) => {
              const I = Icons[a.icon];
              return (
                <button key={i} className="clickable" style={{
                  padding:14, borderRadius:12, background:'#fff', border:'1px solid var(--line)',
                  display:'flex', flexDirection:'column', alignItems:'flex-start', gap:10,
                  textAlign:'left', cursor:'pointer', transition:'all .15s'
                }}>
                  <div className={"icon-tile " + a.tone}><I size={18}/></div>
                  <div style={{fontSize:13, fontWeight:600}}>{a.label}</div>
                  <Icons.chevR size={14} style={{marginLeft:'auto', color:'var(--ink-4)'}}/>
                </button>
              );
            })}
          </div>
        </div>

        <div className="card">
          <div className="card-head">
            <div className="card-title">Activité récente</div>
            <button className="btn btn-ghost btn-sm">Voir tout</button>
          </div>
          <div style={{padding:'8px 0'}}>
            {recentActivity.map((a, i) => {
              const I = Icons[a.icon];
              return (
                <div key={i} className="lrow" style={{padding:'12px 22px'}}>
                  <div className={"icon-tile " + a.color} style={{width:32, height:32}}><I size={14}/></div>
                  <div style={{flex:1, fontSize:13}}>
                    <b>{a.who}</b> <span className="muted">{a.what}</span>
                  </div>
                  <div style={{fontSize:11, color:'var(--ink-4)'}}>{a.when}</div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Mini KPI grid */}
      <div className="section-title">Métriques clés</div>
      <div className="grid-4" style={{gap:12}}>
        {[
          { l:'Recrutements ouverts', v:'12', spark:[3,5,4,6,7,8,9,10,12], color:'#3B82F6' },
          { l:'Formations en cours', v:'28', spark:[20,22,21,24,25,26,28,28,28], color:'#10B981' },
          { l:'Évaluations à clôturer', v:'17', spark:[34,30,28,24,22,20,19,18,17], color:'#8B5CF6' },
          { l:'Bulletins du mois', v:'342', spark:[298,310,318,328,336,340,342,342,342], color:'#F59E0B' },
        ].map((m, i) => (
          <div key={i} className="card card-pad" style={{display:'flex', flexDirection:'column', gap:8}}>
            <div className="muted" style={{fontSize:11, fontWeight:600, textTransform:'uppercase', letterSpacing:'0.05em'}}>{m.l}</div>
            <div className="row" style={{alignItems:'baseline'}}>
              <div style={{fontFamily:'Inter Tight', fontSize:26, fontWeight:800, letterSpacing:'-0.02em'}} className="tabular">{m.v}</div>
              <div style={{marginLeft:'auto'}}>
                <Spark data={m.spark} color={m.color} h={36} w={90} fill={m.color}/>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
window.PageDashboard = PageDashboard;
