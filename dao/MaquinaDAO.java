package dao;

import classes.Maquina;
import classes.TipoMaquina;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Comandos SQL da tabela maquina. */
public class MaquinaDAO {

    private final TipoMaquinaDAO tipoDAO = new TipoMaquinaDAO();

    public void inserir(Connection conn, Maquina m) throws SQLException {
        String sql = "INSERT INTO maquina (numero_serie, status, data_aquisicao, tipo_maquina_id) "
                   + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getNumeroSerie());
            ps.setString(2, m.getStatus());
            ps.setString(3, m.getDataAquisicao().toString());   // yyyy-MM-dd
            ps.setInt(4, m.getTipo().getId());
            ps.executeUpdate();
        }
        m.setId(SqlUtil.ultimoId(conn));
    }

    public Maquina buscarPorId(Connection conn, int id) throws SQLException {
        String sql = "SELECT numero_serie, status, data_aquisicao, tipo_maquina_id FROM maquina WHERE id = ?";
        String serie;
        String status;
        LocalDate data;
        int tipoId;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                serie = rs.getString("numero_serie");
                status = rs.getString("status");
                data = LocalDate.parse(rs.getString("data_aquisicao"));
                tipoId = rs.getInt("tipo_maquina_id");
            }
        }
        TipoMaquina tipo = tipoDAO.buscarPorId(conn, tipoId);
        return new Maquina(id, serie, status, data, tipo);
    }

    public List<Maquina> listar(Connection conn) throws SQLException {
        List<Maquina> lista = new ArrayList<>();
        for (int id : SqlUtil.listarIds(conn, "SELECT id FROM maquina ORDER BY id")) {
            lista.add(buscarPorId(conn, id));
        }
        return lista;
    }

    public void atualizar(Connection conn, Maquina m) throws SQLException {
        String sql = "UPDATE maquina SET numero_serie = ?, status = ?, data_aquisicao = ?, "
                   + "tipo_maquina_id = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getNumeroSerie());
            ps.setString(2, m.getStatus());
            ps.setString(3, m.getDataAquisicao().toString());
            ps.setInt(4, m.getTipo().getId());
            ps.setInt(5, m.getId());
            ps.executeUpdate();
        }
    }

    public void atualizarStatus(Connection conn, int id, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE maquina SET status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public boolean excluir(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM maquina WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
