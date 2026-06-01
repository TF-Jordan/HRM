/* global React */
/* =================================================================
   Form primitives — orange theme, consistent inputs
   ================================================================= */

const FF = {};

FF.Section = ({ title, sub, children, cols = 2 }) => (
  <div style={{marginBottom: 24}}>
    {title && (
      <div style={{
        fontSize: 11, fontWeight: 700, color: 'var(--orange-700)',
        textTransform: 'uppercase', letterSpacing: '0.08em', marginBottom: 14,
        display: 'flex', alignItems: 'center', gap: 10
      }}>
        <span style={{width: 18, height: 2, background: 'var(--grad-orange)', borderRadius: 999}}/>
        {title}
        {sub && <span style={{color: 'var(--ink-4)', fontWeight: 500, textTransform: 'none', letterSpacing: 0, marginLeft: 8, fontSize: 11}}>· {sub}</span>}
      </div>
    )}
    <div style={{display: 'grid', gridTemplateColumns: cols === 1 ? '1fr' : `repeat(${cols}, 1fr)`, gap: 16}}>
      {children}
    </div>
  </div>
);

FF.Field = ({ label, hint, required, span = 1, mapping, children, error }) => (
  <div style={{display: 'flex', flexDirection: 'column', gap: 6, gridColumn: span === 2 ? 'span 2' : 'auto'}}>
    <div style={{display: 'flex', alignItems: 'center', gap: 6}}>
      <label style={{fontSize: 12, fontWeight: 600, color: 'var(--ink-2)'}}>
        {label}
        {required && <span style={{color: 'var(--red-500)', marginLeft: 2}}>*</span>}
      </label>
      {mapping && (
        <span className="mono" style={{
          fontSize: 10, color: 'var(--ink-4)', background: 'var(--bg-soft)',
          padding: '1px 6px', borderRadius: 4, marginLeft: 'auto'
        }}>{mapping}</span>
      )}
    </div>
    {children}
    {hint && !error && <div style={{fontSize: 11, color: 'var(--ink-3)'}}>{hint}</div>}
    {error && <div style={{fontSize: 11, color: 'var(--red-600)', fontWeight: 500}}>⚠ {error}</div>}
  </div>
);

FF.Input = ({ icon, prefix, suffix, ...rest }) => (
  <div style={{
    display: 'flex', alignItems: 'center', gap: 8,
    background: '#fff', border: '1px solid var(--line)', borderRadius: 11,
    padding: '0 14px', boxShadow: 'var(--shadow-xs)',
    transition: 'all .15s'
  }}
  onFocus={ev => {
    ev.currentTarget.style.borderColor = 'var(--orange-400)';
    ev.currentTarget.style.boxShadow = '0 0 0 4px rgba(242,107,15,0.12)';
  }}
  onBlur={ev => {
    ev.currentTarget.style.borderColor = 'var(--line)';
    ev.currentTarget.style.boxShadow = 'var(--shadow-xs)';
  }}>
    {icon && <span style={{color: 'var(--ink-3)', display: 'inline-flex'}}>{icon}</span>}
    {prefix && <span style={{color: 'var(--ink-3)', fontSize: 13, fontWeight: 500}}>{prefix}</span>}
    <input style={{
      border: 'none', outline: 'none', flex: 1, fontSize: 13.5, padding: '10px 0',
      background: 'transparent', color: 'var(--ink)', fontFamily: 'inherit'
    }} {...rest}/>
    {suffix && <span style={{color: 'var(--ink-3)', fontSize: 12.5, fontWeight: 500}}>{suffix}</span>}
  </div>
);

FF.Select = ({ icon, children, ...rest }) => (
  <div style={{
    display: 'flex', alignItems: 'center', gap: 8,
    background: '#fff', border: '1px solid var(--line)', borderRadius: 11,
    padding: '0 14px', boxShadow: 'var(--shadow-xs)', position: 'relative'
  }}>
    {icon && <span style={{color: 'var(--ink-3)', display: 'inline-flex'}}>{icon}</span>}
    <select style={{
      border: 'none', outline: 'none', flex: 1, fontSize: 13.5, padding: '10px 24px 10px 0',
      background: 'transparent', color: 'var(--ink)', fontFamily: 'inherit',
      appearance: 'none'
    }} {...rest}>
      {children}
    </select>
    <span style={{position: 'absolute', right: 14, pointerEvents: 'none', color: 'var(--ink-3)'}}>
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M5 9l7 7 7-7"/></svg>
    </span>
  </div>
);

FF.Textarea = ({ rows = 3, ...rest }) => (
  <textarea rows={rows} style={{
    background: '#fff', border: '1px solid var(--line)', borderRadius: 11,
    padding: '11px 14px', fontSize: 13.5, color: 'var(--ink)',
    fontFamily: 'inherit', resize: 'vertical', outline: 'none', minHeight: 80,
    boxShadow: 'var(--shadow-xs)'
  }} {...rest}/>
);

FF.Radio = ({ options, value, onChange, mapping }) => (
  <div style={{display: 'grid', gridTemplateColumns: `repeat(${Math.min(options.length, 4)}, 1fr)`, gap: 8}}>
    {options.map(o => {
      const active = value === o.value;
      return (
        <button key={o.value} onClick={() => onChange && onChange(o.value)} style={{
          padding: '12px 14px', borderRadius: 11, textAlign: 'left', cursor: 'pointer',
          background: active ? 'var(--orange-50)' : '#fff',
          border: active ? '2px solid var(--orange-500)' : '1px solid var(--line)',
          boxShadow: active ? 'none' : 'var(--shadow-xs)',
          margin: active ? 0 : '1px'
        }}>
          <div style={{display: 'flex', alignItems: 'center', gap: 8}}>
            <div style={{
              width: 14, height: 14, borderRadius: '50%',
              border: active ? 'none' : '2px solid var(--ink-4)',
              background: active ? 'var(--orange-500)' : 'transparent',
              display: 'grid', placeItems: 'center'
            }}>
              {active && <div style={{width: 5, height: 5, background: '#fff', borderRadius: '50%'}}/>}
            </div>
            <div style={{flex: 1}}>
              <div style={{fontSize: 13, fontWeight: 600}}>{o.label}</div>
              {o.sub && <div style={{fontSize: 11, color: 'var(--ink-3)', marginTop: 2}}>{o.sub}</div>}
            </div>
            {o.tag && <span className="mono" style={{fontSize: 9, padding: '1px 6px', borderRadius: 4, background: 'var(--bg-soft)', color: 'var(--ink-3)'}}>{o.tag}</span>}
          </div>
        </button>
      );
    })}
  </div>
);

FF.FileDrop = ({ accept = 'PDF, JPG, PNG', maxMb = 5, hint }) => (
  <div style={{
    padding: '20px', border: '2px dashed var(--line-strong)', borderRadius: 12,
    background: 'var(--bg-dim)', textAlign: 'center', cursor: 'pointer',
    transition: 'all .15s'
  }}
  onMouseEnter={ev => {
    ev.currentTarget.style.borderColor = 'var(--orange-400)';
    ev.currentTarget.style.background = 'var(--orange-50)';
  }}
  onMouseLeave={ev => {
    ev.currentTarget.style.borderColor = 'var(--line-strong)';
    ev.currentTarget.style.background = 'var(--bg-dim)';
  }}>
    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="var(--ink-4)" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" style={{margin: '0 auto 8px', display: 'block'}}>
      <path d="M12 17V5M7 10l5-5 5 5M5 21h14"/>
    </svg>
    <div style={{fontSize: 13, fontWeight: 600, marginBottom: 4}}>Cliquer ou glisser un fichier</div>
    <div style={{fontSize: 11, color: 'var(--ink-3)'}}>{accept} · max {maxMb} MB{hint ? ' · ' + hint : ''}</div>
  </div>
);

FF.Picker = ({ icon, placeholder, value, sub }) => (
  <div style={{
    display: 'flex', alignItems: 'center', gap: 10,
    background: '#fff', border: '1px solid var(--line)', borderRadius: 11,
    padding: '10px 14px', boxShadow: 'var(--shadow-xs)', cursor: 'pointer'
  }}>
    {icon && <span style={{color: 'var(--ink-3)'}}>{icon}</span>}
    {value ? (
      <div style={{flex: 1}}>
        <div style={{fontSize: 13.5, fontWeight: 600}}>{value}</div>
        {sub && <div style={{fontSize: 11, color: 'var(--ink-3)'}}>{sub}</div>}
      </div>
    ) : (
      <span style={{fontSize: 13.5, color: 'var(--ink-4)', flex: 1}}>{placeholder}</span>
    )}
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="var(--ink-3)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M5 9l7 7 7-7"/></svg>
  </div>
);

FF.EmployeePicker = ({ employee, label = "Employé concerné" }) => {
  const { Avatar } = window;
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 12,
      background: '#fff', border: '1px solid var(--line)', borderRadius: 11,
      padding: '8px 12px 8px 8px', boxShadow: 'var(--shadow-xs)', cursor: 'pointer'
    }}>
      {employee ? (
        <>
          <Avatar name={employee.name} color={employee.color || 'orange'}/>
          <div style={{flex: 1}}>
            <div style={{fontSize: 13, fontWeight: 600}}>{employee.name}</div>
            <div className="mono" style={{fontSize: 11, color: 'var(--ink-3)'}}>{employee.id} · {employee.role}</div>
          </div>
        </>
      ) : (
        <span style={{fontSize: 13.5, color: 'var(--ink-4)', flex: 1, padding: '6px 4px'}}>Rechercher un employé…</span>
      )}
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="var(--ink-3)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M5 9l7 7 7-7"/></svg>
    </div>
  );
};

/* Form wrapper */
FF.FormShell = ({ title, subtitle, mapping, children, sidebar, onCancel, onSubmit, submitLabel = 'Enregistrer', draftLabel = 'Brouillon', meta }) => {
  const { Icons } = window;
  return (
    <div>
      <div style={{marginBottom: 24}}>
        <div style={{display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8}}>
          <span style={{
            fontFamily: 'JetBrains Mono', fontSize: 11, fontWeight: 600,
            color: 'var(--orange-700)', background: 'var(--orange-50)',
            padding: '3px 10px', borderRadius: 999
          }}>→ {mapping}</span>
          {meta && <span style={{fontSize: 12, color: 'var(--ink-3)'}}>{meta}</span>}
        </div>
        <h1 className="h-display" style={{fontSize: 32}}>{title}</h1>
        {subtitle && <div className="page-subtitle">{subtitle}</div>}
      </div>

      <div style={{display: 'grid', gridTemplateColumns: sidebar ? '1fr 320px' : '1fr', gap: 20}}>
        <div className="card card-pad-lg">
          {children}

          <div style={{
            display: 'flex', justifyContent: 'flex-end', gap: 10,
            marginTop: 32, paddingTop: 24, borderTop: '1px solid var(--line-soft)'
          }}>
            <button className="btn btn-ghost" onClick={onCancel}>Annuler</button>
            <button className="btn btn-secondary">
              <Icons.doc size={14}/> {draftLabel}
            </button>
            <button className="btn btn-primary" onClick={onSubmit}>
              <Icons.send size={14}/> {submitLabel}
            </button>
          </div>
        </div>

        {sidebar && <div className="col gap-3" style={{minWidth: 0}}>{sidebar}</div>}
      </div>
    </div>
  );
};

window.FF = FF;
