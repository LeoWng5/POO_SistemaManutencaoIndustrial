import database.ConexaoDB;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TesteConexao {
    public static void main(String[] args) {
        System.out.println("Iniciando teste do banco de dados...");
        try {
            ConexaoDB.inicializar();
            
            try (Connection conn = ConexaoDB.getConexao();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name")) {
                
                System.out.println("✅ Banco OK. Tabelas criadas:");
                while (rs.next()) {
                    System.out.println(" - " + rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            System.out.println("\n❌ ERRO NO BANCO DE DADOS:");
            e.printStackTrace(); // ISSO VAI MOSTRAR A LINHA EXATA DO ERRO NO TERMINAL
        } catch (Exception e) {
            System.out.println("❌ OUTRO ERRO:");
            e.printStackTrace();
        }
    }
}