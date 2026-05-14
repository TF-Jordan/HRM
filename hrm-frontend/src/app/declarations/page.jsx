'use client';
import { useState } from 'react';
import { Icons } from '../../components/Icons';
import PageHeader from '../../components/PageHeader';

export default function Declarations() {
  const kpis = [
    { label: 'Déclarations à jour', value: '8', icon: 'check', color: '#059669', bg: '#ecfdf5' },
    { label: 'En retard', value: '1', icon: 'alert', color: '#ef4444', bg: '#fef2f2' },
    { label: 'En préparation', value: '3', icon: 'file', color: '#f59e0b', bg: '#fffbeb' },
    { label: 'Prochaine échéance', value: '15 Mai', icon: 'calendar', color: '#3b82f6', bg: '#dbeafe' },
  ];

  const declarations = [
    { ref: 'DSN-2026-05', type: 'DSN Mensuelle', period: 'Mai 2026', deadline: '15 Mai 2026', status: 'En retard', statusColor: '#ef4444', amount: '48.2M XAF' },
    { ref: 'CNPS-2026-Q2', type: 'CNPS Trimestrielle', period: 'Q2 2026', deadline: '15 Juil 2026', status: 'En préparation', statusColor: '#f59e0b', amount: '12.5M XAF' },
    { ref: 'IRPP-2026-05', type: 'IRPP Mensuel', period: 'Mai 2026', deadline: '15 Juin 2026', status: 'En préparation', statusColor: '#f59e0b', amount: '18.7M XAF' },
    { ref: 'TCS-2026-05', type: 'Taxe communale', period: 'Mai 2026', deadline: '15 Juin 2026', status: 'En préparation', statusColor: '#f59e0b', amount: '3.2M XAF' },
    { ref: 'DSN-2026-04', type: 'DSN Mensuelle', period: 'Avril 2026', deadline: '15 Mai 2026', status: 'Déposée', statusColor: '#059669', amount: '47.8M XAF' },
    { ref: 'DSN-2026-03', type: 'DSN Mensuelle', period: 'Mars 2026', deadline: '15 Avr 2026', status: 'Déposée', statusColor: '#059669', amount: '46.5M XAF' },
    { ref: 'CNPS-2026-Q1', type: 'CNPS Trimestrielle', period: 'Q1 2026', deadline: '15 Avr 2026', status: 'Déposée', statusColor: '#059669', amount: '11.8M XAF' },
    { ref: 'DADS-2025', type: 'DADS Annuelle', period: '2025', deadline: '31 Jan 2026', status: 'Déposée', statusColor: '#059669', amount: '156.2M XAF' },
  ];

  const months = [
    { name: 'Jan', status: 'done' }, { name: 'Fév', status: 'done' }, { name: 'Mar', status: 'done' },
    { name: 'Avr', status: 'done' }, { name: 'Mai', status: 'late' }, { name: 'Juin', status: 'upcoming' },
    { name: 'Juil', status: 'upcoming' }, { name: 'Août', status: 'future' }, { name: 'Sep', status: 'future' },
    { name: 'Oct', status: 'future' }, { name: 'Nov', status: 'future' }, { name: 'Déc', status: 'future' },
  ];

  const calendarEvents = {
    Jan: ['DADS 2025'],
    Fév: ['DSN Fév'],
    Mar: ['DSN Mar'],
    Avr: ['DSN Avr', 'CNPS Q1'],
    Mai: ['DSN Mai'],
    Juin: ['IRPP Mai', 'TCS Mai'],
    Juil: ['DSN Juin', 'CNPS Q2'],
    Août: ['DSN Juil'],
    Sep: ['DSN Août'],
    Oct: ['DSN Sep', 'CNPS Q3'],
    Nov: ['DSN Oct'],
    Déc: ['DSN Nov'],
  };

  const statusColors = {
    done: { bg: '#ecfdf5', border: '#86efac', text: '#059669' },
    late: { bg: '#fef2f2', border: '#fca5a5', text: '#ef4444' },
    upcoming: { bg: '#fffbeb', border: '#fde68a', text: '#d97706' },
    future: { bg: '#f8fafc', border: '#e2e8f0', text: '#94a3b8' },
  };

  return (
    <div>
      <PageHeader title="Déclarations" subtitle="Déclarations sociales et fiscales" />

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
              <div style={{ fontSize: 22, fontWeight: 700, color: '#1e293b' }}>{k.value}</div>
            </div>
          </div>
        ))}
      </div>

      {/* Urgent Banner */}
      <div style={{
        background: '#fef2f2', border: '1px solid #fecaca', borderRadius: 12,
        padding: '14px 20px', marginBottom: 24, display: 'flex', alignItems: 'center', gap: 12
      }}>
        <div style={{
          width: 36, height: 36, borderRadius: 8, background: '#fee2e2',
          display: 'flex', alignItems: 'center', justifyContent: 'center'
        }}>
          <Icons name="alert" size={20} color="#ef4444" />
        </div>
        <div style={{ flex: 1 }}>
          <div style={{ fontWeight: 700, color: '#991b1b', fontSize: 14 }}>DSN Mai 2026 — Échéance dépassée</div>
          <div style={{ color: '#b91c1c', fontSize: 13 }}>La déclaration sociale nominative de Mai 2026 devait être déposée avant le 15 Mai 2026. Veuillez régulariser immédiatement.</div>
        </div>
        <button style={{
          background: '#ef4444', color: '#fff', border: 'none', borderRadius: 8,
          padding: '8px 16px', fontSize: 13, fontWeight: 600, cursor: 'pointer', whiteSpace: 'nowrap'
        }}>Déposer maintenant</button>
      </div>

      {/* Declarations Table */}
      <div style={{ background: '#fff', borderRadius: 14, overflow: 'hidden', boxShadow: '0 1px 4px rgba(0,0,0,0.06)', marginBottom: 24 }}>
        <div style={{ padding: '16px 20px', borderBottom: '1px solid #f1f5f9' }}>
          <h3 style={{ fontSize: 15, fontWeight: 700, color: '#1e293b' }}>Toutes les déclarations</h3>
        </div>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
          <thead>
            <tr style={{ background: '#f8fafc' }}>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Réf.</th>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Type</th>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Période</th>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Échéance</th>
              <th style={{ padding: '12px 16px', textAlign: 'right', fontWeight: 600, color: '#64748b' }}>Montant</th>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Statut</th>
            </tr>
          </thead>
          <tbody>
            {declarations.map((d, i) => (
              <tr key={i} style={{ borderTop: '1px solid #f1f5f9' }}>
                <td style={{ padding: '12px 16px', fontWeight: 600, color: '#6366f1' }}>{d.ref}</td>
                <td style={{ padding: '12px 16px', color: '#1e293b' }}>{d.type}</td>
                <td style={{ padding: '12px 16px', color: '#475569' }}>{d.period}</td>
                <td style={{ padding: '12px 16px', color: '#64748b' }}>{d.deadline}</td>
                <td style={{ padding: '12px 16px', textAlign: 'right', fontWeight: 600, color: '#1e293b' }}>{d.amount}</td>
                <td style={{ padding: '12px 16px' }}>
                  <span style={{
                    background: `${d.statusColor}18`, color: d.statusColor,
                    padding: '4px 12px', borderRadius: 20, fontSize: 12, fontWeight: 600
                  }}>{d.status}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Compliance Calendar */}
      <div style={{ background: '#fff', borderRadius: 14, padding: 24, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
        <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 20, color: '#1e293b' }}>Calendrier de conformité 2026</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 12 }}>
          {months.map((m, i) => {
            const colors = statusColors[m.status];
            const events = calendarEvents[m.name] || [];
            return (
              <div key={i} style={{
                background: colors.bg, border: `1px solid ${colors.border}`,
                borderRadius: 10, padding: 14, minHeight: 80
              }}>
                <div style={{
                  display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8
                }}>
                  <span style={{ fontWeight: 700, fontSize: 13, color: colors.text }}>{m.name}</span>
                  {m.status === 'done' && <Icons name="check" size={14} color="#059669" />}
                  {m.status === 'late' && <Icons name="alert" size={14} color="#ef4444" />}
                  {m.status === 'upcoming' && <Icons name="time" size={14} color="#d97706" />}
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                  {events.map((ev, ei) => (
                    <div key={ei} style={{
                      fontSize: 10, color: colors.text, background: `${colors.border}66`,
                      padding: '2px 6px', borderRadius: 4, whiteSpace: 'nowrap', overflow: 'hidden',
                      textOverflow: 'ellipsis'
                    }}>{ev}</div>
                  ))}
                </div>
              </div>
            );
          })}
        </div>

        {/* Legend */}
        <div style={{ display: 'flex', gap: 20, marginTop: 16, justifyContent: 'center' }}>
          {[
            { label: 'Déposée', color: '#059669', bg: '#ecfdf5' },
            { label: 'En retard', color: '#ef4444', bg: '#fef2f2' },
            { label: 'À venir', color: '#d97706', bg: '#fffbeb' },
            { label: 'Futur', color: '#94a3b8', bg: '#f8fafc' },
          ].map((l, i) => (
            <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12, color: '#64748b' }}>
              <div style={{ width: 12, height: 12, borderRadius: 3, background: l.bg, border: `1px solid ${l.color}40` }} />
              {l.label}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
