/* global React, ReactDOM */
const { useState } = React;

function App() {
  const [page, setPage] = useState('dashboard');

  const PAGES = {
    dashboard: window.PageDashboard,
    analytics: window.PageAnalytics,
    employees: window.PageEmployees,
    contracts: window.PageContracts,
    skills: window.PageSkills,
    recruitment: window.PageRecruitment,
    time: window.PageTime,
    leaves: window.PageLeaves,
    missions: window.PageMissions,
    payroll: window.PagePayroll,
    loans: window.PageLoans,
    expenses: window.PageExpenses,
    reviews: window.PageReviews,
    trainings: window.PageTrainings,
    budget: window.PageBudget,
    medical: window.PageMedical,
    declarations: window.PageDeclarations,
    settings: window.PageSettings,
  };
  const Page = PAGES[page] || (() => <div className="page">Page introuvable: {page}</div>);

  return (
    <div className="app">
      <window.Sidebar active={page} onNav={setPage}/>
      <div className="main">
        <window.Topbar/>
        <div className="page" data-screen-label={page}>
          <Page goto={setPage}/>
        </div>
      </div>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(<App/>);
