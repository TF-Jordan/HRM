'use client';
import { Icons } from './Icons';
import Avatar from './Avatar';

export default function Topbar() {
  return (
    <div className="topbar">
      <div className="search">
        <Icons.search size={16} />
        <input placeholder="Rechercher un employé, contrat, bulletin, formation…" />
        <kbd style={{ fontSize: 11, color: 'var(--ink-3)', background: 'var(--bg-soft)', padding: '2px 6px', borderRadius: 6, border: '1px solid var(--line)' }}>⌘K</kbd>
      </div>
      <div className="row gap-2" style={{ marginLeft: 'auto' }}>
        <button className="icon-btn"><Icons.info size={16} /></button>
        <button className="icon-btn"><Icons.bell size={16} /><span className="dot" /></button>
        <div className="user-chip">
          <div style={{ textAlign: 'right' }}>
            <div className="user-chip-name">Faïsal Sab</div>
            <div className="user-chip-role">Admin RH · DRH</div>
          </div>
          <Avatar name="Faïsal Sab" />
        </div>
      </div>
    </div>
  );
}
