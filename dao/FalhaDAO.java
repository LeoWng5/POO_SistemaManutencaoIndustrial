package dao;

import classes.Falha;
import classes.Maquina;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Comandos SQL da tabela falha. */
public class FalhaDAO {

    private final MaquinaDAO maquinaDAO = new MaquinaDAO();

    public void inserir(Connection conn, Falha f) throws SQLException {
        String sql = "INSERT INTO falha (data_hora, descricao, maquina_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getDataHora().toString());
            ps.setString(2, f.getDescricao());
            ps.setInt(3, f.getMaquina().getId());
            ps.executeUpdate();
        }
        f.setId(SqlUtil.ultimoId(conn));
    }

    public Falha buscarPorId(Connection conn, int id) throws SQLException {
        String sql = "SELECT data_hora, descricao, maquina_id FROM falha WHERE id = ?";
        LocalDateTime dataHora;
        String descricao;
        int maquinaId;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                dataHora = LocalDateTime.parse(rs.getString("data_hora"));
                descricao = rs.getString("descricao");
                maquinaId = rs.getInt("maquina_id");
            }
        }
        Maquina maquina = maquinaDAO.buscarPorId(conn, maquinaId);
        return new Falha(id, descricao, maquina, dataHora);
    }

    public List<Falha> listar(Connection conn) throws SQLException {
        List<Falha> lista = new ArrayList<>();
        for (int id : SqlUtil.listarIds(conn, "SELECT id FROM falha ORDER BY id")) {
            lista.add(buscarPorId(conn, id));
        }
        return lista;
    }
}
