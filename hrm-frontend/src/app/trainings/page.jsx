'use client';
import { useState } from 'react';
import { Icons } from '../../components/Icons';
import Avatar from '../../components/Avatar';
import PageHeader from '../../components/PageHeader';

export default function Trainings() {
  const [category, setCategory] = useState('all');

  const kpis = [
    { label: 'Formations actives', value: '18', icon: 'book', color: '#6366f1', bg: '#eef2ff' },
    { label: 'Participants', value: '124', icon: 'users', color: '#059669', bg: '#ecfdf5' },
    { label: 'Budget utilisé', value: '67%', icon: 'money', color: '#f59e0b', bg: '#fffbeb' },
    { label: 'Taux réussite', value: '92%', icon: 'check', color: '#3b82f6', bg: '#dbeafe' },
  ];

  const categories = [
    { key: 'all', label: 'Toutes' },
    { key: 'tech', label: 'Technique' },
    { key: 'management', label: 'Management' },
    { key: 'soft', label: 'Soft Skills' },
    { key: 'compliance', label: 'Conformité' },
    { key: 'language', label: 'Langues' },
  ];

  const trainings = [
    {
      title: 'Leadership & Management', category: 'management', duration: '3 jours',
      enrolled: 18, capacity: 20, startDate: '20 Mai 2026', instructor: 'Dr. Kouamé',
      gradient: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)', status: 'En cours'
    },
    {
      title: 'Excel Avancé', category: 'tech', duration: '2 jours',
      enrolled: 25, capacity: 25, startDate: '15 Mai 2026', instructor: 'Mme. Ndiaye',
      gradient: 'linear-gradient(135deg, #059669 0%, #34d399 100%)', status: 'Complet'
    },
    {
      title: 'Communication efficace', category: 'soft', duration: '1 jour',
      enrolled: 12, capacity: 30, startDate: '25 Mai 2026', instructor: 'M. Toure',
      gradient: 'linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%)', status: 'Inscriptions'
    },
    {
      title: 'Droit du travail OHADA', category: 'compliance', duration: '2 jours',
      enrolled: 15, capacity: 20, startDate: '1 Juin 2026', instructor: 'Me. Bakayoko',
      gradient: 'linear-gradient(135deg, #ef4444 0%, #f97316 100%)', status: 'Inscriptions'
    },
    {
      title: 'Anglais professionnel B2', category: 'language', duration: '12 semaines',
      enrolled: 8, capacity: 15, startDate: '3 Juin 2026', instructor: 'Mr. Smith',
      gradient: 'linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%)', status: 'Inscriptions'
    },
    {
      title: 'Gestion de projet Agile', category: 'management', duration: '3 jours',
      enrolled: 20, capacity: 20, startDate: '10 Mai 2026', instructor: 'M. Diallo',
      gradient: 'linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%)', status: 'Terminée'
    },
  ];

  const enrollments = [
    { employee: 'Aminata Diallo', training: 'Leadership & Management', date: '12 Mai 2026', progress: 60, status: 'En cours', statusColor: '#3b82f6' },
    { employee: 'Moussa Traoré', training: 'Excel Avancé', date: '10 Mai 2026', progress: 100, status: 'Terminée', statusColor: '#059669' },
    { employee: 'Fatou Sow', training: 'Communication efficace', date: '14 Mai 2026', progress: 0, status: 'Inscrite', statusColor: '#f59e0b' },
    { employee: 'Amadou Ba', training: 'Gestion de projet Agile', date: '5 Mai 2026', progress: 100, status: 'Terminée', statusColor: '#059669' },
    { employee: 'Aïssatou Camara', training: 'Anglais professionnel B2', date: '13 Mai 2026', progress: 15, status: 'En cours', statusColor: '#3b82f6' },
  ];

  const filteredTrainings = category === 'all' ? trainings : trainings.filter(t => t.category === category);

  return (
    <div>
      <PageHeader title="Formations" subtitle="Catalogue et suivi des formations" />

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

      {/* Category Chips */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 20, flexWrap: 'wrap' }}>
        {categories.map(c => (
          <button key={c.key} onClick={() => setCategory(c.key)} style={{
            padding: '8px 18px', borderRadius: 20, border: 'none', cursor: 'pointer',
            background: category === c.key ? '#6366f1' : '#f1f5f9',
            color: category === c.key ? '#fff' : '#64748b',
            fontWeight: 600, fontSize: 13, transition: 'all 0.15s'
          }}>{c.label}</button>
        ))}
      </div>

      {/* Training Cards Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 20, marginBottom: 32 }}>
        {filteredTrainings.map((t, i) => (
          <div key={i} style={{
            background: '#fff', borderRadius: 14, overflow: 'hidden',
            boxShadow: '0 1px 4px rgba(0,0,0,0.06)'
          }}>
            {/* Gradient Header */}
            <div style={{
              background: t.gradient, padding: 20, color: '#fff', minHeight: 90,
              display: 'flex', flexDirection: 'column', justifyContent: 'flex-end'
            }}>
              <div style={{ fontSize: 16, fontWeight: 700 }}>{t.title}</div>
              <div style={{ fontSize: 12, opacity: 0.85, marginTop: 4 }}>{t.instructor}</div>
            </div>

            <div style={{ padding: 16 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 12 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12, color: '#64748b' }}>
                  <Icons name="time" size={14} color="#94a3b8" />
                  {t.duration}
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12, color: '#64748b' }}>
                  <Icons name="calendar" size={14} color="#94a3b8" />
                  {t.startDate}
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                <div style={{ fontSize: 13, color: '#475569' }}>
                  <span style={{ fontWeight: 600 }}>{t.enrolled}</span>/{t.capacity} inscrits
                </div>
                <span style={{
                  background: t.status === 'Complet' ? '#fef2f2' : t.status === 'Terminée' ? '#ecfdf5' : t.status === 'En cours' ? '#dbeafe' : '#fffbeb',
                  color: t.status === 'Complet' ? '#ef4444' : t.status === 'Terminée' ? '#059669' : t.status === 'En cours' ? '#3b82f6' : '#d97706',
                  padding: '4px 10px', borderRadius: 20, fontSize: 11, fontWeight: 600
                }}>{t.status}</span>
              </div>

              {/* Progress bar */}
              <div style={{ height: 4, background: '#f1f5f9', borderRadius: 2, overflow: 'hidden' }}>
                <div style={{
                  width: `${(t.enrolled / t.capacity) * 100}%`, height: '100%',
                  background: t.enrolled >= t.capacity ? '#ef4444' : '#6366f1', borderRadius: 2
                }} />
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Enrollments Table */}
      <div style={{ background: '#fff', borderRadius: 14, overflow: 'hidden', boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
        <div style={{ padding: '16px 20px', borderBottom: '1px solid #f1f5f9' }}>
          <h3 style={{ fontSize: 15, fontWeight: 700, color: '#1e293b' }}>Inscriptions récentes</h3>
        </div>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
          <thead>
            <tr style={{ background: '#f8fafc' }}>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Employé</th>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Formation</th>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Date inscription</th>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Progression</th>
              <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#64748b' }}>Statut</th>
            </tr>
          </thead>
          <tbody>
            {enrollments.map((e, i) => (
              <tr key={i} style={{ borderTop: '1px solid #f1f5f9' }}>
                <td style={{ padding: '12px 16px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    <Avatar name={e.employee} size={32} />
                    <span style={{ fontWeight: 600, color: '#1e293b' }}>{e.employee}</span>
                  </div>
                </td>
                <td style={{ padding: '12px 16px', color: '#475569' }}>{e.training}</td>
                <td style={{ padding: '12px 16px', color: '#64748b' }}>{e.date}</td>
                <td style={{ padding: '12px 16px', width: 140 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <div style={{ flex: 1, height: 6, background: '#f1f5f9', borderRadius: 3, overflow: 'hidden' }}>
                      <div style={{
                        width: `${e.progress}%`, height: '100%', borderRadius: 3,
                        background: e.progress === 100 ? '#059669' : '#6366f1'
                      }} />
                    </div>
                    <span style={{ fontSize: 11, color: '#94a3b8', minWidth: 32 }}>{e.progress}%</span>
                  </div>
                </td>
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
