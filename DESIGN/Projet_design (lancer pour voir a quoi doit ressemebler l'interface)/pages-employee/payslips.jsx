/* global React */
const { useState: useS_eps } = React;

function EmpPagePayslips() {
  const { Icons, PageHeader, Line } = window;
  const [selected, setSelected] = useS_eps(null);

  const payslips = [
    { id:'PSL-2026-04-0142', period:'Avril 2026', gross:'1 245 000', net:'1 066 960', date:'28 avr 2026', status:'paid', channel:'MTN MoMo' },
    { id:'PSL-2026-03-0142', period:'Mars 2026', gross:'1 245 000', net:'1 052 480', date:'29 mar 2026', status:'paid', channel:'MTN MoMo' },
    { id:'PSL-2026-02-0142', period:'Février 2026', gross:'1 245 000', net:'1 048 200', date:'27 fév 2026', status:'paid', channel:'MTN MoMo' },
    { id:'PSL-2026-01-0142', period:'Janvier 2026', gross:'1 245 000', net:'1 042 800', date:'30 jan 2026', status:'paid', channel:'MTN MoMo' },
    { id:'PSL-2025-12-0142', period:'Décembre 2025 · 13ème mois', gross:'1 850 000', net:'1 580 200', date:'18 déc 2025', status:'paid', channel:'Banque' },
    { id:'PSL-2025-11-0142', period:'Novembre 2025', gross:'1 110 000', net:'945 600', date:'28 nov 2025', status:'paid', channel:'MTN MoMo' },
    { id:'PSL-2025-10-0142', period:'Octobre 2025', gross:'1 110 000', net:'945 600', date:'30 oct 2025', status:'paid', channel:'MTN MoMo' },
  ];

  if (selected) {
    return <PayslipDetail data={selected} onBack={() => setSelected(null)}/>;
  }

  return (
    <div>
      <PageHeader
        uc="Rémunération"
        title="Ma paie"
        subtitle="Bulletins de paie, cumuls et historique de versement"
        actions={
          <>
            <button className="btn btn-secondary"><Icons.download/> Tout télécharger (ZIP)</button>
            <button className="btn btn-primary"><Icons.doc size={14}/> Attestation salaire</button>
          </>
        }
      />

      {/* Hero next payslip */}
      <div className="card card-pad-lg" style={{
        background:'linear-gradient(135deg, #1A150E 0%, #2D2520 100%)',
        color:'#fff', marginBottom:20, position:'relative', overflow:'hidden'
      }}>
        <div style={{position:'absolute', top:-100, right:-100, width:400, height:400,
          background:'radial-gradient(circle, rgba(249,115,22,0.3) 0%, transparent 70%)'}}/>
        <div className="row" style={{alignItems:'flex-start', position:'relative'}}>
          <div style={{flex:1}}>
            <div className="row gap-2">
              <span className="badge orange" style={{background:'rgba(249,115,22,0.2)', color:'#FFB066'}}>Prochain bulletin</span>
              <span className="mono" style={{fontSize:11, opacity:0.6}}>PSL-2026-05-0142</span>
            </div>
            <div className="h-display" style={{fontSize:30, color:'#fff', marginTop:8}}>Bulletin · Mai 2026</div>
            <div style={{fontSize:13, opacity:0.7, marginTop:4}}>Disponible le 28 mai 2026 · paiement prévu via MTN Mobile Money</div>

            <div className="row gap-4" style={{marginTop:24, flexWrap:'wrap'}}>
              <div>
                <div style={{fontSize:11, opacity:0.6, textTransform:'uppercase', letterSpacing:'0.05em'}}>Brut estimé</div>
                <div style={{fontFamily:'Inter Tight', fontSize:24, fontWeight:800}} className="tabular">1 245 000 <span style={{fontSize:13, opacity:0.6}}>XAF</span></div>
              </div>
              <div>
                <div style={{fontSize:11, opacity:0.6, textTransform:'uppercase', letterSpacing:'0.05em'}}>Net estimé</div>
                <div style={{fontFamily:'Inter Tight', fontSize:24, fontWeight:800}} className="tabular" style={{color:'#FFB066'}}>1 066 960 <span style={{fontSize:13, opacity:0.6, color:'#fff'}}>XAF</span></div>
              </div>
              <div>
                <div style={{fontSize:11, opacity:0.6, textTransform:'uppercase', letterSpacing:'0.05em'}}>Jours travaillés</div>
                <div style={{fontFamily:'Inter Tight', fontSize:24, fontWeight:800}} className="tabular">22 / 22</div>
              </div>
            </div>
          </div>
          <div className="col gap-2">
            <button className="btn" style={{background:'rgba(255,255,255,0.1)', color:'#fff', border:'1px solid rgba(255,255,255,0.2)'}}>
              <Icons.info size={14}/> Détail estimation
            </button>
          </div>
        </div>
      </div>

      {/* Annual summary */}
      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'Cumul brut 2026', v:'6 225 000', sub:'XAF · sur 12 mois prévisionnel', tone:'orange' },
          { l:'Cumul net 2026', v:'5 297 440', sub:'XAF · YTD', tone:'green' },
          { l:'IRPP retenu', v:'562 000', sub:'XAF · cumul YTD', tone:'red' },
          { l:'Cotisations CNPS', v:'157 500', sub:'XAF · cumul YTD', tone:'blue' },
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
        <div className="card-head">
          <div className="card-title">Historique de mes bulletins</div>
          <div className="row gap-2">
            <button className="chip orange active">2026</button>
            <button className="chip">2025</button>
            <button className="chip">2024</button>
          </div>
        </div>
        <table className="t">
          <thead><tr><th>Période</th><th>Référence</th><th>Brut (XAF)</th><th>Net à payer (XAF)</th><th>Date versement</th><th>Canal</th><th></th></tr></thead>
          <tbody>
            {payslips.map(p => (
              <tr key={p.id} onClick={() => setSelected(p)} style={{cursor:'pointer'}}>
                <td>
                  <div className="row gap-2">
                    <div className="icon-tile orange" style={{width:36, height:36}}><Icons.payroll size={16}/></div>
                    <div>
                      <div style={{fontSize:13.5, fontWeight:700}}>{p.period}</div>
                      <div style={{fontSize:11, color:'var(--ink-3)'}}>{p.status === 'paid' && '✓ Reçu'}</div>
                    </div>
                  </div>
                </td>
                <td className="mono" style={{fontSize:11, color:'var(--ink-3)'}}>{p.id}</td>
                <td className="tabular">{p.gross}</td>
                <td className="tabular" style={{fontWeight:700, color:'var(--orange-700)'}}>{p.net}</td>
                <td className="muted">{p.date}</td>
                <td>
                  <span className="tag">{p.channel}</span>
                </td>
                <td>
                  <div className="row gap-2">
                    <button className="icon-btn" style={{width:28, height:28}} onClick={ev => ev.stopPropagation()}><Icons.download size={12}/></button>
                    <button className="icon-btn" style={{width:28, height:28}} onClick={ev => ev.stopPropagation()}><Icons.chevR size={12}/></button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function PayslipDetail({ data, onBack }) {
  const { Icons } = window;
  return (
    <div>
      <div className="row gap-2" style={{marginBottom:14}}>
        <button className="btn btn-ghost btn-sm" onClick={onBack}>
          <Icons.chevR size={14} style={{transform:'rotate(180deg)'}}/> Retour
        </button>
        <span className="muted" style={{fontSize:12}}>Ma paie / {data.period}</span>
      </div>

      <div className="row" style={{marginBottom:20, gap:12}}>
        <div>
          <div className="h-display" style={{fontSize:28}}>Bulletin · {data.period}</div>
          <div className="row gap-2" style={{marginTop:6}}>
            <span className="mono" style={{fontSize:11, color:'var(--ink-3)'}}>{data.id}</span>
            <span className="badge green">{data.status === 'paid' ? 'Reçu le ' + data.date : 'En cours'}</span>
          </div>
        </div>
        <div className="row gap-2" style={{marginLeft:'auto'}}>
          <button className="btn btn-secondary"><Icons.print size={14}/> Imprimer</button>
          <button className="btn btn-secondary"><Icons.send size={14}/> Envoyer par email</button>
          <button className="btn btn-primary"><Icons.download size={14}/> Télécharger PDF</button>
        </div>
      </div>

      {/* Payslip */}
      <div className="card card-pad-lg">
        <div className="row" style={{borderBottom:'2px solid var(--ink)', paddingBottom:14, marginBottom:18}}>
          <div className="row gap-2">
            <div className="sidebar-logo" style={{width:38, height:38, fontSize:18}}>R</div>
            <div>
              <div style={{fontFamily:'Inter Tight', fontSize:18, fontWeight:800}}>RT-Comops SARL</div>
              <div style={{fontSize:11, color:'var(--ink-3)'}}>BP 1234 Yaoundé · RCCM/Y/2018/B/12345 · NIU M122001234567</div>
            </div>
          </div>
          <div style={{marginLeft:'auto', textAlign:'right'}}>
            <div style={{fontFamily:'Inter Tight', fontSize:14, fontWeight:700}}>BULLETIN DE PAIE</div>
            <div className="mono" style={{fontSize:12, color:'var(--ink-3)'}}>Période · {data.period}</div>
            <div className="mono" style={{fontSize:11, color:'var(--ink-4)', marginTop:2}}>N° {data.id}</div>
          </div>
        </div>

        <div className="grid-2" style={{marginBottom:18, gap:14}}>
          <div style={{padding:14, background:'var(--bg-dim)', borderRadius:10}}>
            <div className="muted" style={{fontSize:10, textTransform:'uppercase', letterSpacing:'0.06em', fontWeight:600}}>Salariée</div>
            <div style={{fontSize:14, fontWeight:600, marginTop:2}}>Aminata Diallo</div>
            <div style={{fontSize:12, color:'var(--ink-3)'}}>EMP-0142 · Lead Mobile Engineer · CDI</div>
            <div style={{fontSize:11, color:'var(--ink-3)', marginTop:4}}>CNPS · 110428937H · NIU M0123456789F</div>
          </div>
          <div style={{padding:14, background:'var(--bg-dim)', borderRadius:10}}>
            <div className="muted" style={{fontSize:10, textTransform:'uppercase', letterSpacing:'0.06em', fontWeight:600}}>Versement</div>
            <div style={{fontSize:14, fontWeight:600, marginTop:2}}>{data.channel}</div>
            <div style={{fontSize:12, color:'var(--ink-3)'}}>237 6** ** 34 56</div>
            <div style={{fontSize:11, color:'var(--ink-3)', marginTop:4}}>Versé le {data.date}</div>
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
            <tr><td style={{padding:'8px'}}>Prime de transport</td><td></td><td></td><td style={{padding:'8px', textAlign:'right'}}>40 000</td><td></td></tr>
            <tr><td style={{padding:'8px'}}>Indemnité responsabilité</td><td></td><td></td><td style={{padding:'8px', textAlign:'right'}}>120 000</td><td></td></tr>
            <tr><td style={{padding:'8px'}}>Heures supplémentaires</td><td style={{padding:'8px', textAlign:'right'}}>12h</td><td style={{padding:'8px', textAlign:'right'}}>125%</td><td style={{padding:'8px', textAlign:'right'}}>59 000</td><td></td></tr>
            <tr style={{background:'var(--bg-dim)'}}>
              <td style={{padding:'10px 8px', fontWeight:700}}>Brut imposable</td><td></td><td></td>
              <td style={{padding:'10px 8px', textAlign:'right', fontWeight:700}}>{data.gross}</td><td></td>
            </tr>
            <tr style={{background:'var(--orange-50)'}}>
              <td style={{padding:'8px', fontWeight:600}} colSpan="5">RETENUES SALARIALES</td>
            </tr>
            <tr><td style={{padding:'8px'}}>CNPS · Vieillesse (4,2%)</td><td style={{padding:'8px', textAlign:'right'}}>750 000</td><td style={{padding:'8px', textAlign:'right'}}>4,2%</td><td></td><td style={{padding:'8px', textAlign:'right'}}>31 500</td></tr>
            <tr><td style={{padding:'8px'}}>IRPP (barème)</td><td style={{padding:'8px', textAlign:'right'}}>1 245 000</td><td></td><td></td><td style={{padding:'8px', textAlign:'right'}}>112 400</td></tr>
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
              <td style={{padding:'14px 10px', textAlign:'right', fontFamily:'Inter Tight', fontSize:22, fontWeight:800}}>{data.net}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  );
}

window.EmpPagePayslips = EmpPagePayslips;
