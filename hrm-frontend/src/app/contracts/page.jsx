'use client';
import { useState } from 'react';
import { Icons } from '../../components/Icons';
import Avatar from '../../components/Avatar';
import PageHeader from '../../components/PageHeader';
import { Donut } from '../../components/Charts';

export default function Contracts() {
  const kpis = [
    { label: 'CDI', value: '285', icon: 'file-text', color: '#3b82f6' },
    { label: 'CDD', value: '32', icon: 'clock', color: '#f59e0b' },
    { label: 'Alternance', value: '15', icon: 'book', color: '#8b5cf6' },
    { label: 'Stage', value: '10', icon: 'briefcase', color: '#10b981' },
    { label: 'Fin de contrat < 30j', value: '6', icon: 'alert-triangle', color: '#ef4444' },
    { label: 'Renouvellements', value: '4', icon: 'refresh-cw', color: '#06b6d4' },
  ];

  const contractTypes = [
    { label: 'CDI', value: 285, color: '#3b82f6' },
    { label: 'CDD', value: 32, color: '#f59e0b' },
    { label: 'Alternance', value: 15, color: '#8b5cf6' },
    { label: 'Stage', value: 10, color: '#10b981' },
  ];

  const alerts = [
    { name: 'Alexandre Bonnet', type: 'Fin période essai', date: '15 Fév 2024', days: 12 },
    { name: 'Maxime Lambert', type: 'Fin de stage', date: '28 Fév 2024', days: 25 },
    { name: 'Sarah Cohen', type: 'Fin période essai', date: '01 Mar 2024', days: 27 },
  ];

  const contracts = [
    { id: 'CTR-001', employee: 'Sophie Martin', avatar: 'SM', type: 'CDI', start: '2019-03-15', end: '—', status: 'Actif', salary: '78 000€' },
    { id: 'CTR-002', employee: 'Pierre Durand', avatar: 'PD', type: 'CDI', start: '2020-06-01', end: '—', status: 'Actif', salary: '62 000€' },
    { id: 'CTR-003', employee: 'Lucas Bernard', avatar: 'LB', type: 'CDI', start: '2022-04-20', end: '—', status: 'Actif', salary: '48 000€' },
    { id: 'CTR-004', employee: 'Alexandre Bonnet', avatar: 'AB', type: 'CDI', start: '2024-01-02', end: '—', status: 'Période essai', salary: '50 000€' },
    { id: 'CTR-005', employee: 'Maxime Lambert', avatar: 'MxL', type: 'Stage', start: '2024-01-08', end: '2024-07-08', status: 'Actif', salary: '18 000€' },
    { id: 'CTR-006', employee: 'Sarah Cohen', avatar: 'SC', type: 'CDD', start: '2023-09-01', end: '2024-08-31', status: 'Actif', salary: '42 000€' },
  ];

  return (
    <div className="page-contracts">
      <PageHeader
        title="Contrats"
        subtitle="Gestion des contrats de travail"
        icon="file-text"
      />

      {/* KPI Cards + Donut */}
      <div className="contracts-top">
        <div className="kpi-grid-6">
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
        <div className="card contract-donut-card">
          <h3 className="card-title">Répartition par type</h3>
          <Donut data={contractTypes} size={180} />
        </div>
      </div>

      {/* Alert Banner */}
      <div className="alert-banner alert-warning">
        <Icons name="alert-triangle" size={20} />
        <div className="alert-content">
          <strong>3 échéances à venir dans les 30 prochains jours</strong>
          <div className="alert-details">
            {alerts.map((a, i) => (
              <span key={i} className="alert-item">
                <Avatar initials={a.name.split(' ').map((n) => n[0]).join('')} size={20} />
                {a.name} — {a.type} ({a.date}, J-{a.days})
              </span>
            ))}
          </div>
        </div>
      </div>

      {/* Contracts Table */}
      <div className="card">
        <h3 className="card-title">Liste des contrats</h3>
        <div className="table-responsive">
          <table className="data-table">
            <thead>
              <tr>
                <th>Réf.</th>
                <th>Employé</th>
                <th>Type</th>
                <th>Début</th>
                <th>Fin</th>
                <th>Statut</th>
                <th>Salaire brut</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {contracts.map((c) => (
                <tr key={c.id}>
                  <td className="font-mono text-sm">{c.id}</td>
                  <td>
                    <div className="employee-cell">
                      <Avatar initials={c.avatar} size={28} />
                      <span>{c.employee}</span>
                    </div>
                  </td>
                  <td><span className={`badge badge-${c.type === 'CDI' ? 'blue' : c.type === 'CDD' ? 'yellow' : c.type === 'Stage' ? 'green' : 'purple'}`}>{c.type}</span></td>
                  <td>{c.start}</td>
                  <td>{c.end}</td>
                  <td>
                    <span className={`badge badge-${c.status === 'Actif' ? 'green' : 'blue'}`}>{c.status}</span>
                  </td>
                  <td>{c.salary}</td>
                  <td>
                    <button className="btn-icon"><Icons name="eye" size={16} /></button>
                    <button className="btn-icon"><Icons name="download" size={16} /></button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
