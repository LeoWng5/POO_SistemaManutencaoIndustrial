package classes;

import java.time.LocalDate;

public class ManutencaoPreventiva extends Manutencao {
    private int periodicidadeDias;
    private LocalDate proximaData;

    public ManutencaoPreventiva(String id, String descricao, Maquina maquina, int periodicidadeDias) {
        super(id, descricao, maquina);
        this.periodicidadeDias = periodicidadeDias;
        this.proximaData = LocalDate.now().plusDays(periodicidadeDias);
    }

    @Override
    public double calcularCusto() {
        // Custo fixo da preventiva + o custo das peças
        return 50.0 + calcularCustoPecas();
    }

    @Override
    public void exibirDetalhes() {
        System.out.println("=== MANUTENÇÃO PREVENTIVA ===");
        System.out.println("ID: " + getId() + " | Máquina: " + getMaquina().getNome());
        System.out.println("Periodicidade: " + periodicidadeDias + " dias | Próxima: " + proximaData);
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
    public int getPeriodicidadeDias() { 
        return periodicidadeDias; 
    }
    public void setPeriodicidadeDias(int periodicidadeDias) { 
        this.periodicidadeDias = periodicidadeDias; 
    }

    public LocalDate getProximaData() { 
        return proximaData; 
    }
    public void setProximaData(LocalDate proximaData) { 
        this.proximaData = proximaData; 
    }
}