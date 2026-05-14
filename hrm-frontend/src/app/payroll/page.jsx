'use client';
import { useState } from 'react';
import { Icons } from '../../components/Icons';
import Avatar from '../../components/Avatar';
import PageHeader from '../../components/PageHeader';
import { Donut, Line } from '../../components/Charts';

export default function Payroll() {
  const [tab, setTab] = useState('runs');

  const steps = [
    { label: 'Collecte', done: true },
    { label: 'Calcul', done: true },
    { label: 'Validation', done: false, active: true },
    { label: 'Virement', done: false },
    { label: 'Clôture', done: false },
  ];

  const summaryStats = [
    { label: 'Employés payés', value: '247', sub: '/252', icon: 'users', color: '#a78bfa' },
    { label: 'Masse brute', value: '156.4M', sub: 'XAF', icon: 'money', color: '#34d399' },
    { label: 'Cotisations', value: '48.2M', sub: 'XAF', icon: 'shield', color: '#fbbf24' },
    { label: 'Net à payer', value: '108.2M', sub: 'XAF', icon: 'wallet', color: '#60a5fa' },
  ];

  const salaryEvolution = [
    { month: 'Jan', brut: 145, net: 98 },
    { month: 'Fév', brut: 148, net: 100 },
    { month: 'Mar', brut: 150, net: 101 },
    { month: 'Avr', brut: 152, net: 103 },
    { month: 'Mai', brut: 156, net: 108 },
  ];

  const compositionData = [
    { label: 'Salaire de base', value: 65, color: '#6366f1' },
    { label: 'Primes', value: 15, color: '#a78bfa' },
    { label: 'Heures sup.', value: 8, color: '#34d399' },
    { label: 'Avantages', value: 12, color: '#fbbf24' },
  ];

  const payrollRuns = [
    { period: 'Mai 2026', status: 'En cours', employees: 247, brut: '156.4M', net: '108.2M', statusColor: '#fbbf24' },
    { period: 'Avril 2026', status: 'Clôturé', employees: 250, brut: '154.8M', net: '106.9M', statusColor: '#34d399' },
    { period: 'Mars 2026', status: 'Clôturé', employees: 248, brut: '150.2M', net: '103.5M', statusColor: '#34d399' },
    { period: 'Février 2026', status: 'Clôturé', employees: 245, brut: '148.0M', net: '102.1M', statusColor: '#34d399' },
    { period: 'Janvier 2026', status: 'Clôturé', employees: 244, brut: '145.5M', net: '100.3M', statusColor: '#34d399' },
  ];

  const payslipLines = [
    { label: 'Salaire de base', base: '160h', rate: '3 500', gain: '560 000', deduction: '' },
    { label: 'Prime d\'ancienneté', base: '5%', rate: '', gain: '28 000', deduction: '' },
    { label: 'Prime de transport', base: '', rate: '', gain: '25 000', deduction: '' },
    { label: 'Heures supplémentaires', base: '12h', rate: '3 063', gain: '36 750', deduction: '' },
    { label: 'CNPS (Vieillesse)', base: '649 750', rate: '4.2%', gain: '', deduction: '27 290' },
    { label: 'CNPS (Assurance maladie)', base: '649 750', rate: '1.0%', gain: '', deduction: '6 498' },
    { label: 'Impôt sur le revenu (IRPP)', base: '', rate: '', gain: '', deduction: '78 500' },
    { label: 'Taxe communale', base: '', rate: '1%', gain: '', deduction: '6 498' },
  ];

  return (
    <div>
      <PageHeader title="Paie" subtitle="Gestion de la paie et des bulletins de salaire" />

      {/* Hero Card */}
      <div style={{
        background: 'linear-gradient(135deg, #1e1b4b 0%, #312e81 100%)',
        borderRadius: 16, padding: 32, marginBottom: 24, color: '#fff'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
          <div>
            <div style={{ fontSize: 14, opacity: 0.7, marginBottom: 4 }}>Période en cours</div>
            <div style={{ fontSize: 24, fontWeight: 700 }}>Mai 2026</div>
          </div>
          <span style={{
            background: 'rgba(251,191,36,0.2)', color: '#fbbf24',
            padding: '6px 16px', borderRadius: 20, fontSize: 13, fontWeight: 600
          }}>En validation</span>
        </div>

        {/* Stepper */}
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: 32, gap: 0 }}>
          {steps.map((s, i) => (
            <div key={i} style={{ display: 'flex', alignItems: 'center', flex: 1 }}>
              <div style={{
                width: 32, height: 32, borderRadius: '50%',
                background: s.done ? '#34d399' : s.active ? '#fbbf24' : 'rgba(255,255,255,0.2)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: 13, fontWeight: 700, color: s.done || s.active ? '#1e1b4b' : '#fff',
                flexShrink: 0
              }}>
                {s.done ? '✓' : i + 1}
              </div>
              <div style={{ fontSize: 12, marginLeft: 8, opacity: s.done || s.active ? 1 : 0.5 }}>{s.label}</div>
              {i < steps.length - 1 && (
                <div style={{
                  flex: 1, height: 2, marginLeft: 12,
                  background: s.done ? '#34d399' : 'rgba(255,255,255,0.2)'
                }} />
              )}
            </div>
          ))}
        </div>

        {/* Summary Stats */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 16 }}>
          {summaryStats.map((stat, i) => (
            <div key={i} style={{
              background: 'rgba(255,255,255,0.08)', borderRadius: 12, padding: 16
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                <div style={{
                  width: 32, height: 32, borderRadius: 8,
                  background: `${stat.color}22`, display: 'flex',
                  alignItems: 'center', justifyContent: 'center'
                }}>
                  <Icons name={stat.icon} size={16} color={stat.color} />
                </div>
                <span style={{ fontSize: 12, opacity: 0.7 }}>{stat.label}</span>
              </div>
              <div style={{ fontSize: 22, fontWeight: 700 }}>
                {stat.value} <span style={{ fontSize: 13, opacity: 0.6 }}>{stat.sub}</span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Charts */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 24, marginBottom: 24 }}>
        <div style={{ background: '#fff', borderRadius: 14, padding: 24, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
          <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 16, color: '#1e293b' }}>Évolution masse salariale (M XAF)</h3>
          <Line data={salaryEvolution} xKey="month" series={[
            { key: 'brut', color: '#6366f1', label: 'Brut' },
            { key: 'net', color: '#34d399', label: 'Net' },
          ]} height={220} />
        </div>
        <div style={{ background: '#fff', borderRadius: 14, padding: 24, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
          <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 16, color: '#1e293b' }}>Composition rémunération</h3>
          <Donut data={compositionData} size={180} />
        </div>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
        {[
          { key: 'runs', label: 'Cycles de paie' },
          { key: 'slip', label: 'Bulletin exemple' },
        ].map(t => (
          <button key={t.key} onClick={() => setTab(t.key)} style={{
            padding: '8px 20px', borderRadius: 8, border: 'none', cursor: 'pointer',
            background: tab === t.key ? '#6366f1' : '#f1f5f9',
            color: tab === t.key ? '#fff' : '#64748b',
            fontWeight: 600, fontSize: 13
          }}>{t.label}</button>
        ))}
      </div>

      {/* Payroll Runs Table */}
      {tab === 'runs' && (
        <div style={{ background: '#fff', borderRadius: 14, overflow: 'hidden', boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
            <thead>
              <tr style={{ background: '#f8fafc' }}>
                <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Période</th>
                <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Statut</th>
                <th style={{ padding: '12px 16px', textAlign: 'right', fontWeight: 600, color: '#64748b' }}>Employés</th>
                <th style={{ padding: '12px 16px', textAlign: 'right', fontWeight: 600, color: '#64748b' }}>Masse brute</th>
                <th style={{ padding: '12px 16px', textAlign: 'right', fontWeight: 600, color: '#64748b' }}>Net à payer</th>
              </tr>
            </thead>
            <tbody>
              {payrollRuns.map((r, i) => (
                <tr key={i} style={{ borderTop: '1px solid #f1f5f9' }}>
                  <td style={{ padding: '12px 16px', fontWeight: 600, color: '#1e293b' }}>{r.period}</td>
                  <td style={{ padding: '12px 16px' }}>
                    <span style={{
                      background: `${r.statusColor}22`, color: r.statusColor,
                      padding: '4px 12px', borderRadius: 20, fontSize: 12, fontWeight: 600
                    }}>{r.status}</span>
                  </td>
                  <td style={{ padding: '12px 16px', textAlign: 'right', color: '#475569' }}>{r.employees}</td>
                  <td style={{ padding: '12px 16px', textAlign: 'right', fontWeight: 600, color: '#1e293b' }}>{r.brut} XAF</td>
                  <td style={{ padding: '12px 16px', textAlign: 'right', fontWeight: 600, color: '#1e293b' }}>{r.net} XAF</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Payslip Preview */}
      {tab === 'slip' && (
        <div style={{ background: '#fff', borderRadius: 14, padding: 32, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <Avatar name="Aminata Diallo" size={40} />
              <div>
                <div style={{ fontWeight: 700, color: '#1e293b' }}>Aminata Diallo</div>
                <div style={{ fontSize: 12, color: '#64748b' }}>Responsable RH · Mai 2026</div>
              </div>
            </div>
            <span style={{
              background: '#dbeafe', color: '#3b82f6',
              padding: '4px 12px', borderRadius: 20, fontSize: 12, fontWeight: 600
            }}>Bulletin N° 2026-05-042</span>
          </div>

          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13, marginBottom: 24 }}>
            <thead>
              <tr style={{ background: '#f8fafc' }}>
                <th style={{ padding: '10px 14px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Rubrique</th>
                <th style={{ padding: '10px 14px', textAlign: 'right', fontWeight: 600, color: '#64748b' }}>Base</th>
                <th style={{ padding: '10px 14px', textAlign: 'right', fontWeight: 600, color: '#64748b' }}>Taux</th>
                <th style={{ padding: '10px 14px', textAlign: 'right', fontWeight: 600, color: '#64748b' }}>Gains</th>
                <th style={{ padding: '10px 14px', textAlign: 'right', fontWeight: 600, color: '#64748b' }}>Retenues</th>
              </tr>
            </thead>
            <tbody>
              {payslipLines.map((l, i) => (
                <tr key={i} style={{ borderTop: '1px solid #f1f5f9' }}>
                  <td style={{ padding: '10px 14px', color: '#1e293b' }}>{l.label}</td>
                  <td style={{ padding: '10px 14px', textAlign: 'right', color: '#475569' }}>{l.base}</td>
                  <td style={{ padding: '10px 14px', textAlign: 'right', color: '#475569' }}>{l.rate}</td>
                  <td style={{ padding: '10px 14px', textAlign: 'right', color: '#34d399', fontWeight: 600 }}>{l.gain}</td>
                  <td style={{ padding: '10px 14px', textAlign: 'right', color: '#ef4444', fontWeight: 600 }}>{l.deduction}</td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr style={{ borderTop: '2px solid #e2e8f0' }}>
                <td colSpan={3} style={{ padding: '12px 14px', fontWeight: 700, color: '#1e293b' }}>TOTAUX</td>
                <td style={{ padding: '12px 14px', textAlign: 'right', fontWeight: 700, color: '#34d399' }}>649 750</td>
                <td style={{ padding: '12px 14px', textAlign: 'right', fontWeight: 700, color: '#ef4444' }}>118 786</td>
              </tr>
              <tr style={{ background: '#f0fdf4' }}>
                <td colSpan={4} style={{ padding: '12px 14px', fontWeight: 700, fontSize: 15, color: '#1e293b' }}>NET À PAYER</td>
                <td style={{ padding: '12px 14px', textAlign: 'right', fontWeight: 700, fontSize: 15, color: '#059669' }}>530 964 XAF</td>
              </tr>
            </tfoot>
          </table>
        </div>
      )}
    </div>
  );
}
