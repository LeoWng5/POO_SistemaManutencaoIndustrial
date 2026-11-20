package classes;
import java.time.LocalDate;

public class Maquina extends Equipamento {
    private String numeroSerie;
    private LocalDate dataAquisicao;
    private TipoMaquina tipo;
    private String caminhoFoto; // NOVO CAMPO

    public Maquina(int id, String numeroSerie, String status, LocalDate dataAquisicao, TipoMaquina tipo, String caminhoFoto) {
        super(id, tipo.getNome(), status);
        this.numeroSerie = numeroSerie;
        this.dataAquisicao = dataAquisicao;
        this.tipo = tipo;
        this.caminhoFoto = caminhoFoto;
    }

    public String getNumeroSerie() { 
        return numeroSerie; 
    }
    public LocalDate getDataAquisicao() { 
        return dataAquisicao; 
    }
    public TipoMaquina getTipo() { 
        return tipo; 
    }
    public String getFabricante() { 
        return tipo.getFabricantePadrao(); 
    }
    public String getFoto() { 
        return caminhoFoto; 
    }
    
    public void setFoto(String caminhoFoto) { 
        this.caminhoFoto = caminhoFoto;     
    }
        // Método para calcular a depreciação (vida útil de 5 anos = 1825 dias)
    public int getSaudeEquipamento() {
        long diasDeUso = java.time.temporal.ChronoUnit.DAYS.between(this.dataAquisicao, java.time.LocalDate.now());
        int vidaUtilDias = 1825; 
        if (diasDeUso >= vidaUtilDias) return 0;
        double saude = 100.0 - ((double) diasDeUso / vidaUtilDias * 100.0);
        return (int) saude;
    }
    
    @Override
    public String toString() { return getNome() + " (Série: " + numeroSerie + ")"; }
}