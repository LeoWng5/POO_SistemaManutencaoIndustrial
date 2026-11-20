package service;

public class Sessao {
    private static Sessao instancia;
    private String login;
    private String perfil;

    private Sessao() {} // Construtor privado para ser Singleton

    public static Sessao getInstance() {
        if (instancia == null) {
            instancia = new Sessao();
        }
        return instancia;
    }

    public void logar(String login, String perfil) {
        this.login = login;
        this.perfil = perfil;
    }

    public void deslogar() {
        this.login = null;
        this.perfil = null;
    }

    public String getLogin() { return login; }
    public String getPerfil() { return perfil; }
    
    // O método que a interface vai usar para bloquear botões!
    public boolean isAdmin() {
        return "ADM".equals(perfil);
    }
}