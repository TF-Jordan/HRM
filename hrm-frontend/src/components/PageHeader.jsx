export default function PageHeader({ uc, crumbs = [], breadcrumbs, title, subtitle, actions, children }) {
  const bc = (crumbs && crumbs.length) ? crumbs : (breadcrumbs || []);
  return (
    <div className="page-header">
      <div>
        {(uc || bc.length > 0) && (
          <div className="breadcrumb">
            {uc && <span style={{ fontFamily: 'JetBrains Mono', fontSize: 11, color: 'var(--orange-700)', background: 'var(--orange-50)', padding: '2px 8px', borderRadius: 999, marginRight: 8 }}>{uc}</span>}
            {bc.map((c, i) => (
              <span key={i} style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                <span style={i === bc.length - 1 ? { color: 'var(--ink-2)', fontWeight: 600 } : {}}>{c}</span>
                {i < bc.length - 1 && <span className="crumb-sep">›</span>}
              </span>
            ))}
          </div>
        )}
        <h1 className="h-display">{title}</h1>
        {subtitle && <div className="page-subtitle">{subtitle}</div>}
        {children}
      </div>
      {actions && <div className="row gap-2">{actions}</div>}
    </div>
  );
}
