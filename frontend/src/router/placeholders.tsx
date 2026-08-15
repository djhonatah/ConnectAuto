/**
 * Placeholders temporários só para validar a navegação entre rotas.
 * Serão substituídos por componentes reais em src/pages quando a
 * estrutura de pastas (issue "definir estrutura de pastas") for criada.
 */
export function HomePage() {
  return (
    <section>
      <h1>Início</h1>
      <p>Página inicial do ConnectAuto.</p>
    </section>
  );
}

export function SobrePage() {
  return (
    <section>
      <h1>Sobre</h1>
      <p>Rota de exemplo para validar a navegação sem reload de página.</p>
    </section>
  );
}

export function NotFoundPage() {
  return (
    <section>
      <h1>404</h1>
      <p>Página não encontrada.</p>
    </section>
  );
}
