package dao;

import classes.Tecnico;
import classes.TipoTecnico;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TecnicoDAO {
    private final TipoTecnicoDAO tipoDAO = new TipoTecnicoDAO();

    public void inserir(Connection conn, Tecnico t) throws SQLException {
        String sql = "INSERT INTO tecnico (nome, telefone, url_foto, disponivel, tipo_tecnico_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getNome());
            ps.setString(2, t.getTelefone());
            ps.setString(3, t.getFoto()); 
            ps.setInt(4, t.isDisponivel() ? 1 : 0);
            ps.setInt(5, t.getTipo().getId());
            ps.executeUpdate();
        }
        t.setId(SqlUtil.ultimoId(conn));
    }

    public Tecnico buscarPorId(Connection conn, int id) throws SQLException {
        String sql = "SELECT nome, telefone, url_foto, disponivel, tipo_tecnico_id FROM tecnico WHERE id = ?";
        String nome, telefone, foto;
        boolean disponivel;
        int tipoId;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                nome = rs.getString("nome");
                telefone = rs.getString("telefone");
                foto = rs.getString("url_foto"); // <-- AQUI
                disponivel = rs.getInt("disponivel") == 1;
                tipoId = rs.getInt("tipo_tecnico_id");
            }
        }
        TipoTecnico tipo = tipoDAO.buscarPorId(conn, tipoId);
        return new Tecnico(id, nome, telefone, disponivel, tipo, foto);
    }

    public List<Tecnico> listar(Connection conn) throws SQLException {
        List<Tecnico> lista = new ArrayList<>();
        for (int id : SqlUtil.listarIds(conn, "SELECT id FROM tecnico ORDER BY id")) {
            lista.add(buscarPorId(conn, id));
        }
        return lista;
    }

    public void atualizar(Connection conn, Tecnico t) throws SQLException {
        String sql = "UPDATE tecnico SET nome = ?, telefone = ?, url_foto = ?, disponivel = ?, tipo_tecnico_id = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getNome());
            ps.setString(2, t.getTelefone());
            ps.setString(3, t.getFoto());
            ps.setInt(4, t.isDisponivel() ? 1 : 0);
            ps.setInt(5, t.getTipo().getId());
            ps.setInt(6, t.getId());
            ps.executeUpdate();
        }
    }

    public void atualizarDisponibilidade(Connection conn, int id, boolean disponivel) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE tecnico SET disponivel = ? WHERE id = ?")) {
            ps.setInt(1, disponivel ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public boolean excluir(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM tecnico WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int contarPorDisponibilidade(Connection conn, boolean disponivel) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tecnico WHERE disponivel = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, disponivel ? 1 : 0);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}