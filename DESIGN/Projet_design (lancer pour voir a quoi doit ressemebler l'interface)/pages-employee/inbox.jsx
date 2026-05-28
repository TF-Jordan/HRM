/* global React */
const { useState: useS_einbox } = React;

function EmpPageInbox() {
  const { Icons, Avatar, PageHeader } = window;
  const [filter, setFilter] = useS_einbox('all');

  const notifications = [
    { id:1, type:'leave', icon:'leave', tone:'amber', title:'Votre congé du 18-22 mai est en attente d\'approbation', desc:'Marc Foga doit valider votre demande', time:'il y a 2j', unread:true, from:'Système RH', actions:['Voir la demande'] },
    { id:2, type:'review', icon:'review', tone:'violet', title:'Entretien d\'évaluation Q1 planifié', desc:'Vendredi 16 mai à 14h00 en salle Tokyo avec Marc Foga', time:'il y a 1j', unread:true, from:'Marc Foga', actions:['Ajouter à mon calendrier','Préparer'] },
    { id:3, type:'training', icon:'training', tone:'blue', title:'Inscription à AWS Cloud Architect confirmée', desc:'Démarrage lundi 20 mai · 40h · hybride', time:'il y a 1j', unread:true, from:'Système formation', actions:['Voir détails'] },
    { id:4, type:'expense', icon:'expense', tone:'green', title:'Votre note de frais NF-2026-0238 a été remboursée', desc:'28 400 XAF crédités sur MTN MoMo', time:'il y a 4j', unread:true, from:'Système paie', actions:['Voir bulletin'] },
    { id:5, type:'announce', icon:'info', tone:'orange', title:'📣 Nouveau plan de formation 2026', desc:'24 formations disponibles dont 8 en ligne. Découvrez les nouveautés.', time:'il y a 5j', unread:false, from:'DRH · Faïsal Sab', actions:['Voir le catalogue'] },
    { id:6, type:'team', icon:'users', tone:'teal', title:'Léa Ondoa rejoint votre équipe le 22 mai', desc:'Designer UI, sous votre management direct', time:'il y a 1 sem.', unread:false, from:'Yannick Etoa', actions:['Préparer onboarding'] },
    { id:7, type:'birthday', icon:'star', tone:'violet', title:'🎂 Aujourd\'hui, Sarah Nguemo fête son anniversaire', desc:'Pensez à lui souhaiter !', time:'il y a 6j', unread:false, from:'Système RH' },
    { id:8, type:'time', icon:'time', tone:'red', title:'Votre feuille de temps de la semaine 19 doit être soumise', desc:'Échéance: vendredi 17 mai à 18h', time:'il y a 1 sem.', unread:false, from:'Système temps' },
    { id:9, type:'medical', icon:'medical', tone:'red', title:'Visite médicale annuelle planifiée pour le 22 mai', desc:'Dr. Nkoa · 9h00 · clinique Saint-Vincent', time:'il y a 2 sem.', unread:false, from:'Médecine du travail' },
    { id:10, type:'payroll', icon:'payroll', tone:'amber', title:'Votre bulletin de paie d\'avril est disponible', desc:'1 066 960 XAF versés le 28 avril sur MTN MoMo', time:'il y a 2 sem.', unread:false, from:'Système paie', actions:['Télécharger PDF'] },
  ];

  const filters = [
    { id:'all', label:'Tout', count:notifications.length },
    { id:'unread', label:'Non lu', count:notifications.filter(n => n.unread).length },
    { id:'leave', label:'Congés' },
    { id:'review', label:'Évaluations' },
    { id:'payroll', label:'Paie' },
    { id:'announce', label:'Annonces' },
  ];

  const filtered = filter === 'all' ? notifications :
                   filter === 'unread' ? notifications.filter(n => n.unread) :
                   notifications.filter(n => n.type === filter);

  return (
    <div>
      <PageHeader
        uc="Entreprise"
        title="Notifications"
        subtitle="Toutes vos notifications, demandes et annonces en un seul endroit"
        actions={
          <>
            <button className="btn btn-secondary"><Icons.check size={14}/> Tout marquer comme lu</button>
            <button className="btn btn-secondary"><Icons.settings size={14}/> Préférences</button>
          </>
        }
      />

      <div className="grid-4" style={{marginBottom:20}}>
        {[
          { l:'Non lues', v:'4', sub:'à traiter', tone:'orange' },
          { l:'Actions requises', v:'2', sub:'avant cette semaine', tone:'red' },
          { l:'Cette semaine', v:'12', sub:'reçues', tone:'blue' },
          { l:'Toutes', v:'128', sub:'30 derniers jours', tone:'gray' },
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

      <div className="row gap-2" style={{marginBottom:16, flexWrap:'wrap'}}>
        {filters.map(f => (
          <button key={f.id} onClick={() => setFilter(f.id)} className={"chip" + (filter === f.id ? ' orange active' : '')}>
            {f.label}{f.count !== undefined && ` (${f.count})`}
          </button>
        ))}
      </div>

      <div className="card">
        {filtered.map((n) => {
          const I = Icons[n.icon];
          return (
            <div key={n.id} className="lrow" style={{
              padding:'18px 22px', gap:14,
              background: n.unread ? 'linear-gradient(90deg, rgba(249,115,22,0.04) 0%, transparent 60%)' : 'transparent',
              borderLeft: n.unread ? '3px solid var(--orange-500)' : '3px solid transparent'
            }}>
              <div className={"icon-tile " + n.tone} style={{width:42, height:42, flexShrink:0}}><I size={18}/></div>
              <div style={{flex:1, minWidth:0}}>
                <div className="row gap-2" style={{marginBottom:4}}>
                  {n.unread && <span style={{width:7, height:7, background:'var(--orange-500)', borderRadius:'50%'}}/>}
                  <span style={{fontSize:14, fontWeight: n.unread ? 700 : 600}}>{n.title}</span>
                </div>
                <div style={{fontSize:12.5, color:'var(--ink-3)'}}>{n.desc}</div>
                <div className="row gap-3" style={{marginTop:8, fontSize:11.5, color:'var(--ink-4)'}}>
                  <span>De · <b style={{color:'var(--ink-3)'}}>{n.from}</b></span>
                  <span>{n.time}</span>
                </div>
              </div>
              <div className="col gap-2" style={{alignItems:'flex-end'}}>
                {n.actions && n.actions.map((a, i) => (
                  <button key={i} className={i === 0 ? "btn btn-dark btn-sm" : "btn btn-secondary btn-sm"}>{a}</button>
                ))}
                {!n.actions && <button className="icon-btn" style={{width:28, height:28}}><Icons.more size={12}/></button>}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
window.EmpPageInbox = EmpPageInbox;
