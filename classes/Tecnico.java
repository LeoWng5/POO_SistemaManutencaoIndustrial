package classes;

public class Tecnico {
    private int id;
    private String nome;
    private String telefone;
    private boolean disponivel;
    private TipoTecnico tipo;
    private String caminhoFoto; // NOVO CAMPO

    public Tecnico(int id, String nome, String telefone, boolean disponivel, TipoTecnico tipo, String caminhoFoto) {
        this.id = id; this.nome = nome; this.telefone = telefone; 
        this.disponivel = disponivel; this.tipo = tipo; this.caminhoFoto = caminhoFoto;
    }

    public Tecnico(int id, String nome, String telefone, TipoTecnico tipo, String caminhoFoto) {
        this(id, nome, telefone, true, tipo, caminhoFoto);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public String getTelefone() { return telefone; }
    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }
    public TipoTecnico getTipo() { return tipo; }
    public String getEspecialidade() { return tipo.getEspecialidade(); }
    public String getFoto() { return caminhoFoto; }
    public void setFoto(String caminhoFoto) { this.caminhoFoto = caminhoFoto; }
    
    @Override
    public String toString() { return nome; }
}