package classes;

import java.util.ArrayList;
import java.util.List;

/**
 * Molde (template) de máquina, criado pelo ADM.
 * Guarda o nome do tipo, o fabricante padrão e a "receita" de peças recomendadas.
 */
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

    public void adicionarPecaRecomendada(Peca peca, int quantidade) {
        pecasRecomendadas.add(new ItemPeca(peca, quantidade));
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getFabricantePadrao() { return fabricantePadrao; }
    public void setFabricantePadrao(String fabricantePadrao) { this.fabricantePadrao = fabricantePadrao; }

    public List<ItemPeca> getPecasRecomendadas() { return pecasRecomendadas; }

    // Usado pelas caixas de seleção da tela
    @Override
    public String toString() {
        return nome + " (" + fabricantePadrao + ")";
    }
}
