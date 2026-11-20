package classes;
import java.time.LocalDate;

public class ManutencaoPreventiva extends Manutencao {
    private int periodicidadeDias;
    private LocalDate proximaData;

    public ManutencaoPreventiva(int id, String descricao, Maquina maquina, int periodicidadeDias) {
        super(id, descricao, maquina);
        this.periodicidadeDias = periodicidadeDias;
        this.proximaData = LocalDate.now().plusDays(periodicidadeDias);
    }
    
    @Override
    public String getTipo() { return "PREVENTIVA"; }
    
    public int getPeriodicidadeDias() { return periodicidadeDias; }
    public void setPeriodicidadeDias(int periodicidadeDias) { this.periodicidadeDias = periodicidadeDias; }
    public LocalDate getProximaData() { return proximaData; }
    public void setProximaData(LocalDate proximaData) { this.proximaData = proximaData; }
}