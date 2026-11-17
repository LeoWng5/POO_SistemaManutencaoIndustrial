package classes;

import exception.EstoqueInsuficienteException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class Manutencao {
    private int id;
    private LocalDate data;
    private String descricao;
    private double custo;
    private Tecnico tecnico;
    private Maquina maquina;
    private String status;

    // Lista com ItemPeca
    private List<ItemPeca> itens = new ArrayList<>();

    public Manutencao(int id, String descricao, Maquina maquina) {
        this.id = id;
        this.descricao = descricao;
        this.maquina = maquina;
        this.data = LocalDate.now();
        this.status = "ABERTA";
    }

    // Uso normal: dá baixa no estoque da peça e registra o item
    public void adicionarPeca(Peca peca, int qtd) throws EstoqueInsuficienteException {
        peca.baixarEstoque(qtd);
        itens.add(new ItemPeca(peca, qtd));
    }

    // Uso ao carregar do banco: só registra o item, SEM mexer no estoque
    public void carregarItem(Peca peca, int qtd) {
        itens.add(new ItemPeca(peca, qtd));
    }

    public double calcularCustoPecas() {
        double total = 0;
        for (ItemPeca item : itens) {
            total += item.getSubtotal();
        }
        return total;
    }

    public void concluir() {
        this.status = "CONCLUIDA";
        this.custo = calcularCusto();
    }

    public abstract double calcularCusto();
    public abstract void exibirDetalhes();
    // Texto gravado na coluna "tipo" do banco: CORRETIVA ou PREVENTIVA
    public abstract String getTipo();

    // Métodos Setters e Getters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getCusto() {
        return custo;
    }

    public void setCusto(double custo) {
        this.custo = custo;
    }

    public Tecnico getTecnico() {
        return tecnico;
    }

    public void setTecnico(Tecnico tecnico) {
        this.tecnico = tecnico;
    }

    public Maquina getMaquina() {
        return maquina;
    }

    public void setMaquina(Maquina maquina) {
        this.maquina = maquina;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ItemPeca> getItens() {
        return itens;
    }
}
