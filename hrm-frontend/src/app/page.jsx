'use client';
import { useState } from 'react';
import { Icons } from '../components/Icons';
import Avatar from '../components/Avatar';
import PageHeader from '../components/PageHeader';
import { Donut, Spark, Line } from '../components/Charts';

export default function Dashboard() {
  const kpis = [
    { label: 'Effectif total', value: '342', change: '+12', icon: 'users', color: '#3b82f6' },
    { label: 'Masse salariale', value: '142.8M', change: '+3.2%', icon: 'dollar', color: '#10b981' },
    { label: 'Taux absentéisme', value: '3.4%', change: '-0.8%', icon: 'calendar', color: '#f59e0b' },
    { label: 'Engagement', value: '78', change: '+5pts', icon: 'heart', color: '#8b5cf6' },
  ];

  const todos = [
    { text: 'Valider 3 demandes de congés', urgent: true },
    { text: 'Finaliser entretien annuel — S. Martin', urgent: false },
    { text: 'Renouveler contrat CDD — P. Durand', urgent: true },
    { text: 'Compléter fiche poste Dev Senior', urgent: false },
  ];

  const events = [
    { date: '15 Jan', text: 'Comité RH mensuel', type: 'meeting' },
    { date: '18 Jan', text: 'Fin période essai — L. Bernard', type: 'alert' },
    { date: '20 Jan', text: 'Formation sécurité', type: 'training' },
    { date: '25 Jan', text: 'Paie — clôture saisie', type: 'deadline' },
  ];

  const quickActions = [
    { label: 'Nouvel employé', icon: 'user-plus', color: '#3b82f6' },
    { label: 'Demande congé', icon: 'calendar', color: '#10b981' },
    { label: 'Lancer recrutement', icon: 'briefcase', color: '#8b5cf6' },
    { label: 'Générer rapport', icon: 'file-text', color: '#f59e0b' },
  ];

  const recentActivity = [
    { user: 'Sophie Martin', action: 'a soumis une demande de congé', time: 'Il y a 2h', avatar: 'SM' },
    { user: 'Pierre Durand', action: 'a complété sa formation', time: 'Il y a 4h', avatar: 'PD' },
    { user: 'Marie Leroy', action: 'a signé son contrat', time: 'Hier', avatar: 'ML' },
    { user: 'Lucas Bernard', action: 'a mis à jour son profil', time: 'Hier', avatar: 'LB' },
  ];

  const miniKpis = [
    { label: 'Recrutements en cours', value: '8', spark: [2, 4, 3, 6, 5, 8], color: '#3b82f6' },
    { label: 'Formations ce mois', value: '12', spark: [5, 8, 6, 10, 9, 12], color: '#10b981' },
    { label: 'Entretiens planifiés', value: '15', spark: [8, 10, 12, 9, 14, 15], color: '#f59e0b' },
    { label: 'Tickets RH ouverts', value: '6', spark: [10, 8, 9, 7, 8, 6], color: '#ef4444' },
  ];

  const headcountData = [
    { month: 'Juil', value: 310 },
    { month: 'Août', value: 315 },
    { month: 'Sep', value: 320 },
    { month: 'Oct', value: 328 },
    { month: 'Nov', value: 335 },
    { month: 'Déc', value: 342 },
  ];

  const deptData = [
    { label: 'Tech', value: 35, color: '#3b82f6' },
    { label: 'Commercial', value: 25, color: '#10b981' },
    { label: 'RH', value: 15, color: '#f59e0b' },
    { label: 'Finance', value: 15, color: '#8b5cf6' },
    { label: 'Autre', value: 10, color: '#6b7280' },
  ];

  return (
    <div className="page-dashboard">
      <PageHeader
        title="Tableau de bord"
        subtitle="Vue d'ensemble de vos indicateurs RH"
        icon="home"
      />

      {/* KPI Hero Row */}
      <div className="kpi-hero-row">
        {kpis.map((kpi, i) => (
          <div key={i} className="kpi-hero-card">
            <div className="kpi-hero-icon" style={{ background: kpi.color + '18', color: kpi.color }}>
              <Icons name={kpi.icon} size={24} />
            </div>
            <div className="kpi-hero-content">
              <div className="kpi-hero-value">{kpi.value}</div>
              <div className="kpi-hero-label">{kpi.label}</div>
            </div>
            <span className={`kpi-hero-change ${kpi.change.startsWith('+') ? 'positive' : 'negative'}`}>
              {kpi.change}
            </span>
          </div>
        ))}
      </div>

      {/* Charts Row */}
      <div className="dashboard-grid-2">
        <div className="card">
          <h3 className="card-title">Évolution de l'effectif</h3>
          <Line data={headcountData} height={200} color="#3b82f6" />
        </div>
        <div className="card">
          <h3 className="card-title">Répartition par département</h3>
          <Donut data={deptData} size={200} />
        </div>
      </div>

      {/* Todo + Events */}
      <div className="dashboard-grid-2">
        <div className="card">
          <h3 className="card-title">
            <Icons name="check-circle" size={18} /> À faire
          </h3>
          <ul className="todo-list">
            {todos.map((todo, i) => (
              <li key={i} className={`todo-item ${todo.urgent ? 'urgent' : ''}`}>
                <span className="todo-dot" />
                {todo.text}
                {todo.urgent && <span className="badge badge-red">Urgent</span>}
              </li>
            ))}
          </ul>
        </div>
        <div className="card">
          <h3 className="card-title">
            <Icons name="calendar" size={18} /> Événements à venir
          </h3>
          <ul className="event-list">
            {events.map((ev, i) => (
              <li key={i} className="event-item">
                <span className="event-date">{ev.date}</span>
                <span className="event-text">{ev.text}</span>
                <span className={`badge badge-${ev.type}`}>{ev.type}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>

      {/* Quick Actions + Recent Activity */}
      <div className="dashboard-grid-2">
        <div className="card">
          <h3 className="card-title">Actions rapides</h3>
          <div className="quick-actions-grid">
            {quickActions.map((action, i) => (
              <button key={i} className="quick-action-btn">
                <Icons name={action.icon} size={20} style={{ color: action.color }} />
                <span>{action.label}</span>
              </button>
            ))}
          </div>
        </div>
        <div className="card">
          <h3 className="card-title">Activité récente</h3>
          <ul className="activity-list">
            {recentActivity.map((item, i) => (
              <li key={i} className="activity-item">
                <Avatar initials={item.avatar} size={32} />
                <div className="activity-content">
                  <span className="activity-user">{item.user}</span>{' '}
                  <span className="activity-action">{item.action}</span>
                  <div className="activity-time">{item.time}</div>
                </div>
              </li>
            ))}
          </ul>
        </div>
      </div>

      {/* Mini KPI Grid */}
      <div className="kpi-mini-grid">
        {miniKpis.map((kpi, i) => (
          <div key={i} className="kpi-mini-card">
            <div className="kpi-mini-header">
              <span className="kpi-mini-label">{kpi.label}</span>
              <span className="kpi-mini-value" style={{ color: kpi.color }}>{kpi.value}</span>
            </div>
            <Spark data={kpi.spark} color={kpi.color} height={32} />
          </div>
        ))}
      </div>
    </div>
  );
}
