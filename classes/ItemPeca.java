package classes;

public class ItemPeca {
    private Peca peca;
    private int quantidade;

    public ItemPeca(Peca peca, int quantidade) {
        this.peca = peca;
        this.quantidade = quantidade;
    }

    public double getSubtotal() {
        return peca.getValorUnitario() * quantidade;
    }

    public Peca getPeca() {
        return peca;
    }
    public int getQuantidade() {
        return quantidade;
    }
}
