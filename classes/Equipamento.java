package classes;

public abstract class Equipamento {
    private int id;
    private String nome;
    private String status;

    // Constructor de Equipamento
    public Equipamento(int id, String nome, String status) {
        this.id = id;
        this.nome = nome;
        this.status = status;
    }

    // Métodos Setters e Getters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
