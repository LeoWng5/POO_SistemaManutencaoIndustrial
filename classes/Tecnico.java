package classes;

public class Tecnico {
    private int id;
    private String nome;
    private String telefone;
    private boolean disponivel;
    private TipoTecnico tipo;

    // Construtor para um técnico novo (nasce disponível)
    public Tecnico(int id, String nome, String telefone, TipoTecnico tipo) {
        this(id, nome, telefone, true, tipo);
    }

    // Construtor usado ao carregar do banco
    public Tecnico(int id, String nome, String telefone, boolean disponivel, TipoTecnico tipo) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.disponivel = disponivel;
        this.tipo = tipo;
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

    public TipoTecnico getTipo() {
        return tipo;
    }
    public void setTipo(TipoTecnico tipo) {
        this.tipo = tipo;
    }

    // Atalhos que vêm do molde
    public String getCargo() {
        return tipo.getCargo();
    }
    public String getEspecialidade() {
        return tipo.getEspecialidade();
    }
    public double getValorHora() {
        return tipo.getValorHora();
    }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | Tel: %s | %s",
                id, nome, tipo.getEspecialidade(), telefone,
                disponivel ? "DISPONÍVEL" : "OCUPADO");
    }
}
