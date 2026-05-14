'use client';
import { useState } from 'react';
import { Icons } from '../../components/Icons';
import Avatar from '../../components/Avatar';
import PageHeader from '../../components/PageHeader';

export default function Leaves() {
  const [activeTab, setActiveTab] = useState('requests');

  const kpis = [
    { label: 'Demandes en attente', value: '8', icon: 'inbox', color: '#f59e0b' },
    { label: 'En congé aujourd\'hui', value: '12', icon: 'calendar', color: '#3b82f6' },
    { label: 'Jours posés ce mois', value: '145', icon: 'clock', color: '#8b5cf6' },
    { label: 'Taux d\'approbation', value: '92%', icon: 'check-circle', color: '#10b981' },
  ];

  const requests = [
    { id: 'DEM-001', name: 'Sophie Martin', avatar: 'SM', type: 'Congé payé', start: '2024-02-05', end: '2024-02-09', days: 5, status: 'En attente', reason: 'Vacances familiales' },
    { id: 'DEM-002', name: 'Pierre Durand', avatar: 'PD', type: 'RTT', start: '2024-01-22', end: '2024-01-22', days: 1, status: 'En attente', reason: 'Personnel' },
    { id: 'DEM-003', name: 'Marie Leroy', avatar: 'ML', type: 'Congé payé', start: '2024-02-12', end: '2024-02-16', days: 5, status: 'Approuvé', reason: 'Voyage' },
    { id: 'DEM-004', name: 'Lucas Bernard', avatar: 'LB', type: 'Congé maladie', start: '2024-01-15', end: '2024-01-17', days: 3, status: 'Approuvé', reason: 'Maladie' },
    { id: 'DEM-005', name: 'Emma Petit', avatar: 'EP', type: 'Congé payé', start: '2024-01-08', end: '2024-01-19', days: 10, status: 'En cours', reason: 'Vacances' },
    { id: 'DEM-006', name: 'Thomas Roux', avatar: 'TR', type: 'RTT', start: '2024-01-25', end: '2024-01-26', days: 2, status: 'En attente', reason: 'Personnel' },
    { id: 'DEM-007', name: 'Julie Moreau', avatar: 'JM', type: 'Congé sans solde', start: '2024-03-01', end: '2024-03-15', days: 11, status: 'En attente', reason: 'Projet personnel' },
    { id: 'DEM-008', name: 'Nicolas Fournier', avatar: 'NF', type: 'Congé payé', start: '2024-02-19', end: '2024-02-23', days: 5, status: 'En attente', reason: 'Vacances ski' },
  ];

  const calendarDays = Array.from({ length: 31 }, (_, i) => {
    const day = i + 1;
    const leaves = [];
    if (day >= 8 && day <= 19) leaves.push({ initials: 'EP', color: '#3b82f6' });
    if (day >= 15 && day <= 17) leaves.push({ initials: 'LB', color: '#ef4444' });
    if (day === 22) leaves.push({ initials: 'PD', color: '#f59e0b' });
    if (day >= 25 && day <= 26) leaves.push({ initials: 'TR', color: '#f59e0b' });
    return { day, leaves, isWeekend: [6, 7, 13, 14, 20, 21, 27, 28].includes(day) };
  });

  const balances = [
    { name: 'Sophie Martin', avatar: 'SM', cp: { total: 25, taken: 12, remaining: 13 }, rtt: { total: 10, taken: 4, remaining: 6 } },
    { name: 'Pierre Durand', avatar: 'PD', cp: { total: 25, taken: 18, remaining: 7 }, rtt: { total: 10, taken: 7, remaining: 3 } },
    { name: 'Marie Leroy', avatar: 'ML', cp: { total: 25, taken: 8, remaining: 17 }, rtt: { total: 10, taken: 3, remaining: 7 } },
    { name: 'Lucas Bernard', avatar: 'LB', cp: { total: 25, taken: 15, remaining: 10 }, rtt: { total: 10, taken: 5, remaining: 5 } },
    { name: 'Emma Petit', avatar: 'EP', cp: { total: 25, taken: 20, remaining: 5 }, rtt: { total: 10, taken: 8, remaining: 2 } },
    { name: 'Thomas Roux', avatar: 'TR', cp: { total: 25, taken: 10, remaining: 15 }, rtt: { total: 10, taken: 2, remaining: 8 } },
  ];

  return (
    <div className="page-leaves">
      <PageHeader
        title="Congés & Absences"
        subtitle="Gestion des demandes de congés"
        icon="calendar"
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

      {/* Tabs */}
      <div className="tabs">
        <button
          className={`tab ${activeTab === 'requests' ? 'active' : ''}`}
          onClick={() => setActiveTab('requests')}
        >
          <Icons name="inbox" size={14} /> Demandes
        </button>
        <button
          className={`tab ${activeTab === 'calendar' ? 'active' : ''}`}
          onClick={() => setActiveTab('calendar')}
        >
          <Icons name="calendar" size={14} /> Calendrier
        </button>
        <button
          className={`tab ${activeTab === 'balances' ? 'active' : ''}`}
          onClick={() => setActiveTab('balances')}
        >
          <Icons name="bar-chart-2" size={14} /> Soldes
        </button>
      </div>

      {/* Requests Tab */}
      {activeTab === 'requests' && (
        <div className="card">
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Réf.</th>
                  <th>Employé</th>
                  <th>Type</th>
                  <th>Début</th>
                  <th>Fin</th>
                  <th>Jours</th>
                  <th>Motif</th>
                  <th>Statut</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {requests.map((req) => (
                  <tr key={req.id}>
                    <td className="font-mono text-sm">{req.id}</td>
                    <td>
                      <div className="employee-cell">
                        <Avatar initials={req.avatar} size={28} />
                        <span>{req.name}</span>
                      </div>
                    </td>
                    <td>
                      <span className={`badge badge-${
                        req.type === 'Congé payé' ? 'blue' :
                        req.type === 'RTT' ? 'purple' :
                        req.type === 'Congé maladie' ? 'red' :
                        'gray'
                      }`}>
                        {req.type}
                      </span>
                    </td>
                    <td>{req.start}</td>
                    <td>{req.end}</td>
                    <td>{req.days}j</td>
                    <td className="text-sm">{req.reason}</td>
                    <td>
                      <span className={`badge badge-${
                        req.status === 'Approuvé' ? 'green' :
                        req.status === 'En attente' ? 'yellow' :
                        req.status === 'En cours' ? 'blue' :
                        'red'
                      }`}>
                        {req.status}
                      </span>
                    </td>
                    <td>
                      {req.status === 'En attente' && (
                        <div className="action-btns">
                          <button className="btn-icon btn-approve"><Icons name="check" size={14} /></button>
                          <button className="btn-icon btn-reject"><Icons name="x" size={14} /></button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Calendar Tab */}
      {activeTab === 'calendar' && (
        <div className="card">
          <h3 className="card-title">Janvier 2024</h3>
          <div className="leave-calendar">
            <div className="calendar-header">
              {['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'].map((d) => (
                <div key={d} className="calendar-day-name">{d}</div>
              ))}
            </div>
            <div className="calendar-grid">
              {calendarDays.map((d) => (
                <div
                  key={d.day}
                  className={`calendar-cell ${d.isWeekend ? 'weekend' : ''} ${d.leaves.length > 0 ? 'has-leave' : ''}`}
                >
                  <span className="calendar-date">{d.day}</span>
                  <div className="calendar-leaves">
                    {d.leaves.map((l, i) => (
                      <Avatar key={i} initials={l.initials} size={18} style={{ borderColor: l.color }} />
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Balances Tab */}
      {activeTab === 'balances' && (
        <div className="card">
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Employé</th>
                  <th>CP Total</th>
                  <th>CP Pris</th>
                  <th>CP Restant</th>
                  <th>RTT Total</th>
                  <th>RTT Pris</th>
                  <th>RTT Restant</th>
                </tr>
              </thead>
              <tbody>
                {balances.map((b, i) => (
                  <tr key={i}>
                    <td>
                      <div className="employee-cell">
                        <Avatar initials={b.avatar} size={28} />
                        <span>{b.name}</span>
                      </div>
                    </td>
                    <td>{b.cp.total}</td>
                    <td>{b.cp.taken}</td>
                    <td className={b.cp.remaining <= 5 ? 'text-red font-medium' : 'font-medium'}>
                      {b.cp.remaining}
                    </td>
                    <td>{b.rtt.total}</td>
                    <td>{b.rtt.taken}</td>
                    <td className={b.rtt.remaining <= 2 ? 'text-red font-medium' : 'font-medium'}>
                      {b.rtt.remaining}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
