/* global React */

function PageSkills() {
  const { Icons, Avatar, PageHeader } = window;

  const skillCategories = [
    { name:'Technique', count:42, color:'#F97316' },
    { name:'Management', count:14, color:'#FB923C' },
    { name:'Métier', count:28, color:'#FCD34D' },
    { name:'Soft skills', count:18, color:'#34D399' },
    { name:'Langues', count:8, color:'#60A5FA' },
  ];

  const heatmap = [
    { skill:'React Native', cat:'Tech', emp:18, avg:3.8 },
    { skill:'AWS / Cloud', cat:'Tech', emp:14, avg:3.4 },
    { skill:'TypeScript', cat:'Tech', emp:24, avg:4.1 },
    { skill:'Leadership', cat:'Mgmt', emp:22, avg:3.6 },
    { skill:'Vente B2B', cat:'Métier', emp:18, avg:4.2 },
    { skill:'Présentation', cat:'Soft', emp:38, avg:3.7 },
    { skill:'Anglais', cat:'Lang', emp:84, avg:3.2 },
    { skill:'Data Analysis', cat:'Tech', emp:12, avg:3.5 },
  ];

  const experts = [
    { n:'Aminata Diallo', c:'orange', s:'React Native · Architecture mobile', level:5 },
    { n:'Olivier Manga', c:'green', s:'AWS Cloud · DevOps', level:5 },
    { n:'Hervé Tankeu', c:'orange', s:'Vente B2B · Négociation', level:5 },
    { n:'Marc Foga', c:'green', s:'Leadership · Strategy', level:5 },
    { n:'Sarah Nguemo', c:'violet', s:'Design Thinking · UX', level:4 },
  ];

  return (
    <div>
      <PageHeader
        uc="Capital humain"
        title="Compétences"
        subtitle="Référentiel, cartographie et plans de développement"
        actions={
          <>
            <button className="btn btn-secondary"><Icons.download/> Référentiel</button>
            <button className="btn btn-primary"><Icons.plus/> Ajouter une compétence</button>
          </>
        }
      />

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'Compétences', v:'110', sub:'5 catégories · 12 nouvelles 2026', tone:'orange' },
          { l:'Évaluées', v:'2 480', sub:'7,3 par employé en moyenne', tone:'blue' },
          { l:'Compétences rares', v:'18', sub:'< 3 experts internes', tone:'red' },
          { l:'Score moyen', v:'3,6 / 5', sub:'+0,2 vs Q4 2025', tone:'green' },
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

      <div className="row gap-2" style={{marginBottom:16}}>
        <button className="chip orange active">Toutes ({skillCategories.reduce((s,c)=>s+c.count, 0)})</button>
        {skillCategories.map((c,i) => (
          <button key={i} className="chip">{c.name} ({c.count})</button>
        ))}
      </div>

      <div style={{display:'grid', gridTemplateColumns:'2fr 1fr', gap:16, marginBottom:20}}>
        <div className="card">
          <div className="card-head">
            <div className="card-title">Cartographie · Top compétences</div>
            <span className="muted" style={{fontSize:12}}>Maîtrise & couverture par compétence</span>
          </div>
          <div style={{padding:'18px 22px'}}>
            {heatmap.map((s,i) => (
              <div key={i} style={{marginBottom:14}}>
                <div className="row" style={{marginBottom:6}}>
                  <span style={{fontSize:13, fontWeight:600}}>{s.skill}</span>
                  <span className="tag" style={{marginLeft:8}}>{s.cat}</span>
                  <span className="muted" style={{marginLeft:'auto', fontSize:12}}>
                    <b style={{color:'var(--ink-2)'}}>{s.emp}</b> employés · niveau <b style={{color:'var(--orange-600)'}}>{s.avg}/5</b>
                  </span>
                </div>
                <div className="row gap-1">
                  {[1,2,3,4,5].map(lvl => (
                    <div key={lvl} style={{flex:1, height:18, borderRadius:4,
                      background: lvl <= Math.floor(s.avg) ? 'var(--orange-500)' :
                                  lvl === Math.ceil(s.avg) && s.avg % 1 ? 'var(--orange-200)' :
                                  'var(--bg-soft)'}}/>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="card">
          <div className="card-head"><div className="card-title">Experts internes ★★★★★</div></div>
          <div style={{padding:18}}>
            {experts.map((e,i) => (
              <div key={i} className="row gap-3" style={{padding:'10px 0', borderBottom: i < experts.length - 1 ? '1px solid var(--line-soft)' : 'none'}}>
                <Avatar name={e.n} color={e.c}/>
                <div style={{flex:1, minWidth:0}}>
                  <div style={{fontSize:13, fontWeight:600}}>{e.n}</div>
                  <div style={{fontSize:11, color:'var(--ink-3)', whiteSpace:'nowrap', overflow:'hidden', textOverflow:'ellipsis'}}>{e.s}</div>
                </div>
                <div className="row gap-1">
                  {[1,2,3,4,5].map(i => (
                    <Icons.star key={i} size={10} style={{color: i <= e.level ? 'var(--orange-500)' : 'var(--bg-soft)'}}/>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Gap analysis */}
      <div className="card">
        <div className="card-head">
          <div>
            <div className="card-title">Analyse des écarts · Plan de développement</div>
            <div className="muted" style={{fontSize:12, marginTop:2}}>Compétences cibles vs niveau actuel</div>
          </div>
        </div>
        <table className="t">
          <thead><tr><th>Compétence</th><th>Catégorie</th><th>Cible</th><th>Niveau actuel</th><th>Écart</th><th>À former</th><th>Formation proposée</th></tr></thead>
          <tbody>
            {[
              { s:'TypeScript avancé', cat:'Tech', t:4, c:3.2, gap:-0.8, train:18, sug:'TR-008 · TypeScript Mastery' },
              { s:'AWS Cloud', cat:'Tech', t:4, c:3.4, gap:-0.6, train:14, sug:'TR-002 · Architecture Cloud AWS' },
              { s:'Leadership', cat:'Mgmt', t:4, c:3.6, gap:-0.4, train:22, sug:'TR-001 · Leadership & Management' },
              { s:'Anglais B2', cat:'Lang', t:3, c:3.2, gap:0.2, train:0, sug:'—' },
              { s:'Data Analysis', cat:'Tech', t:3.5, c:3.5, gap:0, train:0, sug:'—' },
              { s:'Vente B2B', cat:'Métier', t:4, c:4.2, gap:0.2, train:0, sug:'—' },
            ].map((r,i) => (
              <tr key={i}>
                <td style={{fontSize:13, fontWeight:600}}>{r.s}</td>
                <td><span className="tag">{r.cat}</span></td>
                <td className="tabular">{r.t}</td>
                <td className="tabular">{r.c}</td>
                <td>
                  <span className={"badge " + (r.gap < 0 ? 'red' : r.gap === 0 ? 'gray' : 'green')}>
                    {r.gap < 0 ? r.gap : r.gap === 0 ? '0' : '+' + r.gap}
                  </span>
                </td>
                <td className="tabular">{r.train > 0 ? r.train : '—'}</td>
                <td className="muted" style={{fontSize:12.5}}>{r.sug}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

window.PageSkills = PageSkills;
