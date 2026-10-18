package classes;

import exception.EstoqueInsuficienteException;
import java.io.Serializable; // <-- Adicionado
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class Manutencao implements Serializable { // <-- Adicionado Serializable aqui
    private String id;
    private LocalDate data;
    private String descricao;
    private double custo;
    private Tecnico tecnico;
    private Maquina maquina;
    private String status;
    
    // Listas para controlar as peças usadas sem precisar da classe ItemPeca
    private List<Peca> pecasUtilizadas = new ArrayList<>();
    private List<Integer> quantidades = new ArrayList<>();

    public Manutencao(String id, String descricao, Maquina maquina) {
        this.id = id;
        this.descricao = descricao;
        this.maquina = maquina;
        this.data = LocalDate.now();
        this.status = "ABERTA";
    }

    // Aqui a EstoqueInsuficienteException é disparada!
    public void adicionarPeca(Peca peca, int qtd) throws EstoqueInsuficienteException {
        peca.baixarEstoque(qtd); 
        pecasUtilizadas.add(peca);
        quantidades.add(qtd);
    }

    public double calcularCustoPecas() {
        double total = 0;
        for (int i = 0; i < pecasUtilizadas.size(); i++) {
            total += pecasUtilizadas.get(i).getValorUnitario() * quantidades.get(i);
        }
        return total;
    }

    public void concluir() {
        this.status = "CONCLUIDA";
        this.custo = calcularCusto();
    }

    // Métodos que as classes filhas são OBRIGADAS a implementar
    public abstract double calcularCusto();
    public abstract void exibirDetalhes();

    // Getters e Setters
    public String getId() { 
        return id; 
    }
    public void setId(String id) { 
        this.id = id; 
    }

    public LocalDate getData() { 
        return data; 
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
}