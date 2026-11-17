package database;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gerencia a conexão com o banco SQLite.
 * O banco é um único arquivo (manutencao.db) criado na pasta onde o programa roda.
 */
public class ConexaoDB {

    private static final String URL = "jdbc:sqlite:manutencao.db";
    private static final String ARQUIVO_SCHEMA = "schema.sql";

    private ConexaoDB() {
        // classe utilitária: não deve ser instanciada
    }

    /** Abre uma nova conexão. Quem chama deve fechar (use try-with-resources). */
    public static Connection getConexao() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver do SQLite não encontrado. "
                    + "Coloque o arquivo sqlite-jdbc.jar na pasta lib/ e inclua no classpath.", e);
        }
        Connection conn = DriverManager.getConnection(URL);
        // O SQLite ignora chaves estrangeiras a menos que isto seja ligado em CADA conexão.
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }

    /** Cria as tabelas (se ainda não existirem) executando o schema.sql. */
    public static void inicializar() throws SQLException {
        String conteudo;
        try {
            conteudo = Files.readString(Paths.get(ARQUIVO_SCHEMA), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SQLException("Não foi possível ler o arquivo " + ARQUIVO_SCHEMA
                    + " (ele deve estar na pasta raiz do projeto): " + e.getMessage(), e);
        }

        try (Connection conn = getConexao(); Statement st = conn.createStatement()) {
            for (String comando : conteudo.split(";")) {
                String limpo = removerComentarios(comando).trim();
                if (!limpo.isEmpty()) {
                    st.execute(limpo);
                }
            }
        }
    }

    private static String removerComentarios(String texto) {
        StringBuilder sb = new StringBuilder();
        for (String linha : texto.split("\n")) {
            if (!linha.trim().startsWith("--")) {
                sb.append(linha).append("\n");
            }
        }
        return sb.toString();
    }
}
