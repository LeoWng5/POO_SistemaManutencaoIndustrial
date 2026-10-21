package classes;

public class ManutencaoCorretiva extends Manutencao {
    private String causaFalha;
    private String gravidade;

    public ManutencaoCorretiva(String id, String descricao, Maquina maquina, String causaFalha, String gravidade) {
        super(id, descricao, maquina);
        this.causaFalha = causaFalha;
        this.gravidade = gravidade;
    }

    @Override
    public double calcularCusto() {
        // Custo fixo de mão de obra + o custo das peças que vieram lá da classe mãe
        return 100.0 + calcularCustoPecas();
    }

    @Override
    public void exibirDetalhes() {
        System.out.println("=== MANUTENÇÃO CORRETIVA ===");
        System.out.println("ID: " + getId() + " | Máquina: " + getMaquina().getNome());
        System.out.println("Causa: " + causaFalha + " | Gravidade: " + gravidade);
        System.out.println("Status: " + getStatus() + " | Custo Total: R$ " + String.format("%.2f", getCusto()));
        if (getTecnico() != null) {
            System.out.println("Técnico Responsável: " + getTecnico().getNome());
        }
        
        // NOVO: Listando as peças utilizadas
        System.out.println("--- Peças Utilizadas ---");
        if (getItens().isEmpty()) {
            System.out.println("Nenhuma peça utilizada.");
        } else {
            for (ItemPeca item : getItens()) {
                System.out.printf("- %s | Qtd: %d | Subtotal: R$ %.2f\n", 
                    item.getPeca().getNome(), 
                    item.getQuantidade(), 
                    item.getSubtotal());
            }
        }
        System.out.println("============================");
    }

    //Métodos Setter e Getters
    public String getCausaFalha() { 
        return causaFalha; 
    }
    public void setCausaFalha(String causaFalha) { 
        this.causaFalha = causaFalha; 
    }

    public String getGravidade() { 
        return gravidade; 
    }
    public void setGravidade(String gravidade) { 
        this.gravidade = gravidade; 
    }
}