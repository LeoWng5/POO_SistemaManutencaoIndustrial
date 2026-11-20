package dao;

import classes.Peca;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Comandos SQL da tabela peca. */
public class PecaDAO {

    // Transforma a linha atual do ResultSet num objeto Peca
    static Peca mapear(ResultSet rs) throws SQLException {
        return new Peca(
            rs.getInt("id"),
            rs.getString("nome"),
            rs.getString("descricao"),
            rs.getString("categoria"),
            rs.getInt("quantidade_estoque"),
            rs.getInt("estoque_minimo"),
            rs.getDouble("valor_unitario"));
    }

    public void inserir(Connection conn, Peca p) throws SQLException {
        String sql = "INSERT INTO peca (nome, descricao, categoria, quantidade_estoque, "
                   + "estoque_minimo, valor_unitario) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNome());
            ps.setString(2, p.getDescricao());
            ps.setString(3, p.getCategoria());
            ps.setInt(4, p.getQuantidadeEstoque());
            ps.setInt(5, p.getEstoqueMinimo());
            ps.setDouble(6, p.getValorUnitario());
            ps.executeUpdate();
        }
        p.setId(SqlUtil.ultimoId(conn));
    }

    public Peca buscarPorId(Connection conn, int id) throws SQLException {
        String sql = "SELECT id, nome, descricao, categoria, quantidade_estoque, "
                   + "estoque_minimo, valor_unitario FROM peca WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Peca buscarPorNome(Connection conn, String nome) throws SQLException {
        String sql = "SELECT id, nome, descricao, categoria, quantidade_estoque, "
                   + "estoque_minimo, valor_unitario FROM peca WHERE nome = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<Peca> listar(Connection conn) throws SQLException {
        String sql = "SELECT id, nome, descricao, categoria, quantidade_estoque, "
                   + "estoque_minimo, valor_unitario FROM peca ORDER BY id";
        List<Peca> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public void atualizar(Connection conn, Peca p) throws SQLException {
        String sql = "UPDATE peca SET nome = ?, descricao = ?, categoria = ?, "
                   + "quantidade_estoque = ?, estoque_minimo = ?, valor_unitario = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNome());
            ps.setString(2, p.getDescricao());
            ps.setString(3, p.getCategoria());
            ps.setInt(4, p.getQuantidadeEstoque());
            ps.setInt(5, p.getEstoqueMinimo());
            ps.setDouble(6, p.getValorUnitario());
            ps.setInt(7, p.getId());
            ps.executeUpdate();
        }
    }

    public boolean excluir(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM peca WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
