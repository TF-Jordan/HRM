/* global React */
const { Icons: I_dd, Avatar: A_dd, PageHeader: PH_dd, Donut: Do_dd, Line: Li_dd } = window;

function DocPageDashboard({ goto }) {
  return (
    <div>
      <PH_dd
        uc="Médecine du travail · 14 mai 2026"
        title="Bonjour Dr. Nkoa"
        subtitle="6 consultations aujourd'hui · 7 échéances à surveiller cette semaine"
        actions={
          <>
            <button className="btn btn-secondary"><I_dd.download/> Registre médical</button>
            <button className="btn btn-primary"><I_dd.plus/> Programmer une visite</button>
          </>
        }
      />

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'Consultations · jour', v:'6', sub:'2 réalisées · 4 à venir', tone:'orange', icon:'medical' },
          { l:'À planifier (90j)', v:'18', sub:'visites annuelles à programmer', tone:'amber', icon:'cal' },
          { l:'Aptitudes restrictives', v:'14', sub:'à suivre · 4 contrôles', tone:'red', icon:'alert' },
          { l:'Certificats à valider', v:'3', sub:'maladie · 1 maternité', tone:'blue', icon:'doc' },
        ].map((k,i) => {
          const Ic = I_dd[k.icon];
          return (
            <div key={i} className={"kpi " + k.tone}>
              <div className="kpi-icon"><Ic size={18}/></div>
              <div className="kpi-label">{k.l}</div>
              <div className="kpi-value tabular">{k.v}</div>
              <div className="kpi-foot">{k.sub}</div>
            </div>
          );
        })}
      </div>

      <div style={{display:'grid', gridTemplateColumns:'1.4fr 1fr', gap:16, marginBottom:20}}>
        <div className="card">
          <div className="card-head">
            <div>
              <div className="card-title">Planning d'aujourd'hui</div>
              <div className="muted" style={{fontSize:12, marginTop:2}}>Mardi 14 mai 2026 · CMA Yaoundé · cabinet 2</div>
            </div>
            <button className="btn btn-ghost btn-sm">Voir la semaine</button>
          </div>
          <div>
            {[
              { time:'08:30', dur:'30 min', n:'Léa Ondoa', emp:'EMP-0234', c:'teal', type:'Embauche', tone:'green', status:'done', notes:'CDI · stage→embauche · 1ère visite' },
              { time:'09:15', dur:'30 min', n:'Pierre Kouam', emp:'EMP-0177', c:'amber', type:'Période d\'essai', tone:'blue', status:'done', notes:'Comptable · 4 mois ancienneté' },
              { time:'10:00', dur:'45 min', n:'Marc Foga', emp:'EMP-0021', c:'green', type:'Annuelle', tone:'orange', status:'in-progress', notes:'En consultation · cadre supérieur' },
              { time:'11:00', dur:'30 min', n:'Estelle Bilong', emp:'EMP-0119', c:'amber', type:'Pré-reprise', tone:'violet', status:'upcoming', notes:'Retour de congé maternité' },
              { time:'14:00', dur:'30 min', n:'Aminata Diallo', emp:'EMP-0142', c:'orange', type:'Annuelle', tone:'orange', status:'upcoming', notes:'Lead Mobile · pas d\'antécédent' },
              { time:'15:30', dur:'30 min', n:'Karine Djoumessi', emp:'EMP-0254', c:'blue', type:'Reprise', tone:'red', status:'upcoming', notes:'Arrêt 8j · grippe' },
            ].map((v, i) => (
              <div key={i} className="lrow" style={{padding:'14px 22px', gap:14,
                background: v.status === 'in-progress' ? 'linear-gradient(90deg, rgba(242,107,15,0.06) 0%, transparent 60%)' : 'transparent',
                borderLeft: v.status === 'in-progress' ? '3px solid var(--orange-500)' : '3px solid transparent'}}>
                <div style={{width:64, textAlign:'center', flexShrink:0}}>
                  <div className="mono" style={{fontSize:14, fontWeight:700, color: v.status === 'done' ? 'var(--ink-4)' : 'var(--ink)'}}>{v.time}</div>
                  <div style={{fontSize:10, color:'var(--ink-4)'}}>{v.dur}</div>
                </div>
                <A_dd name={v.n} color={v.c}/>
                <div style={{flex:1, minWidth:0}}>
                  <div className="row gap-2">
                    <span style={{fontSize:13.5, fontWeight:600}}>{v.n}</span>
                    <span className="mono" style={{fontSize:10, color:'var(--ink-4)'}}>{v.emp}</span>
                  </div>
                  <div style={{fontSize:11.5, color:'var(--ink-3)', marginTop:2}}>{v.notes}</div>
                </div>
                <span className={"badge " + v.tone}>{v.type}</span>
                {v.status === 'done' && <span className="badge green">✓ Terminée</span>}
                {v.status === 'in-progress' && <span className="badge orange pulse">● En cours</span>}
                {v.status === 'upcoming' && (
                  <button className="btn btn-secondary btn-sm">Ouvrir dossier</button>
                )}
              </div>
            ))}
          </div>
        </div>

        <div className="col gap-3">
          <div className="card">
            <div className="card-head"><div className="card-title">Statut d'aptitude · population</div></div>
            <div style={{padding:18, display:'flex', flexDirection:'column', alignItems:'center'}}>
              <div style={{position:'relative'}}>
                <Do_dd data={[
                  { value:324, color:'#10B981' },
                  { value:14, color:'#F59E0B' },
                  { value:4, color:'#EF4444' },
                ]} size={170} thick={24}/>
                <div style={{position:'absolute', inset:0, display:'grid', placeItems:'center', textAlign:'center'}}>
                  <div>
                    <div style={{fontFamily:'Inter Tight', fontSize:30, fontWeight:800}}>342</div>
                    <div style={{fontSize:11, color:'var(--ink-3)'}}>salariés suivis</div>
                  </div>
                </div>
              </div>
              <div style={{width:'100%', marginTop:18}}>
                {[
                  ['#10B981','Aptes sans réserve','324','94,7%'],
                  ['#F59E0B','Aptes avec restrictions','14','4,1%'],
                  ['#EF4444','Inaptes temporaires','4','1,2%'],
                ].map((x,i) => (
                  <div key={i} className="row" style={{padding:'5px 0', fontSize:12}}>
                    <span style={{width:10, height:10, borderRadius:3, background:x[0]}}/>
                    <span style={{flex:1, marginLeft:8}}>{x[1]}</span>
                    <span className="tabular" style={{fontWeight:700}}>{x[2]}</span>
                    <span className="muted tabular" style={{marginLeft:8, width:40, textAlign:'right'}}>{x[3]}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="card card-pad" style={{background:'linear-gradient(90deg, var(--red-50) 0%, #FFF4F4 100%)', border:'1px solid #FCA5A5'}}>
            <div className="row gap-3">
              <div className="icon-tile red" style={{width:42, height:42}}><I_dd.alert size={20}/></div>
              <div style={{flex:1}}>
                <div style={{fontSize:13.5, fontWeight:700}}>7 visites annuelles expirent &lt; 30j</div>
                <div style={{fontSize:11.5, color:'var(--ink-3)', marginTop:2}}>Obligation légale · à programmer</div>
              </div>
              <button className="btn btn-dark btn-sm">Voir</button>
            </div>
          </div>

          <div className="card card-pad" style={{background:'linear-gradient(90deg, var(--amber-50) 0%, #FFFAEC 100%)', border:'1px solid var(--amber-500)'}}>
            <div className="row gap-3">
              <div className="icon-tile amber" style={{width:42, height:42}}><I_dd.doc size={20}/></div>
              <div style={{flex:1}}>
                <div style={{fontSize:13.5, fontWeight:700}}>3 certificats en attente de validation</div>
                <div style={{fontSize:11.5, color:'var(--ink-3)', marginTop:2}}>Arrêts maladie déposés par les salariés</div>
              </div>
              <button className="btn btn-dark btn-sm">Valider</button>
            </div>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-head">
          <div className="card-title">Évolution des visites · 12 mois</div>
          <div className="row gap-2">
            <button className="chip orange active">Toutes</button>
            <button className="chip">Annuelles</button>
            <button className="chip">Reprises</button>
          </div>
        </div>
        <div style={{padding:'18px 22px'}}>
          <Li_dd data={[28, 32, 30, 34, 29, 35, 31, 38, 42, 36, 33, 42]}
            labels={['Jui','Juil','Aoû','Sep','Oct','Nov','Déc','Jan','Fév','Mar','Avr','Mai']}
            color="#F26B0F" fill="#F26B0F" h={200}/>
        </div>
      </div>
    </div>
  );
}
window.DocPageDashboard = DocPageDashboard;
