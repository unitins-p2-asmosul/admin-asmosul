package br.org.asmosul.api.pessoas.services;

import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import br.org.asmosul.api.pessoas.dtos.CepDTO;
import br.org.asmosul.api.pessoas.models.Uf;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CepService {

    private final RestClient restClient;

    public CepService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("https://viacep.com.br/ws").build();
    }

    public CepService() {
        this(RestClient.builder());
    }

    public CepDTO buscarCep(String cep) {
        if (cep == null || cep.isBlank()) {
            throw ValidationException.of("cep", "O CEP é obrigatório");
        }

        String cepLimpo = cep.replaceAll("\\D", "");

        if (cepLimpo.length() != 8) {
            throw ValidationException.of("cep", "O CEP deve conter 8 dígitos numéricos");
        }

        try {
            ViaCepResponse response =
                    restClient
                            .get()
                            .uri("/{cep}/json/", cepLimpo)
                            .retrieve()
                            .body(ViaCepResponse.class);

            if (response == null || response.isErro()) {
                throw new EntidadeNaoEncontradaException("CEP não encontrado: " + cep);
            }

            Uf ufEnum = null;
            if (response.uf() != null && !response.uf().isBlank()) {
                try {
                    ufEnum = Uf.valueOf(response.uf().trim().toUpperCase());
                } catch (IllegalArgumentException ignored) {
                }
            }

            return new CepDTO(
                    response.cep(),
                    response.logradouro(),
                    response.complemento(),
                    response.bairro(),
                    response.localidade(),
                    ufEnum);
        } catch (EntidadeNaoEncontradaException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new EntidadeNaoEncontradaException("CEP não encontrado: " + cep);
        }
    }

    public record ViaCepResponse(
            String cep,
            String logradouro,
            String complemento,
            String bairro,
            String localidade,
            String uf,
            String erro) {
        public boolean isErro() {
            return "true".equalsIgnoreCase(erro);
        }
    }
}
