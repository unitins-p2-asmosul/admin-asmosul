package br.org.asmosul.api.doacoes.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.exceptions.GlobalExceptionHandler;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import br.org.asmosul.api.doacoes.dtos.MovimentacaoDTO;
import br.org.asmosul.api.doacoes.services.MovimentacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Controller - MovimentacaoController")
class MovimentacaoControllerTest {

    @Mock
    private MovimentacaoService movimentacaoService;

    @InjectMocks
    private MovimentacaoController movimentacaoController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(movimentacaoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Nested
    @DisplayName("POST /doacoes/movimentacoes - Cadastro")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar movimentação com sucesso retornando status 201 e Location header")
        void cadastrar_comDadosValidos_retornaStatus201ELocationHeader() throws Exception {
            var itemReq = new MovimentacaoDTO.ItemMovimentacaoRequisicao(10L, 1L, 5);
            var requisicao = new MovimentacaoDTO.Requisicao(2L, "Transferência entre estoques", List.of(itemReq));

            var itemDetalhe = new MovimentacaoDTO.ItemMovimentacaoDetalhe(100L, 10L, "Arroz", 1L, "Origem", 5);
            var detalheRetorno = new MovimentacaoDTO.Detalhe(
                    1L, 2L, "Destino", LocalDateTime.now(), "Transferência entre estoques", List.of(itemDetalhe));

            when(movimentacaoService.cadastrar(any(MovimentacaoDTO.Requisicao.class))).thenReturn(detalheRetorno);

            mockMvc.perform(
                            post("/doacoes/movimentacoes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isCreated())
                    .andExpect(
                            header().string(
                                    "Location",
                                    org.hamcrest.Matchers.matchesPattern(".*/doacoes/movimentacoes/1")))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.idNovoArmazenamento").value(2L))
                    .andExpect(jsonPath("$.nomeNovoArmazenamento").value("Destino"))
                    .andExpect(jsonPath("$.descricao").value("Transferência entre estoques"))
                    .andExpect(jsonPath("$.itens[0].quantidade").value(5));
        }

        @Test
        @DisplayName("Deve retornar status 404 quando um recurso referenciado não for encontrado na criação")
        void cadastrar_comEntidadeInexistente_retornaStatus404() throws Exception {
            var itemReq = new MovimentacaoDTO.ItemMovimentacaoRequisicao(999L, 1L, 5);
            var requisicao = new MovimentacaoDTO.Requisicao(2L, "Item inexistente", List.of(itemReq));

            when(movimentacaoService.cadastrar(any(MovimentacaoDTO.Requisicao.class)))
                    .thenThrow(new EntidadeNaoEncontradaException("Item não encontrado com o ID informado: 999"));

            mockMvc.perform(
                            post("/doacoes/movimentacoes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Recurso Não Encontrado"))
                    .andExpect(jsonPath("$.detail").value("Item não encontrado com o ID informado: 999"));
        }

        @Test
        @DisplayName("Deve retornar status 400 quando a lista de itens estiver vazia")
        void cadastrar_comItensVazios_retornaStatus400() throws Exception {
            var requisicao = new MovimentacaoDTO.Requisicao(2L, "Sem itens", List.of());

            mockMvc.perform(
                            post("/doacoes/movimentacoes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 400 quando regra de negócio for violada (origem igual destino ou saldo insuficiente)")
        void cadastrar_comRegraViolada_retornaStatus400() throws Exception {
            var itemReq = new MovimentacaoDTO.ItemMovimentacaoRequisicao(10L, 2L, 5);
            var requisicao = new MovimentacaoDTO.Requisicao(2L, "Origem igual a destino", List.of(itemReq));

            when(movimentacaoService.cadastrar(any(MovimentacaoDTO.Requisicao.class)))
                    .thenThrow(ValidationException.of("itens", "O endereço de origem não pode ser idêntico ao endereço de destino no mesmo item."));

            mockMvc.perform(
                            post("/doacoes/movimentacoes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Erro de Validação"));
        }
    }

    @Nested
    @DisplayName("GET /doacoes/movimentacoes - Listagem")
    class Listar {

        @Test
        @DisplayName("Deve listar movimentações retornando status 200 e resposta paginada")
        void listar_retornaStatus200ERespostaPaginada() throws Exception {
            var resumo = new MovimentacaoDTO.Resumo(1L, "Arroz", "Origem", "Destino", 10, LocalDateTime.now());
            var respostaPaginada = RespostaPaginada.dePage(new PageImpl<>(List.of(resumo), PageRequest.of(0, 10), 1));

            when(movimentacaoService.listar(any(), any())).thenReturn(respostaPaginada);

            mockMvc.perform(get("/doacoes/movimentacoes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados[0].id").value(1L))
                    .andExpect(jsonPath("$.dados[0].nomeItem").value("Arroz"))
                    .andExpect(jsonPath("$.dados[0].quantidade").value(10))
                    .andExpect(jsonPath("$.totalElementos").value(1));
        }
    }

    @Nested
    @DisplayName("GET /doacoes/movimentacoes/{id} - Detalhe")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar status 200 e os detalhes da movimentação quando existir")
        void buscarPorId_quandoExistir_retornaStatus200EDetalhe() throws Exception {
            var itemDetalhe = new MovimentacaoDTO.ItemMovimentacaoDetalhe(100L, 10L, "Feijão", 1L, "Origem", 8);
            var detalhe = new MovimentacaoDTO.Detalhe(
                    1L, 2L, "Destino", LocalDateTime.now(), "Descrição detalhada", List.of(itemDetalhe));

            when(movimentacaoService.buscarPorId(1L)).thenReturn(detalhe);

            mockMvc.perform(get("/doacoes/movimentacoes/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nomeNovoArmazenamento").value("Destino"))
                    .andExpect(jsonPath("$.descricao").value("Descrição detalhada"))
                    .andExpect(jsonPath("$.itens[0].nomeItem").value("Feijão"));
        }

        @Test
        @DisplayName("Deve retornar status 404 quando movimentação não existir")
        void buscarPorId_quandoNaoExistir_retornaStatus404() throws Exception {
            when(movimentacaoService.buscarPorId(999L))
                    .thenThrow(new EntidadeNaoEncontradaException("Movimentação não encontrada com o ID informado: 999"));

            mockMvc.perform(get("/doacoes/movimentacoes/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Recurso Não Encontrado"));
        }
    }

    @Nested
    @DisplayName("PATCH /doacoes/movimentacoes/{id} - Atualização")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar descrição com sucesso retornando status 200")
        void atualizar_comDadosValidos_retornaStatus200() throws Exception {
            var atualizacao = new MovimentacaoDTO.Atualizacao("Descrição alterada com sucesso");
            var detalhe = new MovimentacaoDTO.Detalhe(
                    1L, 2L, "Destino", LocalDateTime.now(), "Descrição alterada com sucesso", List.of());

            when(movimentacaoService.atualizar(eq(1L), any(MovimentacaoDTO.Atualizacao.class))).thenReturn(detalhe);

            mockMvc.perform(
                            patch("/doacoes/movimentacoes/{id}", 1L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(atualizacao)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.descricao").value("Descrição alterada com sucesso"));
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar atualizar movimentação inexistente")
        void atualizar_quandoNaoExistir_retornaStatus404() throws Exception {
            var atualizacao = new MovimentacaoDTO.Atualizacao("Descrição nova");

            when(movimentacaoService.atualizar(eq(999L), any(MovimentacaoDTO.Atualizacao.class)))
                    .thenThrow(new EntidadeNaoEncontradaException("Movimentação não encontrada com o ID informado: 999"));

            mockMvc.perform(
                            patch("/doacoes/movimentacoes/{id}", 999L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(atualizacao)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Regra RN22 - Ausência de Endpoint de Deleção")
    class RegraRN22 {

        @Test
        @DisplayName("Deve retornar 405 Method Not Allowed ao tentar chamar DELETE em /doacoes/movimentacoes/{id}")
        void deletar_deveRetornar405MethodNotAllowed() throws Exception {
            mockMvc.perform(delete("/doacoes/movimentacoes/{id}", 1L))
                    .andExpect(status().isMethodNotAllowed());
        }
    }
}
