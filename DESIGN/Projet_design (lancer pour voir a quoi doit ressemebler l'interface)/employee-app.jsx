/* global React, ReactDOM */
const { useState: useS_eapp } = React;

function EmpApp() {
  const [page, setPage] = useS_eapp('home');

  const PAGES = {
    home: window.EmpPageHome,
    profile: window.EmpPageProfile,
    documents: window.EmpPageDocuments,
    payslips: window.EmpPagePayslips,
    expenses: window.EmpPageExpenses,
    loans: window.EmpPageLoans,
    leaves: window.EmpPageLeaves,
    time: window.EmpPageTime,
    missions: window.EmpPageMissions,
    trainings: window.EmpPageTrainings,
    reviews: window.EmpPageReviews,
    skills: window.EmpPageSkills,
    medical: window.EmpPageMedical,
    directory: window.EmpPageDirectory,
    inbox: window.EmpPageInbox,
  };
  const Page = PAGES[page] || (() => <div className="page">Page introuvable: {page}</div>);

  return (
    <div className="app">
      <window.EmpSidebar active={page} onNav={setPage}/>
      <div className="main">
        <window.EmpTopbar goto={setPage}/>
        <div className="page" data-screen-label={page}>
          <Page goto={setPage}/>
        </div>
      </div>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(<EmpApp/>);
