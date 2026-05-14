'use client';
import { useState } from 'react';
import { Icons } from '../../components/Icons';
import Avatar from '../../components/Avatar';
import PageHeader from '../../components/PageHeader';
import { Spark, Donut } from '../../components/Charts';

const employees = [
  { id: 1, name: 'Sophie Martin', role: 'Directrice RH', dept: 'Ressources Humaines', status: 'Actif', date: '2019-03-15', email: 'sophie.martin@company.com', phone: '+33 6 12 34 56 78', avatar: 'SM', salary: '78 000€', contract: 'CDI', location: 'Paris' },
  { id: 2, name: 'Pierre Durand', role: 'Développeur Senior', dept: 'Technologie', status: 'Actif', date: '2020-06-01', email: 'pierre.durand@company.com', phone: '+33 6 23 45 67 89', avatar: 'PD', salary: '62 000€', contract: 'CDI', location: 'Lyon' },
  { id: 3, name: 'Marie Leroy', role: 'Chef de projet', dept: 'Technologie', status: 'Actif', date: '2021-01-10', email: 'marie.leroy@company.com', phone: '+33 6 34 56 78 90', avatar: 'ML', salary: '55 000€', contract: 'CDI', location: 'Paris' },
  { id: 4, name: 'Lucas Bernard', role: 'Commercial', dept: 'Commercial', status: 'Actif', date: '2022-04-20', email: 'lucas.bernard@company.com', phone: '+33 6 45 67 89 01', avatar: 'LB', salary: '48 000€', contract: 'CDI', location: 'Marseille' },
  { id: 5, name: 'Emma Petit', role: 'Designer UX', dept: 'Technologie', status: 'Congé', date: '2021-09-01', email: 'emma.petit@company.com', phone: '+33 6 56 78 90 12', avatar: 'EP', salary: '52 000€', contract: 'CDI', location: 'Paris' },
  { id: 6, name: 'Thomas Roux', role: 'Comptable', dept: 'Finance', status: 'Actif', date: '2018-11-15', email: 'thomas.roux@company.com', phone: '+33 6 67 89 01 23', avatar: 'TR', salary: '45 000€', contract: 'CDI', location: 'Paris' },
  { id: 7, name: 'Julie Moreau', role: 'Chargée de recrutement', dept: 'Ressources Humaines', status: 'Actif', date: '2022-02-01', email: 'julie.moreau@company.com', phone: '+33 6 78 90 12 34', avatar: 'JM', salary: '42 000€', contract: 'CDI', location: 'Lyon' },
  { id: 8, name: 'Nicolas Fournier', role: 'Ingénieur DevOps', dept: 'Technologie', status: 'Actif', date: '2023-01-15', email: 'nicolas.fournier@company.com', phone: '+33 6 89 01 23 45', avatar: 'NF', salary: '58 000€', contract: 'CDI', location: 'Paris' },
  { id: 9, name: 'Camille Girard', role: 'Responsable Marketing', dept: 'Marketing', status: 'Actif', date: '2020-08-01', email: 'camille.girard@company.com', phone: '+33 6 90 12 34 56', avatar: 'CG', salary: '55 000€', contract: 'CDI', location: 'Paris' },
  { id: 10, name: 'Alexandre Bonnet', role: 'Analyste données', dept: 'Technologie', status: 'Période essai', date: '2024-01-02', email: 'alexandre.bonnet@company.com', phone: '+33 6 01 23 45 67', avatar: 'AB', salary: '50 000€', contract: 'CDI', location: 'Lyon' },
  { id: 11, name: 'Isabelle Mercier', role: 'Assistante de direction', dept: 'Direction', status: 'Actif', date: '2017-05-10', email: 'isabelle.mercier@company.com', phone: '+33 6 12 34 56 00', avatar: 'IM', salary: '40 000€', contract: 'CDI', location: 'Paris' },
  { id: 12, name: 'Maxime Lambert', role: 'Stagiaire développeur', dept: 'Technologie', status: 'Actif', date: '2024-01-08', email: 'maxime.lambert@company.com', phone: '+33 6 23 45 67 00', avatar: 'MxL', salary: '18 000€', contract: 'Stage', location: 'Paris' },
];

function OverviewTab({ employee }) {
  const personalInfo = [
    { label: 'Email', value: employee.email, icon: 'mail' },
    { label: 'Téléphone', value: employee.phone, icon: 'phone' },
    { label: 'Localisation', value: employee.location, icon: 'map-pin' },
    { label: 'Date d\'entrée', value: employee.date, icon: 'calendar' },
    { label: 'Contrat', value: employee.contract, icon: 'file-text' },
    { label: 'Salaire brut annuel', value: employee.salary, icon: 'dollar' },
  ];

  const career = [
    { date: '2024', event: 'Poste actuel — ' + employee.role },
    { date: '2022', event: 'Promotion' },
    { date: '2020', event: 'Changement de département' },
    { date: employee.date.slice(0, 4), event: 'Embauche' },
  ];

  const skills = [
    { name: 'Leadership', level: 85 },
    { name: 'Communication', level: 90 },
    { name: 'Technique', level: 70 },
    { name: 'Gestion de projet', level: 80 },
    { name: 'Anglais', level: 75 },
  ];

  const documents = [
    { name: 'Contrat de travail', date: '2019-03-15', type: 'PDF' },
    { name: 'Avenant salaire', date: '2022-06-01', type: 'PDF' },
    { name: 'Attestation formation', date: '2023-11-20', type: 'PDF' },
    { name: 'Entretien annuel 2023', date: '2023-12-15', type: 'PDF' },
  ];

  return (
    <div className="overview-tab">
      <div className="dashboard-grid-2">
        {/* Personal Info */}
        <div className="card">
          <h3 className="card-title">
            <Icons name="user" size={18} /> Informations personnelles
          </h3>
          <div className="info-grid">
            {personalInfo.map((info, i) => (
              <div key={i} className="info-item">
                <Icons name={info.icon} size={14} className="info-icon" />
                <div>
                  <div className="info-label">{info.label}</div>
                  <div className="info-value">{info.value}</div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Career Timeline */}
        <div className="card">
          <h3 className="card-title">
            <Icons name="clock" size={18} /> Parcours
          </h3>
          <div className="timeline">
            {career.map((item, i) => (
              <div key={i} className="timeline-item">
                <div className="timeline-dot" />
                <div className="timeline-content">
                  <span className="timeline-date">{item.date}</span>
                  <span className="timeline-event">{item.event}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="dashboard-grid-2">
        {/* Skills */}
        <div className="card">
          <h3 className="card-title">
            <Icons name="star" size={18} /> Compétences
          </h3>
          <div className="skills-list">
            {skills.map((skill, i) => (
              <div key={i} className="skill-row">
                <span className="skill-name">{skill.name}</span>
                <div className="skill-bar-bg">
                  <div
                    className="skill-bar-fill"
                    style={{ width: `${skill.level}%` }}
                  />
                </div>
                <span className="skill-pct">{skill.level}%</span>
              </div>
            ))}
          </div>
        </div>

        {/* Documents */}
        <div className="card">
          <h3 className="card-title">
            <Icons name="folder" size={18} /> Documents
          </h3>
          <ul className="doc-list">
            {documents.map((doc, i) => (
              <li key={i} className="doc-item">
                <Icons name="file-text" size={16} className="doc-icon" />
                <div className="doc-info">
                  <span className="doc-name">{doc.name}</span>
                  <span className="doc-date">{doc.date}</span>
                </div>
                <span className="badge">{doc.type}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>

      {/* Hierarchy */}
      <div className="card">
        <h3 className="card-title">
          <Icons name="git-branch" size={18} /> Hiérarchie
        </h3>
        <div className="hierarchy">
          <div className="hierarchy-node manager">
            <Avatar initials="DG" size={36} />
            <div>
              <div className="hierarchy-name">Direction Générale</div>
              <div className="hierarchy-role">CEO</div>
            </div>
          </div>
          <div className="hierarchy-line" />
          <div className="hierarchy-node current">
            <Avatar initials={employee.avatar} size={36} />
            <div>
              <div className="hierarchy-name">{employee.name}</div>
              <div className="hierarchy-role">{employee.role}</div>
            </div>
          </div>
          <div className="hierarchy-line" />
          <div className="hierarchy-reports">
            {['AL', 'BM', 'CD'].map((init, i) => (
              <div key={i} className="hierarchy-node report">
                <Avatar initials={init} size={28} />
                <span className="hierarchy-role">Report {i + 1}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

function EmployeeDetail({ employee, onBack }) {
  const [activeTab, setActiveTab] = useState('overview');

  const tabs = [
    { id: 'overview', label: 'Vue d\'ensemble', icon: 'user' },
    { id: 'contracts', label: 'Contrats', icon: 'file-text' },
    { id: 'leaves', label: 'Congés', icon: 'calendar' },
    { id: 'payroll', label: 'Paie', icon: 'dollar' },
    { id: 'training', label: 'Formations', icon: 'book' },
    { id: 'evaluations', label: 'Évaluations', icon: 'star' },
  ];

  return (
    <div className="employee-detail">
      <button className="back-btn" onClick={onBack}>
        <Icons name="arrow-left" size={16} /> Retour à la liste
      </button>

      {/* Hero Card */}
      <div className="employee-hero-card">
        <Avatar initials={employee.avatar} size={72} />
        <div className="employee-hero-info">
          <h2 className="employee-hero-name">{employee.name}</h2>
          <p className="employee-hero-role">{employee.role} — {employee.dept}</p>
          <div className="employee-hero-meta">
            <span><Icons name="map-pin" size={14} /> {employee.location}</span>
            <span><Icons name="mail" size={14} /> {employee.email}</span>
            <span className={`badge badge-${employee.status === 'Actif' ? 'green' : employee.status === 'Congé' ? 'yellow' : 'blue'}`}>
              {employee.status}
            </span>
          </div>
        </div>
        <div className="employee-hero-actions">
          <button className="btn btn-outline"><Icons name="edit" size={14} /> Modifier</button>
          <button className="btn btn-outline"><Icons name="mail" size={14} /> Email</button>
        </div>
      </div>

      {/* Tabs */}
      <div className="tabs">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            className={`tab ${activeTab === tab.id ? 'active' : ''}`}
            onClick={() => setActiveTab(tab.id)}
          >
            <Icons name={tab.icon} size={14} /> {tab.label}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      <div className="tab-content">
        {activeTab === 'overview' && <OverviewTab employee={employee} />}
        {activeTab === 'contracts' && (
          <div className="card placeholder-tab">
            <Icons name="file-text" size={48} className="placeholder-icon" />
            <p>Onglet Contrats — à développer</p>
          </div>
        )}
        {activeTab === 'leaves' && (
          <div className="card placeholder-tab">
            <Icons name="calendar" size={48} className="placeholder-icon" />
            <p>Onglet Congés — à développer</p>
          </div>
        )}
        {activeTab === 'payroll' && (
          <div className="card placeholder-tab">
            <Icons name="dollar" size={48} className="placeholder-icon" />
            <p>Onglet Paie — à développer</p>
          </div>
        )}
        {activeTab === 'training' && (
          <div className="card placeholder-tab">
            <Icons name="book" size={48} className="placeholder-icon" />
            <p>Onglet Formations — à développer</p>
          </div>
        )}
        {activeTab === 'evaluations' && (
          <div className="card placeholder-tab">
            <Icons name="star" size={48} className="placeholder-icon" />
            <p>Onglet Évaluations — à développer</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default function Employees() {
  const [view, setView] = useState('list');
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [search, setSearch] = useState('');
  const [filterDept, setFilterDept] = useState('Tous');
  const [filterStatus, setFilterStatus] = useState('Tous');

  const departments = ['Tous', ...new Set(employees.map((e) => e.dept))];
  const statuses = ['Tous', 'Actif', 'Congé', 'Période essai'];

  const filtered = employees.filter((e) => {
    const matchSearch = e.name.toLowerCase().includes(search.toLowerCase()) ||
      e.role.toLowerCase().includes(search.toLowerCase());
    const matchDept = filterDept === 'Tous' || e.dept === filterDept;
    const matchStatus = filterStatus === 'Tous' || e.status === filterStatus;
    return matchSearch && matchDept && matchStatus;
  });

  const miniKpis = [
    { label: 'Total employés', value: '342', icon: 'users', color: '#3b82f6' },
    { label: 'Nouveaux ce mois', value: '5', icon: 'user-plus', color: '#10b981' },
    { label: 'En congé', value: '12', icon: 'calendar', color: '#f59e0b' },
    { label: 'Période d\'essai', value: '8', icon: 'clock', color: '#8b5cf6' },
  ];

  const handleSelectEmployee = (emp) => {
    setSelectedEmployee(emp);
    setView('detail');
  };

  const handleBack = () => {
    setView('list');
    setSelectedEmployee(null);
  };

  if (view === 'detail' && selectedEmployee) {
    return (
      <div className="page-employees">
        <PageHeader title="Employés" subtitle="Gestion du personnel" icon="users" />
        <EmployeeDetail employee={selectedEmployee} onBack={handleBack} />
      </div>
    );
  }

  return (
    <div className="page-employees">
      <PageHeader title="Employés" subtitle="Gestion du personnel" icon="users" />

      {/* Mini KPIs */}
      <div className="kpi-hero-row">
        {miniKpis.map((kpi, i) => (
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

      {/* Search & Filters */}
      <div className="card filters-bar">
        <div className="search-box">
          <Icons name="search" size={16} />
          <input
            type="text"
            placeholder="Rechercher un employé..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <div className="filter-group">
          <label>Département</label>
          <select value={filterDept} onChange={(e) => setFilterDept(e.target.value)}>
            {departments.map((d) => (
              <option key={d} value={d}>{d}</option>
            ))}
          </select>
        </div>
        <div className="filter-group">
          <label>Statut</label>
          <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
            {statuses.map((s) => (
              <option key={s} value={s}>{s}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Employees Table */}
      <div className="card">
        <div className="table-responsive">
          <table className="data-table">
            <thead>
              <tr>
                <th>Employé</th>
                <th>Poste</th>
                <th>Département</th>
                <th>Statut</th>
                <th>Date d'entrée</th>
                <th>Contrat</th>
                <th>Localisation</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((emp) => (
                <tr key={emp.id} className="clickable-row" onClick={() => handleSelectEmployee(emp)}>
                  <td>
                    <div className="employee-cell">
                      <Avatar initials={emp.avatar} size={32} />
                      <div>
                        <div className="font-medium">{emp.name}</div>
                        <div className="text-muted text-sm">{emp.email}</div>
                      </div>
                    </div>
                  </td>
                  <td>{emp.role}</td>
                  <td>{emp.dept}</td>
                  <td>
                    <span className={`badge badge-${emp.status === 'Actif' ? 'green' : emp.status === 'Congé' ? 'yellow' : 'blue'}`}>
                      {emp.status}
                    </span>
                  </td>
                  <td>{emp.date}</td>
                  <td>{emp.contract}</td>
                  <td>{emp.location}</td>
                  <td>
                    <button className="btn-icon" onClick={(e) => { e.stopPropagation(); handleSelectEmployee(emp); }}>
                      <Icons name="eye" size={16} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="table-footer">
          <span>{filtered.length} employé(s) affiché(s) sur {employees.length}</span>
        </div>
      </div>
    </div>
  );
}
