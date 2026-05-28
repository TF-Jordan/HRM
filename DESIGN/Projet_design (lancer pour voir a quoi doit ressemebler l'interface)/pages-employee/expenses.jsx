/* global React */
const { useState: useS_eex } = React;

function EmpPageExpenses() {
  const { Icons, PageHeader, Bars } = window;
  const [mode, setMode] = useS_eex('list');

  if (mode === 'new') return <ExpenseSubmissionForm onBack={() => setMode('list')}/>;

  return (
    <div>
      <PageHeader
        uc="Rémunération"
        title="Mes notes de frais"
        subtitle="Saisie, suivi de remboursement et historique"
        actions={
          <>
            <button className="btn btn-secondary"><Icons.download/> Export</button>
            <button className="btn btn-primary" onClick={() => setMode('new')}>
              <Icons.plus/> Nouvelle note
            </button>
          </>
        }
      />

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'En attente', v:'1', sub:'350 000 XAF', tone:'amber' },
          { l:'Approuvées', v:'2', sub:'195 700 XAF', tone:'blue' },
          { l:'Remboursées (mois)', v:'8', sub:'524 200 XAF', tone:'green' },
          { l:'Délai moyen', v:'2,8 j', sub:'mes 90 derniers jours', tone:'orange' },
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
          <div className="card-title">Mes notes de frais</div>
          <div className="row gap-2">
            <button className="chip orange active">Toutes</button>
            <button className="chip">En attente (1)</button>
            <button className="chip">Remboursées</button>
          </div>
        </div>
        <table className="t">
          <thead><tr><th>Réf.</th><th>Objet</th><th>Date</th><th>Lignes</th><th>Montant (XAF)</th><th>Statut</th><th>Étape</th><th></th></tr></thead>
          <tbody>
            {[
              { id:'NF-2026-0242', title:'Conférence Mobile Summit · Lagos', date:'09 mai 2026', lines:11, total:'546 800', status:'pending', step:'Manager direct' },
              { id:'NF-2026-0238', title:'Repas client · Le Méridien', date:'02 mai 2026', lines:1, total:'28 400', status:'approved', step:'Paiement en cours' },
              { id:'NF-2026-0231', title:'Taxi aéroport (×3)', date:'24 avr 2026', lines:3, total:'18 500', status:'paid', step:'Reçu sur MoMo' },
              { id:'NF-2026-0225', title:'Achat matériel développement', date:'18 avr 2026', lines:4, total:'124 600', status:'paid', step:'Reçu sur MoMo' },
              { id:'NF-2026-0214', title:'Déjeuner équipe (10 pers.)', date:'12 avr 2026', lines:1, total:'94 800', status:'paid', step:'Reçu sur MoMo' },
              { id:'NF-2026-0198', title:'Vol Yaoundé → Douala', date:'06 avr 2026', lines:2, total:'85 000', status:'paid', step:'Reçu sur MoMo' },
            ].map(r => (
              <tr key={r.id}>
                <td className="mono" style={{fontSize:11, color:'var(--ink-3)'}}>{r.id}</td>
                <td>
                  <div style={{fontSize:13, fontWeight:600}}>{r.title}</div>
                  <div style={{fontSize:11, color:'var(--ink-3)'}}>{r.lines} ligne{r.lines > 1 ? 's' : ''}</div>
                </td>
                <td className="muted">{r.date}</td>
                <td className="tabular">{r.lines}</td>
                <td className="tabular" style={{fontWeight:700}}>{r.total}</td>
                <td>
                  {r.status === 'pending' && <span className="badge amber">En attente</span>}
                  {r.status === 'approved' && <span className="badge blue">Approuvée</span>}
                  {r.status === 'paid' && <span className="badge green">Remboursée</span>}
                </td>
                <td className="muted" style={{fontSize:12}}>{r.step}</td>
                <td><button className="icon-btn" style={{width:28, height:28}}><Icons.chevR size={12}/></button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function ExpenseSubmissionForm({ onBack }) {
  const { Icons } = window;
  const [lines, setLines] = useS_eex([
    { date:'12 mai 2026', cat:'Transport', desc:'Vol Yaoundé → Lagos', amount:185000, hasReceipt:true },
    { date:'12 mai 2026', cat:'Hébergement', desc:'Hôtel Eko Atlantic · 3 nuits', amount:264000, hasReceipt:true },
    { date:'13 mai 2026', cat:'Repas', desc:'Repas avec clients', amount:42000, hasReceipt:true },
    { date:'14 mai 2026', cat:'Transport', desc:'Taxi Lagos centre', amount:18500, hasReceipt:false },
  ]);

  const total = lines.reduce((s,l) => s + l.amount, 0);

  return (
    <div>
      <div className="row gap-2" style={{marginBottom:14}}>
        <button className="btn btn-ghost btn-sm" onClick={onBack}>
          <Icons.chevR size={14} style={{transform:'rotate(180deg)'}}/> Retour
        </button>
        <span className="muted" style={{fontSize:12}}>Mes notes de frais / Nouvelle note</span>
      </div>

      <div className="h-display" style={{fontSize:28, marginBottom:6}}>Nouvelle note de frais</div>
      <div style={{fontSize:13.5, color:'var(--ink-3)', marginBottom:24}}>Ajoutez vos dépenses et joignez les justificatifs.</div>

      <div style={{display:'grid', gridTemplateColumns:'2fr 1fr', gap:20}}>
        <div className="card card-pad-lg">
          <div className="grid-2" style={{gap:14, marginBottom:18}}>
            <div className="field">
              <label className="label">Objet de la note</label>
              <input className="input" defaultValue="Conférence Mobile Summit · Lagos"/>
            </div>
            <div className="field">
              <label className="label">Lié à un ordre de mission ?</label>
              <select className="select">
                <option>MO-2026-0086 · Lagos (Ng) · 22-25 mai</option>
                <option>Aucun</option>
              </select>
            </div>
          </div>

          <div className="row" style={{marginBottom:14}}>
            <div style={{fontSize:13, fontWeight:700}}>Lignes de dépenses</div>
            <button className="btn btn-secondary btn-sm" style={{marginLeft:'auto'}}><Icons.plus size={12}/> Ajouter une ligne</button>
          </div>

          <table className="t" style={{marginLeft:-22, marginRight:-22, width:'calc(100% + 44px)'}}>
            <thead>
              <tr><th>Date</th><th>Catégorie</th><th>Description</th><th>Montant (XAF)</th><th>Justif.</th><th></th></tr>
            </thead>
            <tbody>
              {lines.map((l, i) => (
                <tr key={i}>
                  <td className="muted">{l.date}</td>
                  <td><span className={"badge " + (l.cat === 'Transport' ? 'blue' : l.cat === 'Repas' ? 'orange' : l.cat === 'Hébergement' ? 'violet' : 'gray')}>{l.cat}</span></td>
                  <td style={{fontSize:13}}>{l.desc}</td>
                  <td className="tabular" style={{fontWeight:600}}>{l.amount.toLocaleString('fr-FR').replace(/,/g, ' ')}</td>
                  <td>
                    {l.hasReceipt ? <span className="badge green">Joint</span> : <button className="btn btn-secondary btn-sm"><Icons.upload size={12}/></button>}
                  </td>
                  <td>
                    <div className="row gap-2">
                      <button className="icon-btn" style={{width:28, height:28}}><Icons.edit size={12}/></button>
                      <button className="icon-btn" style={{width:28, height:28}}><Icons.trash size={12}/></button>
                    </div>
                  </td>
                </tr>
              ))}
              <tr style={{background:'var(--bg-dim)'}}>
                <td colSpan="3" style={{fontWeight:700}}>Total à rembourser</td>
                <td className="tabular" style={{fontWeight:800, fontFamily:'Inter Tight', fontSize:18}}>{total.toLocaleString('fr-FR').replace(/,/g, ' ')}</td>
                <td colSpan="2"></td>
              </tr>
            </tbody>
          </table>

          <div style={{marginTop:18, padding:18, border:'2px dashed var(--line)', borderRadius:12, textAlign:'center', background:'var(--bg-dim)'}}>
            <Icons.upload size={28} style={{color:'var(--ink-4)', margin:'0 auto 8px', display:'block'}}/>
            <div style={{fontSize:13, fontWeight:600, marginBottom:4}}>Déposer plusieurs justificatifs</div>
            <div style={{fontSize:11, color:'var(--ink-3)'}}>L'OCR détecte automatiquement montant et date</div>
          </div>
        </div>

        <div className="col gap-3">
          <div className="card card-pad" style={{
            background:'linear-gradient(135deg, var(--orange-50) 0%, #fff 100%)',
            border:'1px solid var(--orange-200)'
          }}>
            <div style={{fontSize:11, textTransform:'uppercase', letterSpacing:'0.05em', fontWeight:600, color:'var(--ink-3)'}}>Récapitulatif</div>
            <div style={{fontFamily:'Inter Tight', fontSize:34, fontWeight:800, letterSpacing:'-0.025em', marginTop:4, color:'var(--orange-700)'}} className="tabular">
              {total.toLocaleString('fr-FR').replace(/,/g, ' ')}
            </div>
            <div style={{fontSize:12, color:'var(--ink-2)'}}>XAF · à rembourser</div>

            <div className="divider"/>

            <div className="col gap-2">
              <div className="row" style={{fontSize:12.5}}>
                <span className="muted">Transport</span>
                <span className="tabular" style={{marginLeft:'auto', fontWeight:600}}>203 500</span>
              </div>
              <div className="row" style={{fontSize:12.5}}>
                <span className="muted">Hébergement</span>
                <span className="tabular" style={{marginLeft:'auto', fontWeight:600}}>264 000</span>
              </div>
              <div className="row" style={{fontSize:12.5}}>
                <span className="muted">Repas</span>
                <span className="tabular" style={{marginLeft:'auto', fontWeight:600}}>42 000</span>
              </div>
            </div>
          </div>

          <div className="card card-pad">
            <div className="card-title" style={{fontSize:14, marginBottom:12}}>Versement</div>
            <div className="row gap-2" style={{padding:'8px 10px', background:'var(--bg-dim)', borderRadius:10}}>
              <div className="icon-tile amber" style={{width:30, height:30}}><span style={{fontWeight:800, fontSize:12}}>M</span></div>
              <div>
                <div style={{fontSize:13, fontWeight:600}}>MTN MoMo</div>
                <div className="mono" style={{fontSize:11, color:'var(--ink-3)'}}>+237 6 78 ** ** 56</div>
              </div>
            </div>
            <div style={{fontSize:11, color:'var(--ink-3)', marginTop:10}}>Délai estimé · 2-3 jours après approbation</div>
          </div>

          <div className="row gap-2">
            <button className="btn btn-secondary" style={{flex:1, justifyContent:'center'}} onClick={onBack}>Brouillon</button>
            <button className="btn btn-primary" style={{flex:1, justifyContent:'center'}}>
              <Icons.send size={14}/> Soumettre
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

window.EmpPageExpenses = EmpPageExpenses;
