package br.org.asmosul.api.acesso.config;

import br.org.asmosul.api.acesso.models.Conta;
import br.org.asmosul.api.acesso.models.Perfil;
import br.org.asmosul.api.acesso.repositories.ContaRepository;
import br.org.asmosul.api.pessoas.models.Pessoa;
import br.org.asmosul.api.pessoas.models.TipoPessoa;
import br.org.asmosul.api.pessoas.repositories.PessoaRepository;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
public class ContaAdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ContaAdminSeeder.class);

    private static final String USUARIO_ADMIN = "asmosul";
    private static final String SENHA_PADRAO = "Asmosul@1234";

    private final ContaRepository contaRepository;
    private final PessoaRepository pessoaRepository;
    private final PasswordEncoder passwordEncoder;

    public ContaAdminSeeder(
        ContaRepository contaRepository,
        PessoaRepository pessoaRepository,
        PasswordEncoder passwordEncoder) {
        this.contaRepository = contaRepository;
        this.pessoaRepository = pessoaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Idempotência: só cria se a conta "asmosul" ainda não existir no banco
        if (contaRepository.existsByNomeUsuario(USUARIO_ADMIN)) {
            log.info("Conta padrão '{}' já existe no banco de dados. Pulando seeder.", USUARIO_ADMIN);
            return;
        }

        // 1. Obtém uma pessoa física ativa ou cria uma pessoa administradora caso a tabela esteja vazia
        Pessoa pessoaAdmin = obterOuCriarPessoaFisicaParaAdmin();

        // 2. Cria a conta com todos os perfis e senha codificada via BCrypt
        Conta conta = new Conta(
            pessoaAdmin,
            USUARIO_ADMIN,
            passwordEncoder.encode(SENHA_PADRAO),
            Set.of(Perfil.values()) // Atribui todos os 5 perfis da RN01
        );

        // US-46: Definimos como false para não forçar troca de senha no dev/local
        conta.definirSenhaDefinitiva(conta.getSenhaHash());

        contaRepository.save(conta);

        log.info("Seeder de conta executado com sucesso: usuário '{}' criado vinculado à pessoa '{}'.",
            USUARIO_ADMIN, pessoaAdmin.getNome());
    }

    private Pessoa obterOuCriarPessoaFisicaParaAdmin() {
        // Tenta achar qualquer pessoa física ativa que ainda não possua conta vinculada
        return pessoaRepository.findAll().stream()
            .filter(p -> p.isAtivo() && p.getTipoPessoa() == TipoPessoa.FISICA)
            .filter(p -> !contaRepository.existsByPessoaId(p.getId()))
            .findFirst()
            .orElseGet(this::criarPessoaAdminPadrao);
    }

    private Pessoa criarPessoaAdminPadrao() {
        Pessoa novaPessoa = Pessoa.criarMinima(
            "Administrador Asmosul",
            "02699584010",
            TipoPessoa.FISICA,
            "63999999999"
        );
        novaPessoa.setEmail("admin@asmosul.org.br");
        return pessoaRepository.save(novaPessoa);
    }
}
