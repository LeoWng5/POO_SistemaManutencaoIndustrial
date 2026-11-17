import database.ConexaoDB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Teste rápido: cria o banco e lista as tabelas. Rode antes de mexer no resto. */
public class TesteConexao {
    public static void main(String[] args) {
        try {
            ConexaoDB.inicializar();
            try (Connection conn = ConexaoDB.getConexao();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name")) {
                System.out.println("Banco OK. Tabelas criadas:");
                while (rs.next()) {
                    System.out.println(" - " + rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            System.out.println("ERRO: " + e.getMessage());
        }
    }
}
