/* global React */
const { useState: useS_eprof } = React;

function EmpPageProfile() {
  const { Icons, Avatar, PageHeader, EMP_ME } = window;
  const [tab, setTab] = useS_eprof('overview');

  return (
    <div>
      {/* Profile hero */}
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
            <Avatar name={EMP_ME.name} size="xl" color={EMP_ME.avatar}/>
            <button className="icon-btn" style={{
              position:'absolute', bottom:-6, right:-6, width:30, height:30, borderRadius:'50%'
            }}>
              <Icons.edit size={12}/>
            </button>
          </div>
          <div style={{flex:1}}>
            <div className="row gap-2" style={{marginBottom:6}}>
              <span className="mono" style={{fontSize:11, color:'var(--orange-700)', background:'var(--orange-50)', padding:'2px 8px', borderRadius:999}}>{EMP_ME.id}</span>
              <span className="badge green">CDI · Actif</span>
            </div>
            <div className="h-display" style={{fontSize:32}}>{EMP_ME.name}</div>
            <div style={{fontSize:14, color:'var(--ink-2)', marginTop:4}}>{EMP_ME.role} · {EMP_ME.dept}</div>
            <div className="row gap-4" style={{marginTop:16, flexWrap:'wrap'}}>
              <span className="row gap-2" style={{fontSize:13, color:'var(--ink-2)'}}><Icons.cal size={14} style={{color:'var(--ink-4)'}}/> Chez RT-Comops depuis le {EMP_ME.startDate}</span>
              <span className="row gap-2" style={{fontSize:13, color:'var(--ink-2)'}}><Icons.briefcase size={14} style={{color:'var(--ink-4)'}}/> {EMP_ME.site}</span>
              <span className="row gap-2" style={{fontSize:13, color:'var(--ink-2)'}}><Icons.users size={14} style={{color:'var(--ink-4)'}}/> Manage 4 personnes</span>
            </div>
          </div>
          <div className="col gap-2">
            <button className="btn btn-primary"><Icons.edit size={14}/> Modifier mon profil</button>
            <button className="btn btn-secondary"><Icons.print size={14}/> Imprimer ma fiche</button>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="row gap-2" style={{marginBottom:20, borderBottom:'1px solid var(--line-soft)'}}>
        {[
          { id:'overview', label:'Informations personnelles' },
          { id:'contact', label:'Contact & adresse' },
          { id:'banking', label:'Banque & paiement' },
          { id:'family', label:'Famille & dépendants' },
          { id:'emergency', label:'Contact d\'urgence' },
        ].map(t => (
          <button key={t.id} onClick={() => setTab(t.id)} style={{
            padding:'10px 14px',
            borderBottom: tab === t.id ? '2px solid var(--orange-500)' : '2px solid transparent',
            fontSize:13.5, fontWeight: tab === t.id ? 700 : 500,
            color: tab === t.id ? 'var(--ink)' : 'var(--ink-3)'
          }}>{t.label}</button>
        ))}
      </div>

      {/* Content */}
      <div style={{display:'grid', gridTemplateColumns:'2fr 1fr', gap:16}}>
        <div className="card">
          <div className="card-head">
            <div className="card-title">
              {tab === 'overview' && 'Informations personnelles'}
              {tab === 'contact' && 'Contact & adresse'}
              {tab === 'banking' && 'Banque & paiement'}
              {tab === 'family' && 'Famille & dépendants'}
              {tab === 'emergency' && 'Contact d\'urgence'}
            </div>
            <button className="btn btn-ghost btn-sm"><Icons.edit size={13}/> Modifier</button>
          </div>

          {tab === 'overview' && (
            <div className="grid-2" style={{padding:'20px 24px', gap:20}}>
              {[
                ['Nom complet', EMP_ME.name],
                ['Date de naissance', '12 mars 1988 · 38 ans'],
                ['Lieu de naissance', 'Douala, Cameroun'],
                ['Nationalité', 'Camerounaise'],
                ['Sexe', 'Féminin'],
                ['Situation matrimoniale', 'Mariée'],
                ['Pièce d\'identité', 'CNI · 110428937'],
                ['Date émission CNI', '22 août 2021'],
                ['N° CNPS', EMP_ME.cnps],
                ['NIU (fiscalité)', 'M0123456789F'],
                ['Permis de conduire', 'Catégorie B · 2010'],
                ['Langues parlées', 'Français, Anglais, Ewondo'],
              ].map(([l,v], i) => (
                <div key={i}>
                  <div style={{fontSize:11, color:'var(--ink-3)', textTransform:'uppercase', letterSpacing:'0.05em', fontWeight:600}}>{l}</div>
                  <div style={{fontSize:13.5, marginTop:4, color:'var(--ink-2)'}}>{v}</div>
                </div>
              ))}
            </div>
          )}

          {tab === 'contact' && (
            <div className="grid-2" style={{padding:'20px 24px', gap:20}}>
              {[
                ['Email professionnel', EMP_ME.email, true],
                ['Email personnel', 'aminata.d@gmail.com', true],
                ['Téléphone mobile', EMP_ME.phone, true],
                ['Téléphone domicile', '+237 2 22 12 34 56', true],
                ['WhatsApp', '+237 6 78 12 34 56', true],
                ['Adresse postale', 'BP 1234, Yaoundé', false],
                ['Adresse domicile', 'Quartier Bastos, Rue 1.834', false],
                ['Ville', 'Yaoundé', false],
                ['Région', 'Centre', false],
                ['Code postal', '00237', false],
              ].map(([l,v,verif], i) => (
                <div key={i}>
                  <div className="row gap-2">
                    <span style={{fontSize:11, color:'var(--ink-3)', textTransform:'uppercase', letterSpacing:'0.05em', fontWeight:600}}>{l}</span>
                    {verif && <span className="badge green" style={{fontSize:9, padding:'1px 6px'}}>Vérifié</span>}
                  </div>
                  <div style={{fontSize:13.5, marginTop:4, color:'var(--ink-2)'}}>{v}</div>
                </div>
              ))}
            </div>
          )}

          {tab === 'banking' && (
            <div style={{padding:24}}>
              <div style={{
                padding:'22px 24px', borderRadius:14,
                background:'linear-gradient(135deg, #1A150E 0%, #2D2520 100%)',
                color:'#fff', position:'relative', overflow:'hidden', marginBottom:16
              }}>
                <div style={{position:'absolute', top:-40, right:-40, width:200, height:200,
                  background:'radial-gradient(circle, rgba(249,115,22,0.3) 0%, transparent 70%)'}}/>
                <div className="row" style={{position:'relative'}}>
                  <div>
                    <div style={{fontSize:11, opacity:0.6, textTransform:'uppercase', letterSpacing:'0.06em'}}>Compte bancaire principal</div>
                    <div style={{fontFamily:'Inter Tight', fontSize:22, fontWeight:700, marginTop:6}}>Afriland First Bank</div>
                    <div className="mono" style={{fontSize:14, marginTop:8, letterSpacing:'0.05em'}}>•••• •••• •••• 4562</div>
                  </div>
                  <div style={{marginLeft:'auto'}}>
                    <span className="badge green" style={{background:'rgba(16,185,129,0.2)', color:'#34D399'}}>Actif · vérifié</span>
                  </div>
                </div>
              </div>

              <div className="grid-2" style={{gap:14}}>
                <div style={{padding:14, border:'1px solid var(--line)', borderRadius:12, background:'#fff'}}>
                  <div className="row" style={{marginBottom:6}}>
                    <div className="icon-tile amber" style={{width:32, height:32}}><span style={{fontWeight:800, fontFamily:'Inter Tight'}}>M</span></div>
                    <div style={{marginLeft:10}}>
                      <div style={{fontSize:13, fontWeight:600}}>MTN Mobile Money</div>
                      <div className="mono" style={{fontSize:11, color:'var(--ink-3)'}}>+237 6 78 12 ** 56</div>
                    </div>
                    <span className="badge orange" style={{marginLeft:'auto', fontSize:10}}>Principal</span>
                  </div>
                  <div style={{fontSize:11, color:'var(--ink-3)'}}>Utilisé pour: salaire, remboursements</div>
                </div>
                <div style={{padding:14, border:'1px solid var(--line)', borderRadius:12, background:'#fff'}}>
                  <div className="row" style={{marginBottom:6}}>
                    <div className="icon-tile orange" style={{width:32, height:32}}><span style={{fontWeight:800, fontFamily:'Inter Tight'}}>O</span></div>
                    <div style={{marginLeft:10}}>
                      <div style={{fontSize:13, fontWeight:600}}>Orange Money</div>
                      <div className="mono" style={{fontSize:11, color:'var(--ink-3)'}}>+237 6 99 87 ** 12</div>
                    </div>
                  </div>
                  <div style={{fontSize:11, color:'var(--ink-3)'}}>Compte secondaire</div>
                </div>
              </div>

              <div className="divider"/>

              <div className="row" style={{padding:'8px 4px'}}>
                <div>
                  <div style={{fontSize:11, color:'var(--ink-3)', textTransform:'uppercase', letterSpacing:'0.05em', fontWeight:600}}>Préférence de paiement salaire</div>
                  <div className="row gap-2" style={{marginTop:8}}>
                    <button className="chip orange active">Banque Afriland (100%)</button>
                    <button className="chip">Répartir</button>
                  </div>
                </div>
              </div>
            </div>
          )}

          {tab === 'family' && (
            <div style={{padding:24}}>
              <div className="row" style={{marginBottom:14}}>
                <div className="card-title" style={{fontSize:14}}>Conjoint(e)</div>
                <button className="btn btn-ghost btn-sm" style={{marginLeft:'auto'}}><Icons.edit size={12}/></button>
              </div>
              <div style={{padding:14, background:'var(--bg-dim)', borderRadius:12, marginBottom:20}}>
                <div className="row gap-3">
                  <Avatar name="Patrick Diallo" color="blue" size="lg"/>
                  <div>
                    <div style={{fontSize:14, fontWeight:700}}>Patrick Diallo</div>
                    <div style={{fontSize:12, color:'var(--ink-3)'}}>Né le 8 juin 1985 · Ingénieur civil · Mariés depuis 2015</div>
                  </div>
                </div>
              </div>

              <div className="row" style={{marginBottom:14}}>
                <div className="card-title" style={{fontSize:14}}>Enfants à charge (2)</div>
                <button className="btn btn-secondary btn-sm" style={{marginLeft:'auto'}}><Icons.plus size={12}/> Ajouter</button>
              </div>
              <div className="grid-2" style={{gap:12}}>
                {[
                  { n:'Léa Diallo', age:'8 ans · CE2', sub:'École Notre-Dame · scolarisée', c:'violet' },
                  { n:'Yannis Diallo', age:'5 ans · maternelle', sub:'École Notre-Dame', c:'teal' },
                ].map((c, i) => (
                  <div key={i} style={{padding:14, border:'1px solid var(--line)', borderRadius:12}}>
                    <div className="row gap-3">
                      <Avatar name={c.n} color={c.c}/>
                      <div>
                        <div style={{fontSize:13.5, fontWeight:700}}>{c.n}</div>
                        <div style={{fontSize:11, color:'var(--ink-3)'}}>{c.age}</div>
                        <div style={{fontSize:11, color:'var(--ink-3)', marginTop:2}}>{c.sub}</div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
              <div className="card-pad" style={{padding:12, background:'var(--orange-50)', borderRadius:10, marginTop:16, fontSize:12.5}}>
                <div className="row gap-2"><Icons.info size={14} style={{color:'var(--orange-600)'}}/> 2 personnes à charge déclarées · impact sur le calcul IRPP</div>
              </div>
            </div>
          )}

          {tab === 'emergency' && (
            <div style={{padding:24}}>
              {[
                { n:'Patrick Diallo', rel:'Conjoint', tel:'+237 6 99 12 34 56', email:'p.diallo@example.com', priority:1, c:'blue' },
                { n:'Marie Mballa', rel:'Mère', tel:'+237 2 22 45 67 89', email:'—', priority:2, c:'amber' },
                { n:'Dr. Owono', rel:'Médecin traitant', tel:'+237 6 75 34 12 90', email:'cabinet.owono@gmail.com', priority:3, c:'green' },
              ].map((c, i) => (
                <div key={i} className="row gap-3" style={{padding:14, border:'1px solid var(--line)', borderRadius:12, marginBottom:10}}>
                  <Avatar name={c.n} color={c.c} size="lg"/>
                  <div style={{flex:1}}>
                    <div className="row gap-2">
                      <span style={{fontSize:14, fontWeight:700}}>{c.n}</span>
                      <span className="tag">{c.rel}</span>
                      {c.priority === 1 && <span className="badge orange" style={{fontSize:10}}>Priorité 1</span>}
                    </div>
                    <div className="row gap-3" style={{marginTop:6, fontSize:12, color:'var(--ink-3)'}}>
                      <span className="row gap-1"><Icons.phone size={12}/> {c.tel}</span>
                      <span className="row gap-1"><Icons.mail size={12}/> {c.email}</span>
                    </div>
                  </div>
                  <button className="icon-btn"><Icons.edit size={13}/></button>
                </div>
              ))}
              <button className="btn btn-secondary btn-sm" style={{marginTop:8}}><Icons.plus size={13}/> Ajouter un contact</button>
            </div>
          )}
        </div>

        {/* Right column: completeness + history */}
        <div className="col gap-3">
          <div className="card card-pad">
            <div className="row" style={{marginBottom:14}}>
              <span className="card-title" style={{fontSize:14}}>Complétude du profil</span>
            </div>
            <div style={{position:'relative', display:'flex', justifyContent:'center', marginBottom:14}}>
              <Donut data={[
                { value:88, color:'#10B981' },
                { value:12, color:'var(--bg-soft)' },
              ]} size={140} thick={18}/>
              <div style={{position:'absolute', inset:0, display:'grid', placeItems:'center', textAlign:'center'}}>
                <div>
                  <div style={{fontFamily:'Inter Tight', fontSize:28, fontWeight:800, color:'var(--green-600)'}}>88%</div>
                  <div style={{fontSize:10, color:'var(--ink-3)'}}>complété</div>
                </div>
              </div>
            </div>
            <div style={{fontSize:11.5, color:'var(--ink-3)', marginBottom:10, fontWeight:600, textTransform:'uppercase', letterSpacing:'0.05em'}}>À compléter :</div>
            {[
              { l:'Photo de profil', done:true },
              { l:'Coordonnées bancaires', done:true },
              { l:'Personne à charge', done:true },
              { l:'Diplômes & certifications', done:false },
              { l:'CV complet', done:false },
            ].map((it, i) => (
              <div key={i} className="row gap-2" style={{padding:'6px 0', fontSize:12.5}}>
                {it.done ?
                  <Icons.check size={14} style={{color:'var(--green-500)'}}/> :
                  <div style={{width:14, height:14, borderRadius:4, border:'1.5px dashed var(--ink-4)'}}/>
                }
                <span style={{color: it.done ? 'var(--ink-3)' : 'var(--ink-2)', textDecoration: it.done ? 'line-through' : 'none'}}>{it.l}</span>
              </div>
            ))}
          </div>

          <div className="card card-pad">
            <div className="card-title" style={{fontSize:14, marginBottom:14}}>Historique récent</div>
            {[
              { d:'12 mai', a:'Mise à jour téléphone WhatsApp', tone:'blue' },
              { d:'04 mai', a:'Photo de profil changée', tone:'orange' },
              { d:'02 avr', a:'Compte MTN MoMo ajouté', tone:'amber' },
              { d:'14 jan', a:'Promotion vers Lead Mobile Eng.', tone:'green' },
            ].map((h, i) => (
              <div key={i} className="row gap-3" style={{padding:'8px 0', borderBottom: i < 3 ? '1px solid var(--line-soft)' : 'none'}}>
                <div className={"sd " + h.tone}></div>
                <div style={{flex:1, fontSize:12.5}}>{h.a}</div>
                <span className="muted" style={{fontSize:11}}>{h.d}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
window.EmpPageProfile = EmpPageProfile;
