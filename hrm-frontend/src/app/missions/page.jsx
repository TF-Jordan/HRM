'use client';
import { useState } from 'react';
import { Icons } from '../../components/Icons';
import Avatar from '../../components/Avatar';
import PageHeader from '../../components/PageHeader';

export default function Missions() {
  const kpis = [
    { label: 'Missions en cours', value: '14', icon: 'map', color: '#3b82f6' },
    { label: 'Budget mensuel', value: '45.2K€', icon: 'dollar', color: '#10b981' },
    { label: 'Collaborateurs en mission', value: '18', icon: 'users', color: '#8b5cf6' },
    { label: 'Frais en attente', value: '12.8K€', icon: 'credit-card', color: '#f59e0b' },
  ];

  const missions = [
    { id: 'MIS-001', employee: 'Sophie Martin', avatar: 'SM', destination: 'Londres', client: 'TechCorp UK', start: '2024-01-15', end: '2024-01-19', status: 'En cours', budget: '3 200€', spent: '1 800€' },
    { id: 'MIS-002', employee: 'Pierre Durand', avatar: 'PD', destination: 'Berlin', client: 'DataGmbH', start: '2024-01-22', end: '2024-01-26', status: 'Planifié', budget: '4 100€', spent: '0€' },
    { id: 'MIS-003', employee: 'Marie Leroy', avatar: 'ML', destination: 'Lyon', client: 'InnoLab', start: '2024-01-10', end: '2024-01-12', status: 'Terminé', budget: '1 500€', spent: '1 420€' },
    { id: 'MIS-004', employee: 'Lucas Bernard', avatar: 'LB', destination: 'Madrid', client: 'IberiaConsulting', start: '2024-02-05', end: '2024-02-09', status: 'Planifié', budget: '3 800€', spent: '0€' },
    { id: 'MIS-005', employee: 'Nicolas Fournier', avatar: 'NF', destination: 'Amsterdam', client: 'CloudNL', start: '2024-01-29', end: '2024-02-02', status: 'Planifié', budget: '3 600€', spent: '0€' },
    { id: 'MIS-006', employee: 'Camille Girard', avatar: 'CG', destination: 'Bordeaux', client: 'VinTech', start: '2024-01-08', end: '2024-01-09', status: 'Terminé', budget: '800€', spent: '750€' },
    { id: 'MIS-007', employee: 'Thomas Roux', avatar: 'TR', destination: 'Genève', client: 'SwissFinance', start: '2024-01-17', end: '2024-01-18', status: 'En cours', budget: '2 200€', spent: '1 100€' },
    { id: 'MIS-008', employee: 'Julie Moreau', avatar: 'JM', destination: 'Bruxelles', client: 'EuroHR', start: '2024-02-12', end: '2024-02-14', status: 'Planifié', budget: '1 900€', spent: '0€' },
  ];

  const destinations = [
    { city: 'Londres', country: 'UK', count: 3, lat: 51.5, lng: -0.1 },
    { city: 'Berlin', country: 'DE', count: 2, lat: 52.5, lng: 13.4 },
    { city: 'Madrid', country: 'ES', count: 2, lat: 40.4, lng: -3.7 },
    { city: 'Amsterdam', country: 'NL', count: 1, lat: 52.4, lng: 4.9 },
    { city: 'Lyon', country: 'FR', count: 4, lat: 45.8, lng: 4.8 },
    { city: 'Bordeaux', country: 'FR', count: 2, lat: 44.8, lng: -0.6 },
    { city: 'Genève', country: 'CH', count: 1, lat: 46.2, lng: 6.1 },
    { city: 'Bruxelles', country: 'BE', count: 1, lat: 50.8, lng: 4.4 },
  ];

  return (
    <div className="page-missions">
      <PageHeader
        title="Missions & Déplacements"
        subtitle="Gestion des missions et notes de frais"
        icon="map"
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

      {/* Missions Table + Map */}
      <div className="dashboard-grid-2">
        <div className="card" style={{ gridColumn: 'span 2' }}>
          <h3 className="card-title">Missions</h3>
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Réf.</th>
                  <th>Collaborateur</th>
                  <th>Destination</th>
                  <th>Client</th>
                  <th>Début</th>
                  <th>Fin</th>
                  <th>Budget</th>
                  <th>Dépensé</th>
                  <th>Statut</th>
                </tr>
              </thead>
              <tbody>
                {missions.map((m) => (
                  <tr key={m.id}>
                    <td className="font-mono text-sm">{m.id}</td>
                    <td>
                      <div className="employee-cell">
                        <Avatar initials={m.avatar} size={28} />
                        <span>{m.employee}</span>
                      </div>
                    </td>
                    <td className="font-medium">{m.destination}</td>
                    <td>{m.client}</td>
                    <td>{m.start}</td>
                    <td>{m.end}</td>
                    <td>{m.budget}</td>
                    <td>{m.spent}</td>
                    <td>
                      <span className={`badge badge-${
                        m.status === 'En cours' ? 'blue' :
                        m.status === 'Planifié' ? 'yellow' :
                        'green'
                      }`}>
                        {m.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Destination Map Placeholder */}
      <div className="card">
        <h3 className="card-title">
          <Icons name="map" size={18} /> Destinations fréquentes
        </h3>
        <div className="map-placeholder">
          <div className="map-bg">
            <Icons name="map" size={64} className="map-icon-placeholder" />
            <p>Carte des destinations</p>
          </div>
          <div className="destination-list">
            {destinations.map((dest, i) => (
              <div key={i} className="destination-item">
                <span className="destination-city">{dest.city}</span>
                <span className="destination-country">{dest.country}</span>
                <span className="destination-count">{dest.count} mission(s)</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
