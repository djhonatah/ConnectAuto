export interface ViaCepResponse {
  cep: string;
  logradouro: string;
  bairro: string;
  localidade: string;
  uf: string;
  erro?: boolean;
}

export const viaCepApi = {
  buscar: async (cep: string): Promise<ViaCepResponse> => {
    const response = await fetch(`https://viacep.com.br/ws/${cep}/json/`);
    if (!response.ok) {
      throw new Error(`Erro ${response.status} ao consultar o CEP`);
    }
    return response.json();
  },
};
