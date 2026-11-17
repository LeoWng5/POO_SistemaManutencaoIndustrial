package service;

import classes.*;
import dao.*;
import database.ConexaoDB;
import exception.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Regras de negócio do sistema. Agora os dados ficam no banco SQLite:
 * cada método abre uma conexão, usa os DAOs e fecha a conexão.
 * Operações que mexem em mais de uma tabela rodam numa transação
 * (ou tudo é gravado, ou nada é).
 */
public class SistemaService {

    private final TipoMaquinaDAO tipoMaquinaDAO = new TipoMaquinaDAO();
    private final TipoTecnicoDAO tipoTecnicoDAO = new TipoTecnicoDAO();
    private final MaquinaDAO maquinaDAO = new MaquinaDAO();
    private final TecnicoDAO tecnicoDAO = new TecnicoDAO();
    private final PecaDAO pecaDAO = new PecaDAO();
    private final FalhaDAO falhaDAO = new FalhaDAO();
    private final ManutencaoDAO manutencaoDAO = new ManutencaoDAO();

    // Cria o banco e as tabelas na primeira execução
    public SistemaService() {
        try {
            ConexaoDB.inicializar();
        } catch (SQLException e) {
            throw new BancoDeDadosException("Não foi possível iniciar o banco de dados: " + e.getMessage(), e);
        }
    }

    private void desfazer(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignorada) {
            // nada a fazer: a conexão será fechada em seguida
        }
    }

    // --- MÉTODOS DE CADASTRO ---
    public void cadastrarTipoMaquina(TipoMaquina t) {
        try (Connection conn = ConexaoDB.getConexao()) {
            conn.setAutoCommit(false);
            try {
                tipoMaquinaDAO.inserir(conn, t);    // grava o molde e a receita de peças
                conn.commit();
            } catch (SQLException e) {
                desfazer(conn);
                throw e;
            }
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public void cadastrarTipoTecnico(TipoTecnico t) {
        try (Connection conn = ConexaoDB.getConexao()) {
            tipoTecnicoDAO.inserir(conn, t);
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public void cadastrarMaquina(Maquina m) {
        try (Connection conn = ConexaoDB.getConexao()) {
            maquinaDAO.inserir(conn, m);
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public void cadastrarTecnico(Tecnico t) {
        try (Connection conn = ConexaoDB.getConexao()) {
            tecnicoDAO.inserir(conn, t);
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public void cadastrarPeca(Peca p) {
        try (Connection conn = ConexaoDB.getConexao()) {
            pecaDAO.inserir(conn, p);
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    // --- MÉTODO DE EXCLUSÃO ---
    // Devolve false se não existir. Se houver manutenções/falhas ligadas, o banco recusa
    // e o método lança BancoDeDadosException com uma mensagem amigável.
    public boolean excluirMaquina(int id) {
        try (Connection conn = ConexaoDB.getConexao()) {
            return maquinaDAO.excluir(conn, id);
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    // --- MÉTODOS DE BUSCA ---
    public Maquina buscarMaquina(int id) throws MaquinaInexistenteException {
        try (Connection conn = ConexaoDB.getConexao()) {
            Maquina m = maquinaDAO.buscarPorId(conn, id);
            if (m == null) {
                throw new MaquinaInexistenteException("Máquina com ID " + id + " não encontrada.");
            }
            return m;
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public Tecnico buscarTecnico(int id) {
        try (Connection conn = ConexaoDB.getConexao()) {
            return tecnicoDAO.buscarPorId(conn, id);
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public Peca buscarPeca(int id) {
        try (Connection conn = ConexaoDB.getConexao()) {
            return pecaDAO.buscarPorId(conn, id);
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    // --- REGRAS DE NEGÓCIO ---
    public void registrarFalha(String descricao, int idMaquina) throws MaquinaInexistenteException {
        try (Connection conn = ConexaoDB.getConexao()) {
            conn.setAutoCommit(false);
            try {
                Maquina m = maquinaDAO.buscarPorId(conn, idMaquina);
                if (m == null) {
                    throw new MaquinaInexistenteException("Máquina com ID " + idMaquina + " não encontrada.");
                }
                falhaDAO.inserir(conn, new Falha(0, descricao, m));
                maquinaDAO.atualizarStatus(conn, idMaquina, "QUEBRADA");
                conn.commit();
            } catch (Exception e) {
                desfazer(conn);
                throw e;
            }
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    // Devolve o ID da manutenção criada
    public int abrirCorretiva(String descricao, int idMaquina, String causa, String gravidade)
            throws MaquinaInexistenteException, MaquinaInativaException {
        try (Connection conn = ConexaoDB.getConexao()) {
            Maquina m = maquinaDAO.buscarPorId(conn, idMaquina);
            if (m == null) {
                throw new MaquinaInexistenteException("Máquina com ID " + idMaquina + " não encontrada.");
            }
            if (m.getStatus().equalsIgnoreCase("INATIVA")) {
                throw new MaquinaInativaException("Não é possível manutenção em máquina inativa.");
            }
            Manutencao manutencao = new ManutencaoCorretiva(0, descricao, m, causa, gravidade);
            manutencaoDAO.inserir(conn, manutencao);
            return manutencao.getId();
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    // Devolve o ID da manutenção criada
    public int abrirPreventiva(String descricao, int idMaquina, int periodicidadeDias)
            throws MaquinaInexistenteException, MaquinaInativaException {
        try (Connection conn = ConexaoDB.getConexao()) {
            Maquina m = maquinaDAO.buscarPorId(conn, idMaquina);
            if (m == null) {
                throw new MaquinaInexistenteException("Máquina com ID " + idMaquina + " não encontrada.");
            }
            if (m.getStatus().equalsIgnoreCase("INATIVA")) {
                throw new MaquinaInativaException("Não é possível manutenção em máquina inativa.");
            }
            Manutencao manutencao = new ManutencaoPreventiva(0, descricao, m, periodicidadeDias);
            manutencaoDAO.inserir(conn, manutencao);
            return manutencao.getId();
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public void atribuirTecnico(int idManutencao, int idTecnico)
            throws TecnicoIndisponivelException, ManutencaoInexistenteException, ManutencaoFinalizadaException {
        try (Connection conn = ConexaoDB.getConexao()) {
            conn.setAutoCommit(false);
            try {
                Manutencao m = manutencaoDAO.buscarPorId(conn, idManutencao);
                if (m == null) {
                    throw new ManutencaoInexistenteException("Manutenção com ID " + idManutencao + " não encontrada.");
                }
                if (m.getStatus().equals("CONCLUIDA")) {
                    throw new ManutencaoFinalizadaException("Essa manutenção já foi finalizada.");
                }
                Tecnico t = tecnicoDAO.buscarPorId(conn, idTecnico);
                if (t == null || !t.isDisponivel()) {
                    throw new TecnicoIndisponivelException("Técnico indisponível ou não encontrado.");
                }

                // Se já havia outro técnico nesta manutenção, ele volta a ficar disponível
                if (m.getTecnico() != null) {
                    tecnicoDAO.atualizarDisponibilidade(conn, m.getTecnico().getId(), true);
                }

                t.ocupar();
                m.setTecnico(t);
                m.setStatus("EM_EXECUCAO");
                tecnicoDAO.atualizarDisponibilidade(conn, t.getId(), false);
                manutencaoDAO.atualizar(conn, m);
                conn.commit();
            } catch (Exception e) {
                desfazer(conn);
                throw e;
            }
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public void adicionarPecaNaManutencao(int idManutencao, int idPeca, int quantidade)
            throws EstoqueInsuficienteException, ManutencaoInexistenteException,
                   ManutencaoFinalizadaException, PecaInexistenteException {
        try (Connection conn = ConexaoDB.getConexao()) {
            conn.setAutoCommit(false);
            try {
                Manutencao m = manutencaoDAO.buscarPorId(conn, idManutencao);
                if (m == null) {
                    throw new ManutencaoInexistenteException("Manutenção com ID " + idManutencao + " não encontrada.");
                }
                if (m.getStatus().equals("CONCLUIDA")) {
                    throw new ManutencaoFinalizadaException("Manutenção já concluída.");
                }
                Peca peca = pecaDAO.buscarPorId(conn, idPeca);
                if (peca == null) {
                    throw new PecaInexistenteException("Peça com ID " + idPeca + " não encontrada no estoque.");
                }

                // Valida a quantidade (mensagem amigável) e depois grava no banco
                peca.baixarEstoque(quantidade);
                manutencaoDAO.registrarPeca(conn, idManutencao, idPeca, quantidade);
                conn.commit();
            } catch (Exception e) {
                desfazer(conn);
                throw e;
            }
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    // Devolve o custo total calculado
    public double concluirManutencao(int idManutencao)
            throws ManutencaoInexistenteException, ManutencaoFinalizadaException {
        try (Connection conn = ConexaoDB.getConexao()) {
            conn.setAutoCommit(false);
            try {
                Manutencao m = manutencaoDAO.buscarPorId(conn, idManutencao);
                if (m == null) {
                    throw new ManutencaoInexistenteException("Manutenção com ID " + idManutencao + " não encontrada.");
                }
                if (m.getStatus().equals("CONCLUIDA")) {
                    throw new ManutencaoFinalizadaException("Essa manutenção já foi finalizada.");
                }

                m.concluir();   // muda o status e calcula o custo (mão de obra + peças)
                manutencaoDAO.atualizar(conn, m);
                if (m.getTecnico() != null) {
                    tecnicoDAO.atualizarDisponibilidade(conn, m.getTecnico().getId(), true);
                }
                maquinaDAO.atualizarStatus(conn, m.getMaquina().getId(), "ATIVA");
                conn.commit();
                return m.getCusto();
            } catch (Exception e) {
                desfazer(conn);
                throw e;
            }
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    // --- LISTAGENS (lidas direto do banco, usadas pelas tabelas da tela) ---
    public List<TipoMaquina> getTiposMaquina() {
        try (Connection conn = ConexaoDB.getConexao()) {
            return tipoMaquinaDAO.listar(conn);
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public List<TipoTecnico> getTiposTecnico() {
        try (Connection conn = ConexaoDB.getConexao()) {
            return tipoTecnicoDAO.listar(conn);
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public List<Maquina> getMaquinas() {
        try (Connection conn = ConexaoDB.getConexao()) {
            return maquinaDAO.listar(conn);
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public List<Tecnico> getTecnicos() {
        try (Connection conn = ConexaoDB.getConexao()) {
            return tecnicoDAO.listar(conn);
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public List<Peca> getPecas() {
        try (Connection conn = ConexaoDB.getConexao()) {
            return pecaDAO.listar(conn);
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public List<Falha> getFalhas() {
        try (Connection conn = ConexaoDB.getConexao()) {
            return falhaDAO.listar(conn);
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public List<Manutencao> getManutencoes() {
        try (Connection conn = ConexaoDB.getConexao()) {
            return manutencaoDAO.listar(conn);
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }
}
