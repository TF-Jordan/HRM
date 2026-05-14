'use client';
import { useState } from 'react';
import { Icons } from '../../components/Icons';
import Avatar from '../../components/Avatar';
import PageHeader from '../../components/PageHeader';

export default function Recruitment() {
  const kpis = [
    { label: 'Postes ouverts', value: '12', icon: 'briefcase', color: '#3b82f6' },
    { label: 'Candidatures reçues', value: '184', icon: 'users', color: '#10b981' },
    { label: 'Entretiens planifiés', value: '15', icon: 'calendar', color: '#f59e0b' },
    { label: 'Time to hire moyen', value: '28j', icon: 'clock', color: '#8b5cf6' },
  ];

  const pipeline = {
    columns: [
      {
        id: 'new',
        title: 'Nouveau',
        color: '#6b7280',
        candidates: [
          { name: 'Alice Dupont', role: 'Développeur React', date: '12 Jan', avatar: 'AD', source: 'LinkedIn' },
          { name: 'Bob Martin', role: 'DevOps Engineer', date: '11 Jan', avatar: 'BM', source: 'Site carrière' },
          { name: 'Clara Petit', role: 'Designer UX', date: '10 Jan', avatar: 'CP', source: 'Cooptation' },
        ],
      },
      {
        id: 'preselected',
        title: 'Présélectionné',
        color: '#3b82f6',
        candidates: [
          { name: 'David Roux', role: 'Développeur React', date: '8 Jan', avatar: 'DR', source: 'LinkedIn' },
          { name: 'Eva Leroy', role: 'Chef de projet', date: '7 Jan', avatar: 'EL', source: 'Indeed' },
        ],
      },
      {
        id: 'interview',
        title: 'Entretien',
        color: '#f59e0b',
        candidates: [
          { name: 'François Girard', role: 'DevOps Engineer', date: '5 Jan', avatar: 'FG', source: 'LinkedIn' },
          { name: 'Gabrielle Moreau', role: 'Data Analyst', date: '4 Jan', avatar: 'GM', source: 'Site carrière' },
          { name: 'Hugo Bernard', role: 'Développeur React', date: '3 Jan', avatar: 'HB', source: 'Cooptation' },
        ],
      },
      {
        id: 'offer',
        title: 'Offre',
        color: '#8b5cf6',
        candidates: [
          { name: 'Inès Fournier', role: 'Designer UX', date: '2 Jan', avatar: 'IF', source: 'LinkedIn' },
        ],
      },
      {
        id: 'hired',
        title: 'Embauché',
        color: '#10b981',
        candidates: [
          { name: 'Jules Lambert', role: 'Data Analyst', date: '1 Jan', avatar: 'JL', source: 'Indeed' },
        ],
      },
    ],
  };

  const offers = [
    { id: 'REC-001', title: 'Développeur React Senior', dept: 'Technologie', location: 'Paris', type: 'CDI', candidates: 24, status: 'Ouvert', priority: 'Haute', created: '2024-01-05' },
    { id: 'REC-002', title: 'DevOps Engineer', dept: 'Technologie', location: 'Lyon', type: 'CDI', candidates: 18, status: 'Ouvert', priority: 'Haute', created: '2024-01-03' },
    { id: 'REC-003', title: 'Designer UX', dept: 'Technologie', location: 'Paris', type: 'CDI', candidates: 15, status: 'Ouvert', priority: 'Moyenne', created: '2023-12-20' },
    { id: 'REC-004', title: 'Chef de projet digital', dept: 'Marketing', location: 'Paris', type: 'CDI', candidates: 22, status: 'Ouvert', priority: 'Moyenne', created: '2023-12-15' },
    { id: 'REC-005', title: 'Data Analyst', dept: 'Technologie', location: 'Paris', type: 'CDI', candidates: 30, status: 'En cours', priority: 'Haute', created: '2023-12-10' },
    { id: 'REC-006', title: 'Comptable senior', dept: 'Finance', location: 'Paris', type: 'CDI', candidates: 12, status: 'Ouvert', priority: 'Basse', created: '2023-12-05' },
  ];

  return (
    <div className="page-recruitment">
      <PageHeader
        title="Recrutement"
        subtitle="Pipeline et gestion des candidatures"
        icon="briefcase"
      />

      {/* KPI Mini Cards */}
      <div className="kpi-hero-row">
        {kpis.map((kpi, i) => (
          <div key={i} className="kpi-mini-card-h">
            <div className="kpi-mini-icon" style={{ background: kpi.color + '18', color: kpi.color }}>
              <Icons name={kpi.icon} size={20} />
            </div>
            <div>
              <div className="kpi-mini-value">{kpi.value}</div>
              <div className="kpi-mini-label">{kpi.label}</div>
            </div>
          </div>
        ))}
      </div>

      {/* Kanban Pipeline */}
      <div className="card">
        <h3 className="card-title">Pipeline de recrutement</h3>
        <div className="kanban-board">
          {pipeline.columns.map((col) => (
            <div key={col.id} className="kanban-column">
              <div className="kanban-header" style={{ borderTopColor: col.color }}>
                <span className="kanban-title">{col.title}</span>
                <span className="kanban-count">{col.candidates.length}</span>
              </div>
              <div className="kanban-cards">
                {col.candidates.map((cand, i) => (
                  <div key={i} className="kanban-card">
                    <div className="kanban-card-header">
                      <Avatar initials={cand.avatar} size={28} />
                      <div>
                        <div className="kanban-card-name">{cand.name}</div>
                        <div className="kanban-card-role">{cand.role}</div>
                      </div>
                    </div>
                    <div className="kanban-card-footer">
                      <span className="kanban-card-date">{cand.date}</span>
                      <span className="badge badge-outline">{cand.source}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Offers Table */}
      <div className="card">
        <h3 className="card-title">Offres d'emploi</h3>
        <div className="table-responsive">
          <table className="data-table">
            <thead>
              <tr>
                <th>Réf.</th>
                <th>Poste</th>
                <th>Département</th>
                <th>Lieu</th>
                <th>Type</th>
                <th>Candidats</th>
                <th>Statut</th>
                <th>Priorité</th>
                <th>Créée le</th>
              </tr>
            </thead>
            <tbody>
              {offers.map((offer) => (
                <tr key={offer.id}>
                  <td className="font-mono text-sm">{offer.id}</td>
                  <td className="font-medium">{offer.title}</td>
                  <td>{offer.dept}</td>
                  <td>{offer.location}</td>
                  <td><span className="badge badge-blue">{offer.type}</span></td>
                  <td>{offer.candidates}</td>
                  <td>
                    <span className={`badge badge-${offer.status === 'Ouvert' ? 'green' : 'yellow'}`}>
                      {offer.status}
                    </span>
                  </td>
                  <td>
                    <span className={`badge badge-${offer.priority === 'Haute' ? 'red' : offer.priority === 'Moyenne' ? 'yellow' : 'gray'}`}>
                      {offer.priority}
                    </span>
                  </td>
                  <td>{offer.created}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
