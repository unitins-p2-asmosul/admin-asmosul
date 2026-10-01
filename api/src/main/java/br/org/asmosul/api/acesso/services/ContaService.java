package br.org.asmosul.api.acesso.services;

import br.org.asmosul.api.acesso.dtos.ContaDTO;
import br.org.asmosul.api.acesso.dtos.ContaFiltroDTO;
import br.org.asmosul.api.acesso.models.Conta;
import br.org.asmosul.api.acesso.models.Perfil;
import br.org.asmosul.api.acesso.repositories.ContaRepository;
import br.org.asmosul.api.acesso.repositories.ContaSpecification;
import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.ConflitoDadosException;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import br.org.asmosul.api.comum.utils.PaginacaoUtils;
import br.org.asmosul.api.pessoas.models.Pessoa;
import br.org.asmosul.api.pessoas.models.TipoPessoa;
import br.org.asmosul.api.pessoas.repositories.PessoaRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContaService {

    private static final Set<String> CAMPOS_ORDENACAO_VALIDOS =
            Set.of("id", "nomeUsuario", "dataCriacao", "dataInativo", "redefinirSenha");

    // Limite do algoritmo BCrypt: bytes excedentes são rejeitados no encode
    private static final int TAMANHO_MAXIMO_SENHA_BYTES = 72;

    private final ContaRepository contaRepository;
    private final PessoaRepository pessoaRepository;
    private final PasswordEncoder passwordEncoder;

    public ContaService(
            ContaRepository contaRepository,
            PessoaRepository pessoaRepository,
            PasswordEncoder passwordEncoder) {
        this.contaRepository = contaRepository;
        this.pessoaRepository = pessoaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ContaDTO.Detalhe cadastrar(ContaDTO.Requisicao requisicao) {
        Pessoa pessoa =
                pessoaRepository
                        .findById(requisicao.pessoaId())
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Pessoa não encontrada com o ID informado: "
                                                        + requisicao.pessoaId()));

        if (!pessoa.isAtivo()) {
            throw ValidationException.of("pessoaId", "A pessoa informada está inativa.");
        }

        // RN018 - Apenas pessoas físicas podem possuir contas
        if (pessoa.getTipoPessoa() == TipoPessoa.JURIDICA) {
            throw ValidationException.of(
                    "pessoaId", "Apenas pessoas físicas podem possuir contas.");
        }

        // RN010 - Uma pessoa pode possuir no máximo uma conta
        if (contaRepository.existsByPessoaId(pessoa.getId())) {
            throw new ConflitoDadosException("Esta pessoa já possui uma conta vinculada.");
        }

        if (contaRepository.existsByNomeUsuario(requisicao.nomeUsuario())) {
            throw new ConflitoDadosException("Já existe uma conta com este nome de usuário.");
        }

        validarTamanhoSenha("senhaTemporaria", requisicao.senhaTemporaria());
        String senhaHash = passwordEncoder.encode(requisicao.senhaTemporaria());

        Conta contaSalva = contaRepository.save(requisicao.paraEntidade(pessoa, senhaHash));

        return ContaDTO.Detalhe.deEntidade(contaSalva);
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<ContaDTO.Resumo> listar(ContaFiltroDTO filtro, Pageable paginacao) {
        Pageable paginacaoSanitizada =
                PaginacaoUtils.sanitizarPaginacao(
                        paginacao, CAMPOS_ORDENACAO_VALIDOS, "nomeUsuario");

        Page<Conta> pagina =
                contaRepository.findAll(ContaSpecification.comFiltro(filtro), paginacaoSanitizada);

        return RespostaPaginada.dePage(pagina.map(ContaDTO.Resumo::deEntidade));
    }

    // US-54 - O detalhe exibe também contas inativas (com a data de inativação)
    @Transactional(readOnly = true)
    public ContaDTO.Detalhe buscarPorId(Long id) {
        Conta conta =
                contaRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Conta não encontrada com o ID informado: " + id));

        return ContaDTO.Detalhe.deEntidade(conta);
    }

    // RN019 - Apenas o nome de usuário pode ser alterado; a pessoa vinculada é imutável
    @Transactional
    public ContaDTO.Detalhe atualizar(Long id, ContaDTO.Atualizacao requisicao) {
        Conta conta = buscarContaAtiva(id);

        if (contaRepository.existsByNomeUsuarioAndIdNot(requisicao.nomeUsuario(), id)) {
            throw new ConflitoDadosException("Já existe uma conta com este nome de usuário.");
        }

        conta.alterarNomeUsuario(requisicao.nomeUsuario());

        return ContaDTO.Detalhe.deEntidade(conta);
    }

    // RN01 - Atribuição e remoção de perfis de acesso
    @Transactional
    public ContaDTO.Detalhe atualizarPerfis(Long id, ContaDTO.AtualizacaoPerfis requisicao) {
        Conta conta = buscarContaAtiva(id);

        conta.atualizarPerfis(requisicao.perfis());

        return ContaDTO.Detalhe.deEntidade(conta);
    }

    // US-59 - O Gerenciador de Acesso define uma nova senha temporária
    @Transactional
    public void redefinirSenhaAdmin(Long id, ContaDTO.RedefinirSenhaAdmin requisicao) {
        Conta conta = buscarContaAtiva(id);

        validarTamanhoSenha("novaSenhaTemporaria", requisicao.novaSenhaTemporaria());
        conta.definirSenhaTemporaria(passwordEncoder.encode(requisicao.novaSenhaTemporaria()));
    }

    // US-46 - O próprio usuário, identificado pelo token, define a senha definitiva
    @Transactional
    public void alterarSenhaPropria(String nomeUsuario, ContaDTO.RedefinirSenhaPropria requisicao) {
        Conta conta =
                contaRepository
                        .findByNomeUsuarioAndDataInativoIsNull(nomeUsuario)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Conta ativa não encontrada para o usuário autenticado."));

        if (!passwordEncoder.matches(requisicao.senhaAtual(), conta.getSenhaHash())) {
            throw ValidationException.of("senhaAtual", "A senha atual está incorreta.");
        }

        validarTamanhoSenha("novaSenha", requisicao.novaSenha());
        conta.definirSenhaDefinitiva(passwordEncoder.encode(requisicao.novaSenha()));
    }

    @Transactional
    public void desativar(Long id) {
        Conta conta = buscarContaAtiva(id);

        conta.desativar();
    }

    // RN02 - A reativação só é permitida se a pessoa vinculada continuar ativa
    @Transactional
    public void reativar(Long id) {
        Conta conta =
                contaRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Conta não encontrada com o ID informado: " + id));

        // Reativar uma conta já ativa é idempotente, como em Pessoas e Categorias
        if (conta.isAtivo()) {
            return;
        }

        if (!conta.getPessoa().isAtivo()) {
            throw ValidationException.of("pessoaId", "A pessoa vinculada está inativa.");
        }

        conta.reativar();
    }

    // US-57 - Opções estáticas de perfis de acesso
    public List<Perfil> listarPerfis() {
        return List.of(Perfil.values());
    }

    private Conta buscarContaAtiva(Long id) {
        return contaRepository
                .findByIdAndDataInativoIsNull(id)
                .orElseThrow(
                        () ->
                                new EntidadeNaoEncontradaException(
                                        "Conta ativa não encontrada com o ID informado: " + id));
    }

    private void validarTamanhoSenha(String campo, String senha) {
        if (senha.getBytes(StandardCharsets.UTF_8).length > TAMANHO_MAXIMO_SENHA_BYTES) {
            throw ValidationException.of(campo, "A senha deve ter no máximo 72 bytes.");
        }
    }
}
