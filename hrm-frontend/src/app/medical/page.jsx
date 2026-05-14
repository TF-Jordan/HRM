'use client';
import { useState } from 'react';
import { Icons } from '../../components/Icons';
import Avatar from '../../components/Avatar';
import PageHeader from '../../components/PageHeader';
import { Donut } from '../../components/Charts';

export default function Medical() {
  const kpis = [
    { label: 'Visites à jour', value: '218', icon: 'check', color: '#059669', bg: '#ecfdf5' },
    { label: 'Visites expirées', value: '7', icon: 'alert', color: '#ef4444', bg: '#fef2f2' },
    { label: 'Prochaines 30j', value: '15', icon: 'calendar', color: '#f59e0b', bg: '#fffbeb' },
    { label: 'Taux couverture', value: '97%', icon: 'shield', color: '#3b82f6', bg: '#dbeafe' },
  ];

  const aptitudeData = [
    { label: 'Apte', value: 205, color: '#059669' },
    { label: 'Apte avec réserves', value: 13, color: '#f59e0b' },
    { label: 'Inapte temporaire', value: 5, color: '#f97316' },
    { label: 'Non évalué', value: 29, color: '#e2e8f0' },
  ];

  const visits = [
    { employee: 'Aminata Diallo', type: 'Périodique', date: '10 Mai 2026', medecin: 'Dr. Mbala', result: 'Apte', resultColor: '#059669', nextDate: '10 Mai 2027' },
    { employee: 'Moussa Traoré', type: 'Reprise', date: '8 Mai 2026', medecin: 'Dr. Kouamé', result: 'Apte avec réserves', resultColor: '#f59e0b', nextDate: '8 Nov 2026' },
    { employee: 'Fatou Sow', type: 'Embauche', date: '5 Mai 2026', medecin: 'Dr. Mbala', result: 'Apte', resultColor: '#059669', nextDate: '5 Mai 2027' },
    { employee: 'Amadou Ba', type: 'Périodique', date: '2 Mai 2026', medecin: 'Dr. Ndiaye', result: 'Apte', resultColor: '#059669', nextDate: '2 Mai 2027' },
    { employee: 'Aïssatou Camara', type: 'Périodique', date: '28 Avr 2026', medecin: 'Dr. Mbala', result: 'Inapte temporaire', resultColor: '#f97316', nextDate: '28 Juil 2026' },
  ];

  const certificates = [
    { employee: 'Ibrahim Diop', type: 'Arrêt maladie', debut: '12 Mai 2026', fin: '19 Mai 2026', jours: 7, status: 'En cours', statusColor: '#3b82f6' },
    { employee: 'Mariam Konaté', type: 'Accident travail', debut: '5 Mai 2026', fin: '5 Juin 2026', jours: 30, status: 'En cours', statusColor: '#3b82f6' },
    { employee: 'Ousmane Sy', type: 'Arrêt maladie', debut: '1 Mai 2026', fin: '8 Mai 2026', jours: 7, status: 'Terminé', statusColor: '#059669' },
    { employee: 'Fatoumata Bah', type: 'Maternité', debut: '15 Avr 2026', fin: '15 Juil 2026', jours: 90, status: 'En cours', statusColor: '#3b82f6' },
  ];

  return (
    <div>
      <PageHeader title="Médical" subtitle="Suivi médical et visites de santé au travail" />

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

      {/* Alert Banner */}
      <div style={{
        background: '#fef2f2', border: '1px solid #fecaca', borderRadius: 12,
        padding: '14px 20px', marginBottom: 24, display: 'flex', alignItems: 'center', gap: 12
      }}>
        <Icons name="alert" size={20} color="#ef4444" />
        <div style={{ flex: 1 }}>
          <span style={{ fontWeight: 600, color: '#991b1b' }}>7 visites médicales expirant</span>
          <span style={{ color: '#b91c1c', fontSize: 13 }}> — Ces employés doivent repasser leur visite médicale dans les plus brefs délais.</span>
        </div>
        <button style={{
          background: '#ef4444', color: '#fff', border: 'none', borderRadius: 8,
          padding: '8px 16px', fontSize: 13, fontWeight: 600, cursor: 'pointer'
        }}>Voir la liste</button>
      </div>

      {/* Visits Table + Aptitude Donut */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 24, marginBottom: 24 }}>
        <div style={{ background: '#fff', borderRadius: 14, overflow: 'hidden', boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
          <div style={{ padding: '16px 20px', borderBottom: '1px solid #f1f5f9' }}>
            <h3 style={{ fontSize: 15, fontWeight: 700, color: '#1e293b' }}>Dernières visites médicales</h3>
          </div>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
            <thead>
              <tr style={{ background: '#f8fafc' }}>
                <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Employé</th>
                <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Type</th>
                <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Date</th>
                <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Médecin</th>
                <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Résultat</th>
                <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Prochaine</th>
              </tr>
            </thead>
            <tbody>
              {visits.map((v, i) => (
                <tr key={i} style={{ borderTop: '1px solid #f1f5f9' }}>
                  <td style={{ padding: '12px 16px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                      <Avatar name={v.employee} size={32} />
                      <span style={{ fontWeight: 600, color: '#1e293b' }}>{v.employee}</span>
                    </div>
                  </td>
                  <td style={{ padding: '12px 16px', color: '#475569' }}>{v.type}</td>
                  <td style={{ padding: '12px 16px', color: '#64748b' }}>{v.date}</td>
                  <td style={{ padding: '12px 16px', color: '#475569' }}>{v.medecin}</td>
                  <td style={{ padding: '12px 16px' }}>
                    <span style={{
                      background: `${v.resultColor}18`, color: v.resultColor,
                      padding: '4px 12px', borderRadius: 20, fontSize: 12, fontWeight: 600
                    }}>{v.result}</span>
                  </td>
                  <td style={{ padding: '12px 16px', color: '#64748b' }}>{v.nextDate}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div style={{ background: '#fff', borderRadius: 14, padding: 24, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
          <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 16, color: '#1e293b' }}>Statut d'aptitude</h3>
          <Donut data={aptitudeData} size={180} />
        </div>
      </div>

      {/* Medical Certificates Table */}
      <div style={{ background: '#fff', borderRadius: 14, overflow: 'hidden', boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
        <div style={{ padding: '16px 20px', borderBottom: '1px solid #f1f5f9' }}>
          <h3 style={{ fontSize: 15, fontWeight: 700, color: '#1e293b' }}>Certificats médicaux en cours</h3>
        </div>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
          <thead>
            <tr style={{ background: '#f8fafc' }}>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Employé</th>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Type</th>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Début</th>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Fin</th>
              <th style={{ padding: '12px 16px', textAlign: 'center', fontWeight: 600, color: '#64748b' }}>Jours</th>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Statut</th>
            </tr>
          </thead>
          <tbody>
            {certificates.map((c, i) => (
              <tr key={i} style={{ borderTop: '1px solid #f1f5f9' }}>
                <td style={{ padding: '12px 16px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    <Avatar name={c.employee} size={32} />
                    <span style={{ fontWeight: 600, color: '#1e293b' }}>{c.employee}</span>
                  </div>
                </td>
                <td style={{ padding: '12px 16px', color: '#475569' }}>{c.type}</td>
                <td style={{ padding: '12px 16px', color: '#64748b' }}>{c.debut}</td>
                <td style={{ padding: '12px 16px', color: '#64748b' }}>{c.fin}</td>
                <td style={{ padding: '12px 16px', textAlign: 'center', fontWeight: 600, color: '#1e293b' }}>{c.jours}</td>
                <td style={{ padding: '12px 16px' }}>
                  <span style={{
                    background: `${c.statusColor}18`, color: c.statusColor,
                    padding: '4px 12px', borderRadius: 20, fontSize: 12, fontWeight: 600
                  }}>{c.status}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
