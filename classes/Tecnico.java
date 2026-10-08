package classes;

public class Tecnico {
    private String id;
    private String nome;
    private String especialidade;
    private String telefone;
    private boolean disponivel;

    // Construtor
    public Tecnico(String id, String nome, String especialidade, String telefone) {
        this.id = id;
        this.nome = nome;
        this.especialidade = especialidade;
        this.telefone = telefone;
        this.disponivel = true;   // todo técnico criado está inicialmente disponível
    }

    // Métodos de negócio
    public void alterarDisponibilidade() {
        this.disponivel = !this.disponivel;
    }

    public void ocupar() {
        this.disponivel = false;
    }

    public void liberar() {
        this.disponivel = true;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEspecialidade() {
        return especialidade;
    }
    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public String getTelefone() {
        return telefone;
    }
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public boolean isDisponivel() {
        return disponivel;
    }
    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }

    // toString pra facilitar listagem no console
    @Override
    public String toString() {
        return String.format("[%s] %s | %s | Tel: %s | %s",
                id, nome, especialidade, telefone,
                disponivel ? "DISPONÍVEL" : "OCUPADO");
    }
}
