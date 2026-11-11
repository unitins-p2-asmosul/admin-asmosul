package br.org.asmosul.api.doacoes.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.asmosul.api.comum.config.BaseAPITest;
import br.org.asmosul.api.doacoes.dtos.KitDTO;
import br.org.asmosul.api.doacoes.models.CategoriaItem;
import br.org.asmosul.api.doacoes.models.EnderecoArmazenamento;
import br.org.asmosul.api.doacoes.models.Estoque;
import br.org.asmosul.api.doacoes.models.Item;
import br.org.asmosul.api.doacoes.models.ItemKit;
import br.org.asmosul.api.doacoes.models.Kit;
import br.org.asmosul.api.doacoes.models.UnidadeMedida;
import br.org.asmosul.api.doacoes.repositories.CategoriaItemRepository;
import br.org.asmosul.api.doacoes.repositories.EnderecoArmazenamentoRepository;
import br.org.asmosul.api.doacoes.repositories.EstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.ItemKitRepository;
import br.org.asmosul.api.doacoes.repositories.ItemRepository;
import br.org.asmosul.api.doacoes.repositories.KitRepository;
import br.org.asmosul.api.pessoas.models.Uf;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@DisplayName("Testes de Integração - KitController")
class KitControllerTest extends BaseAPITest {

    @Autowired private KitRepository kitRepository;
    @Autowired private ItemKitRepository itemKitRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private CategoriaItemRepository categoriaItemRepository;
    @Autowired private EstoqueRepository estoqueRepository;
    @Autowired private EnderecoArmazenamentoRepository enderecoArmazenamentoRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private Item itemArroz;
    private Item itemFeijao;
    private EnderecoArmazenamento enderecoPadrao;

    @BeforeEach
    void setUp() {
        itemKitRepository.deleteAll();
        kitRepository.deleteAll();
        estoqueRepository.deleteAll();
        itemRepository.deleteAll();
        categoriaItemRepository.deleteAll();
        enderecoArmazenamentoRepository.deleteAll();

        CategoriaItem categoria =
                categoriaItemRepository.save(
                        new CategoriaItem("Alimentos Não Perecíveis", "Alimentos em geral"));

        itemArroz =
                itemRepository.save(
                        new Item(
                                "Arroz Branco 5kg",
                                categoria,
                                2500,
                                UnidadeMedida.PACOTE,
                                "Pacote de arroz"));
        itemFeijao =
                itemRepository.save(
                        new Item(
                                "Feijão Carioca 1kg",
                                categoria,
                                800,
                                UnidadeMedida.PACOTE,
                                "Pacote de feijão"));

        enderecoPadrao =
                enderecoArmazenamentoRepository.save(
                        new EnderecoArmazenamento(
                                "Galpão Central",
                                "77000-000",
                                Uf.TO,
                                "Palmas",
                                "Plano Diretor Sul",
                                "Av. Teotônio Segurado",
                                "S/N",
                                null,
                                "Setor principal"));
    }

    @Nested
    @DisplayName("POST /doacoes/kits - Cadastro de Kit")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar kit com sucesso retornando status 201 e Location header")
        void cadastrar_comDadosValidos_retornaStatus201ELocationHeader() throws Exception {
            var requisicao =
                    new KitDTO.Requisicao(
                            "  Cesta   Básica   Familiar ",
                            "Cesta padrão mensal",
                            List.of(
                                    new KitDTO.ItemKitRequisicao(itemArroz.getId(), 2),
                                    new KitDTO.ItemKitRequisicao(itemFeijao.getId(), 3)));

            mockMvc.perform(
                            post("/doacoes/kits")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isCreated())
                    .andExpect(
                            header().string(
                                            "Location",
                                            Matchers.matchesPattern(".*/doacoes/kits/\\d+")))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.nome").value("Cesta Básica Familiar"))
                    .andExpect(jsonPath("$.descricao").value("Cesta padrão mensal"))
                    .andExpect(jsonPath("$.totalItens").value(2))
                    .andExpect(jsonPath("$.ativo").value(true))
                    .andExpect(jsonPath("$.itens.length()").value(2));
        }

        @Test
        @DisplayName("Deve retornar status 409 quando o nome do kit já estiver cadastrado")
        void cadastrar_comNomeDuplicado_retornaStatus409() throws Exception {
            kitRepository.save(new Kit("Cesta Básica", "Descrição"));

            var requisicao =
                    new KitDTO.Requisicao(
                            "cesta   básica",
                            "Outra descrição",
                            List.of(new KitDTO.ItemKitRequisicao(itemArroz.getId(), 1)));

            mockMvc.perform(
                            post("/doacoes/kits")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Conflito de Dados"));
        }

        @Test
        @DisplayName("Deve retornar status 400 quando houver itens repetidos no mesmo kit")
        void cadastrar_comItensRepetidos_retornaStatus400() throws Exception {
            var requisicao =
                    new KitDTO.Requisicao(
                            "Kit Duplicado",
                            "Descrição",
                            List.of(
                                    new KitDTO.ItemKitRequisicao(itemArroz.getId(), 1),
                                    new KitDTO.ItemKitRequisicao(itemArroz.getId(), 2)));

            mockMvc.perform(
                            post("/doacoes/kits")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /doacoes/kits - Listagem Paginada de Kits")
    class Listar {

        @Test
        @DisplayName("Deve retornar listagem paginada com status 200")
        void listar_comSucesso_retornaStatus200EListaPaginada() throws Exception {
            Kit kit = kitRepository.save(new Kit("Kit Inverno", "Agasalhos"));
            itemKitRepository.save(new ItemKit(itemArroz, kit, 2));

            mockMvc.perform(get("/doacoes/kits"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados").isArray())
                    .andExpect(jsonPath("$.dados.length()").value(1))
                    .andExpect(jsonPath("$.dados[0].nome").value("Kit Inverno"))
                    .andExpect(jsonPath("$.dados[0].totalItens").value(1));
        }
    }

    @Nested
    @DisplayName("GET /doacoes/kits/{id}/disponibilidade - Disponibilidade por Local")
    class Disponibilidade {

        @Test
        @DisplayName("Deve retornar quantidade máxima de kits possíveis no local de armazenamento")
        void consultarDisponibilidade_retornaStatus200EMaxKitsPossiveis() throws Exception {
            Kit kit = kitRepository.save(new Kit("Kit Alimentação", "Básico"));
            itemKitRepository.save(new ItemKit(itemArroz, kit, 2));
            itemKitRepository.save(new ItemKit(itemFeijao, kit, 3));

            estoqueRepository.save(new Estoque(itemArroz, enderecoPadrao, 10)); // 10/2 = 5
            estoqueRepository.save(new Estoque(itemFeijao, enderecoPadrao, 9)); // 9/3 = 3

            mockMvc.perform(
                            get("/doacoes/kits/{id}/disponibilidade", kit.getId())
                                    .param(
                                            "localArmazenamentoId",
                                            String.valueOf(enderecoPadrao.getId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idKit").value(kit.getId()))
                    .andExpect(jsonPath("$.localArmazenamentoId").value(enderecoPadrao.getId()))
                    .andExpect(jsonPath("$.quantidadeDisponivel").value(3))
                    .andExpect(jsonPath("$.itens.length()").value(2));
        }
    }

    @Nested
    @DisplayName("PUT /doacoes/kits/{id} e PATCH desativar/reativar")
    class AtualizarEDesativar {

        @Test
        @DisplayName("Deve atualizar kit com sucesso retornando status 200")
        void atualizar_comDadosValidos_retornaStatus200() throws Exception {
            Kit kit = kitRepository.save(new Kit("Kit Original", "Descrição original"));
            itemKitRepository.save(new ItemKit(itemArroz, kit, 1));

            var requisicao =
                    new KitDTO.Atualizacao(
                            "Kit Atualizado",
                            "Nova descrição",
                            List.of(new KitDTO.ItemKitRequisicao(itemFeijao.getId(), 4)));

            mockMvc.perform(
                            put("/doacoes/kits/{id}", kit.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Kit Atualizado"))
                    .andExpect(jsonPath("$.descricao").value("Nova descrição"))
                    .andExpect(jsonPath("$.itens.length()").value(1))
                    .andExpect(jsonPath("$.itens[0].idItem").value(itemFeijao.getId()))
                    .andExpect(jsonPath("$.itens[0].quantidade").value(4));
        }

        @Test
        @DisplayName("Deve desativar e reativar kit retornando status 204")
        void desativarEReativar_retornaStatus204() throws Exception {
            Kit kit = kitRepository.save(new Kit("Kit Temporário", "Descrição"));

            mockMvc.perform(patch("/doacoes/kits/{id}/desativar", kit.getId()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(patch("/doacoes/kits/{id}/reativar", kit.getId()))
                    .andExpect(status().isNoContent());
        }
    }
}
