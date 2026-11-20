package classes;

public class Peca {
    private int id, quantidadeEstoque, estoqueMinimo;
    private String nome, descricao, categoria;
    private double valorUnitario;

    public Peca(int id, String nome, String descricao, String categoria, int qtd, int estoqueMinimo, double valor) {
        this.id = id; this.nome = nome; this.descricao = descricao; this.categoria = categoria;
        this.quantidadeEstoque = qtd; this.estoqueMinimo = estoqueMinimo; this.valorUnitario = valor;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public int getQuantidadeEstoque() { return quantidadeEstoque; }
    public void setQuantidadeEstoque(int quantidadeEstoque) { this.quantidadeEstoque = quantidadeEstoque; }
    public int getEstoqueMinimo() { return estoqueMinimo; }
    public void setEstoqueMinimo(int estoqueMinimo) { this.estoqueMinimo = estoqueMinimo; }
    public double getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(double valorUnitario) { this.valorUnitario = valorUnitario; }
    
    public void baixarEstoque(int qtd) throws exception.EstoqueInsuficienteException {
        if (qtd > quantidadeEstoque) throw new exception.EstoqueInsuficienteException("Estoque insuficiente.");
        this.quantidadeEstoque -= qtd;
    }
    
    @Override
    public String toString() { return nome; }
}