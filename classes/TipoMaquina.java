package classes;
import java.util.ArrayList;
import java.util.List;

public class TipoMaquina {
    private int id;
    private String nome;
    private String fabricantePadrao;
    private List<ItemPeca> pecasRecomendadas = new ArrayList<>();

    public TipoMaquina(int id, String nome, String fabricantePadrao) {
        this.id = id;
        this.nome = nome;
        this.fabricantePadrao = fabricantePadrao;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getFabricantePadrao() { return fabricantePadrao; }
    public void setFabricantePadrao(String fabricantePadrao) { this.fabricantePadrao = fabricantePadrao; }
    
    public void adicionarPecaRecomendada(Peca p, int qtd) { this.pecasRecomendadas.add(new ItemPeca(p, qtd)); }
    public List<ItemPeca> getPecasRecomendadas() { return pecasRecomendadas; }
    
    @Override
    public String toString() { return nome; }
}