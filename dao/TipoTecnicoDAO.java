package dao;

import classes.TipoTecnico;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TipoTecnicoDAO {

    private TipoTecnico mapear(ResultSet rs) throws SQLException {
        return new TipoTecnico(
            rs.getInt("id"),
            rs.getString("especialidade")
        );
    }

    public void inserir(Connection conn, TipoTecnico t) throws SQLException {
        String sql = "INSERT INTO tipo_tecnico (especialidade) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getEspecialidade());
            ps.executeUpdate();
        }
        t.setId(SqlUtil.ultimoId(conn));
    }

    public TipoTecnico buscarPorId(Connection conn, int id) throws SQLException {
        String sql = "SELECT id, especialidade FROM tipo_tecnico WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<TipoTecnico> listar(Connection conn) throws SQLException {
        String sql = "SELECT id, especialidade FROM tipo_tecnico ORDER BY especialidade";
        List<TipoTecnico> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public void atualizar(Connection conn, TipoTecnico t) throws SQLException {
        String sql = "UPDATE tipo_tecnico SET especialidade = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getEspecialidade());
            ps.setInt(2, t.getId());
            ps.executeUpdate();
        }
    }

    public boolean excluir(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM tipo_tecnico WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}