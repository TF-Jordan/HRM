'use client';
import { useState } from 'react';
import { Icons } from '../../components/Icons';
import PageHeader from '../../components/PageHeader';
import { Donut, Line, Bars } from '../../components/Charts';

export default function Budget() {
  const budgetOverview = {
    total: '44.0M',
    currency: 'XAF',
    consumed: 28.6,
    remaining: 15.4,
    consumedPct: 65,
  };

  const departmentBudgets = [
    { label: 'Production', budget: 12.5, consumed: 9.2, pct: 74 },
    { label: 'Commercial', budget: 8.0, consumed: 5.8, pct: 73 },
    { label: 'RH', budget: 6.5, consumed: 4.1, pct: 63 },
    { label: 'Finance', budget: 5.5, consumed: 3.0, pct: 55 },
    { label: 'IT', budget: 7.0, consumed: 4.2, pct: 60 },
    { label: 'Juridique', budget: 4.5, consumed: 2.3, pct: 51 },
  ];

  const topFormations = [
    { name: 'Leadership & Management', cost: '3.2M', hours: 240, participants: 18 },
    { name: 'Excel Avancé', cost: '1.8M', hours: 160, participants: 25 },
    { name: 'Droit du travail OHADA', cost: '2.5M', hours: 120, participants: 15 },
    { name: 'Gestion de projet Agile', cost: '2.1M', hours: 180, participants: 20 },
    { name: 'Anglais professionnel B2', cost: '1.5M', hours: 480, participants: 8 },
  ];

  const monthlyTrend = [
    { month: 'Jan', budget: 3.7, actual: 3.2 },
    { month: 'Fév', budget: 3.7, actual: 3.5 },
    { month: 'Mar', budget: 3.7, actual: 4.1 },
    { month: 'Avr', budget: 3.7, actual: 3.8 },
    { month: 'Mai', budget: 3.7, actual: 3.6 },
  ];

  const categoryData = [
    { label: 'Salaires', value: 55, color: '#6366f1' },
    { label: 'Formations', value: 18, color: '#a78bfa' },
    { label: 'Avantages', value: 15, color: '#34d399' },
    { label: 'Recrutement', value: 8, color: '#fbbf24' },
    { label: 'Divers', value: 4, color: '#f97316' },
  ];

  const engagements = [
    { ref: 'ENG-2026-001', description: 'Formation Leadership Q2', department: 'RH', amount: '3 200 000', engaged: '2 800 000', status: 'Engagé', statusColor: '#3b82f6' },
    { ref: 'ENG-2026-002', description: 'Recrutement développeurs', department: 'IT', amount: '1 500 000', engaged: '750 000', status: 'Partiel', statusColor: '#f59e0b' },
    { ref: 'ENG-2026-003', description: 'Séminaire commercial', department: 'Commercial', amount: '2 100 000', engaged: '2 100 000', status: 'Soldé', statusColor: '#059669' },
    { ref: 'ENG-2026-004', description: 'Matériel informatique', department: 'IT', amount: '4 500 000', engaged: '1 200 000', status: 'Engagé', statusColor: '#3b82f6' },
    { ref: 'ENG-2026-005', description: 'Visite médicale annuelle', department: 'RH', amount: '800 000', engaged: '0', status: 'Planifié', statusColor: '#94a3b8' },
  ];

  return (
    <div>
      <PageHeader title="Budget" subtitle="Suivi budgétaire et engagements" />

      {/* Hero Card */}
      <div style={{
        background: 'linear-gradient(135deg, #1e1b4b 0%, #312e81 100%)',
        borderRadius: 16, padding: 32, marginBottom: 24, color: '#fff'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
          <div>
            <div style={{ fontSize: 14, opacity: 0.7, marginBottom: 4 }}>Budget annuel formation</div>
            <div style={{ fontSize: 32, fontWeight: 700 }}>
              {budgetOverview.total} <span style={{ fontSize: 16, opacity: 0.7 }}>{budgetOverview.currency}</span>
            </div>
          </div>
          <div style={{ display: 'flex', gap: 24 }}>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: 12, opacity: 0.6, marginBottom: 4 }}>Consommé</div>
              <div style={{ fontSize: 20, fontWeight: 700, color: '#fbbf24' }}>{budgetOverview.consumed}M</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: 12, opacity: 0.6, marginBottom: 4 }}>Disponible</div>
              <div style={{ fontSize: 20, fontWeight: 700, color: '#34d399' }}>{budgetOverview.remaining}M</div>
            </div>
          </div>
        </div>

        {/* Split bar */}
        <div style={{ height: 12, background: 'rgba(255,255,255,0.15)', borderRadius: 6, overflow: 'hidden', marginBottom: 8 }}>
          <div style={{
            width: `${budgetOverview.consumedPct}%`, height: '100%',
            background: 'linear-gradient(90deg, #fbbf24, #f59e0b)', borderRadius: 6
          }} />
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, opacity: 0.6 }}>
          <span>{budgetOverview.consumedPct}% consommé</span>
          <span>{100 - budgetOverview.consumedPct}% disponible</span>
        </div>
      </div>

      {/* Department Bars + Category Donut */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 24, marginBottom: 24 }}>
        <div style={{ background: '#fff', borderRadius: 14, padding: 24, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
          <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 16, color: '#1e293b' }}>Consommation par département</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            {departmentBudgets.map((d, i) => (
              <div key={i}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                  <span style={{ fontSize: 13, color: '#475569' }}>{d.label}</span>
                  <span style={{ fontSize: 12, color: '#94a3b8' }}>{d.consumed}M / {d.budget}M ({d.pct}%)</span>
                </div>
                <div style={{ height: 8, background: '#f1f5f9', borderRadius: 4, overflow: 'hidden' }}>
                  <div style={{
                    width: `${d.pct}%`, height: '100%', borderRadius: 4,
                    background: d.pct > 70 ? '#f59e0b' : '#6366f1'
                  }} />
                </div>
              </div>
            ))}
          </div>
        </div>
        <div style={{ background: '#fff', borderRadius: 14, padding: 24, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
          <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 16, color: '#1e293b' }}>Répartition par catégorie</h3>
          <Donut data={categoryData} size={180} />
        </div>
      </div>

      {/* Monthly Trend + Top formations */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24, marginBottom: 24 }}>
        <div style={{ background: '#fff', borderRadius: 14, padding: 24, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
          <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 16, color: '#1e293b' }}>Tendance mensuelle (M XAF)</h3>
          <Line data={monthlyTrend} xKey="month" series={[
            { key: 'budget', color: '#e2e8f0', label: 'Budget prévu' },
            { key: 'actual', color: '#6366f1', label: 'Réalisé' },
          ]} height={200} />
        </div>
        <div style={{ background: '#fff', borderRadius: 14, padding: 24, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
          <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 16, color: '#1e293b' }}>Top formations (heures & coût)</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            {topFormations.map((f, i) => (
              <div key={i} style={{
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                padding: '8px 0', borderBottom: i < topFormations.length - 1 ? '1px solid #f1f5f9' : 'none'
              }}>
                <div>
                  <div style={{ fontSize: 13, fontWeight: 600, color: '#1e293b' }}>{f.name}</div>
                  <div style={{ fontSize: 11, color: '#94a3b8' }}>{f.participants} participants · {f.hours}h</div>
                </div>
                <span style={{ fontWeight: 700, color: '#6366f1', fontSize: 13 }}>{f.cost} XAF</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Engagements Table */}
      <div style={{ background: '#fff', borderRadius: 14, overflow: 'hidden', boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
        <div style={{ padding: '16px 20px', borderBottom: '1px solid #f1f5f9' }}>
          <h3 style={{ fontSize: 15, fontWeight: 700, color: '#1e293b' }}>Détail des engagements</h3>
        </div>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
          <thead>
            <tr style={{ background: '#f8fafc' }}>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Réf.</th>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Description</th>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Département</th>
              <th style={{ padding: '12px 16px', textAlign: 'right', fontWeight: 600, color: '#64748b' }}>Montant</th>
              <th style={{ padding: '12px 16px', textAlign: 'right', fontWeight: 600, color: '#64748b' }}>Engagé</th>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Statut</th>
            </tr>
          </thead>
          <tbody>
            {engagements.map((e, i) => (
              <tr key={i} style={{ borderTop: '1px solid #f1f5f9' }}>
                <td style={{ padding: '12px 16px', fontWeight: 600, color: '#6366f1' }}>{e.ref}</td>
                <td style={{ padding: '12px 16px', color: '#1e293b' }}>{e.description}</td>
                <td style={{ padding: '12px 16px', color: '#475569' }}>{e.department}</td>
                <td style={{ padding: '12px 16px', textAlign: 'right', fontWeight: 600, color: '#1e293b' }}>{e.amount} XAF</td>
                <td style={{ padding: '12px 16px', textAlign: 'right', color: '#475569' }}>{e.engaged} XAF</td>
                <td style={{ padding: '12px 16px' }}>
                  <span style={{
                    background: `${e.statusColor}18`, color: e.statusColor,
                    padding: '4px 12px', borderRadius: 20, fontSize: 12, fontWeight: 600
                  }}>{e.status}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
