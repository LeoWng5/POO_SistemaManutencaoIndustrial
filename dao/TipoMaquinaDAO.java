package dao;

import classes.ItemPeca;
import classes.Peca;
import classes.TipoMaquina;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Comandos SQL das tabelas tipo_maquina (molde de máquina)
 * e tipo_maquina_peca (a "receita" de peças recomendadas).
 */
public class TipoMaquinaDAO {

    public void inserir(Connection conn, TipoMaquina t) throws SQLException {
        String sql = "INSERT INTO tipo_maquina (nome, fabricante_padrao) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getNome());
            ps.setString(2, t.getFabricantePadrao());
            ps.executeUpdate();
        }
        t.setId(SqlUtil.ultimoId(conn));
        salvarReceita(conn, t);
    }

    public TipoMaquina buscarPorId(Connection conn, int id) throws SQLException {
        String sql = "SELECT nome, fabricante_padrao FROM tipo_maquina WHERE id = ?";
        TipoMaquina tipo;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                tipo = new TipoMaquina(id, rs.getString("nome"), rs.getString("fabricante_padrao"));
            }
        }
        carregarReceita(conn, tipo);
        return tipo;
    }

    public List<TipoMaquina> listar(Connection conn) throws SQLException {
        List<TipoMaquina> lista = new ArrayList<>();
        for (int id : SqlUtil.listarIds(conn, "SELECT id FROM tipo_maquina ORDER BY nome")) {
            lista.add(buscarPorId(conn, id));
        }
        return lista;
    }

    // Atualiza o molde e troca a receita inteira pela nova
    public void atualizar(Connection conn, TipoMaquina t) throws SQLException {
        String sql = "UPDATE tipo_maquina SET nome = ?, fabricante_padrao = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getNome());
            ps.setString(2, t.getFabricantePadrao());
            ps.setInt(3, t.getId());
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM tipo_maquina_peca WHERE tipo_maquina_id = ?")) {
            ps.setInt(1, t.getId());
            ps.executeUpdate();
        }
        salvarReceita(conn, t);
    }

    public boolean excluir(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM tipo_maquina WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private void salvarReceita(Connection conn, TipoMaquina t) throws SQLException {
        String sql = "INSERT INTO tipo_maquina_peca (tipo_maquina_id, peca_id, quantidade) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ItemPeca item : t.getPecasRecomendadas()) {
                ps.setInt(1, t.getId());
                ps.setInt(2, item.getPeca().getId());
                ps.setInt(3, item.getQuantidade());
                ps.executeUpdate();
            }
        }
    }

    private void carregarReceita(Connection conn, TipoMaquina t) throws SQLException {
        String sql = "SELECT p.id, p.nome, p.descricao, p.categoria, p.quantidade_estoque, "
                   + "p.estoque_minimo, p.valor_unitario, tmp.quantidade "
                   + "FROM tipo_maquina_peca tmp JOIN peca p ON p.id = tmp.peca_id "
                   + "WHERE tmp.tipo_maquina_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Peca peca = PecaDAO.mapear(rs);
                    t.adicionarPecaRecomendada(peca, rs.getInt("quantidade"));
                }
            }
        }
    }
}
