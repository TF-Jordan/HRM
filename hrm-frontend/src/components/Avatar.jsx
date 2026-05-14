const COLORS = ['orange','blue','green','violet','amber','teal'];

export default function Avatar({ name = '', size, color }) {
  const i = (name.charCodeAt(0) || 0) % COLORS.length;
  const initials = name.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase();
  return <div className={`avatar ${size || ''} ${color || COLORS[i]}`}>{initials}</div>;
}
