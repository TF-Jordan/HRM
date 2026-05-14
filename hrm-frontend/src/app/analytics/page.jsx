'use client';
import { useState } from 'react';
import { Icons } from '../../components/Icons';
import Avatar from '../../components/Avatar';
import PageHeader from '../../components/PageHeader';
import { Line, Bars, Donut } from '../../components/Charts';

export default function Analytics() {
  const [period, setPeriod] = useState('12m');

  const heroKpis = [
    { label: 'Coût moyen / employé', value: '52.4K€', change: '+2.1%', icon: 'dollar', color: '#3b82f6' },
    { label: 'Productivité', value: '94%', change: '+3.5%', icon: 'trending-up', color: '#10b981' },
    { label: 'Turnover', value: '8.2%', change: '-1.4%', icon: 'user-minus', color: '#f59e0b' },
    { label: 'Time to hire', value: '28j', change: '-5j', icon: 'clock', color: '#8b5cf6' },
  ];

  const effectifTurnover = [
    { month: 'Jan', effectif: 310, turnover: 2.1 },
    { month: 'Fév', effectif: 312, turnover: 1.8 },
    { month: 'Mar', effectif: 318, turnover: 2.5 },
    { month: 'Avr', effectif: 320, turnover: 1.9 },
    { month: 'Mai', effectif: 325, turnover: 2.2 },
    { month: 'Jun', effectif: 328, turnover: 1.7 },
    { month: 'Juil', effectif: 330, turnover: 2.0 },
    { month: 'Août', effectif: 329, turnover: 2.3 },
    { month: 'Sep', effectif: 332, turnover: 1.6 },
    { month: 'Oct', effectif: 335, turnover: 1.9 },
    { month: 'Nov', effectif: 338, turnover: 2.1 },
    { month: 'Déc', effectif: 342, turnover: 1.8 },
  ];

  const agePyramid = [
    { range: '18-25', men: 18, women: 22 },
    { range: '26-30', men: 35, women: 30 },
    { range: '31-35', men: 42, women: 38 },
    { range: '36-40', men: 38, women: 35 },
    { range: '41-45', men: 28, women: 25 },
    { range: '46-50', men: 15, women: 18 },
    { range: '51-55', men: 8, women: 10 },
    { range: '56+', men: 5, women: 4 },
  ];

  const costTrend = [
    { month: 'Juil', value: 11.2 },
    { month: 'Août', value: 11.5 },
    { month: 'Sep', value: 11.8 },
    { month: 'Oct', value: 12.0 },
    { month: 'Nov', value: 12.1 },
    { month: 'Déc', value: 12.4 },
  ];

  const enpsData = [
    { label: 'Promoteurs', value: 45, color: '#10b981' },
    { label: 'Passifs', value: 33, color: '#f59e0b' },
    { label: 'Détracteurs', value: 22, color: '#ef4444' },
  ];

  const departureReasons = [
    { reason: 'Opportunité externe', count: 12, pct: 35 },
    { reason: 'Rémunération', count: 8, pct: 24 },
    { reason: 'Équilibre vie pro/perso', count: 6, pct: 18 },
    { reason: 'Management', count: 4, pct: 12 },
    { reason: 'Autre', count: 4, pct: 11 },
  ];

  const deptPerformance = [
    { dept: 'Technologie', headcount: 120, turnover: '6.5%', absenteeism: '2.8%', satisfaction: 82, budget: '98%' },
    { dept: 'Commercial', dept2: 'Commercial', headcount: 85, turnover: '9.2%', absenteeism: '3.1%', satisfaction: 75, budget: '102%' },
    { dept: 'Ressources Humaines', headcount: 52, turnover: '4.1%', absenteeism: '2.2%', satisfaction: 88, budget: '95%' },
    { dept: 'Finance', headcount: 45, turnover: '7.8%', absenteeism: '3.5%', satisfaction: 72, budget: '97%' },
    { dept: 'Marketing', headcount: 40, turnover: '11.0%', absenteeism: '4.2%', satisfaction: 70, budget: '105%' },
  ];

  return (
    <div className="page-analytics">
      <PageHeader
        title="Analytics RH"
        subtitle="Analyse approfondie de vos données RH"
        icon="bar-chart-2"
      />

      {/* Period Filter */}
      <div className="period-filter">
        {['3m', '6m', '12m', '24m'].map((p) => (
          <button
            key={p}
            className={`period-btn ${period === p ? 'active' : ''}`}
            onClick={() => setPeriod(p)}
          >
            {p}
          </button>
        ))}
      </div>

      {/* Hero KPIs */}
      <div className="kpi-hero-row">
        {heroKpis.map((kpi, i) => (
          <div key={i} className="kpi-hero-card">
            <div className="kpi-hero-icon" style={{ background: kpi.color + '18', color: kpi.color }}>
              <Icons name={kpi.icon} size={24} />
            </div>
            <div className="kpi-hero-content">
              <div className="kpi-hero-value">{kpi.value}</div>
              <div className="kpi-hero-label">{kpi.label}</div>
            </div>
            <span className={`kpi-hero-change ${kpi.change.startsWith('+') || kpi.change.startsWith('-') ? (kpi.change.includes('-') ? 'negative' : 'positive') : ''}`}>
              {kpi.change}
            </span>
          </div>
        ))}
      </div>

      {/* Effectif & Turnover + Age Pyramid */}
      <div className="dashboard-grid-2">
        <div className="card">
          <h3 className="card-title">Effectif & Turnover</h3>
          <Line
            data={effectifTurnover.map((d) => ({ month: d.month, value: d.effectif }))}
            height={220}
            color="#3b82f6"
          />
        </div>
        <div className="card">
          <h3 className="card-title">Pyramide des âges</h3>
          <div className="age-pyramid">
            {agePyramid.map((row, i) => (
              <div key={i} className="pyramid-row">
                <div className="pyramid-bar-left">
                  <div className="pyramid-fill men" style={{ width: `${row.men * 2}px` }} />
                  <span className="pyramid-val">{row.men}</span>
                </div>
                <span className="pyramid-label">{row.range}</span>
                <div className="pyramid-bar-right">
                  <div className="pyramid-fill women" style={{ width: `${row.women * 2}px` }} />
                  <span className="pyramid-val">{row.women}</span>
                </div>
              </div>
            ))}
            <div className="pyramid-legend">
              <span className="legend-men">● Hommes</span>
              <span className="legend-women">● Femmes</span>
            </div>
          </div>
        </div>
      </div>

      {/* Cost Trend + eNPS + Departure Reasons */}
      <div className="dashboard-grid-3">
        <div className="card">
          <h3 className="card-title">Tendance coûts (M€)</h3>
          <Bars data={costTrend} height={180} color="#3b82f6" />
        </div>
        <div className="card">
          <h3 className="card-title">eNPS Engagement</h3>
          <Donut data={enpsData} size={160} />
          <div className="enps-score">
            <span className="enps-value">+23</span>
            <span className="enps-label">Score eNPS</span>
          </div>
        </div>
        <div className="card">
          <h3 className="card-title">Top motifs de départ</h3>
          <ul className="departure-list">
            {departureReasons.map((r, i) => (
              <li key={i} className="departure-item">
                <span className="departure-reason">{r.reason}</span>
                <div className="departure-bar-bg">
                  <div className="departure-bar-fill" style={{ width: `${r.pct}%` }} />
                </div>
                <span className="departure-pct">{r.pct}%</span>
              </li>
            ))}
          </ul>
        </div>
      </div>

      {/* Department Performance Table */}
      <div className="card">
        <h3 className="card-title">Performance par département</h3>
        <div className="table-responsive">
          <table className="data-table">
            <thead>
              <tr>
                <th>Département</th>
                <th>Effectif</th>
                <th>Turnover</th>
                <th>Absentéisme</th>
                <th>Satisfaction</th>
                <th>Budget</th>
              </tr>
            </thead>
            <tbody>
              {deptPerformance.map((row, i) => (
                <tr key={i}>
                  <td className="font-medium">{row.dept}</td>
                  <td>{row.headcount}</td>
                  <td>{row.turnover}</td>
                  <td>{row.absenteeism}</td>
                  <td>
                    <div className="satisfaction-bar">
                      <div
                        className="satisfaction-fill"
                        style={{
                          width: `${row.satisfaction}%`,
                          background: row.satisfaction >= 80 ? '#10b981' : row.satisfaction >= 70 ? '#f59e0b' : '#ef4444',
                        }}
                      />
                      <span>{row.satisfaction}%</span>
                    </div>
                  </td>
                  <td>{row.budget}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
