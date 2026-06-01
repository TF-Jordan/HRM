/* global React */

function EmpPageDocuments() {
  const { Icons, PageHeader } = window;

  const docs = [
    { cat:'Contrat', items:[
      { n:'Contrat CDI · signé', d:'14 fév 2022', size:'2,4 MB', icon:'contract', tone:'green' },
      { n:'Avenant · Promotion Lead', d:'14 jan 2026', size:'620 KB', icon:'contract', tone:'orange' },
      { n:'Avenant · Augmentation', d:'12 jul 2025', size:'480 KB', icon:'contract', tone:'orange' },
    ]},
    { cat:'Bulletins de paie', items:[
      { n:'Bulletin avril 2026', d:'28 avr 2026', size:'180 KB', icon:'payroll', tone:'amber' },
      { n:'Bulletin mars 2026', d:'29 mar 2026', size:'180 KB', icon:'payroll', tone:'amber' },
      { n:'Bulletin février 2026', d:'27 fév 2026', size:'180 KB', icon:'payroll', tone:'amber' },
      { n:'Tous les bulletins 2025 (ZIP)', d:'12 mois', size:'2,1 MB', icon:'doc', tone:'gray' },
    ]},
    { cat:'Attestations & certificats', items:[
      { n:'Attestation de salaire (banque)', d:'Générée le 02 mai', size:'120 KB', icon:'badge', tone:'blue' },
      { n:'Attestation de travail', d:'Générée le 14 jan', size:'95 KB', icon:'badge', tone:'blue' },
      { n:'Certificat médical visite annuelle', d:'14 avr 2026', size:'320 KB', icon:'medical', tone:'red' },
      { n:'Certificat AWS Solutions Architect', d:'23 sept 2023', size:'1,2 MB', icon:'star', tone:'orange' },
      { n:'Certificat Leadership niveau 1', d:'05 fév 2025', size:'1,1 MB', icon:'star', tone:'violet' },
    ]},
    { cat:'Personnel', items:[
      { n:'CNI · scan', d:'Téléversé le 14 fév 2022', size:'1,1 MB', icon:'badge', tone:'blue' },
      { n:'CV · janvier 2026', d:'12 jan 2026', size:'480 KB', icon:'doc', tone:'gray' },
      { n:'Photo profil HD', d:'04 mai 2026', size:'2,8 MB', icon:'doc', tone:'gray' },
    ]},
  ];

  return (
    <div>
      <PageHeader
        uc="Espace personnel"
        title="Mes documents"
        subtitle="Contrats, bulletins, attestations et certificats — accès permanent"
        actions={
          <>
            <button className="btn btn-secondary"><Icons.upload/> Téléverser un document</button>
            <button className="btn btn-primary"><Icons.doc size={14}/> Demander une attestation</button>
          </>
        }
      />

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'Documents total', v:'24', sub:'42,8 MB', tone:'orange', icon:'doc' },
          { l:'Bulletins archivés', v:'48', sub:'4 ans d\'historique', tone:'amber', icon:'payroll' },
          { l:'Certifications', v:'5', sub:'à jour', tone:'green', icon:'star' },
          { l:'Attestations émises', v:'8', sub:'depuis 2022', tone:'blue', icon:'badge' },
        ].map((k,i) => {
          const I = Icons[k.icon];
          return (
            <div key={i} className={"kpi " + k.tone}>
              <div className="kpi-icon"><I size={18}/></div>
              <div className="kpi-label">{k.l}</div>
              <div className="kpi-value tabular">{k.v}</div>
              <div className="kpi-foot">{k.sub}</div>
            </div>
          );
        })}
      </div>

      <div className="card card-pad-lg" style={{
        background:'linear-gradient(90deg, var(--orange-50) 0%, #FFFAEC 100%)',
        border:'1px solid var(--orange-200)', marginBottom:20
      }}>
        <div className="row gap-3">
          <div className="icon-tile orange" style={{width:42, height:42}}><Icons.doc size={20}/></div>
          <div style={{flex:1}}>
            <div style={{fontSize:14, fontWeight:700}}>Besoin d'une attestation ? Générez-la en 30 secondes</div>
            <div style={{fontSize:12.5, color:'var(--ink-2)', marginTop:2}}>
              Attestation employeur, salaire, présence, banque… document officiel signé électroniquement.
            </div>
          </div>
          <button className="btn btn-dark">Générer une attestation</button>
        </div>
      </div>

      {/* Documents by category */}
      {docs.map((section, i) => (
        <div key={i} style={{marginBottom:22}}>
          <div className="section-title">{section.cat} <span style={{color:'var(--ink-3)', textTransform:'none', fontWeight:500, letterSpacing:0, marginLeft:4}}>({section.items.length})</span></div>
          <div className="card">
            {section.items.map((doc, j) => {
              const I = Icons[doc.icon];
              return (
                <div key={j} className="lrow">
                  <div className={"icon-tile " + doc.tone}><I size={16}/></div>
                  <div style={{flex:1, minWidth:0}}>
                    <div style={{fontSize:13.5, fontWeight:600}}>{doc.n}</div>
                    <div style={{fontSize:11.5, color:'var(--ink-3)'}}>{doc.d} · {doc.size}</div>
                  </div>
                  <div className="row gap-2">
                    <button className="btn btn-ghost btn-sm"><Icons.send size={12}/> Email</button>
                    <button className="btn btn-secondary btn-sm"><Icons.download size={12}/> Télécharger</button>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      ))}
    </div>
  );
}
window.EmpPageDocuments = EmpPageDocuments;
