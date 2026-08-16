interface BrandMarkProps {
  className?: string;
}

/** Marca ConnectAuto: pino de localização + silhueta de carro, sempre sobre
 * fundo branco — usada na sidebar e na tela de login. */
export function BrandMark({ className }: BrandMarkProps) {
  return (
    <svg className={className} viewBox="-10 -6 120 140" aria-hidden="true">
      <rect x="-10" y="-6" width="120" height="140" rx="18" fill="#F5F5F5" />
      <path
        d="M50,4 C75,4 94,23 94,48 C94,78 50,122 50,122 C50,122 6,78 6,48 C6,23 25,4 50,4 Z"
        fill="var(--accent)"
      />
      <circle cx="50" cy="47" r="30" fill="#0A0A0B" />
      <g
        transform="translate(35,33) scale(1.25)"
        stroke="#F5F5F5"
        strokeWidth="2"
        fill="none"
        strokeLinecap="round"
        strokeLinejoin="round"
      >
        <path d="M3 13l1.5-5a2 2 0 0 1 2-1.4h11a2 2 0 0 1 2 1.4L21 13" />
        <path d="M3 13h18v4a1 1 0 0 1-1 1h-1.4a1 1 0 0 1-1-1v-.7h-11v.7a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1z" />
        <circle cx="7.5" cy="16.3" r="1.5" fill="#F5F5F5" />
        <circle cx="16.5" cy="16.3" r="1.5" fill="#F5F5F5" />
      </g>
    </svg>
  );
}
