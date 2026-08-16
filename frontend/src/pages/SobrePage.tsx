import { BrandMark } from '../components/BrandMark';
import './SobrePage.css';

export function SobrePage() {
  return (
    <section className="sobre-page">
      <div className="sobre-page__hero">
        <BrandMark className="sobre-page__mark" />
        <h1>
          Connect<strong>Auto</strong>
        </h1>
        <p className="sobre-page__tagline">
          Gestão de frota e rede de concessionárias em um único painel.
        </p>
      </div>

      <p className="sobre-page__lead">
        O ConnectAuto é a plataforma interna de gestão de estoque para concessionárias: um lugar
        único para cadastrar, editar e dar baixa em veículos conforme eles entram e saem do pátio.
      </p>

      <div className="sobre-page__cards">
        <div className="sobre-page__card">
          <div className="sobre-page__card-icon sobre-page__card-icon--accent">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M3 13l1.5-5a2 2 0 0 1 2-1.4h11a2 2 0 0 1 2 1.4L21 13" />
              <path d="M3 13h18v5a1 1 0 0 1-1 1h-1.5a1 1 0 0 1-1-1v-1h-11v1a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1z" />
            </svg>
          </div>
          <h3>Cadastro completo</h3>
          <p>
            Marca, modelo, ano, cor, combustível, chassi e valor — todos os dados de cada veículo em
            um só lugar.
          </p>
        </div>

        <div className="sobre-page__card">
          <div className="sobre-page__card-icon sobre-page__card-icon--success">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M3 21h18M5 21V7l7-4 7 4v14M9 21v-6h6v6" />
            </svg>
          </div>
          <h3>Rede de concessionárias</h3>
          <p>
            Associe veículos a concessionárias e acompanhe o estoque de cada ponto de venda em tempo
            real.
          </p>
        </div>

        <div className="sobre-page__card">
          <div className="sobre-page__card-icon sobre-page__card-icon--neutral">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M9 12l2 2 4-4" />
              <circle cx="12" cy="12" r="10" />
            </svg>
          </div>
          <h3>Integridade garantida</h3>
          <p>
            Validações espelham as regras do backend — o que é salvo é sempre consistente e
            confiável.
          </p>
        </div>

        <div className="sobre-page__card">
          <div className="sobre-page__card-icon sobre-page__card-icon--accent">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0Z" />
              <circle cx="12" cy="10" r="3" />
            </svg>
          </div>
          <h3>CEP inteligente</h3>
          <p>
            Integração automática com ViaCEP para preenchimento e validação de endereços das
            concessionárias.
          </p>
        </div>
      </div>

      <div className="sobre-page__stack">
        <h2>Tecnologias</h2>
        <div className="sobre-page__tech-grid">
          <div className="sobre-page__tech">
            <span className="sobre-page__tech-name">React</span>
            <span className="sobre-page__tech-desc">Interface reativa</span>
          </div>
          <div className="sobre-page__tech">
            <span className="sobre-page__tech-name">Spring Boot</span>
            <span className="sobre-page__tech-desc">Backend Java</span>
          </div>
          <div className="sobre-page__tech">
            <span className="sobre-page__tech-name">React Query</span>
            <span className="sobre-page__tech-desc">Cache inteligente</span>
          </div>
          <div className="sobre-page__tech">
            <span className="sobre-page__tech-name">MapStruct</span>
            <span className="sobre-page__tech-desc">Mapeamento de DTOs</span>
          </div>
          <div className="sobre-page__tech">
            <span className="sobre-page__tech-name">ViaCEP</span>
            <span className="sobre-page__tech-desc">Validação de endereço</span>
          </div>
          <div className="sobre-page__tech">
            <span className="sobre-page__tech-name">Zod</span>
            <span className="sobre-page__tech-desc">Validação de formulário</span>
          </div>
        </div>
      </div>
    </section>
  );
}
