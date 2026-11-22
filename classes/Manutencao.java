package classes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import service.RelogioSistema;

public abstract class Manutencao {

    private int id;
    private LocalDate data;
    private String status, descricao;
    private double custo;
    private Tecnico tecnico;
    private Maquina maquina;
    private List<ItemPeca> itens = new ArrayList<>();

    public Manutencao(int id, String descricao, Maquina maquina) {
        this.id = id;
        this.descricao = descricao;
        this.maquina = maquina;
        this.data = RelogioSistema.hoje();
        this.status = "ABERTA";
    }

    public abstract String getTipo();

    public void carregarItem(Peca p, int qtd) { this.itens.add(new ItemPeca(p, qtd)); }

    public double calcularCusto() {
        double custoMaoDeObra = 100.0;
        double total = custoMaoDeObra;
        for (ItemPeca i : itens) total += i.getSubtotal();
        return total;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getCusto() { return custo; }
    public void setCusto(double custo) { this.custo = custo; }
    public Tecnico getTecnico() { return tecnico; }
    public void setTecnico(Tecnico tecnico) { this.tecnico = tecnico; }
    public Maquina getMaquina() { return maquina; }
    public void setMaquina(Maquina maquina) { this.maquina = maquina; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public List<ItemPeca> getItens() { return itens; }
    public boolean recuperaVidaUtil() { return false; }
}