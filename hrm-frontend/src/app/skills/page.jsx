'use client';
import { useState } from 'react';
import { Icons } from '../../components/Icons';
import Avatar from '../../components/Avatar';
import PageHeader from '../../components/PageHeader';

export default function Skills() {
  const [activeCategory, setActiveCategory] = useState('Tous');

  const kpis = [
    { label: 'Compétences cartographiées', value: '156', icon: 'layers', color: '#3b82f6' },
    { label: 'Experts internes', value: '45', icon: 'award', color: '#10b981' },
    { label: 'Gaps identifiés', value: '23', icon: 'alert-circle', color: '#f59e0b' },
    { label: 'Formations en cours', value: '18', icon: 'book', color: '#8b5cf6' },
  ];

  const categories = ['Tous', 'Technique', 'Management', 'Langues', 'Soft Skills', 'Métier'];

  const topSkills = [
    { name: 'JavaScript', category: 'Technique', level: 92, employees: 45, trend: 'up' },
    { name: 'Python', category: 'Technique', level: 85, employees: 32, trend: 'up' },
    { name: 'React', category: 'Technique', level: 88, employees: 38, trend: 'up' },
    { name: 'Leadership', category: 'Management', level: 78, employees: 28, trend: 'stable' },
    { name: 'Anglais C1', category: 'Langues', level: 82, employees: 55, trend: 'up' },
    { name: 'Gestion de projet', category: 'Management', level: 80, employees: 35, trend: 'stable' },
    { name: 'Communication', category: 'Soft Skills', level: 75, employees: 60, trend: 'up' },
    { name: 'SQL', category: 'Technique', level: 79, employees: 30, trend: 'down' },
    { name: 'Docker', category: 'Technique', level: 70, employees: 22, trend: 'up' },
    { name: 'Négociation', category: 'Soft Skills', level: 68, employees: 18, trend: 'stable' },
    { name: 'Allemand B2', category: 'Langues', level: 60, employees: 12, trend: 'down' },
    { name: 'Comptabilité IFRS', category: 'Métier', level: 85, employees: 15, trend: 'stable' },
  ];

  const experts = [
    { name: 'Pierre Durand', avatar: 'PD', skill: 'JavaScript', level: 'Expert', dept: 'Tech' },
    { name: 'Marie Leroy', avatar: 'ML', skill: 'React', level: 'Expert', dept: 'Tech' },
    { name: 'Sophie Martin', avatar: 'SM', skill: 'Leadership', level: 'Expert', dept: 'RH' },
    { name: 'Nicolas Fournier', avatar: 'NF', skill: 'Docker', level: 'Expert', dept: 'Tech' },
    { name: 'Camille Girard', avatar: 'CG', skill: 'Communication', level: 'Expert', dept: 'Marketing' },
  ];

  const gapAnalysis = [
    { skill: 'Kubernetes', current: 15, target: 40, gap: -25, priority: 'Haute', action: 'Formation planifiée' },
    { skill: 'Data Science', current: 20, target: 45, gap: -25, priority: 'Haute', action: 'Recrutement en cours' },
    { skill: 'Cybersécurité', current: 10, target: 30, gap: -20, priority: 'Haute', action: 'Formation Q2' },
    { skill: 'Espagnol B2', current: 8, target: 20, gap: -12, priority: 'Moyenne', action: 'À planifier' },
    { skill: 'UX Research', current: 12, target: 20, gap: -8, priority: 'Moyenne', action: 'Recrutement en cours' },
    { skill: 'IA / Machine Learning', current: 5, target: 25, gap: -20, priority: 'Haute', action: 'Formation Q1' },
  ];

  const filteredSkills = activeCategory === 'Tous'
    ? topSkills
    : topSkills.filter((s) => s.category === activeCategory);

  return (
    <div className="page-skills">
      <PageHeader
        title="Compétences"
        subtitle="Cartographie et gestion des compétences"
        icon="award"
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

      {/* Category Chips */}
      <div className="filter-chips">
        {categories.map((cat) => (
          <button
            key={cat}
            className={`chip ${activeCategory === cat ? 'active' : ''}`}
            onClick={() => setActiveCategory(cat)}
          >
            {cat}
          </button>
        ))}
      </div>

      {/* Skills Heatmap + Experts */}
      <div className="dashboard-grid-2">
        <div className="card">
          <h3 className="card-title">Cartographie des compétences</h3>
          <div className="skills-heatmap">
            {filteredSkills.map((skill, i) => (
              <div key={i} className="heatmap-row">
                <span className="heatmap-name">{skill.name}</span>
                <span className="badge badge-outline">{skill.category}</span>
                <div className="heatmap-bar-bg">
                  <div
                    className="heatmap-bar-fill"
                    style={{
                      width: `${skill.level}%`,
                      background: skill.level >= 80 ? '#10b981' : skill.level >= 60 ? '#f59e0b' : '#ef4444',
                    }}
                  />
                </div>
                <span className="heatmap-pct">{skill.level}%</span>
                <span className="heatmap-count">{skill.employees} pers.</span>
                <Icons
                  name={skill.trend === 'up' ? 'trending-up' : skill.trend === 'down' ? 'trending-down' : 'minus'}
                  size={14}
                  className={`trend-icon trend-${skill.trend}`}
                />
              </div>
            ))}
          </div>
        </div>

        <div className="card">
          <h3 className="card-title">Experts internes</h3>
          <ul className="expert-list">
            {experts.map((expert, i) => (
              <li key={i} className="expert-item">
                <Avatar initials={expert.avatar} size={36} />
                <div className="expert-info">
                  <div className="expert-name">{expert.name}</div>
                  <div className="expert-detail">{expert.skill} — {expert.dept}</div>
                </div>
                <span className="badge badge-green">{expert.level}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>

      {/* Gap Analysis */}
      <div className="card">
        <h3 className="card-title">
          <Icons name="alert-circle" size={18} /> Analyse des écarts (Gap Analysis)
        </h3>
        <div className="table-responsive">
          <table className="data-table">
            <thead>
              <tr>
                <th>Compétence</th>
                <th>Niveau actuel</th>
                <th>Cible</th>
                <th>Écart</th>
                <th>Priorité</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {gapAnalysis.map((row, i) => (
                <tr key={i}>
                  <td className="font-medium">{row.skill}</td>
                  <td>
                    <div className="gap-bar">
                      <div className="gap-fill current" style={{ width: `${row.current * 2}px` }} />
                      <span>{row.current}%</span>
                    </div>
                  </td>
                  <td>
                    <div className="gap-bar">
                      <div className="gap-fill target" style={{ width: `${row.target * 2}px` }} />
                      <span>{row.target}%</span>
                    </div>
                  </td>
                  <td className="text-red font-medium">{row.gap}%</td>
                  <td>
                    <span className={`badge badge-${row.priority === 'Haute' ? 'red' : 'yellow'}`}>
                      {row.priority}
                    </span>
                  </td>
                  <td>{row.action}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
