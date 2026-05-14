'use client';
import { useState } from 'react';
import { Icons } from '../../components/Icons';
import Avatar from '../../components/Avatar';
import PageHeader from '../../components/PageHeader';
import { Line, Bars } from '../../components/Charts';

export default function Time() {
  const kpis = [
    { label: 'Présents aujourd\'hui', value: '298', icon: 'users', color: '#10b981' },
    { label: 'Absents', value: '44', icon: 'user-minus', color: '#ef4444' },
    { label: 'Heures sup. ce mois', value: '156h', icon: 'clock', color: '#f59e0b' },
    { label: 'Taux ponctualité', value: '94.2%', icon: 'check-circle', color: '#3b82f6' },
  ];

  const todayClockIns = [
    { name: 'Sophie Martin', avatar: 'SM', arrival: '08:02', departure: '—', status: 'Présent', hours: '—' },
    { name: 'Pierre Durand', avatar: 'PD', arrival: '08:15', departure: '—', status: 'Présent', hours: '—' },
    { name: 'Marie Leroy', avatar: 'ML', arrival: '08:45', departure: '—', status: 'Retard', hours: '—' },
    { name: 'Lucas Bernard', avatar: 'LB', arrival: '09:00', departure: '—', status: 'Présent', hours: '—' },
    { name: 'Emma Petit', avatar: 'EP', arrival: '—', departure: '—', status: 'Congé', hours: '—' },
    { name: 'Thomas Roux', avatar: 'TR', arrival: '07:55', departure: '—', status: 'Présent', hours: '—' },
    { name: 'Julie Moreau', avatar: 'JM', arrival: '08:30', departure: '—', status: 'Présent', hours: '—' },
    { name: 'Nicolas Fournier', avatar: 'NF', arrival: '—', departure: '—', status: 'Télétravail', hours: '—' },
  ];

  const weeklyTrend = [
    { month: 'Lun', value: 305 },
    { month: 'Mar', value: 310 },
    { month: 'Mer', value: 298 },
    { month: 'Jeu', value: 312 },
    { month: 'Ven', value: 280 },
  ];

  const timesheets = [
    { name: 'Sophie Martin', avatar: 'SM', week: 'S02-2024', hours: '39h00', overtime: '1h00', status: 'Validé', project: 'Projet Alpha' },
    { name: 'Pierre Durand', avatar: 'PD', week: 'S02-2024', hours: '41h30', overtime: '3h30', status: 'Validé', project: 'Projet Beta' },
    { name: 'Marie Leroy', avatar: 'ML', week: 'S02-2024', hours: '38h00', overtime: '0h00', status: 'En attente', project: 'Projet Alpha' },
    { name: 'Lucas Bernard', avatar: 'LB', week: 'S02-2024', hours: '40h00', overtime: '2h00', status: 'Validé', project: 'Projet Gamma' },
    { name: 'Nicolas Fournier', avatar: 'NF', week: 'S02-2024', hours: '42h00', overtime: '4h00', status: 'En attente', project: 'Projet Beta' },
    { name: 'Camille Girard', avatar: 'CG', week: 'S02-2024', hours: '37h30', overtime: '0h00', status: 'Refusé', project: 'Projet Delta' },
  ];

  return (
    <div className="page-time">
      <PageHeader
        title="Temps & Présence"
        subtitle="Suivi du temps de travail et pointages"
        icon="clock"
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

      {/* Today Clock-Ins + Weekly Trend */}
      <div className="dashboard-grid-2">
        <div className="card">
          <h3 className="card-title">Pointages du jour</h3>
          <div className="table-responsive">
            <table className="data-table compact">
              <thead>
                <tr>
                  <th>Employé</th>
                  <th>Arrivée</th>
                  <th>Départ</th>
                  <th>Statut</th>
                </tr>
              </thead>
              <tbody>
                {todayClockIns.map((row, i) => (
                  <tr key={i}>
                    <td>
                      <div className="employee-cell">
                        <Avatar initials={row.avatar} size={24} />
                        <span>{row.name}</span>
                      </div>
                    </td>
                    <td>{row.arrival}</td>
                    <td>{row.departure}</td>
                    <td>
                      <span className={`badge badge-${
                        row.status === 'Présent' ? 'green' :
                        row.status === 'Retard' ? 'red' :
                        row.status === 'Congé' ? 'yellow' :
                        'blue'
                      }`}>
                        {row.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        <div className="card">
          <h3 className="card-title">Tendance hebdomadaire (présences)</h3>
          <Bars data={weeklyTrend} height={220} color="#3b82f6" />
        </div>
      </div>

      {/* Timesheets Table */}
      <div className="card">
        <h3 className="card-title">Feuilles de temps</h3>
        <div className="table-responsive">
          <table className="data-table">
            <thead>
              <tr>
                <th>Employé</th>
                <th>Semaine</th>
                <th>Heures</th>
                <th>Heures sup.</th>
                <th>Projet</th>
                <th>Statut</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {timesheets.map((ts, i) => (
                <tr key={i}>
                  <td>
                    <div className="employee-cell">
                      <Avatar initials={ts.avatar} size={28} />
                      <span>{ts.name}</span>
                    </div>
                  </td>
                  <td>{ts.week}</td>
                  <td>{ts.hours}</td>
                  <td className={ts.overtime !== '0h00' ? 'text-orange font-medium' : ''}>
                    {ts.overtime}
                  </td>
                  <td>{ts.project}</td>
                  <td>
                    <span className={`badge badge-${
                      ts.status === 'Validé' ? 'green' :
                      ts.status === 'En attente' ? 'yellow' :
                      'red'
                    }`}>
                      {ts.status}
                    </span>
                  </td>
                  <td>
                    <button className="btn-icon"><Icons name="eye" size={16} /></button>
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
