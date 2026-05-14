export function Donut({ data = [], segments, size = 160, thick = 28, width }) {
  const arr = (data && data.length ? data : segments) || [];
  const w = thick ?? width ?? 28;
  const total = arr.reduce((s, d) => s + (d.value || 0), 0) || 1;
  const r = (size - w) / 2;
  const c = 2 * Math.PI * r;
  let acc = 0;
  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
      <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="var(--bg-soft)" strokeWidth={w} />
      {arr.map((d, i) => {
        const len = (d.value / total) * c;
        const dash = `${len} ${c - len}`;
        const off = c - acc;
        acc += len;
        return (
          <circle key={i} cx={size / 2} cy={size / 2} r={r} fill="none"
            stroke={d.color} strokeWidth={w} strokeDasharray={dash} strokeDashoffset={off}
            transform={`rotate(-90 ${size / 2} ${size / 2})`} />
        );
      })}
    </svg>
  );
}

export function Spark({ data = [], color = 'var(--orange-500)', h = 40, w = 120, fill }) {
  if (!data?.length) return null;
  const max = Math.max(...data), min = Math.min(...data);
  const step = w / (data.length - 1);
  const pts = data.map((v, i) => {
    const x = i * step;
    const y = h - ((v - min) / (max - min || 1)) * (h - 4) - 2;
    return [x, y];
  });
  const d = "M " + pts.map(p => p.join(",")).join(" L ");
  const fillD = d + ` L ${w},${h} L 0,${h} Z`;
  return (
    <svg width={w} height={h} viewBox={`0 0 ${w} ${h}`}>
      {fill && <path d={fillD} fill={fill} opacity=".22" />}
      <path d={d} stroke={color} strokeWidth="2" fill="none" strokeLinecap="round" />
      <circle cx={pts[pts.length - 1][0]} cy={pts[pts.length - 1][1]} r="3" fill={color} />
    </svg>
  );
}

export function Bars({ data = [], h = 140 }) {
  if (!data.length) return null;
  const max = Math.max(...data.map(d => (typeof d === 'object' ? d.value : d))) || 1;
  return (
    <div style={{ display: 'flex', alignItems: 'flex-end', gap: 8, height: h, padding: '8px 0' }}>
      {data.map((d, i) => {
        const v = typeof d === 'object' ? d.value : d;
        const label = typeof d === 'object' ? d.label : '';
        const color = typeof d === 'object' ? d.color : 'var(--grad-orange)';
        return (
          <div key={i} style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 6, alignItems: 'center' }}>
            <div style={{ flex: 1, width: '100%', display: 'flex', alignItems: 'flex-end' }}>
              <div style={{ width: '100%', background: color, borderRadius: '6px 6px 0 0', height: `${(v / max) * 100}%`, minHeight: 4 }} />
            </div>
            {label && <div style={{ fontSize: 10, color: 'var(--ink-3)', fontWeight: 600 }}>{label}</div>}
          </div>
        );
      })}
    </div>
  );
}

export function Line({ data = [], h = 180, color = 'var(--orange-500)', labels = [], fill }) {
  if (!data.length) return null;
  const w = 560, pL = 32, pB = 24, pT = 10, pR = 8;
  const max = Math.max(...data) * 1.1, min = Math.min(...data) * 0.9;
  const innerW = w - pL - pR, innerH = h - pT - pB;
  const step = innerW / (data.length - 1);
  const pts = data.map((v, i) => [pL + i * step, pT + innerH - ((v - min) / (max - min || 1)) * innerH]);
  const d = "M " + pts.map(p => p.join(",")).join(" L ");
  const fillD = d + ` L ${pts[pts.length - 1][0]},${pT + innerH} L ${pts[0][0]},${pT + innerH} Z`;
  return (
    <svg viewBox={`0 0 ${w} ${h}`} width="100%" style={{ maxHeight: h }}>
      {[0, 1, 2, 3, 4].map(i => {
        const y = pT + (innerH / 4) * i;
        return <line key={i} x1={pL} y1={y} x2={w - pR} y2={y} stroke="var(--line-soft)" strokeDasharray="3 3" />;
      })}
      {fill && <path d={fillD} fill={fill} opacity=".18" />}
      <path d={d} stroke={color} strokeWidth="2.2" fill="none" strokeLinecap="round" strokeLinejoin="round" />
      {pts.map((p, i) => <circle key={i} cx={p[0]} cy={p[1]} r="2.5" fill="white" stroke={color} strokeWidth="1.5" />)}
      {labels.map((l, i) => <text key={i} x={pts[i][0]} y={h - 6} textAnchor="middle" fontSize="9" fill="var(--ink-3)">{l}</text>)}
    </svg>
  );
}
