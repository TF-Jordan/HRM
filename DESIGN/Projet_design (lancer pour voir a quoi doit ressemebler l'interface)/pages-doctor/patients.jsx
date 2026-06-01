/* global React */
const { useState: useS_dpt } = React;
const { Icons: I_pt, Avatar: A_pt, PageHeader: PH_pt } = window;

function DocPagePatients({ goto, setPatient }) {
  const patients = [
    { id:'EMP-0142', name:'Aminata Diallo', age:38, sex:'F', role:'Lead Mobile Eng.', dept:'Engineering', last:'14 avr 2026', next:'14 avr 2027', status:'fit', visits:4, c:'orange' },
    { id:'EMP-0021', name:'Marc Foga', age:42, sex:'M', role:'Dir. Technique', dept:'Direction', last:'02 fév 2026', next:'02 fév 2027', status:'fit', visits:6, c:'green' },
    { id:'EMP-0098', name:'Joseph Mbarga', age:35, sex:'M', role:'Business Developer', dept:'Commercial', last:'10 mar 2026', next:'10 mar 2027', status:'restricted', visits:3, c:'blue', restriction:'Limiter port charge >10kg' },
    { id:'EMP-0177', name:'Pierre Kouam', age:29, sex:'M', role:'Comptable', dept:'Finance & RH', last:'15 jan 2026', next:'15 jan 2027', status:'fit', visits:1, c:'amber' },
    { id:'EMP-0119', name:'Estelle Bilong', age:41, sex:'F', role:'Chargée de paie', dept:'Finance & RH', last:'18 jan 2026', next:'18 jul 2026', status:'maternity', visits:8, c:'amber' },
    { id:'EMP-0254', name:'Karine Djoumessi', age:33, sex:'F', role:'Field Operations', dept:'Opérations', last:'—', next:'15 mai 2026', status:'pending', visits:2, c:'blue', alert:'Visite reprise à programmer' },
    { id:'EMP-0067', name:'Olivier Manga', age:36, sex:'M', role:'DevOps Engineer', dept:'Engineering', last:'22 sep 2025', next:'22 sep 2026', status:'restricted', visits:5, c:'green', restriction:'Travail écran limité 6h/j' },
    { id:'EMP-0188', name:'Hervé Tankeu', age:39, sex:'M', role:'Sales Manager', dept:'Commercial', last:'05 nov 2025', next:'05 nov 2026', status:'fit', visits:4, c:'orange' },
    { id:'EMP-0234', name:'Léa Ondoa', age:24, sex:'F', role:'Designer UI · stage', dept:'Engineering', last:'14 mai 2026', next:'14 mai 2027', status:'fit', visits:1, c:'teal' },
  ];

  return (
    <div>
      <PH_pt
        uc="Dossiers médicaux"
        title="Patients"
        subtitle="342 salariés suivis · 4 sites · accès soumis au secret médical"
        actions={
          <>
            <button className="btn btn-secondary"><I_pt.download/> Export anonymisé</button>
            <button className="btn btn-primary"><I_pt.plus/> Ouvrir un dossier</button>
          </>
        }
      />

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'Dossiers actifs', v:'342', sub:'+12 sur 12 mois', tone:'orange' },
          { l:'Aptes', v:'324', sub:'94,7% de l\'effectif', tone:'green' },
          { l:'Avec restrictions', v:'14', sub:'à suivre régulièrement', tone:'amber' },
          { l:'En arrêt en cours', v:'4', sub:'3 maladie · 1 maternité', tone:'red' },
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

      <div className="card card-pad" style={{marginBottom:16}}>
        <div className="row gap-3" style={{flexWrap:'wrap'}}>
          <div className="row gap-2" style={{flex:1, minWidth:280, padding:'8px 14px', background:'#fff', border:'1px solid var(--line)', borderRadius:12}}>
            <I_pt.search size={15} style={{color:'var(--ink-3)'}}/>
            <input style={{border:'none', outline:'none', flex:1, fontSize:13.5}} placeholder="Nom, ID employé, département…"/>
          </div>
          <button className="chip orange active">Tous (342)</button>
          <button className="chip">Aptes (324)</button>
          <button className="chip">Restrictions (14)</button>
          <button className="chip">En arrêt (4)</button>
          <button className="chip">À programmer (18)</button>
        </div>
      </div>

      <div className="card">
        <table className="t">
          <thead>
            <tr>
              <th>Patient</th>
              <th>Âge · Sexe</th>
              <th>Poste · département</th>
              <th>Dernière visite</th>
              <th>Prochaine échéance</th>
              <th>Aptitude</th>
              <th>Dossier</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {patients.map(p => (
              <tr key={p.id} onClick={() => { setPatient && setPatient(p); goto && goto('patient-file'); }} style={{cursor:'pointer'}}>
                <td>
                  <div className="row gap-2">
                    <A_pt name={p.name} color={p.c} size="sm"/>
                    <div>
                      <div style={{fontSize:13, fontWeight:600}}>{p.name}</div>
                      <div className="mono" style={{fontSize:10.5, color:'var(--ink-4)'}}>{p.id}</div>
                    </div>
                  </div>
                </td>
                <td className="tabular">{p.age} ans · {p.sex}</td>
                <td>
                  <div style={{fontSize:13}}>{p.role}</div>
                  <div style={{fontSize:11, color:'var(--ink-3)'}}>{p.dept}</div>
                </td>
                <td className="muted">{p.last}</td>
                <td>
                  <div style={{fontSize:12.5}}>{p.next}</div>
                  {p.alert && <div style={{fontSize:11, color:'var(--red-600)', fontWeight:600}}>⚠ {p.alert}</div>}
                </td>
                <td>
                  {p.status === 'fit' && <span className="badge green">Apte</span>}
                  {p.status === 'restricted' && <span className="badge amber">Restrictions</span>}
                  {p.status === 'pending' && <span className="badge red">À voir</span>}
                  {p.status === 'maternity' && <span className="badge violet">Maternité</span>}
                  {p.restriction && <div style={{fontSize:10.5, color:'var(--ink-3)', marginTop:3, fontStyle:'italic'}}>{p.restriction}</div>}
                </td>
                <td className="tabular muted">{p.visits} visites</td>
                <td><button className="icon-btn" style={{width:28, height:28}}><I_pt.chevR size={12}/></button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
window.DocPagePatients = DocPagePatients;

function DocPagePatientFile({ patient, onBack }) {
  const [tab, setTab] = useS_dpt('history');
  const p = patient || { id:'EMP-0142', name:'Aminata Diallo', age:38, sex:'F', role:'Lead Mobile Eng.', dept:'Engineering', last:'14 avr 2026', next:'14 avr 2027', status:'fit', c:'orange' };

  return (
    <div>
      <div className="row gap-2" style={{marginBottom:14}}>
        <button className="btn btn-ghost btn-sm" onClick={onBack}>
          <I_pt.chevR size={14} style={{transform:'rotate(180deg)'}}/> Retour à la liste
        </button>
        <span className="muted" style={{fontSize:12}}>Patients / {p.name}</span>
        <span className="badge red" style={{marginLeft:'auto'}}>🔒 Secret médical</span>
      </div>

      <div className="card card-pad-lg" style={{
        background:'linear-gradient(135deg, #FFFAF2 0%, #fff 60%)',
        border:'1px solid var(--orange-200)', marginBottom:20
      }}>
        <div className="row gap-4" style={{alignItems:'flex-start'}}>
          <A_pt name={p.name} color={p.c} size="xl"/>
          <div style={{flex:1}}>
            <div className="row gap-2" style={{marginBottom:4}}>
              <span className="mono" style={{fontSize:11, color:'var(--orange-700)', background:'var(--orange-50)', padding:'2px 8px', borderRadius:999}}>{p.id}</span>
              <span className="badge green">Apte</span>
              <span className="tag">Dernière visite · 14 avr 2026</span>
            </div>
            <div className="h-display" style={{fontSize:30}}>{p.name}</div>
            <div style={{fontSize:14, color:'var(--ink-2)', marginTop:4}}>{p.role} · {p.dept}</div>
            <div className="row gap-4" style={{marginTop:14, flexWrap:'wrap', fontSize:12.5, color:'var(--ink-2)'}}>
              <span>📅 Née le 12 mars 1988 · 38 ans</span>
              <span>♀ Sexe féminin</span>
              <span>📞 +237 6 78 12 34 56</span>
              <span>🩸 Groupe O+</span>
              <span>👨‍👩‍👧 Mariée · 2 enfants</span>
            </div>
          </div>
          <div className="col gap-2">
            <button className="btn btn-primary"><I_pt.plus size={14}/> Nouvelle visite</button>
            <button className="btn btn-secondary"><I_pt.download size={14}/> Exporter le dossier</button>
          </div>
        </div>
      </div>

      <div className="row gap-2" style={{marginBottom:20, borderBottom:'1px solid var(--line-soft)'}}>
        {[
          { id:'history', label:'Historique des visites' },
          { id:'antecedents', label:'Antécédents médicaux' },
          { id:'restrictions', label:'Aptitudes & restrictions' },
          { id:'certificates', label:'Certificats' },
          { id:'exposures', label:'Risques professionnels' },
          { id:'documents', label:'Documents' },
        ].map(t => (
          <button key={t.id} onClick={() => setTab(t.id)} style={{
            padding:'10px 14px',
            borderBottom: tab === t.id ? '2px solid var(--orange-500)' : '2px solid transparent',
            fontSize:13.5, fontWeight: tab === t.id ? 700 : 500,
            color: tab === t.id ? 'var(--ink)' : 'var(--ink-3)'
          }}>{t.label}</button>
        ))}
      </div>

      {tab === 'history' && (
        <div style={{display:'grid', gridTemplateColumns:'2fr 1fr', gap:16}}>
          <div className="card">
            <div className="card-head">
              <div className="card-title">Chronologie des visites</div>
              <button className="btn btn-primary btn-sm"><I_pt.plus size={12}/> Nouvelle entrée</button>
            </div>
            <div style={{padding:'18px 26px'}}>
              {[
                { d:'14 avr 2026', type:'Annuelle', tone:'orange', result:'Apte', notes:'Examen complet RAS · TA 12/8 · poids stable. À surveiller la fatigue oculaire liée au temps écran.', medecin:'Dr. M. Nkoa' },
                { d:'14 mai 2025', type:'Annuelle', tone:'orange', result:'Apte', notes:'État général satisfaisant. Vaccin antitétanique mis à jour.', medecin:'Dr. M. Nkoa' },
                { d:'08 jan 2025', type:'Reprise', tone:'red', result:'Apte', notes:'Reprise après arrêt grippal de 4 jours. Aucune séquelle.', medecin:'Dr. P. Owono' },
                { d:'12 avr 2024', type:'Annuelle', tone:'orange', result:'Apte', notes:'RAS · bilan lipidique normal.', medecin:'Dr. M. Nkoa' },
                { d:'14 fév 2022', type:'Embauche', tone:'green', result:'Apte', notes:'Aptitude au poste de Lead Mobile Engineer. Vue corrigée par lentilles.', medecin:'Dr. M. Nkoa' },
              ].map((v, i, arr) => (
                <div key={i} className="row" style={{gap:14, paddingBottom:18, alignItems:'flex-start', position:'relative'}}>
                  {i < arr.length - 1 && <div style={{position:'absolute', left:18, top:36, bottom:0, width:2, background:'var(--line)'}}/>}
                  <div className={"icon-tile " + v.tone} style={{position:'relative', zIndex:1}}><I_pt.medical size={14}/></div>
                  <div style={{flex:1}}>
                    <div className="row gap-2">
                      <span style={{fontSize:13.5, fontWeight:700}}>{v.d}</span>
                      <span className={"badge " + v.tone}>{v.type}</span>
                      <span className="badge green">{v.result}</span>
                    </div>
                    <div style={{fontSize:12.5, color:'var(--ink-2)', marginTop:6, lineHeight:1.5}}>{v.notes}</div>
                    <div style={{fontSize:11, color:'var(--ink-4)', marginTop:4}}>Examen par {v.medecin}</div>
                  </div>
                  <button className="btn btn-ghost btn-sm">Voir <I_pt.chevR size={11}/></button>
                </div>
              ))}
            </div>
          </div>

          <div className="col gap-3">
            <div className="card card-pad">
              <div className="card-title" style={{fontSize:14, marginBottom:12}}>Constantes (dernière visite)</div>
              {[
                { l:'Tension artérielle', v:'12/8', n:true },
                { l:'Fréquence cardiaque', v:'72 bpm', n:true },
                { l:'Poids', v:'62 kg', n:true },
                { l:'Taille', v:'168 cm', n:true },
                { l:'IMC', v:'21,9', n:true },
                { l:'Acuité visuelle', v:'8/10 (lentilles)', n:false },
                { l:'Audition', v:'Normale', n:true },
              ].map((c, i) => (
                <div key={i} className="row" style={{padding:'5px 0', fontSize:12.5, borderBottom: i<6 ? '1px solid var(--line-soft)' : 'none'}}>
                  <span>{c.l}</span>
                  <span className="tabular" style={{marginLeft:'auto', fontWeight:600}}>{c.v}</span>
                  <span className={"sd " + (c.n ? 'green' : 'amber')} style={{marginLeft:8}}/>
                </div>
              ))}
            </div>
            <div className="card card-pad">
              <div className="card-title" style={{fontSize:14, marginBottom:10}}>Vaccinations</div>
              {[
                ['Tétanos','14 avr 2025','À jour'],
                ['Hépatite B','03 juin 2022','À jour'],
                ['Fièvre jaune','12 sep 2019','À renouveler'],
                ['COVID-19','22 jan 2024','À jour'],
              ].map((v, i) => (
                <div key={i} className="row" style={{padding:'5px 0', fontSize:12, borderBottom: i<3 ? '1px solid var(--line-soft)' : 'none'}}>
                  <span style={{flex:1}}>{v[0]}</span>
                  <span className="muted" style={{marginRight:8}}>{v[1]}</span>
                  <span className={"badge " + (v[2] === 'À jour' ? 'green' : 'amber')} style={{fontSize:10}}>{v[2]}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {tab === 'antecedents' && (
        <div className="grid-2">
          <div className="card card-pad-lg">
            <div className="card-title" style={{fontSize:14, marginBottom:14}}>Antécédents personnels</div>
            {[
              { l:'Allergies', v:'Pénicilline (réaction cutanée modérée)', tone:'red' },
              { l:'Maladies chroniques', v:'Aucune', tone:'green' },
              { l:'Interventions chirurgicales', v:'Appendicectomie (2012)', tone:'gray' },
              { l:'Traitements en cours', v:'Aucun', tone:'green' },
              { l:'Tabac / alcool', v:'Non fumeuse · alcool occasionnel', tone:'green' },
              { l:'Activité physique', v:'2 séances de sport / semaine', tone:'green' },
            ].map((a, i) => (
              <div key={i} className="row" style={{padding:'10px 0', borderBottom: i<5 ? '1px solid var(--line-soft)' : 'none', gap:12}}>
                <span className={"sd " + a.tone}/>
                <div style={{flex:1}}>
                  <div style={{fontSize:11, color:'var(--ink-3)', textTransform:'uppercase', letterSpacing:'0.05em', fontWeight:600}}>{a.l}</div>
                  <div style={{fontSize:13, fontWeight:500, marginTop:2}}>{a.v}</div>
                </div>
              </div>
            ))}
          </div>

          <div className="card card-pad-lg">
            <div className="card-title" style={{fontSize:14, marginBottom:14}}>Antécédents familiaux</div>
            {[
              ['Hypertension artérielle', 'Père (62 ans) · sous traitement'],
              ['Diabète type 2', 'Grand-mère maternelle'],
              ['Cancer', 'Aucun antécédent connu'],
              ['Maladies cardiovasculaires', 'Aucun antécédent connu'],
            ].map((a, i) => (
              <div key={i} style={{padding:'10px 0', borderBottom: i<3 ? '1px solid var(--line-soft)' : 'none'}}>
                <div style={{fontSize:13, fontWeight:600}}>{a[0]}</div>
                <div style={{fontSize:12, color:'var(--ink-3)', marginTop:2}}>{a[1]}</div>
              </div>
            ))}
          </div>
        </div>
      )}

      {tab === 'restrictions' && (
        <div className="card card-pad-lg" style={{textAlign:'center', padding:50, color:'var(--ink-3)'}}>
          Aucune restriction d'aptitude active pour ce patient.
        </div>
      )}

      {tab === 'certificates' && (
        <div className="card">
          <table className="t">
            <thead><tr><th>Réf.</th><th>Type</th><th>Du</th><th>Au</th><th>Durée</th><th>Émis par</th><th>Statut</th></tr></thead>
            <tbody>
              {[
                { id:'MC-2025-0089', t:'Maladie', f:'05 jan 2025', to:'08 jan 2025', d:4, emis:'Dr. P. Owono', s:'Validé' },
                { id:'MC-2024-0124', t:'Maladie', f:'18 jun 2024', to:'19 jun 2024', d:2, emis:'Dr. M. Nkoa', s:'Validé' },
                { id:'MC-2023-0067', t:'Maladie', f:'22 nov 2023', to:'27 nov 2023', d:6, emis:'Dr. Tcheumegne', s:'Validé' },
              ].map(r => (
                <tr key={r.id}>
                  <td className="mono" style={{fontSize:11, color:'var(--ink-3)'}}>{r.id}</td>
                  <td><span className="badge red">{r.t}</span></td>
                  <td className="muted">{r.f}</td>
                  <td className="muted">{r.to}</td>
                  <td className="tabular">{r.d} j</td>
                  <td className="muted">{r.emis}</td>
                  <td><span className="badge green">{r.s}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {tab === 'exposures' && (
        <div className="card card-pad-lg">
          <div className="card-title" style={{fontSize:14, marginBottom:14}}>Évaluation des risques au poste</div>
          {[
            { risk:'Écran de visualisation', level:3, max:5, sub:'Travail prolongé sur écran (>6h/j)', mesure:'Pauses régulières · examen ophtalmo annuel' },
            { risk:'Postures statiques', level:2, max:5, sub:'Travail assis prolongé', mesure:'Ergonomie poste validée · siège conforme' },
            { risk:'Stress / charge mentale', level:3, max:5, sub:'Lead technique · responsabilités', mesure:'Suivi psychologique disponible · cellule d\'écoute' },
            { risk:'Bruit', level:1, max:5, sub:'Open space modéré', mesure:'Pas d\'EPI obligatoire' },
            { risk:'Manutention', level:0, max:5, sub:'Pas de port de charges', mesure:'—' },
            { risk:'Risques routiers', level:2, max:5, sub:'Déplacements occasionnels missions', mesure:'Permis valide · véhicule professionnel' },
          ].map((r, i) => (
            <div key={i} style={{padding:'14px 0', borderBottom: i<5 ? '1px solid var(--line-soft)' : 'none'}}>
              <div className="row" style={{marginBottom:6}}>
                <span style={{fontSize:13.5, fontWeight:600}}>{r.risk}</span>
                <span style={{marginLeft:'auto'}} className="row gap-1">
                  {[...Array(r.max)].map((_,j) => (
                    <span key={j} style={{
                      width:6, height:14, borderRadius:2,
                      background: j < r.level ? (r.level >= 4 ? 'var(--red-500)' : r.level >= 3 ? 'var(--orange-500)' : 'var(--amber-500)') : 'var(--bg-soft)'
                    }}/>
                  ))}
                  <span className="tabular" style={{fontSize:11, fontWeight:700, marginLeft:6}}>{r.level}/{r.max}</span>
                </span>
              </div>
              <div style={{fontSize:12, color:'var(--ink-3)'}}>{r.sub}</div>
              <div style={{fontSize:12, color:'var(--ink-2)', marginTop:4}}>🛡 <b>Mesures</b> : {r.mesure}</div>
            </div>
          ))}
        </div>
      )}

      {tab === 'documents' && (
        <div className="card card-pad-lg" style={{textAlign:'center', padding:50, color:'var(--ink-3)'}}>
          Documents médicaux (ordonnances, scans, comptes rendus d'examens) — soumis au secret médical.
        </div>
      )}
    </div>
  );
}
window.DocPagePatientFile = DocPagePatientFile;
