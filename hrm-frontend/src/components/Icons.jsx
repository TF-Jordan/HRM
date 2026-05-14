const S = { fill: 'none', stroke: 'currentColor', strokeWidth: 1.7, strokeLinecap: 'round', strokeLinejoin: 'round' };

export const Icons = {
  dashboard: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <rect x="3" y="3" width="7" height="9" rx="1.5"/>
      <rect x="14" y="3" width="7" height="5" rx="1.5"/>
      <rect x="14" y="12" width="7" height="9" rx="1.5"/>
      <rect x="3" y="16" width="7" height="5" rx="1.5"/>
    </svg>
  ),
  users: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <circle cx="9" cy="8" r="3.2"/>
      <path d="M3 20c0-3.3 2.7-6 6-6s6 2.7 6 6"/>
      <circle cx="17" cy="9" r="2.5"/>
      <path d="M21 19c0-2.5-2-4.5-4-4.5"/>
    </svg>
  ),
  contract: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M14 3H7a2 2 0 00-2 2v14a2 2 0 002 2h10a2 2 0 002-2V8z"/>
      <path d="M14 3v5h5M9 13h6M9 17h4"/>
    </svg>
  ),
  payroll: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <rect x="3" y="6" width="18" height="13" rx="2"/>
      <circle cx="12" cy="12.5" r="2.6"/>
    </svg>
  ),
  leave: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <rect x="3" y="5" width="18" height="16" rx="2"/>
      <path d="M3 9h18M8 3v4M16 3v4"/>
    </svg>
  ),
  loan: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M2 9l10-6 10 6-10 6z"/>
      <path d="M6 12v5c0 1 3 3 6 3s6-2 6-3v-5"/>
    </svg>
  ),
  training: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M22 9L12 4 2 9l10 5 10-5z"/>
      <path d="M6 11.5v4c0 1.5 3 3 6 3s6-1.5 6-3v-4"/>
    </svg>
  ),
  review: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <circle cx="12" cy="12" r="9"/>
      <circle cx="12" cy="12" r="5"/>
      <circle cx="12" cy="12" r="1.5" fill="currentColor"/>
    </svg>
  ),
  recruit: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <rect x="3" y="6" width="18" height="14" rx="2"/>
      <path d="M8 6V4a2 2 0 012-2h4a2 2 0 012 2v2M3 12h18"/>
    </svg>
  ),
  time: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <circle cx="12" cy="12" r="9"/>
      <path d="M12 7v5l3 2"/>
    </svg>
  ),
  expense: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M5 3h11l3 3v15a1 1 0 01-1 1H5a1 1 0 01-1-1V4a1 1 0 011-1z"/>
      <path d="M16 3v3h3M9 13h2.5a1.5 1.5 0 100-3H10a1.5 1.5 0 110-3h2.5"/>
    </svg>
  ),
  medical: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M12 21s-7-4.5-7-11a4 4 0 017-2.6A4 4 0 0119 10c0 6.5-7 11-7 11z"/>
      <path d="M9 11h6M12 8v6"/>
    </svg>
  ),
  analytics: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M3 21V8M9 21V13M15 21v-9M21 21V4"/>
    </svg>
  ),
  declaration: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M5 3h14a1 1 0 011 1v17l-4-3-4 3-4-3-4 3V4a1 1 0 011-1z"/>
      <path d="M8 8h8M8 12h6"/>
    </svg>
  ),
  mission: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M21 10c0 7-9 12-9 12S3 17 3 10a9 9 0 0118 0z"/>
      <circle cx="12" cy="10" r="2.8"/>
    </svg>
  ),
  skill: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M12 2l2.4 5 5.6.5-4.2 3.8 1.3 5.7L12 14l-5.1 3 1.3-5.7L4 7.5 9.6 7z"/>
    </svg>
  ),
  settings: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <circle cx="12" cy="12" r="3"/>
      <path d="M19.4 15a1.7 1.7 0 00.3 1.8l.1.1a2 2 0 11-2.9 2.9l-.1-.1a1.7 1.7 0 00-1.8-.3 1.7 1.7 0 00-1 1.5V21a2 2 0 11-4 0v-.1a1.7 1.7 0 00-1.1-1.5 1.7 1.7 0 00-1.8.3l-.1.1a2 2 0 11-2.9-2.9l.1-.1a1.7 1.7 0 00.3-1.8 1.7 1.7 0 00-1.5-1H3a2 2 0 110-4h.1a1.7 1.7 0 001.5-1.1 1.7 1.7 0 00-.3-1.8l-.1-.1a2 2 0 112.9-2.9l.1.1a1.7 1.7 0 001.8.3H9a1.7 1.7 0 001-1.5V3a2 2 0 114 0v.1a1.7 1.7 0 001 1.5 1.7 1.7 0 001.8-.3l.1-.1a2 2 0 112.9 2.9l-.1.1a1.7 1.7 0 00-.3 1.8V9a1.7 1.7 0 001.5 1H21a2 2 0 110 4h-.1a1.7 1.7 0 00-1.5 1z"/>
    </svg>
  ),
  search: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <circle cx="11" cy="11" r="7"/>
      <path d="M21 21l-4.3-4.3"/>
    </svg>
  ),
  bell: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M6 8a6 6 0 0112 0c0 7 3 9 3 9H3s3-2 3-9"/>
      <path d="M10 21a2 2 0 004 0"/>
    </svg>
  ),
  logout: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M15 3h4a2 2 0 012 2v14a2 2 0 01-2 2h-4M10 17l5-5-5-5M15 12H3"/>
    </svg>
  ),
  plus: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M12 5v14M5 12h14"/>
    </svg>
  ),
  filter: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M3 5h18M6 12h12M10 19h4"/>
    </svg>
  ),
  download: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M12 3v12M7 10l5 5 5-5M5 21h14"/>
    </svg>
  ),
  upload: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M12 17V5M7 10l5-5 5 5M5 21h14"/>
    </svg>
  ),
  edit: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M12 20h9"/>
      <path d="M16.5 3.5a2.1 2.1 0 113 3L7 19l-4 1 1-4z"/>
    </svg>
  ),
  more: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" {...p}>
      <circle cx="5" cy="12" r="1.5" fill="currentColor"/>
      <circle cx="12" cy="12" r="1.5" fill="currentColor"/>
      <circle cx="19" cy="12" r="1.5" fill="currentColor"/>
    </svg>
  ),
  chevR: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M9 5l7 7-7 7"/>
    </svg>
  ),
  chevD: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M5 9l7 7 7-7"/>
    </svg>
  ),
  check: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M5 12l4 4L19 7"/>
    </svg>
  ),
  x: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M6 6l12 12M18 6L6 18"/>
    </svg>
  ),
  trendUp: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M3 17l6-6 4 4 8-8M14 7h7v7"/>
    </svg>
  ),
  trendDown: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M3 7l6 6 4-4 8 8M14 17h7v-7"/>
    </svg>
  ),
  alert: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <circle cx="12" cy="12" r="9"/>
      <path d="M12 8v5M12 16v.5"/>
    </svg>
  ),
  info: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <circle cx="12" cy="12" r="9"/>
      <path d="M12 11v5M12 8v.5"/>
    </svg>
  ),
  cal: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <rect x="3" y="5" width="18" height="16" rx="2"/>
      <path d="M3 9h18M8 3v4M16 3v4"/>
    </svg>
  ),
  doc: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M14 3H7a2 2 0 00-2 2v14a2 2 0 002 2h10a2 2 0 002-2V8z"/>
      <path d="M14 3v5h5"/>
    </svg>
  ),
  pieChart: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M12 2v10l9 3a10 10 0 11-9-13z"/>
    </svg>
  ),
  star: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" fill="currentColor" stroke="none" {...p}>
      <path d="M12 2l2.4 5 5.6.5-4.2 3.8 1.3 5.7L12 14l-5.1 3 1.3-5.7L4 7.5 9.6 7z"/>
    </svg>
  ),
  briefcase: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <rect x="3" y="7" width="18" height="13" rx="2"/>
      <path d="M9 7V5a2 2 0 012-2h2a2 2 0 012 2v2M3 13h18"/>
    </svg>
  ),
  send: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M22 2L11 13M22 2l-7 20-4-9-9-4z"/>
    </svg>
  ),
  shield: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
    </svg>
  ),
  badge: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <circle cx="12" cy="8" r="4"/>
      <path d="M5 21l3-7 4 2 4-2 3 7"/>
    </svg>
  ),
  print: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M6 9V3h12v6M6 18h-1a2 2 0 01-2-2v-5a2 2 0 012-2h14a2 2 0 012 2v5a2 2 0 01-2 2h-1"/>
      <rect x="6" y="14" width="12" height="7"/>
    </svg>
  ),
  rocket: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M14 4c4 0 6 2 6 6 0 0-2 6-9 9l-3-3c3-7 6-9 6-9z"/>
      <circle cx="14" cy="9" r="1.5" fill="currentColor"/>
      <path d="M7 14l-4 1 1-4M9 19l-2 1 1-2"/>
    </svg>
  ),
  link: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <path d="M10 13a5 5 0 007 0l3-3a5 5 0 00-7-7l-1 1"/>
      <path d="M14 11a5 5 0 00-7 0l-3 3a5 5 0 007 7l1-1"/>
    </svg>
  ),
  globe: (p = {}) => (
    <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" {...S} {...p}>
      <circle cx="12" cy="12" r="9"/>
      <path d="M3 12h18M12 3a14 14 0 010 18M12 3a14 14 0 000 18"/>
    </svg>
  ),
};

// Aliases for common alternative names
Icons.dollar = Icons.payroll;
Icons.money = Icons.payroll;
Icons.wallet = Icons.payroll;
Icons.calendar = Icons.cal;
Icons.heart = Icons.star;
Icons['user-plus'] = Icons.users;
Icons['file-text'] = Icons.doc;
Icons.file = Icons.doc;
Icons.building = Icons.briefcase;
Icons.clock = Icons.time;
Icons.mail = Icons.send;
Icons.phone = Icons.bell;
Icons.location = Icons.mission;
Icons.user = Icons.users;
Icons.target = Icons.review;
Icons.award = Icons.star;
Icons.book = Icons.training;
Icons.trending = Icons.trendUp;
Icons.users2 = Icons.users;
Icons.eye = Icons.search;
Icons.copy = Icons.doc;
Icons.refresh = Icons.cal;

// Fallback for any missing icon name -- returns null instead of undefined
const safeIcons = new Proxy(Icons, {
  get(target, prop) {
    return target[prop] || (() => null);
  }
});

export { safeIcons as IconsSafe };
