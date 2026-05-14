'use client';
import { useState } from 'react';
import { Icons } from '../../components/Icons';
import Avatar from '../../components/Avatar';
import PageHeader from '../../components/PageHeader';
import { Bars } from '../../components/Charts';

export default function Expenses() {
  const kpis = [
    { label: 'Notes en attente', value: '23', icon: 'file', color: '#f59e0b', bg: '#fffbeb' },
    { label: 'Montant en attente', value: '4.8M', sub: 'XAF', icon: 'money', color: '#6366f1', bg: '#eef2ff' },
    { label: 'Remboursé ce mois', value: '2.1M', sub: 'XAF', icon: 'check', color: '#059669', bg: '#ecfdf5' },
    { label: 'Rejeté', value: '3', icon: 'x', color: '#ef4444', bg: '#fef2f2' },
  ];

  const pendingReports = [
    { ref: 'NF-2026-0245', employee: 'Aminata Diallo', date: '12 Mai 2026', amount: '345 000', items: 5, status: 'En validation', statusColor: '#f59e0b' },
    { ref: 'NF-2026-0244', employee: 'Moussa Traoré', date: '10 Mai 2026', amount: '128 500', items: 3, status: 'En validation', statusColor: '#f59e0b' },
    { ref: 'NF-2026-0243', employee: 'Fatou Sow', date: '8 Mai 2026', amount: '89 200', items: 2, status: 'Soumise', statusColor: '#3b82f6' },
    { ref: 'NF-2026-0242', employee: 'Amadou Ba', date: '7 Mai 2026', amount: '567 000', items: 8, status: 'En validation', statusColor: '#f59e0b' },
    { ref: 'NF-2026-0241', employee: 'Aïssatou Camara', date: '5 Mai 2026', amount: '45 000', items: 1, status: 'Approuvée', statusColor: '#059669' },
  ];

  const categoryData = [
    { label: 'Transport', value: 35 },
    { label: 'Hébergement', value: 28 },
    { label: 'Restauration', value: 20 },
    { label: 'Fournitures', value: 10 },
    { label: 'Divers', value: 7 },
  ];

  const expenseDetail = {
    ref: 'NF-2026-0245',
    employee: 'Aminata Diallo',
    department: 'Ressources Humaines',
    date: '12 Mai 2026',
    lines: [
      { description: 'Vol Douala → Yaoundé', category: 'Transport', amount: '125 000', receipt: true },
      { description: 'Hôtel Hilton (2 nuits)', category: 'Hébergement', amount: '140 000', receipt: true },
      { description: 'Repas d\'affaires', category: 'Restauration', amount: '35 000', receipt: true },
      { description: 'Taxi aéroport', category: 'Transport', amount: '25 000', receipt: false },
      { description: 'Fournitures de bureau', category: 'Fournitures', amount: '20 000', receipt: true },
    ],
    total: '345 000',
  };

  return (
    <div>
      <PageHeader title="Notes de frais" subtitle="Gestion et validation des notes de frais" />

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

      {/* Table + Chart */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 24, marginBottom: 24 }}>
        {/* Pending Reports Table */}
        <div style={{ background: '#fff', borderRadius: 14, overflow: 'hidden', boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
          <div style={{ padding: '16px 20px', borderBottom: '1px solid #f1f5f9' }}>
            <h3 style={{ fontSize: 15, fontWeight: 700, color: '#1e293b' }}>Notes en attente</h3>
          </div>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
            <thead>
              <tr style={{ background: '#f8fafc' }}>
                <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Réf.</th>
                <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Employé</th>
                <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Date</th>
                <th style={{ padding: '12px 16px', textAlign: 'right', fontWeight: 600, color: '#64748b' }}>Montant</th>
                <th style={{ padding: '12px 16px', textAlign: 'center', fontWeight: 600, color: '#64748b' }}>Lignes</th>
                <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Statut</th>
              </tr>
            </thead>
            <tbody>
              {pendingReports.map((r, i) => (
                <tr key={i} style={{ borderTop: '1px solid #f1f5f9' }}>
                  <td style={{ padding: '12px 16px', fontWeight: 600, color: '#6366f1' }}>{r.ref}</td>
                  <td style={{ padding: '12px 16px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      <Avatar name={r.employee} size={28} />
                      <span style={{ color: '#1e293b' }}>{r.employee}</span>
                    </div>
                  </td>
                  <td style={{ padding: '12px 16px', color: '#64748b' }}>{r.date}</td>
                  <td style={{ padding: '12px 16px', textAlign: 'right', fontWeight: 600, color: '#1e293b' }}>{r.amount} XAF</td>
                  <td style={{ padding: '12px 16px', textAlign: 'center', color: '#64748b' }}>{r.items}</td>
                  <td style={{ padding: '12px 16px' }}>
                    <span style={{
                      background: `${r.statusColor}18`, color: r.statusColor,
                      padding: '4px 12px', borderRadius: 20, fontSize: 12, fontWeight: 600
                    }}>{r.status}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Category Chart */}
        <div style={{ background: '#fff', borderRadius: 14, padding: 24, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
          <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 16, color: '#1e293b' }}>Répartition par catégorie</h3>
          <Bars data={categoryData} height={220} />
        </div>
      </div>

      {/* Expense Detail Preview */}
      <div style={{ background: '#fff', borderRadius: 14, padding: 24, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <Avatar name={expenseDetail.employee} size={40} />
            <div>
              <div style={{ fontWeight: 700, color: '#1e293b' }}>{expenseDetail.employee}</div>
              <div style={{ fontSize: 12, color: '#64748b' }}>{expenseDetail.department} · {expenseDetail.date}</div>
            </div>
          </div>
          <span style={{
            background: '#eef2ff', color: '#6366f1',
            padding: '6px 16px', borderRadius: 20, fontSize: 13, fontWeight: 600
          }}>{expenseDetail.ref}</span>
        </div>

        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
          <thead>
            <tr style={{ background: '#f8fafc' }}>
              <th style={{ padding: '10px 14px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Description</th>
              <th style={{ padding: '10px 14px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Catégorie</th>
              <th style={{ padding: '10px 14px', textAlign: 'right', fontWeight: 600, color: '#64748b' }}>Montant</th>
              <th style={{ padding: '10px 14px', textAlign: 'center', fontWeight: 600, color: '#64748b' }}>Justificatif</th>
            </tr>
          </thead>
          <tbody>
            {expenseDetail.lines.map((l, i) => (
              <tr key={i} style={{ borderTop: '1px solid #f1f5f9' }}>
                <td style={{ padding: '10px 14px', color: '#1e293b' }}>{l.description}</td>
                <td style={{ padding: '10px 14px' }}>
                  <span style={{
                    background: '#f1f5f9', color: '#475569',
                    padding: '3px 10px', borderRadius: 12, fontSize: 12
                  }}>{l.category}</span>
                </td>
                <td style={{ padding: '10px 14px', textAlign: 'right', fontWeight: 600, color: '#1e293b' }}>{l.amount} XAF</td>
                <td style={{ padding: '10px 14px', textAlign: 'center' }}>
                  {l.receipt ? (
                    <Icons name="check" size={16} color="#059669" />
                  ) : (
                    <Icons name="x" size={16} color="#ef4444" />
                  )}
                </td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr style={{ borderTop: '2px solid #e2e8f0', background: '#f8fafc' }}>
              <td colSpan={2} style={{ padding: '12px 14px', fontWeight: 700, color: '#1e293b' }}>TOTAL</td>
              <td style={{ padding: '12px 14px', textAlign: 'right', fontWeight: 700, fontSize: 15, color: '#1e293b' }}>{expenseDetail.total} XAF</td>
              <td />
            </tr>
          </tfoot>
        </table>
      </div>
    </div>
  );
}
