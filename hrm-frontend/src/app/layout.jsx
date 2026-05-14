import './globals.css';
import Providers from './providers';
import Sidebar from '../components/Sidebar';
import Topbar from '../components/Topbar';

export const dynamic = 'force-dynamic';

export const metadata = {
  title: 'HR Core — RT-Comops',
  description: 'Module de gestion des ressources humaines',
};

export default function RootLayout({ children }) {
  return (
    <html lang="fr">
      <body>
        <Providers>
          <div className="app">
            <Sidebar />
            <div className="main">
              <Topbar />
              <div className="page">
                {children}
              </div>
            </div>
          </div>
        </Providers>
      </body>
    </html>
  );
}
