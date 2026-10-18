package classes;

import exception.EstoqueInsuficienteException;

public class Peca {
    private String id;
    private String nome;
    private String descricao;
    private String categoria;        // "MECANICA", "ELETRICA", "LUBRIFICANTE", etc.
    private int quantidadeEstoque;
    private int estoqueMinimo;
    private double valorUnitario;

    // Construtor
    public Peca(String id, String nome, String descricao, String categoria,
                int quantidadeEstoque, int estoqueMinimo, double valorUnitario) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.quantidadeEstoque = quantidadeEstoque;
        this.estoqueMinimo = estoqueMinimo;
        this.valorUnitario = valorUnitario;
    }

    // Métodos de negócio
    public void baixarEstoque(int quantidade) throws EstoqueInsuficienteException {
        if (quantidade <= 0) {
            throw new EstoqueInsuficienteException(
                "Quantidade deve ser maior que zero.");
        }
        if (quantidade > quantidadeEstoque) {
            throw new EstoqueInsuficienteException(
                String.format("Estoque insuficiente de '%s'. Disponível: %d, solicitado: %d",
                        nome, quantidadeEstoque, quantidade));
        }
        this.quantidadeEstoque -= quantidade;
    }

    public void reporEstoque(int quantidade) {
        if (quantidade > 0) {
            this.quantidadeEstoque += quantidade;
        }
    }

    public boolean isEstoqueCritico() {
        return quantidadeEstoque <= estoqueMinimo;
    }

    // Getters e Setters
    public String getId() {
        return id; 
    }
    public void setId(String id){
        this.id = id; 
    }

    public String getNome() {
        return nome; 
    }
    public void setNome(String nome) { 
        this.nome = nome; 
    }

    public String getDescricao() { 
        return descricao; 
    }
    public void setDescricao(String descricao) { 
        this.descricao = descricao; 
    }

    public String getCategoria() { 
        return categoria; 
    }
    public void setCategoria(String categoria) { 
        this.categoria = categoria; 
    }

    public int getQuantidadeEstoque() { 
        return quantidadeEstoque; 
    }
    public void setQuantidadeEstoque(int quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public int getEstoqueMinimo() { 
        return estoqueMinimo; 
    }
    public void setEstoqueMinimo(int estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public double getValorUnitario() { 
        return valorUnitario; 
    }
    public void setValorUnitario(double valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    // toString pra listagem no console
    @Override
    public String toString() {
        String alerta = isEstoqueCritico() ? " ⚠ ESTOQUE CRÍTICO" : "";
        return String.format("[%s] %s (%s) | Qtd: %d | R$ %.2f%s",
                id, nome, categoria, quantidadeEstoque, valorUnitario, alerta);
    }
}