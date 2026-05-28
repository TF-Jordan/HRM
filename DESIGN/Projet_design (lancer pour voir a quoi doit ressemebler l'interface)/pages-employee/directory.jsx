/* global React */

function EmpPageDirectory() {
  const { Icons, Avatar, PageHeader } = window;

  const people = [
    { n:'Marc Foga', r:'Directeur Technique', dept:'Direction', site:'Yaoundé HQ', email:'m.foga@rt-comops.com', phone:'+237 6 78 90 12 34', color:'green', manager:true, fav:true },
    { n:'Faïsal Sab', r:'DRH · CEO', dept:'Direction', site:'Yaoundé HQ', email:'f.sab@rt-comops.com', phone:'+237 6 99 12 34 56', color:'orange', fav:true },
    { n:'Sarah Nguemo', r:'UX Designer', dept:'Engineering', site:'Yaoundé HQ', email:'s.nguemo@rt-comops.com', phone:'+237 6 78 23 45 67', color:'violet', team:true },
    { n:'Olivier Manga', r:'DevOps Engineer', dept:'Engineering', site:'Yaoundé HQ', email:'o.manga@rt-comops.com', phone:'+237 6 56 78 90 12', color:'green', team:true },
    { n:'Karine Djoumessi', r:'Field Operations', dept:'Opérations', site:'Bafoussam', email:'k.djoumessi@rt-comops.com', phone:'+237 6 78 56 12 34', color:'blue', team:true },
    { n:'Hervé Tankeu', r:'Sales Manager', dept:'Commercial', site:'Douala', email:'h.tankeu@rt-comops.com', phone:'+237 6 99 78 45 12', color:'orange', team:true },
    { n:'Joseph Mbarga', r:'Business Developer', dept:'Commercial', site:'Douala', email:'j.mbarga@rt-comops.com', phone:'+237 6 88 12 34 56', color:'blue' },
    { n:'Pierre Kouam', r:'Comptable', dept:'Finance & RH', site:'Yaoundé HQ', email:'p.kouam@rt-comops.com', phone:'+237 6 77 23 45 67', color:'amber' },
    { n:'Estelle Bilong', r:'Chargée de paie', dept:'Finance & RH', site:'Yaoundé HQ', email:'e.bilong@rt-comops.com', phone:'+237 6 99 34 56 78', color:'amber' },
    { n:'Léa Ondoa', r:'Designer UI (Stage)', dept:'Engineering', site:'Yaoundé HQ', email:'l.ondoa@rt-comops.com', phone:'+237 6 55 12 34 56', color:'teal', new:true },
    { n:'Yannick Etoa', r:'Talent Acquisition', dept:'Finance & RH', site:'Douala', email:'y.etoa@rt-comops.com', phone:'+237 6 89 23 45 67', color:'orange' },
    { n:'Diane Tsoumou', r:'Dir. Opérations', dept:'Direction', site:'Yaoundé HQ', email:'d.tsoumou@rt-comops.com', phone:'+237 6 76 34 56 78', color:'violet' },
  ];

  return (
    <div>
      <PageHeader
        uc="Entreprise"
        title="Annuaire"
        subtitle="Retrouvez vos collègues, leurs contacts et leurs rôles"
        actions={
          <div className="row gap-2">
            <button className="chip orange active">Liste</button>
            <button className="chip">Trombinoscope</button>
            <button className="chip">Organigramme</button>
          </div>
        }
      />

      <div className="card card-pad" style={{marginBottom:16}}>
        <div className="row gap-3" style={{flexWrap:'wrap'}}>
          <div className="row gap-2" style={{flex:1, minWidth:280, padding:'8px 14px', background:'#fff', border:'1px solid var(--line)', borderRadius:12}}>
            <Icons.search size={15} style={{color:'var(--ink-3)'}}/>
            <input style={{border:'none', outline:'none', flex:1, fontSize:13.5}} placeholder="Nom, poste, département…"/>
          </div>
          <button className="chip orange active">Tous (342)</button>
          <button className="chip">Mon équipe (4)</button>
          <button className="chip">Engineering (124)</button>
          <button className="chip">Commercial (52)</button>
          <button className="chip">Direction (24)</button>
          <button className="chip"><Icons.star size={11}/> Favoris (5)</button>
        </div>
      </div>

      {/* My team highlight */}
      <div className="section-title">Mon équipe directe</div>
      <div className="grid-4" style={{gap:14, marginBottom:24}}>
        {/* Manager card */}
        <div className="card card-pad" style={{
          background:'linear-gradient(135deg, #FFFAF2 0%, #fff 100%)',
          border:'1px solid var(--orange-200)', textAlign:'center'
        }}>
          <Avatar name="Marc Foga" color="green" size="xl" style={{margin:'0 auto 12px'}}/>
          <div style={{fontSize:11, color:'var(--orange-700)', fontWeight:700, textTransform:'uppercase', letterSpacing:'0.06em', marginBottom:4}}>Mon manager</div>
          <div style={{fontSize:15, fontWeight:700}}>Marc Foga</div>
          <div style={{fontSize:12, color:'var(--ink-3)'}}>Directeur Technique</div>
          <div className="row gap-2" style={{justifyContent:'center', marginTop:14}}>
            <button className="icon-btn" style={{width:30, height:30}}><Icons.mail size={13}/></button>
            <button className="icon-btn" style={{width:30, height:30}}><Icons.phone size={13}/></button>
            <button className="icon-btn" style={{width:30, height:30}}><Icons.send size={13}/></button>
          </div>
        </div>

        {/* Team members */}
        {people.filter(p => p.team).map((p,i) => (
          <div key={i} className="card card-pad" style={{textAlign:'center'}}>
            <Avatar name={p.n} color={p.color} size="xl" style={{margin:'0 auto 12px'}}/>
            <div style={{fontSize:11, color:'var(--ink-3)', fontWeight:600, textTransform:'uppercase', letterSpacing:'0.06em', marginBottom:4}}>Collègue direct</div>
            <div style={{fontSize:15, fontWeight:700}}>{p.n}</div>
            <div style={{fontSize:12, color:'var(--ink-3)'}}>{p.r}</div>
            <div className="row gap-2" style={{justifyContent:'center', marginTop:14}}>
              <button className="icon-btn" style={{width:30, height:30}}><Icons.mail size={13}/></button>
              <button className="icon-btn" style={{width:30, height:30}}><Icons.phone size={13}/></button>
              <button className="icon-btn" style={{width:30, height:30}}><Icons.send size={13}/></button>
            </div>
          </div>
        ))}
      </div>

      {/* All people */}
      <div className="section-title">Tous les collaborateurs</div>
      <div className="card">
        <table className="t">
          <thead><tr><th>Personne</th><th>Département</th><th>Site</th><th>Contact</th><th></th></tr></thead>
          <tbody>
            {people.map((p,i) => (
              <tr key={i}>
                <td>
                  <div className="row gap-3">
                    <Avatar name={p.n} color={p.color}/>
                    <div>
                      <div className="row gap-2">
                        <span style={{fontSize:13.5, fontWeight:600}}>{p.n}</span>
                        {p.fav && <Icons.star size={11} style={{color:'var(--orange-500)'}}/>}
                        {p.new && <span className="badge teal" style={{fontSize:9}}>Nouveau</span>}
                        {p.manager && <span className="badge orange" style={{fontSize:9}}>Mon manager</span>}
                      </div>
                      <div style={{fontSize:11.5, color:'var(--ink-3)'}}>{p.r}</div>
                    </div>
                  </div>
                </td>
                <td><span className="tag">{p.dept}</span></td>
                <td className="muted">{p.site}</td>
                <td>
                  <div style={{fontSize:12}}>{p.email}</div>
                  <div style={{fontSize:11, color:'var(--ink-3)'}} className="mono">{p.phone}</div>
                </td>
                <td>
                  <div className="row gap-2">
                    <button className="icon-btn" style={{width:28, height:28}}><Icons.mail size={12}/></button>
                    <button className="icon-btn" style={{width:28, height:28}}><Icons.send size={12}/></button>
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
window.EmpPageDirectory = EmpPageDirectory;
