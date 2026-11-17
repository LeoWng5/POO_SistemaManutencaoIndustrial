import database.ConexaoDB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 TesteConexao - verifica a integração do projeto com o banco SQLite.
 
  O que faz:

   1. Chama ConexaoDB.inicializar(), que cria o banco e as tabelas
      a partir do arquivo schema.sql.
   2. Consulta o banco e imprime o nome de cada tabela criada.
 
  Resultado esperado: a mensagem "Banco OK" seguida de 10 tabelas.
  Se aparecer "ERRO", confira se o sqlite-jdbc.jar está na pasta lib/
  e se o schema.sql está na pasta raiz do projeto.
 */

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
