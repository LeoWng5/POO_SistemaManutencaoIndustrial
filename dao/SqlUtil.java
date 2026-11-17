package dao;

import exception.BancoDeDadosException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Funções auxiliares usadas por todos os DAOs. */
public final class SqlUtil {

    private SqlUtil() {
    }

    /** Devolve o ID gerado pelo banco no último INSERT feito nesta conexão. */
    public static int ultimoId(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    /** Executa um SELECT que devolve só uma coluna de IDs e junta tudo numa lista. */
    public static List<Integer> listarIds(Connection conn, String sql) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ids.add(rs.getInt(1));
            }
        }
        return ids;
    }

    /** Transforma o erro técnico do SQLite numa mensagem que o usuário entende. */
    public static BancoDeDadosException traduzir(SQLException e) {
        String msg = e.getMessage() == null ? "" : e.getMessage().toUpperCase();
        if (msg.contains("FOREIGN KEY")) {
            return new BancoDeDadosException(
                "Operação não permitida: este registro está ligado a outros "
                + "(por exemplo, máquina com manutenções ou falhas, ou tipo em uso).", e);
        }
        if (msg.contains("UNIQUE")) {
            return new BancoDeDadosException(
                "Já existe um registro com este valor "
                + "(nome, número de série ou cargo repetido).", e);
        }
        if (msg.contains("CHECK")) {
            return new BancoDeDadosException("Valor inválido para um dos campos.", e);
        }
        return new BancoDeDadosException("Erro no banco de dados: " + e.getMessage(), e);
    }
}
