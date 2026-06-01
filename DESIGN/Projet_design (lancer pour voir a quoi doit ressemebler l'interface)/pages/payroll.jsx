/* global React */
const { useState: useS_pay } = React;

function PagePayroll() {
  const { Icons, Avatar, PageHeader, Line, Donut } = window;
  const [tab, setTab] = useS_pay('runs');

  const runs = [
    { id:'PR-2026-05', period:'Mai 2026', status:'En cours', employees:342, gross:'142 850 000', net:'98 245 000', date:'14 mai 2026', tone:'orange', step:3 },
    { id:'PR-2026-04', period:'Avril 2026', status:'Payée', employees:338, gross:'140 220 000', net:'96 980 000', date:'28 avr. 2026', tone:'green', step:5 },
    { id:'PR-2026-03', period:'Mars 2026', status:'Payée', employees:332, gross:'138 540 000', net:'95 110 000', date:'29 mar. 2026', tone:'green', step:5 },
    { id:'PR-2026-02', period:'Février 2026', status:'Payée', employees:328, gross:'135 980 000', net:'93 540 000', date:'27 fév. 2026', tone:'green', step:5 },
    { id:'PR-2026-01', period:'Janvier 2026', status:'Payée', employees:322, gross:'133 410 000', net:'91 770 000', date:'30 jan. 2026', tone:'green', step:5 },
  ];

  return (
    <div>
      <PageHeader
        uc="Rémunération"
        title="Paie"
        subtitle="Calcul, validation et paiement des bulletins de salaire"
        actions={
          <>
            <button className="btn btn-primary"><Icons.plus/> Nouveau cycle</button>
          </>
        }
      />

      {/* Current run hero */}
      <div className="card card-pad-lg" style={{
        background:'linear-gradient(135deg, #1A150E 0%, #2D2520 100%)',
        color:'#fff', marginBottom:20, position:'relative', overflow:'hidden'
      }}>
        <div style={{position:'absolute', top:-100, right:-100, width:400, height:400,
          background:'radial-gradient(circle, rgba(249,115,22,0.3) 0%, transparent 70%)'}}/>
        <div className="row" style={{alignItems:'flex-start', position:'relative'}}>
          <div style={{flex:1}}>
            <div className="row gap-2">
              <span className="badge orange" style={{background:'rgba(249,115,22,0.2)', color:'#FFB066'}}>Cycle actif</span>
              <span className="mono" style={{fontSize:11, opacity:0.6}}>PR-2026-05</span>
            </div>
            <div className="h-display" style={{fontSize:32, color:'#fff', marginTop:8}}>Paie · Mai 2026</div>
            <div style={{fontSize:14, opacity:0.7, marginTop:4}}>Cycle ouvert le 1 mai · Échéance paiement le 28 mai 2026</div>

            <div className="row gap-4" style={{marginTop:24}}>
              <div className="stepper" style={{flex:1, flexWrap:'wrap'}}>
                <span className="step done">1 · Variables</span>
                <span className="step-arrow">→</span>
                <span className="step done">2 · Calcul</span>
                <span className="step-arrow">→</span>
                <span className="step active">3 · Vérif. DRH</span>
                <span className="step-arrow">→</span>
                <span className="step">4 · Validation DG</span>
                <span className="step-arrow">→</span>
                <span className="step">5 · Paiement</span>
              </div>
            </div>
          </div>
          <div className="col gap-2" style={{minWidth:200}}>
            <button className="btn btn-primary">Continuer la vérification</button>
            <button className="btn" style={{background:'rgba(255,255,255,0.1)', color:'#fff', border:'1px solid rgba(255,255,255,0.2)'}}>
              <Icons.print size={14}/> Aperçu bulletins
            </button>
          </div>
        </div>

        <div className="grid-4" style={{marginTop:28, gap:16}}>
          {[
            { l:'Employés payés', v:'342', sub:'sur 342' },
            { l:'Masse brute', v:'142,85M', sub:'XAF · +1,9% vs avr.' },
            { l:'Cotisations sal.', v:'24,12M', sub:'CNPS, IRPP, CFC' },
            { l:'Net à payer', v:'98,24M', sub:'XAF · estimation' },
          ].map((s,i) => (
            <div key={i} style={{padding:'14px 16px', background:'rgba(255,255,255,0.06)', borderRadius:12, border:'1px solid rgba(255,255,255,0.1)'}}>
              <div style={{fontSize:11, opacity:0.7, textTransform:'uppercase', letterSpacing:'0.05em'}}>{s.l}</div>
              <div style={{fontFamily:'Inter Tight', fontSize:24, fontWeight:800, marginTop:6}} className="tabular">{s.v}</div>
              <div style={{fontSize:11, opacity:0.6, marginTop:2}}>{s.sub}</div>
            </div>
          ))}
        </div>
      </div>

      <div style={{display:'grid', gridTemplateColumns:'2fr 1fr', gap:16, marginBottom:20}}>
        <div className="card">
          <div className="card-head">
            <div className="card-title">Évolution de la masse salariale</div>
            <div className="row gap-2">
              <button className="chip orange active">12M</button>
              <button className="chip">YTD</button>
            </div>
          </div>
          <div style={{padding:'18px 22px'}}>
            <Line data={[124,127,128,131,132,134,135,137,139,140,141,142]}
              labels={['Jui','Juil','Aoû','Sep','Oct','Nov','Déc','Jan','Fév','Mar','Avr','Mai']}
              color="#F97316" fill="#F97316" h={220}/>
          </div>
        </div>
        <div className="card">
          <div className="card-head">
            <div className="card-title">Composition · Mai 2026</div>
          </div>
          <div style={{padding:18, display:'flex', flexDirection:'column', alignItems:'center', gap:14}}>
            <div style={{position:'relative'}}>
              <Donut data={[
                { value:62, color:'#F97316' },
                { value:18, color:'#FB923C' },
                { value:12, color:'#FCD34D' },
                { value:8, color:'#34D399' },
              ]} size={160} thick={22}/>
              <div style={{position:'absolute', inset:0, display:'grid', placeItems:'center', textAlign:'center'}}>
                <div>
                  <div style={{fontFamily:'Inter Tight', fontSize:22, fontWeight:800}} className="tabular">142,85M</div>
                  <div style={{fontSize:11, color:'var(--ink-3)'}}>XAF brut</div>
                </div>
              </div>
            </div>
            <div style={{width:'100%'}}>
              {[
                { c:'#F97316', l:'Salaire de base', v:'88,6M', p:'62%' },
                { c:'#FB923C', l:'Primes & indemnités', v:'25,7M', p:'18%' },
                { c:'#FCD34D', l:'Heures supp.', v:'17,1M', p:'12%' },
                { c:'#34D399', l:'Avantages en nature', v:'11,5M', p:'8%' },
              ].map((x,i) => (
                <div key={i} className="row" style={{padding:'6px 0', fontSize:12.5}}>
                  <span style={{width:10, height:10, borderRadius:3, background:x.c}}/>
                  <span style={{flex:1, marginLeft:8}}>{x.l}</span>
                  <span className="tabular">{x.v}</span>
                  <span className="muted" style={{marginLeft:8, width:32, textAlign:'right'}} className="tabular">{x.p}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Cycles list */}
      <div className="card">
        <div className="card-head">
          <div className="card-title">Cycles de paie</div>
          <div className="row gap-2">
            <button className="chip orange active">2026 (5)</button>
            <button className="chip">2025 (12)</button>
            <button className="chip">2024 (12)</button>
          </div>
        </div>
        <table className="t">
          <thead>
            <tr><th>Période</th><th>Statut</th><th>Employés</th><th>Masse brute (XAF)</th><th>Net à payer (XAF)</th><th>Date</th><th></th></tr>
          </thead>
          <tbody>
            {runs.map(r => (
              <tr key={r.id}>
                <td>
                  <div className="row gap-2">
                    <div className={"icon-tile " + r.tone}><Icons.payroll size={14}/></div>
                    <div>
                      <div style={{fontSize:13.5, fontWeight:600}}>{r.period}</div>
                      <div className="mono" style={{fontSize:11, color:'var(--ink-3)'}}>{r.id}</div>
                    </div>
                  </div>
                </td>
                <td>
                  <span className={"badge " + (r.status === 'Payée' ? 'green' : 'orange')}>{r.status}</span>
                  <div style={{marginTop:6, width:120}}>
                    <div className="bar thin"><div style={{width: `${(r.step/5)*100}%`}}/></div>
                  </div>
                </td>
                <td className="tabular">{r.employees}</td>
                <td className="tabular" style={{fontWeight:600}}>{r.gross}</td>
                <td className="tabular" style={{color:'var(--ink-2)'}}>{r.net}</td>
                <td className="muted">{r.date}</td>
                <td>
                  <div className="row gap-2">
                    <button className="btn btn-ghost btn-sm"><Icons.download size={12}/></button>
                    <button className="btn btn-secondary btn-sm">Détails <Icons.chevR size={12}/></button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Sample payslip preview */}
      <div className="section-title">Aperçu bulletin · Aminata Diallo (mai 2026)</div>
      <div className="card card-pad-lg">
        <div className="row" style={{borderBottom:'2px solid var(--ink)', paddingBottom:14, marginBottom:18}}>
          <div>
            <div className="row gap-2" style={{marginBottom:4}}>
              <div className="sidebar-logo" style={{width:32, height:32, fontSize:16}}>R</div>
              <div>
                <div style={{fontFamily:'Inter Tight', fontSize:18, fontWeight:800}}>RT-Comops SARL</div>
                <div style={{fontSize:11, color:'var(--ink-3)'}}>BP 1234 Yaoundé · RCCM/Y/2018/B/12345 · NIU M122001234567</div>
              </div>
            </div>
          </div>
          <div style={{marginLeft:'auto', textAlign:'right'}}>
            <div style={{fontFamily:'Inter Tight', fontSize:14, fontWeight:700, letterSpacing:'-0.01em'}}>BULLETIN DE PAIE</div>
            <div className="mono" style={{fontSize:12, color:'var(--ink-3)'}}>Période · 01–31 mai 2026</div>
            <div className="mono" style={{fontSize:11, color:'var(--ink-4)', marginTop:2}}>N° PSL-2026-05-0142</div>
          </div>
        </div>

        <div className="grid-2" style={{marginBottom:18, gap:14}}>
          <div style={{padding:12, background:'var(--bg-dim)', borderRadius:10}}>
            <div className="muted" style={{fontSize:10, textTransform:'uppercase', letterSpacing:'0.06em', fontWeight:600}}>Salarié</div>
            <div style={{fontSize:14, fontWeight:600, marginTop:2}}>Aminata Diallo</div>
            <div style={{fontSize:12, color:'var(--ink-3)'}}>EMP-0142 · Lead Mobile Engineer · CDI</div>
            <div style={{fontSize:11, color:'var(--ink-3)', marginTop:4}}>CNPS · 110428937H</div>
          </div>
          <div style={{padding:12, background:'var(--bg-dim)', borderRadius:10}}>
            <div className="muted" style={{fontSize:10, textTransform:'uppercase', letterSpacing:'0.06em', fontWeight:600}}>Versement</div>
            <div style={{fontSize:14, fontWeight:600, marginTop:2}}>Mobile Money · MTN MoMo</div>
            <div style={{fontSize:12, color:'var(--ink-3)'}}>237 6** ** 34 56</div>
            <div style={{fontSize:11, color:'var(--ink-3)', marginTop:4}}>Prévu le 28 mai 2026</div>
          </div>
        </div>

        <table style={{width:'100%', borderCollapse:'collapse', fontSize:13}}>
          <thead>
            <tr style={{borderBottom:'1px solid var(--line)'}}>
              <th style={{textAlign:'left', padding:'10px 8px', fontSize:11, color:'var(--ink-3)', textTransform:'uppercase', letterSpacing:'0.05em'}}>Libellé</th>
              <th style={{textAlign:'right', padding:'10px 8px', fontSize:11, color:'var(--ink-3)', textTransform:'uppercase', letterSpacing:'0.05em', width:90}}>Base</th>
              <th style={{textAlign:'right', padding:'10px 8px', fontSize:11, color:'var(--ink-3)', textTransform:'uppercase', letterSpacing:'0.05em', width:70}}>Taux</th>
              <th style={{textAlign:'right', padding:'10px 8px', fontSize:11, color:'var(--ink-3)', textTransform:'uppercase', letterSpacing:'0.05em', width:120}}>Gain (XAF)</th>
              <th style={{textAlign:'right', padding:'10px 8px', fontSize:11, color:'var(--ink-3)', textTransform:'uppercase', letterSpacing:'0.05em', width:120}}>Retenue (XAF)</th>
            </tr>
          </thead>
          <tbody className="tabular">
            <tr style={{background:'var(--orange-50)'}}>
              <td style={{padding:'8px', fontWeight:600}} colSpan="5">RÉMUNÉRATION BRUTE</td>
            </tr>
            <tr><td style={{padding:'8px'}}>Salaire de base</td><td style={{padding:'8px', textAlign:'right'}}>173h33</td><td style={{padding:'8px', textAlign:'right'}}>—</td><td style={{padding:'8px', textAlign:'right'}}>950 000</td><td></td></tr>
            <tr><td style={{padding:'8px'}}>Prime d'ancienneté</td><td style={{padding:'8px', textAlign:'right'}}>950 000</td><td style={{padding:'8px', textAlign:'right'}}>8%</td><td style={{padding:'8px', textAlign:'right'}}>76 000</td><td></td></tr>
            <tr><td style={{padding:'8px'}}>Prime de transport</td><td style={{padding:'8px', textAlign:'right'}}>—</td><td style={{padding:'8px', textAlign:'right'}}>—</td><td style={{padding:'8px', textAlign:'right'}}>40 000</td><td></td></tr>
            <tr><td style={{padding:'8px'}}>Indemnité responsabilité</td><td style={{padding:'8px', textAlign:'right'}}>—</td><td style={{padding:'8px', textAlign:'right'}}>—</td><td style={{padding:'8px', textAlign:'right'}}>120 000</td><td></td></tr>
            <tr><td style={{padding:'8px'}}>Heures supplémentaires</td><td style={{padding:'8px', textAlign:'right'}}>12h</td><td style={{padding:'8px', textAlign:'right'}}>125%</td><td style={{padding:'8px', textAlign:'right'}}>59 000</td><td></td></tr>
            <tr style={{background:'var(--bg-dim)'}}>
              <td style={{padding:'10px 8px', fontWeight:700}}>Brut imposable</td><td></td><td></td>
              <td style={{padding:'10px 8px', textAlign:'right', fontWeight:700}}>1 245 000</td><td></td>
            </tr>

            <tr style={{background:'var(--orange-50)'}}>
              <td style={{padding:'8px', fontWeight:600}} colSpan="5">RETENUES SALARIALES</td>
            </tr>
            <tr><td style={{padding:'8px'}}>CNPS · Vieillesse (4,2%)</td><td style={{padding:'8px', textAlign:'right'}}>750 000</td><td style={{padding:'8px', textAlign:'right'}}>4,2%</td><td></td><td style={{padding:'8px', textAlign:'right'}}>31 500</td></tr>
            <tr><td style={{padding:'8px'}}>IRPP (barème)</td><td style={{padding:'8px', textAlign:'right'}}>1 245 000</td><td style={{padding:'8px', textAlign:'right'}}>—</td><td></td><td style={{padding:'8px', textAlign:'right'}}>112 400</td></tr>
            <tr><td style={{padding:'8px'}}>CAC (10% IRPP)</td><td></td><td style={{padding:'8px', textAlign:'right'}}>10%</td><td></td><td style={{padding:'8px', textAlign:'right'}}>11 240</td></tr>
            <tr><td style={{padding:'8px'}}>CFC (1%)</td><td style={{padding:'8px', textAlign:'right'}}>1 245 000</td><td style={{padding:'8px', textAlign:'right'}}>1%</td><td></td><td style={{padding:'8px', textAlign:'right'}}>12 450</td></tr>
            <tr><td style={{padding:'8px'}}>Redevance audiovisuelle</td><td></td><td></td><td></td><td style={{padding:'8px', textAlign:'right'}}>1 950</td></tr>
            <tr><td style={{padding:'8px'}}>Mutuelle complémentaire</td><td></td><td></td><td></td><td style={{padding:'8px', textAlign:'right'}}>8 500</td></tr>
            <tr style={{background:'var(--bg-dim)'}}>
              <td style={{padding:'10px 8px', fontWeight:700}}>Total retenues</td><td></td><td></td><td></td>
              <td style={{padding:'10px 8px', textAlign:'right', fontWeight:700}}>178 040</td>
            </tr>

            <tr style={{borderTop:'2px solid var(--ink)', background:'var(--ink)', color:'#fff'}}>
              <td style={{padding:'14px 10px', fontWeight:700, fontSize:14}}>NET À PAYER</td>
              <td></td><td></td><td></td>
              <td style={{padding:'14px 10px', textAlign:'right', fontFamily:'Inter Tight', fontSize:20, fontWeight:800}}>1 066 960</td>
            </tr>
          </tbody>
        </table>

        <div className="row" style={{marginTop:20, paddingTop:16, borderTop:'1px solid var(--line-soft)', fontSize:11, color:'var(--ink-3)'}}>
          <span>Cumul brut 2026: <b className="tabular" style={{color:'var(--ink-2)'}}>6 225 000</b> XAF</span>
          <span style={{marginLeft:24}}>Cumul IRPP 2026: <b className="tabular" style={{color:'var(--ink-2)'}}>562 000</b> XAF</span>
          <div className="row gap-2" style={{marginLeft:'auto'}}>
            <button className="btn btn-secondary btn-sm"><Icons.print size={13}/> Imprimer</button>
            <button className="btn btn-secondary btn-sm"><Icons.send size={13}/> Envoyer par email</button>
            <button className="btn btn-primary btn-sm"><Icons.download size={13}/> Télécharger PDF</button>
          </div>
        </div>
      </div>
    </div>
  );
}

window.PagePayroll = PagePayroll;
