package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {
    
    // Retorna o perfil ("ADM" ou "OPERADOR") se a senha estiver correta, ou null se falhar
    public String realizarLogin(Connection conn, String login, String senha) throws SQLException {
        String sql = "SELECT perfil FROM usuario WHERE login = ? AND senha = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, senha);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("perfil");
                }
            }
        }
        return null;
    }
}