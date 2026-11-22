package service;

import classes.*;
import database.ConexaoDB;
import dao.*;
import exception.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class SistemaService {

    private TipoMaquinaDAO tipoMaquinaDAO = new TipoMaquinaDAO();
    private TipoTecnicoDAO tipoTecnicoDAO = new TipoTecnicoDAO();
    private MaquinaDAO maquinaDAO = new MaquinaDAO();
    private TecnicoDAO tecnicoDAO = new TecnicoDAO();
    private PecaDAO pecaDAO = new PecaDAO();
    private FalhaDAO falhaDAO = new FalhaDAO();
    private ManutencaoDAO manutencaoDAO = new ManutencaoDAO();
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    public SistemaService() {
        try {
            ConexaoDB.inicializar();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao iniciar banco de dados: " + e.getMessage());
        }
    }

    // ================= LEITURA =================
    public List<Maquina> getMaquinas() {
        try (Connection c = ConexaoDB.getConexao()) { return maquinaDAO.listar(c); }
        catch (SQLException e) { throw SqlUtil.traduzir(e); }
    }

    public List<Tecnico> getTecnicos() {
        try (Connection c = ConexaoDB.getConexao()) { return tecnicoDAO.listar(c); }
        catch (SQLException e) { throw SqlUtil.traduzir(e); }
    }

    public List<Peca> getPecas() {
        try (Connection c = ConexaoDB.getConexao()) { return pecaDAO.listar(c); }
        catch (SQLException e) { throw SqlUtil.traduzir(e); }
    }

    public List<Manutencao> getManutencoes() {
        try (Connection c = ConexaoDB.getConexao()) { return manutencaoDAO.listar(c); }
        catch (SQLException e) { throw SqlUtil.traduzir(e); }
    }

    public List<TipoMaquina> getTiposMaquina() {
        try (Connection c = ConexaoDB.getConexao()) { return tipoMaquinaDAO.listar(c); }
        catch (SQLException e) { throw SqlUtil.traduzir(e); }
    }

    public List<TipoTecnico> getTiposTecnico() {
        try (Connection c = ConexaoDB.getConexao()) { return tipoTecnicoDAO.listar(c); }
        catch (SQLException e) { throw SqlUtil.traduzir(e); }
    }

    public List<Manutencao> getManutencoesPorPeriodo(LocalDate inicio, LocalDate fim) {
        try (Connection c = ConexaoDB.getConexao()) {
            return manutencaoDAO.listarPorPeriodo(c, inicio, fim);
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public List<Manutencao> getManutencoesPorMaquina(int idMaquina) {
        try (Connection c = ConexaoDB.getConexao()) {
            return manutencaoDAO.listar(c).stream()
                    .filter(m -> m.getMaquina() != null && m.getMaquina().getId() == idMaquina)
                    .toList();
        } catch (SQLException e) { throw SqlUtil.traduzir(e); }
    }

    /** Busca uma manutenção pelo ID (usada pela tela para saber se precisa perguntar a nova vida útil). */
    public Manutencao buscarManutencao(int idManutencao) throws ManutencaoInexistenteException {
        try (Connection c = ConexaoDB.getConexao()) {
            Manutencao m = manutencaoDAO.buscarPorId(c, idManutencao);
            if (m == null) throw new ManutencaoInexistenteException("Manutenção não encontrada.");
            return m;
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    // ================= CADASTROS =================
    public void cadastrarTipoMaquina(TipoMaquina t) {
        try (Connection c = ConexaoDB.getConexao()) { tipoMaquinaDAO.inserir(c, t); }
        catch (SQLException e) { throw SqlUtil.traduzir(e); }
    }

    public void cadastrarTipoTecnico(TipoTecnico t) {
        try (Connection c = ConexaoDB.getConexao()) { tipoTecnicoDAO.inserir(c, t); }
        catch (SQLException e) { throw SqlUtil.traduzir(e); }
    }

    public void cadastrarMaquina(Maquina m) {
        try (Connection c = ConexaoDB.getConexao()) { maquinaDAO.inserir(c, m); }
        catch (SQLException e) { throw SqlUtil.traduzir(e); }
    }

    public void cadastrarTecnico(Tecnico t) {
        try (Connection c = ConexaoDB.getConexao()) { tecnicoDAO.inserir(c, t); }
        catch (SQLException e) { throw SqlUtil.traduzir(e); }
    }

    public void cadastrarPeca(Peca p) {
        try (Connection c = ConexaoDB.getConexao()) { pecaDAO.inserir(c, p); }
        catch (SQLException e) { throw SqlUtil.traduzir(e); }
    }

    // ================= EXCLUSÕES (ADMIN) =================

    public boolean excluirManutencao(int id) {
        try (Connection c = ConexaoDB.getConexao()) {
            // Devolve o técnico à disponibilidade antes de apagar
            Manutencao m = manutencaoDAO.buscarPorId(c, id);
            if (m == null) return false;

            if (m.getTecnico() != null && !m.getStatus().equals("CONCLUIDA")) {
                tecnicoDAO.atualizarDisponibilidade(c, m.getTecnico().getId(), true);
            }

            return manutencaoDAO.excluir(c, id);
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public boolean excluirTecnico(int id) {
        try (Connection c = ConexaoDB.getConexao()) {
            return tecnicoDAO.excluir(c, id);
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public boolean excluirPeca(int id) {
        try (Connection c = ConexaoDB.getConexao()) {
            return pecaDAO.excluir(c, id);
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public boolean excluirTipoMaquina(int id) {
        try (Connection c = ConexaoDB.getConexao()) {
            return tipoMaquinaDAO.excluir(c, id);
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public boolean excluirMaquina(int id) {
        try (Connection c = ConexaoDB.getConexao()) { return maquinaDAO.excluir(c, id); }
        catch (SQLException e) { throw SqlUtil.traduzir(e); }
    }

    // ================= OPERAÇÕES =================
    public void registrarFalha(String descricao, int idMaquina) throws MaquinaInexistenteException {
        try (Connection c = ConexaoDB.getConexao()) {
            Maquina m = maquinaDAO.buscarPorId(c, idMaquina);
            if (m == null) throw new MaquinaInexistenteException("Máquina com ID " + idMaquina + " não encontrada.");

            Falha f = new Falha(0, descricao, m, RelogioSistema.agora());
            falhaDAO.inserir(c, f);
            maquinaDAO.atualizarStatus(c, idMaquina, "QUEBRADA");
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public int abrirCorretiva(String desc, int idMaquina, String causa, String gravidade)
            throws MaquinaInexistenteException, MaquinaInativaException {
        try (Connection c = ConexaoDB.getConexao()) {
            Maquina m = maquinaDAO.buscarPorId(c, idMaquina);
            if (m == null) throw new MaquinaInexistenteException("Máquina não encontrada.");
            if (m.getStatus().equals("INATIVA")) throw new MaquinaInativaException("Máquina inativa não pode sofrer manutenção.");

            ManutencaoCorretiva mc = new ManutencaoCorretiva(0, desc, m, causa, gravidade);
            manutencaoDAO.inserir(c, mc);
            return mc.getId();
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public int abrirPreventiva(String desc, int idMaquina, int dias)
            throws MaquinaInexistenteException, MaquinaInativaException {
        try (Connection c = ConexaoDB.getConexao()) {
            Maquina m = maquinaDAO.buscarPorId(c, idMaquina);
            if (m == null) throw new MaquinaInexistenteException("Máquina não encontrada.");
            if (m.getStatus().equals("INATIVA")) throw new MaquinaInativaException("Máquina inativa não pode sofrer manutenção.");

            ManutencaoPreventiva mp = new ManutencaoPreventiva(0, desc, m, dias);
            manutencaoDAO.inserir(c, mp);
            return mp.getId();
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public boolean temPreventivaAberta(int idMaquina) {
        return getManutencoes().stream()
            .anyMatch(m -> m.getTipo().equals("PREVENTIVA")
                        && !m.getStatus().equals("CONCLUIDA")
                        && m.getMaquina() != null
                        && m.getMaquina().getId() == idMaquina);
    }

    public void atribuirTecnico(int idManutencao, int idTecnico)
            throws TecnicoIndisponivelException, ManutencaoInexistenteException, ManutencaoFinalizadaException {
        try (Connection c = ConexaoDB.getConexao()) {
            Manutencao m = manutencaoDAO.buscarPorId(c, idManutencao);
            if (m == null) throw new ManutencaoInexistenteException("Manutenção não encontrada.");
            if (m.getStatus().equals("CONCLUIDA")) throw new ManutencaoFinalizadaException("Manutenção já concluída.");

            Tecnico t = tecnicoDAO.buscarPorId(c, idTecnico);
            if (t == null) throw new TecnicoIndisponivelException("Técnico não encontrado.");
            if (!t.isDisponivel()) throw new TecnicoIndisponivelException("Técnico já está ocupado.");

            t.setDisponivel(false);
            tecnicoDAO.atualizarDisponibilidade(c, idTecnico, false);
            m.setTecnico(t);
            m.setStatus("EM_EXECUCAO");
            manutencaoDAO.atualizar(c, m);
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public void adicionarPecaNaManutencao(int idManutencao, int idPeca, int qtd)
            throws EstoqueInsuficienteException, ManutencaoInexistenteException, ManutencaoFinalizadaException {
        try (Connection c = ConexaoDB.getConexao()) {
            Manutencao m = manutencaoDAO.buscarPorId(c, idManutencao);
            if (m == null) throw new ManutencaoInexistenteException("Manutenção não encontrada.");
            if (m.getStatus().equals("CONCLUIDA")) throw new ManutencaoFinalizadaException("Manutenção já concluída.");

            manutencaoDAO.registrarPeca(c, idManutencao, idPeca, qtd);
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    /** Conclui a manutenção sem renovar a vida útil (atalho para a versão com anos = 0). */
    public double concluirManutencao(int idManutencao)
            throws ManutencaoInexistenteException, ManutencaoFinalizadaException {
        return concluirManutencao(idManutencao, 0);
    }

    /**
     * Conclui a manutenção. Se for um tipo que renova a vida útil (preventiva),
     * a máquina volta a 100% de saúde e passa a depreciar ao longo da nova estimativa (em anos).
     */
    public double concluirManutencao(int idManutencao, int novaVidaUtilAnos)
            throws ManutencaoInexistenteException, ManutencaoFinalizadaException {
        try (Connection c = ConexaoDB.getConexao()) {
            Manutencao m = manutencaoDAO.buscarPorId(c, idManutencao);
            if (m == null) throw new ManutencaoInexistenteException("Manutenção não encontrada.");
            if (m.getStatus().equals("CONCLUIDA")) throw new ManutencaoFinalizadaException("Manutenção já concluída.");

            double custoFinal = m.calcularCusto();
            m.setStatus("CONCLUIDA");
            m.setCusto(custoFinal);
            manutencaoDAO.atualizar(c, m);

            if (m.getTecnico() != null) {
                tecnicoDAO.atualizarDisponibilidade(c, m.getTecnico().getId(), true);
            }

            // Polimorfismo: só a preventiva renova a vida útil da máquina
            if (m.recuperaVidaUtil() && novaVidaUtilAnos > 0) {
                Maquina maq = m.getMaquina();
                maq.renovarVidaUtil(novaVidaUtilAnos);
                maquinaDAO.atualizar(c, maq);
            }
            maquinaDAO.atualizarStatus(c, m.getMaquina().getId(), "ATIVA");

            return custoFinal;
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }

    public int getTotalMaquinasPorStatus(String status) {
        try (Connection c = ConexaoDB.getConexao()) {
            return maquinaDAO.contarPorStatus(c, status);
        } catch (SQLException e) { throw SqlUtil.traduzir(e); }
    }

    public int getTotalTecnicosPorDisponibilidade(boolean disponivel) {
        try (Connection c = ConexaoDB.getConexao()) {
            return tecnicoDAO.contarPorDisponibilidade(c, disponivel);
        } catch (SQLException e) { throw SqlUtil.traduzir(e); }
    }

    public boolean realizarLogin(String usuario, String senha) {
        try (Connection c = ConexaoDB.getConexao()) {
            String perfil = usuarioDAO.realizarLogin(c, usuario, senha);
            if (perfil != null) {
                Sessao.getInstance().logar(usuario, perfil);
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw SqlUtil.traduzir(e);
        }
    }
}