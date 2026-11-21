package classes;

import java.util.ArrayList;
import java.util.List;

public class TipoMaquina {

    private int id;
    private String nome;
    private String fabricantePadrao;
    private int vidaUtilAnos;
    private List<ItemPeca> pecasRecomendadas = new ArrayList<>();

    public TipoMaquina(int id, String nome, String fabricantePadrao, int vidaUtilAnos) {
        this.id = id;
        this.nome = nome;
        this.fabricantePadrao = fabricantePadrao;
        this.vidaUtilAnos = vidaUtilAnos;
    }

    /** Construtor antigo mantido para compatibilidade (vida útil padrão = 5). */
    public TipoMaquina(int id, String nome, String fabricantePadrao) {
        this(id, nome, fabricantePadrao, 5);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getFabricantePadrao() { return fabricantePadrao; }
    public void setFabricantePadrao(String fabricantePadrao) { this.fabricantePadrao = fabricantePadrao; }
    public int getVidaUtilAnos() { return vidaUtilAnos; }
    public void setVidaUtilAnos(int vidaUtilAnos) { this.vidaUtilAnos = vidaUtilAnos; }

    public List<ItemPeca> getPecasRecomendadas() { return pecasRecomendadas; }

    public void adicionarPecaRecomendada(Peca p, int qtd) {
        pecasRecomendadas.add(new ItemPeca(p, qtd));
    }

    @Override
    public String toString() { return nome; }
}