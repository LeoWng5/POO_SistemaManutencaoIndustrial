package classes;

public class Tecnico {
    private int id;
    private String nome;
    private String telefone;
    private boolean disponivel;
    private TipoTecnico tipo;

    public Tecnico(int id, String nome, String telefone, boolean disponivel, TipoTecnico tipo) {
        this.id = id; 
        this.nome = nome; 
        this.telefone = telefone; 
        this.disponivel = disponivel; 
        this.tipo = tipo;
    }

    public Tecnico(int id, String nome, String telefone, TipoTecnico tipo) {
        this(id, nome, telefone, true, tipo);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }
    public TipoTecnico getTipo() { return tipo; }
    public void setTipo(TipoTecnico tipo) { this.tipo = tipo; }

    public String getEspecialidade() { return tipo.getEspecialidade(); }
    
    @Override
    public String toString() { return nome; }
}