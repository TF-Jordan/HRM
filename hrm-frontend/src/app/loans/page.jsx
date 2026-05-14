'use client';
import { useState } from 'react';
import { Icons } from '../../components/Icons';
import Avatar from '../../components/Avatar';
import PageHeader from '../../components/PageHeader';
import { Donut, Line } from '../../components/Charts';

export default function Loans() {
  const kpis = [
    { label: 'Prêts actifs', value: '34', icon: 'file', color: '#6366f1', bg: '#eef2ff' },
    { label: 'Montant total', value: '28.5M', sub: 'XAF', icon: 'money', color: '#059669', bg: '#ecfdf5' },
    { label: 'Remboursé', value: '12.3M', sub: 'XAF', icon: 'check', color: '#3b82f6', bg: '#dbeafe' },
    { label: 'Taux défaut', value: '2.1%', icon: 'alert', color: '#ef4444', bg: '#fef2f2' },
  ];

  const outstandingData = [
    { month: 'Jan', amount: 25.2 },
    { month: 'Fév', amount: 26.1 },
    { month: 'Mar', amount: 27.0 },
    { month: 'Avr', amount: 27.8 },
    { month: 'Mai', amount: 28.5 },
  ];

  const distributionData = [
    { label: 'Immobilier', value: 45, color: '#6366f1' },
    { label: 'Personnel', value: 25, color: '#a78bfa' },
    { label: 'Véhicule', value: 18, color: '#34d399' },
    { label: 'Éducation', value: 12, color: '#fbbf24' },
  ];

  const loans = [
    { employee: 'Moussa Traoré', type: 'Immobilier', amount: '5 000 000', remaining: '3 200 000', monthly: '150 000', progress: 36, status: 'En cours', statusColor: '#3b82f6' },
    { employee: 'Fatou Sow', type: 'Personnel', amount: '1 200 000', remaining: '400 000', monthly: '80 000', progress: 67, status: 'En cours', statusColor: '#3b82f6' },
    { employee: 'Amadou Ba', type: 'Véhicule', amount: '3 500 000', remaining: '2 800 000', monthly: '120 000', progress: 20, status: 'En cours', statusColor: '#3b82f6' },
    { employee: 'Aïssatou Camara', type: 'Éducation', amount: '800 000', remaining: '200 000', monthly: '50 000', progress: 75, status: 'Bientôt soldé', statusColor: '#f59e0b' },
    { employee: 'Ibrahim Diop', type: 'Personnel', amount: '600 000', remaining: '0', monthly: '60 000', progress: 100, status: 'Soldé', statusColor: '#059669' },
    { employee: 'Mariam Konaté', type: 'Immobilier', amount: '4 200 000', remaining: '4 000 000', monthly: '140 000', progress: 5, status: 'Nouveau', statusColor: '#8b5cf6' },
  ];

  return (
    <div>
      <PageHeader title="Prêts" subtitle="Gestion des prêts au personnel" />

      {/* KPI Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 16, marginBottom: 24 }}>
        {kpis.map((k, i) => (
          <div key={i} style={{
            background: '#fff', borderRadius: 14, padding: 20,
            boxShadow: '0 1px 4px rgba(0,0,0,0.06)', display: 'flex', alignItems: 'center', gap: 14
          }}>
            <div style={{
              width: 44, height: 44, borderRadius: 12, background: k.bg,
              display: 'flex', alignItems: 'center', justifyContent: 'center'
            }}>
              <Icons name={k.icon} size={20} color={k.color} />
            </div>
            <div>
              <div style={{ fontSize: 12, color: '#64748b', marginBottom: 2 }}>{k.label}</div>
              <div style={{ fontSize: 22, fontWeight: 700, color: '#1e293b' }}>
                {k.value} {k.sub && <span style={{ fontSize: 12, color: '#94a3b8' }}>{k.sub}</span>}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Charts */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 24, marginBottom: 24 }}>
        <div style={{ background: '#fff', borderRadius: 14, padding: 24, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
          <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 16, color: '#1e293b' }}>Encours prêts (M XAF)</h3>
          <Line data={outstandingData} xKey="month" series={[
            { key: 'amount', color: '#6366f1', label: 'Encours' },
          ]} height={220} />
        </div>
        <div style={{ background: '#fff', borderRadius: 14, padding: 24, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
          <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 16, color: '#1e293b' }}>Répartition par type</h3>
          <Donut data={distributionData} size={180} />
        </div>
      </div>

      {/* Loans Table */}
      <div style={{ background: '#fff', borderRadius: 14, overflow: 'hidden', boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
        <div style={{ padding: '16px 20px', borderBottom: '1px solid #f1f5f9' }}>
          <h3 style={{ fontSize: 15, fontWeight: 700, color: '#1e293b' }}>Liste des prêts</h3>
        </div>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
          <thead>
            <tr style={{ background: '#f8fafc' }}>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Employé</th>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Type</th>
              <th style={{ padding: '12px 16px', textAlign: 'right', fontWeight: 600, color: '#64748b' }}>Montant</th>
              <th style={{ padding: '12px 16px', textAlign: 'right', fontWeight: 600, color: '#64748b' }}>Restant</th>
              <th style={{ padding: '12px 16px', textAlign: 'right', fontWeight: 600, color: '#64748b' }}>Mensualité</th>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Progression</th>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Statut</th>
            </tr>
          </thead>
          <tbody>
            {loans.map((l, i) => (
              <tr key={i} style={{ borderTop: '1px solid #f1f5f9' }}>
                <td style={{ padding: '12px 16px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    <Avatar name={l.employee} size={32} />
                    <span style={{ fontWeight: 600, color: '#1e293b' }}>{l.employee}</span>
                  </div>
                </td>
                <td style={{ padding: '12px 16px', color: '#475569' }}>{l.type}</td>
                <td style={{ padding: '12px 16px', textAlign: 'right', fontWeight: 600, color: '#1e293b' }}>{l.amount}</td>
                <td style={{ padding: '12px 16px', textAlign: 'right', color: '#475569' }}>{l.remaining}</td>
                <td style={{ padding: '12px 16px', textAlign: 'right', color: '#475569' }}>{l.monthly}</td>
                <td style={{ padding: '12px 16px', width: 140 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <div style={{ flex: 1, height: 6, background: '#f1f5f9', borderRadius: 3, overflow: 'hidden' }}>
                      <div style={{
                        width: `${l.progress}%`, height: '100%', borderRadius: 3,
                        background: l.progress === 100 ? '#059669' : l.progress > 60 ? '#f59e0b' : '#6366f1'
                      }} />
                    </div>
                    <span style={{ fontSize: 11, color: '#94a3b8', minWidth: 32 }}>{l.progress}%</span>
                  </div>
                </td>
                <td style={{ padding: '12px 16px' }}>
                  <span style={{
                    background: `${l.statusColor}18`, color: l.statusColor,
                    padding: '4px 12px', borderRadius: 20, fontSize: 12, fontWeight: 600
                  }}>{l.status}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
