'use client';
import { useState } from 'react';
import { Icons } from '../../components/Icons';
import Avatar from '../../components/Avatar';
import PageHeader from '../../components/PageHeader';
import { Donut } from '../../components/Charts';

export default function Reviews() {
  const kpis = [
    { label: 'Évaluations en cours', value: '45', icon: 'star', color: '#6366f1', bg: '#eef2ff' },
    { label: 'Complétées', value: '187', icon: 'check', color: '#059669', bg: '#ecfdf5' },
    { label: 'Score moyen', value: '3.8', sub: '/5', icon: 'chart', color: '#f59e0b', bg: '#fffbeb' },
    { label: 'Taux complétion', value: '81%', icon: 'target', color: '#3b82f6', bg: '#dbeafe' },
  ];

  const scoreDistribution = [
    { score: '5 - Exceptionnel', count: 28, pct: 12, color: '#059669' },
    { score: '4 - Très bon', count: 72, pct: 31, color: '#34d399' },
    { score: '3 - Satisfaisant', count: 89, pct: 38, color: '#fbbf24' },
    { score: '2 - À améliorer', count: 34, pct: 15, color: '#f97316' },
    { score: '1 - Insuffisant', count: 9, pct: 4, color: '#ef4444' },
  ];

  const statusData = [
    { label: 'Complétée', value: 187, color: '#059669' },
    { label: 'En cours', value: 45, color: '#f59e0b' },
    { label: 'Non démarrée', value: 20, color: '#e2e8f0' },
  ];

  const nineBoxLabels = {
    rows: ['Haut', 'Moyen', 'Bas'],
    cols: ['Bas', 'Moyen', 'Haut'],
  };
  const nineBoxData = [
    [{ count: 2, color: '#fef3c7', label: 'Énigme' }, { count: 8, color: '#d1fae5', label: 'Futur leader' }, { count: 12, color: '#bbf7d0', label: 'Star' }],
    [{ count: 5, color: '#fee2e2', label: 'Risque' }, { count: 45, color: '#e0e7ff', label: 'Contributeur clé' }, { count: 28, color: '#d1fae5', label: 'Forte perf.' }],
    [{ count: 8, color: '#fecaca', label: 'Sous-perf.' }, { count: 18, color: '#fef3c7', label: 'Efficace' }, { count: 15, color: '#e0e7ff', label: 'Expert' }],
  ];

  const reviews = [
    { employee: 'Aminata Diallo', manager: 'Jean-Pierre Mbala', period: '2025-2026', score: 4.2, status: 'Complétée', statusColor: '#059669' },
    { employee: 'Moussa Traoré', manager: 'Jean-Pierre Mbala', period: '2025-2026', score: 3.8, status: 'Complétée', statusColor: '#059669' },
    { employee: 'Fatou Sow', manager: 'Aminata Diallo', period: '2025-2026', score: null, status: 'En cours', statusColor: '#f59e0b' },
    { employee: 'Amadou Ba', manager: 'Aminata Diallo', period: '2025-2026', score: 3.5, status: 'Complétée', statusColor: '#059669' },
    { employee: 'Aïssatou Camara', manager: 'Moussa Traoré', period: '2025-2026', score: null, status: 'Non démarrée', statusColor: '#94a3b8' },
  ];

  return (
    <div>
      <PageHeader title="Évaluations" subtitle="Évaluations de performance et matrices de talent" />

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
                {k.value}{k.sub && <span style={{ fontSize: 13, color: '#94a3b8' }}>{k.sub}</span>}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Score Distribution + Status Donut */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 24, marginBottom: 24 }}>
        <div style={{ background: '#fff', borderRadius: 14, padding: 24, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
          <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 16, color: '#1e293b' }}>Distribution des scores</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {scoreDistribution.map((s, i) => (
              <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                <div style={{ width: 140, fontSize: 13, color: '#475569', flexShrink: 0 }}>{s.score}</div>
                <div style={{ flex: 1, height: 24, background: '#f1f5f9', borderRadius: 6, overflow: 'hidden' }}>
                  <div style={{
                    width: `${s.pct}%`, height: '100%', background: s.color, borderRadius: 6,
                    display: 'flex', alignItems: 'center', paddingLeft: 8
                  }}>
                    <span style={{ fontSize: 11, fontWeight: 600, color: '#fff' }}>{s.count}</span>
                  </div>
                </div>
                <span style={{ fontSize: 12, color: '#94a3b8', width: 36, textAlign: 'right' }}>{s.pct}%</span>
              </div>
            ))}
          </div>
        </div>
        <div style={{ background: '#fff', borderRadius: 14, padding: 24, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
          <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 16, color: '#1e293b' }}>Statut des évaluations</h3>
          <Donut data={statusData} size={180} />
        </div>
      </div>

      {/* 9-Box Matrix */}
      <div style={{ background: '#fff', borderRadius: 14, padding: 24, boxShadow: '0 1px 4px rgba(0,0,0,0.06)', marginBottom: 24 }}>
        <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 20, color: '#1e293b' }}>Matrice 9-Box (Performance vs Potentiel)</h3>
        <div style={{ display: 'flex', gap: 16 }}>
          {/* Y-axis label */}
          <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', gap: 4 }}>
            <div style={{
              writingMode: 'vertical-rl', textOrientation: 'mixed', transform: 'rotate(180deg)',
              fontSize: 12, fontWeight: 600, color: '#64748b', textAlign: 'center'
            }}>Potentiel</div>
          </div>

          <div style={{ flex: 1 }}>
            {/* Row labels + grid */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
              {nineBoxData.map((row, ri) => (
                <div key={ri} style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                  <div style={{ width: 50, fontSize: 11, color: '#94a3b8', textAlign: 'right', paddingRight: 8 }}>
                    {nineBoxLabels.rows[ri]}
                  </div>
                  {row.map((cell, ci) => (
                    <div key={ci} style={{
                      flex: 1, background: cell.color, borderRadius: 10, padding: 16,
                      textAlign: 'center', minHeight: 80, display: 'flex', flexDirection: 'column',
                      alignItems: 'center', justifyContent: 'center', gap: 4
                    }}>
                      <div style={{ fontSize: 22, fontWeight: 700, color: '#1e293b' }}>{cell.count}</div>
                      <div style={{ fontSize: 11, color: '#475569', fontWeight: 500 }}>{cell.label}</div>
                    </div>
                  ))}
                </div>
              ))}
            </div>

            {/* X-axis labels */}
            <div style={{ display: 'flex', gap: 4, marginTop: 8, paddingLeft: 54 }}>
              {nineBoxLabels.cols.map((label, i) => (
                <div key={i} style={{ flex: 1, textAlign: 'center', fontSize: 11, color: '#94a3b8' }}>{label}</div>
              ))}
            </div>
            <div style={{ textAlign: 'center', fontSize: 12, fontWeight: 600, color: '#64748b', marginTop: 4 }}>Performance</div>
          </div>
        </div>
      </div>

      {/* Reviews Table */}
      <div style={{ background: '#fff', borderRadius: 14, overflow: 'hidden', boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
        <div style={{ padding: '16px 20px', borderBottom: '1px solid #f1f5f9' }}>
          <h3 style={{ fontSize: 15, fontWeight: 700, color: '#1e293b' }}>Évaluations récentes</h3>
        </div>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
          <thead>
            <tr style={{ background: '#f8fafc' }}>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Employé</th>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Manager</th>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Période</th>
              <th style={{ padding: '12px 16px', textAlign: 'center', fontWeight: 600, color: '#64748b' }}>Score</th>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Statut</th>
            </tr>
          </thead>
          <tbody>
            {reviews.map((r, i) => (
              <tr key={i} style={{ borderTop: '1px solid #f1f5f9' }}>
                <td style={{ padding: '12px 16px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    <Avatar name={r.employee} size={32} />
                    <span style={{ fontWeight: 600, color: '#1e293b' }}>{r.employee}</span>
                  </div>
                </td>
                <td style={{ padding: '12px 16px', color: '#475569' }}>{r.manager}</td>
                <td style={{ padding: '12px 16px', color: '#475569' }}>{r.period}</td>
                <td style={{ padding: '12px 16px', textAlign: 'center' }}>
                  {r.score ? (
                    <span style={{
                      background: r.score >= 4 ? '#ecfdf5' : r.score >= 3 ? '#fffbeb' : '#fef2f2',
                      color: r.score >= 4 ? '#059669' : r.score >= 3 ? '#d97706' : '#ef4444',
                      padding: '4px 12px', borderRadius: 20, fontSize: 13, fontWeight: 700
                    }}>{r.score}</span>
                  ) : (
                    <span style={{ color: '#94a3b8' }}>—</span>
                  )}
                </td>
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
    </div>
  );
}
