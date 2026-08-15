import './SobrePage.css';

export function SobrePage() {
  return (
    <section className="sobre-page">
      <h1>Sobre</h1>
      <p className="sobre-page__lead">
        ConnectAuto é a plataforma interna de gestão de estoque para concessionárias: um lugar único
        para cadastrar, editar e dar baixa em veículos conforme eles entram e saem do pátio.
      </p>
      <dl className="sobre-page__facts">
        <div>
          <dt>Cadastro</dt>
          <dd>Marca, modelo, ano, cor, combustível, chassi e valor de cada veículo.</dd>
        </div>
        <div>
          <dt>Estoque</dt>
          <dd>Lista viva do que está disponível, com edição e exclusão diretas.</dd>
        </div>
        <div>
          <dt>Integridade</dt>
          <dd>
            Validações espelham as regras do backend, então o que é salvo é sempre consistente.
          </dd>
        </div>
      </dl>
    </section>
  );
}
