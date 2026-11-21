package dao;

import classes.Maquina;
import classes.Manutencao;
import classes.ManutencaoCorretiva;
import classes.ManutencaoPreventiva;
import classes.Peca;
import classes.Tecnico;
import exception.EstoqueInsuficienteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Comandos SQL das tabelas manutencao e manutencao_peca.
 * Corretiva e preventiva ficam na mesma tabela, diferenciadas pela coluna "tipo".
 */
public class ManutencaoDAO {

    private final MaquinaDAO maquinaDAO = new MaquinaDAO();
    private final TecnicoDAO tecnicoDAO = new TecnicoDAO();

    public void inserir(Connection conn, Manutencao m) throws SQLException {
        String sql = "INSERT INTO manutencao (data, status, custo, tecnico_id, maquina_id, tipo, descricao, "
                   + "causa_falha, gravidade, periodicidade_dias, proxima_data) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Campos que só existem em uma das subclasses ficam NULL na outra
        String causa = null;
        String gravidade = null;
        Integer periodicidade = null;
        String proxima = null;
        if (m instanceof ManutencaoCorretiva) {
            ManutencaoCorretiva c = (ManutencaoCorretiva) m;
            causa = c.getCausaFalha();
            gravidade = c.getGravidade();
        } else if (m instanceof ManutencaoPreventiva) {
            ManutencaoPreventiva p = (ManutencaoPreventiva) m;
            periodicidade = p.getPeriodicidadeDias();
            proxima = p.getProximaData() == null ? null : p.getProximaData().toString();
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getData().toString());
            ps.setString(2, m.getStatus());
            ps.setDouble(3, m.getCusto());
            if (m.getTecnico() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, m.getTecnico().getId());
            }
            ps.setInt(5, m.getMaquina().getId());
            ps.setString(6, m.getTipo());
            ps.setString(7, m.getDescricao());
            ps.setString(8, causa);
            ps.setString(9, gravidade);
            if (periodicidade == null) {
                ps.setNull(10, Types.INTEGER);
            } else {
                ps.setInt(10, periodicidade);
            }
            ps.setString(11, proxima);
            ps.executeUpdate();
        }
        m.setId(SqlUtil.ultimoId(conn));
    }

    public Manutencao buscarPorId(Connection conn, int id) throws SQLException {
        String sql = "SELECT data, status, custo, tecnico_id, maquina_id, tipo, descricao, "
                   + "causa_falha, gravidade, periodicidade_dias, proxima_data "
                   + "FROM manutencao WHERE id = ?";
        String data, status, tipo, descricao, causa, gravidade, proxima;
        double custo;
        int tecnicoId, maquinaId, periodicidade;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                data = rs.getString("data");
                status = rs.getString("status");
                custo = rs.getDouble("custo");
                tecnicoId = rs.getInt("tecnico_id");        // 0 quando está NULL (técnico ainda não atribuído)
                maquinaId = rs.getInt("maquina_id");
                tipo = rs.getString("tipo");
                descricao = rs.getString("descricao");
                causa = rs.getString("causa_falha");
                gravidade = rs.getString("gravidade");
                periodicidade = rs.getInt("periodicidade_dias");
                proxima = rs.getString("proxima_data");
            }
        }

        Maquina maquina = maquinaDAO.buscarPorId(conn, maquinaId);

        Manutencao m;
        if ("PREVENTIVA".equals(tipo)) {
            ManutencaoPreventiva p = new ManutencaoPreventiva(id, descricao, maquina, periodicidade);
            p.setProximaData(proxima == null ? null : LocalDate.parse(proxima));
            m = p;
        } else {
            m = new ManutencaoCorretiva(id, descricao, maquina, causa, gravidade);
        }
        m.setData(LocalDate.parse(data));
        m.setStatus(status);
        m.setCusto(custo);
        if (tecnicoId != 0) {
            Tecnico t = tecnicoDAO.buscarPorId(conn, tecnicoId);
            m.setTecnico(t);
        }
        carregarItens(conn, m);
        return m;
    }

    public List<Manutencao> listar(Connection conn) throws SQLException {
        List<Manutencao> lista = new ArrayList<>();
        for (int id : SqlUtil.listarIds(conn, "SELECT id FROM manutencao ORDER BY id")) {
            lista.add(buscarPorId(conn, id));
        }
        return lista;
    }

    // Atualiza o que muda durante a vida da manutenção
    public void atualizar(Connection conn, Manutencao m) throws SQLException {
        String sql = "UPDATE manutencao SET status = ?, custo = ?, tecnico_id = ?, descricao = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getStatus());
            ps.setDouble(2, m.getCusto());
            if (m.getTecnico() == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, m.getTecnico().getId());
            }
            ps.setString(4, m.getDescricao());
            ps.setInt(5, m.getId());
            ps.executeUpdate();
        }
    }

    public boolean excluir(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM manutencao WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Usa uma peça na manutenção: dá baixa no estoque E registra o item.
     * Rode dentro de uma transação, para as duas coisas acontecerem juntas ou nenhuma.
     */
    public void registrarPeca(Connection conn, int manutencaoId, int pecaId, int quantidade)
            throws SQLException, EstoqueInsuficienteException {
        // Só baixa se houver estoque suficiente (a condição fica no próprio SQL)
        String baixa = "UPDATE peca SET quantidade_estoque = quantidade_estoque - ? "
                     + "WHERE id = ? AND quantidade_estoque >= ?";
        try (PreparedStatement ps = conn.prepareStatement(baixa)) {
            ps.setInt(1, quantidade);
            ps.setInt(2, pecaId);
            ps.setInt(3, quantidade);
            if (ps.executeUpdate() == 0) {
                throw new EstoqueInsuficienteException("Estoque insuficiente para esta peça.");
            }
        }
        // Se a peça já foi usada nesta manutenção, soma a quantidade
        String upsert = "INSERT INTO manutencao_peca (manutencao_id, peca_id, quantidade) VALUES (?, ?, ?) "
                      + "ON CONFLICT(manutencao_id, peca_id) DO UPDATE SET quantidade = quantidade + excluded.quantidade";
        try (PreparedStatement ps = conn.prepareStatement(upsert)) {
            ps.setInt(1, manutencaoId);
            ps.setInt(2, pecaId);
            ps.setInt(3, quantidade);
            ps.executeUpdate();
        }
    }

    private void carregarItens(Connection conn, Manutencao m) throws SQLException {
        String sql = "SELECT p.id, p.nome, p.descricao, p.categoria, p.quantidade_estoque, "
                   + "p.estoque_minimo, p.valor_unitario, mp.quantidade "
                   + "FROM manutencao_peca mp JOIN peca p ON p.id = mp.peca_id "
                   + "WHERE mp.manutencao_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, m.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Peca peca = PecaDAO.mapear(rs);
                    m.carregarItem(peca, rs.getInt("quantidade"));
                }
            }
        }
    }

    public List<Manutencao> listarPorPeriodo(Connection conn, java.time.LocalDate inicio, java.time.LocalDate fim) throws SQLException {
        List<Manutencao> lista = new ArrayList<>();
        String sql = "SELECT id FROM manutencao WHERE data >= ? AND data <= ? ORDER BY data";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, inicio.toString());
            ps.setString(2, fim.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(buscarPorId(conn, rs.getInt(1)));
                }
            }
        }
        return lista;
    }
}
