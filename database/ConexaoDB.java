package database;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexaoDB {

    private static final String URL = "jdbc:sqlite:manutencao.db";
    private static final String ARQUIVO_SCHEMA = "schema.sql";

    public static Connection getConexao() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver do SQLite não encontrado.", e);
        }
        // NÃO TEM PRAGMA AQUI. Ele foi 100% removido.
        return DriverManager.getConnection(URL);
    }

    public static void inicializar() throws SQLException {
        String conteudo;
        try {
            conteudo = new String(Files.readAllBytes(Paths.get(ARQUIVO_SCHEMA)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SQLException("Não foi possível ler o arquivo " + ARQUIVO_SCHEMA + ": " + e.getMessage(), e);
        }

        try (Connection conn = getConexao(); Statement st = conn.createStatement()) {
            
            // Limpeza profunda de comentários
            StringBuilder sb = new StringBuilder();
            for (String linha : conteudo.split("\n")) {
                if (!linha.trim().startsWith("--")) {
                    int commentIndex = linha.indexOf("--");
                    if (commentIndex != -1) {
                        linha = linha.substring(0, commentIndex);
                    }
                    sb.append(linha).append("\n");
                }
            }
            
            String[] comandos = sb.toString().split(";");
            for (String comando : comandos) {
                String limpo = comando.trim();
                if (!limpo.isEmpty()) {
                    try {
                        st.execute(limpo);
                    } catch (SQLException e) {
                        // SE DER ERRO, VAI IMPRIMIR A LINHA EXATA NO TERMINAL
                        System.err.println("\n========== ERRO NO SQL ==========");
                        System.err.println("COMANDO: " + limpo);
                        System.err.println("=================================\n");
                        throw e; 
                    }
                }
            }
        }
    }
}