/* global React */
const { useState: useS_elv } = React;

function EmpPageLeaves() {
  const { Icons, PageHeader, Donut } = window;
  const [tab, setTab] = useS_elv('overview');

  if (tab === 'request') return <LeaveRequestForm onBack={() => setTab('overview')}/>;

  return (
    <div>
      <PageHeader
        uc="Activité"
        title="Mes congés"
        subtitle="Solde, demandes en cours et historique"
        actions={
          <button className="btn btn-primary" onClick={() => setTab('request')}>
            <Icons.plus/> Demander un congé
          </button>
        }
      />

      <div style={{display:'grid', gridTemplateColumns:'1fr 1.6fr', gap:16, marginBottom:20}}>
        {/* Balance overview */}
        <div className="card">
          <div className="card-head"><div className="card-title">Mes soldes</div></div>
          <div style={{padding:20}}>
            <div style={{position:'relative', display:'flex', justifyContent:'center', marginBottom:18}}>
              <Donut data={[
                { value:18.5, color:'#F97316' },
                { value:9.5, color:'var(--bg-soft)' },
              ]} size={180} thick={26}/>
              <div style={{position:'absolute', inset:0, display:'grid', placeItems:'center', textAlign:'center'}}>
                <div>
                  <div style={{fontFamily:'Inter Tight', fontSize:42, fontWeight:800, letterSpacing:'-0.03em', lineHeight:1}} className="tabular">18,5</div>
                  <div style={{fontSize:11, color:'var(--ink-3)', marginTop:4}}>jours restants</div>
                  <div className="badge green" style={{marginTop:6}}><Icons.trendUp size={10}/> +1,5j ce mois</div>
                </div>
              </div>
            </div>
            <div className="col gap-2">
              {[
                ['Annuel acquis', '28 j', 'sur 28 prévus', 'var(--orange-500)'],
                ['Annuel pris', '9,5 j', 'YTD 2026', 'var(--ink-3)'],
                ['Annuel restant', '18,5 j', 'à utiliser avant 31/12', 'var(--orange-600)'],
                ['Maladie utilisé', '2 j', 'janvier 2026', 'var(--red-500)'],
                ['RTT', '6 j', 'cumulés', 'var(--amber-500)'],
                ['CET (épargne)', '4 j', 'cumulés', 'var(--violet-500)'],
              ].map(([l,v,s,c], i) => (
                <div key={i} className="row" style={{padding:'8px 12px', background: i % 2 ? 'transparent' : 'var(--bg-dim)', borderRadius:8}}>
                  <span style={{width:8, height:8, borderRadius:2, background:c}}/>
                  <div style={{marginLeft:8, flex:1}}>
                    <div style={{fontSize:12.5, fontWeight:600}}>{l}</div>
                    <div style={{fontSize:11, color:'var(--ink-3)'}}>{s}</div>
                  </div>
                  <span className="tabular" style={{fontSize:13, fontWeight:700}}>{v}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Calendar + alerts */}
        <div className="col gap-3">
          <div className="card">
            <div className="card-head">
              <div>
                <div className="card-title">Mai 2026</div>
                <div className="muted" style={{fontSize:12, marginTop:2}}>2 absences planifiées dans mon équipe</div>
              </div>
              <div className="row gap-2">
                <button className="icon-btn" style={{width:28, height:28}}><Icons.chevR size={12} style={{transform:'rotate(180deg)'}}/></button>
                <button className="icon-btn" style={{width:28, height:28}}><Icons.chevR size={12}/></button>
              </div>
            </div>
            <div style={{padding:16}}>
              <div style={{display:'grid', gridTemplateColumns:'repeat(7, 1fr)', gap:4, marginBottom:6}}>
                {['L','M','M','J','V','S','D'].map((d,i) => (
                  <div key={i} style={{fontSize:10, color:'var(--ink-3)', textAlign:'center', padding:'4px 0', fontWeight:600}}>{d}</div>
                ))}
              </div>
              <div style={{display:'grid', gridTemplateColumns:'repeat(7, 1fr)', gap:4}}>
                {[null,null,null,null].map((_,i) => <div key={'p'+i}/>)}
                {Array.from({length:31}, (_, idx) => {
                  const d = idx + 1;
                  const isMine = [18,19,20,21,22].includes(d);
                  const isToday = d === 14;
                  const isTeam = [13,14,15].includes(d) && !isMine && !isToday;
                  const isWk = (d + 4) % 7 < 2;
                  return (
                    <div key={d} style={{
                      aspectRatio:'1', display:'grid', placeItems:'center',
                      borderRadius:8,
                      background: isToday ? 'var(--grad-orange)' : isMine ? 'var(--orange-50)' : isTeam ? 'var(--blue-50)' : isWk ? 'var(--bg-soft)' : 'transparent',
                      color: isToday ? '#fff' : isWk ? 'var(--ink-4)' : 'var(--ink-2)',
                      fontSize:13, fontWeight: isToday || isMine ? 700 : 500, position:'relative',
                      border: isMine ? '1px solid var(--orange-200)' : 'none'
                    }}>
                      {d}
                      {isMine && !isToday && <span style={{position:'absolute', bottom:3, width:4, height:4, borderRadius:'50%', background:'var(--orange-500)'}}/>}
                    </div>
                  );
                })}
              </div>
              <div className="row gap-3" style={{marginTop:14, paddingTop:12, borderTop:'1px solid var(--line-soft)', fontSize:11}}>
                <span className="row gap-1"><div style={{width:10, height:10, borderRadius:2, background:'var(--orange-200)'}}/> Mes congés</span>
                <span className="row gap-1"><div style={{width:10, height:10, borderRadius:2, background:'var(--blue-50)'}}/> Équipe absente</span>
                <span className="row gap-1"><div style={{width:10, height:10, borderRadius:2, background:'var(--bg-soft)'}}/> Week-end</span>
              </div>
            </div>
          </div>

          {/* Advice card */}
          <div className="card card-pad" style={{
            background:'linear-gradient(90deg, var(--orange-50) 0%, #FFFAEC 100%)',
            border:'1px solid var(--orange-200)'
          }}>
            <div className="row gap-3">
              <div className="icon-tile orange" style={{width:42, height:42}}><Icons.info size={20}/></div>
              <div style={{flex:1}}>
                <div style={{fontSize:14, fontWeight:700}}>💡 Pensez à planifier vos congés d'été</div>
                <div style={{fontSize:12.5, color:'var(--ink-2)', marginTop:2}}>
                  Il vous reste 18,5 jours à utiliser avant le 31 décembre 2026. Les demandes pour juillet-août se font idéalement avant le 15 juin.
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* My requests history */}
      <div className="card">
        <div className="card-head">
          <div className="card-title">Mes demandes</div>
          <div className="row gap-2">
            <button className="chip orange active">Toutes</button>
            <button className="chip">À venir (1)</button>
            <button className="chip">En attente (1)</button>
            <button className="chip">Historique</button>
          </div>
        </div>
        <table className="t">
          <thead><tr><th>Réf.</th><th>Type</th><th>Période</th><th>Jours</th><th>Motif</th><th>Statut</th><th>Validateur</th><th></th></tr></thead>
          <tbody>
            {[
              { id:'LR-0421', type:'Annuel', from:'18 mai 2026', to:'22 mai 2026', days:5, motif:'Vacances famille', status:'pending', validator:'Marc Foga' },
              { id:'LR-0398', type:'Annuel', from:'14 fév 2026', to:'18 fév 2026', days:4.5, motif:'Repos', status:'approved', validator:'Marc Foga' },
              { id:'LR-0356', type:'Maladie', from:'08 jan 2026', to:'09 jan 2026', days:2, motif:'Grippe (certif. joint)', status:'approved', validator:'Marc Foga' },
              { id:'LR-0312', type:'Annuel', from:'22 déc 2025', to:'02 jan 2026', days:8, motif:'Fêtes de fin d\'année', status:'approved', validator:'Marc Foga' },
              { id:'LR-0287', type:'Annuel', from:'05 août 2025', to:'18 août 2025', days:10, motif:'Vacances été', status:'approved', validator:'Marc Foga' },
              { id:'LR-0254', type:'Mariage', from:'18 jun 2025', to:'21 jun 2025', days:4, motif:'Mariage cousine', status:'approved', validator:'Marc Foga' },
            ].map(r => (
              <tr key={r.id}>
                <td className="mono" style={{fontSize:11, color:'var(--ink-3)'}}>{r.id}</td>
                <td>
                  <span className={"badge " + (r.type === 'Annuel' ? 'orange' : r.type === 'Maladie' ? 'red' : 'violet')}>{r.type}</span>
                </td>
                <td>
                  <div style={{fontSize:12.5}}>{r.from}</div>
                  <div style={{fontSize:11, color:'var(--ink-3)'}}>→ {r.to}</div>
                </td>
                <td className="tabular" style={{fontWeight:700}}>{r.days} j</td>
                <td className="muted" style={{fontSize:12}}>{r.motif}</td>
                <td>
                  {r.status === 'pending' && <span className="badge amber">En attente</span>}
                  {r.status === 'approved' && <span className="badge green">Approuvée</span>}
                  {r.status === 'rejected' && <span className="badge red">Refusée</span>}
                </td>
                <td className="muted" style={{fontSize:12}}>{r.validator}</td>
                <td>
                  <button className="icon-btn" style={{width:28, height:28}}>
                    {r.status === 'pending' ? <Icons.x size={12}/> : <Icons.chevR size={12}/>}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function LeaveRequestForm({ onBack }) {
  const { Icons } = window;
  return (
    <div>
      <div className="row gap-2" style={{marginBottom:14}}>
        <button className="btn btn-ghost btn-sm" onClick={onBack}>
          <Icons.chevR size={14} style={{transform:'rotate(180deg)'}}/> Retour
        </button>
        <span className="muted" style={{fontSize:12}}>Mes congés / Nouvelle demande</span>
      </div>

      <div className="h-display" style={{fontSize:28, marginBottom:6}}>Nouvelle demande de congé</div>
      <div style={{fontSize:13.5, color:'var(--ink-3)', marginBottom:24}}>Renseignez la période et le motif. Votre manager sera notifié.</div>

      <div style={{display:'grid', gridTemplateColumns:'2fr 1fr', gap:20}}>
        <div className="card card-pad-lg">
          <div className="col" style={{gap:18}}>
            <div className="field">
              <label className="label">Type de congé</label>
              <div className="grid-3" style={{gap:10}}>
                {[
                  { id:'annual', label:'Annuel', sub:'18,5 j dispo', tone:'orange', active:true },
                  { id:'sick', label:'Maladie', sub:'Certif. requis', tone:'red' },
                  { id:'family', label:'Familial', sub:'Mariage, naissance...', tone:'violet' },
                  { id:'rtt', label:'RTT', sub:'6 j dispo', tone:'amber' },
                  { id:'unpaid', label:'Sans solde', sub:'Sur demande', tone:'gray' },
                  { id:'special', label:'Spécial', sub:'Conventionnel', tone:'blue' },
                ].map(t => (
                  <button key={t.id} style={{
                    padding:'12px 14px', borderRadius:12,
                    background: t.active ? 'var(--orange-50)' : '#fff',
                    border: t.active ? '2px solid var(--orange-500)' : '1px solid var(--line)',
                    textAlign:'left', cursor:'pointer'
                  }}>
                    <div style={{fontSize:13, fontWeight:700}}>{t.label}</div>
                    <div style={{fontSize:11, color:'var(--ink-3)', marginTop:2}}>{t.sub}</div>
                  </button>
                ))}
              </div>
            </div>

            <div className="grid-2" style={{gap:14}}>
              <div className="field">
                <label className="label">Date de début</label>
                <div className="input row gap-2" style={{cursor:'pointer'}}>
                  <Icons.cal size={14} style={{color:'var(--ink-3)'}}/>
                  <span>Lundi 18 mai 2026</span>
                </div>
              </div>
              <div className="field">
                <label className="label">Date de fin</label>
                <div className="input row gap-2" style={{cursor:'pointer'}}>
                  <Icons.cal size={14} style={{color:'var(--ink-3)'}}/>
                  <span>Vendredi 22 mai 2026</span>
                </div>
              </div>
            </div>

            <div className="row gap-2">
              <button className="chip">Demi-journée</button>
              <button className="chip orange active">Journée entière</button>
              <button className="chip">Semaine</button>
            </div>

            <div className="field">
              <label className="label">Motif (optionnel)</label>
              <textarea className="textarea" placeholder="Vacances en famille à Limbé...">Vacances en famille à Limbé.</textarea>
            </div>

            <div className="field">
              <label className="label">Personne de remplacement (optionnel)</label>
              <select className="select">
                <option>Olivier Manga · DevOps Engineer</option>
                <option>Sarah Nguemo · UX Designer</option>
              </select>
            </div>

            <div className="field">
              <label className="label">Justificatif (si requis)</label>
              <div style={{padding:'24px', border:'2px dashed var(--line)', borderRadius:12, textAlign:'center', background:'var(--bg-dim)'}}>
                <Icons.upload size={28} style={{color:'var(--ink-4)', margin:'0 auto 8px', display:'block'}}/>
                <div style={{fontSize:13, fontWeight:600, marginBottom:4}}>Cliquer pour téléverser</div>
                <div style={{fontSize:11, color:'var(--ink-3)'}}>PDF, JPG ou PNG · max 5 MB</div>
              </div>
            </div>
          </div>
        </div>

        {/* Right panel: preview */}
        <div className="col gap-3">
          <div className="card card-pad" style={{
            background:'linear-gradient(135deg, var(--orange-50) 0%, #fff 100%)',
            border:'1px solid var(--orange-200)'
          }}>
            <div style={{fontSize:11, textTransform:'uppercase', letterSpacing:'0.05em', fontWeight:600, color:'var(--ink-3)'}}>Récapitulatif</div>
            <div style={{fontFamily:'Inter Tight', fontSize:34, fontWeight:800, letterSpacing:'-0.025em', marginTop:4, color:'var(--orange-700)'}} className="tabular">5 jours</div>
            <div style={{fontSize:12.5, color:'var(--ink-2)', marginTop:4}}>18 → 22 mai 2026 · Annuel</div>

            <div className="divider"/>

            <div className="row" style={{fontSize:12.5, padding:'4px 0'}}>
              <span style={{color:'var(--ink-3)'}}>Solde avant</span>
              <span className="tabular" style={{marginLeft:'auto', fontWeight:600}}>18,5 j</span>
            </div>
            <div className="row" style={{fontSize:12.5, padding:'4px 0'}}>
              <span style={{color:'var(--ink-3)'}}>Demande</span>
              <span className="tabular" style={{marginLeft:'auto', fontWeight:600, color:'var(--orange-600)'}}>– 5 j</span>
            </div>
            <div className="row" style={{fontSize:13, padding:'8px 0', borderTop:'1px solid var(--orange-200)', marginTop:4}}>
              <span style={{fontWeight:700}}>Solde après</span>
              <span className="tabular" style={{marginLeft:'auto', fontWeight:800, fontFamily:'Inter Tight', fontSize:18}}>13,5 j</span>
            </div>
          </div>

          <div className="card card-pad">
            <div className="card-title" style={{fontSize:14, marginBottom:12}}>Workflow d'approbation</div>
            <div className="col gap-2">
              {[
                { who:'Vous', sub:'Soumission', tone:'orange', done:true },
                { who:'Marc Foga', sub:'Manager direct', tone:'blue', done:false, active:true },
                { who:'DRH', sub:'Validation finale', tone:'gray', done:false },
              ].map((s, i) => (
                <div key={i} className="row gap-3" style={{padding:'8px 0', position:'relative'}}>
                  <div style={{
                    width:28, height:28, borderRadius:'50%',
                    background: s.done ? 'var(--green-500)' : s.active ? 'var(--orange-500)' : '#fff',
                    border: s.done || s.active ? 'none' : '1px solid var(--line)',
                    display:'grid', placeItems:'center', color:'#fff', fontWeight:700, fontSize:11
                  }}>
                    {s.done ? <Icons.check size={12}/> : i+1}
                  </div>
                  <div>
                    <div style={{fontSize:13, fontWeight:600}}>{s.who}</div>
                    <div style={{fontSize:11, color:'var(--ink-3)'}}>{s.sub}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="row gap-2">
            <button className="btn btn-secondary" style={{flex:1, justifyContent:'center'}} onClick={onBack}>Annuler</button>
            <button className="btn btn-primary" style={{flex:1, justifyContent:'center'}}>
              <Icons.send size={14}/> Soumettre
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

window.EmpPageLeaves = EmpPageLeaves;
