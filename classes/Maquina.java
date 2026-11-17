package classes;

import java.time.LocalDate;

public class Maquina extends Equipamento {
    private String numeroSerie;
    private LocalDate dataAquisicao;
    private TipoMaquina tipo;

    // O nome da máquina vem do molde (TipoMaquina)
    public Maquina(int id, String numeroSerie, String status,
                   LocalDate dataAquisicao, TipoMaquina tipo) {
        super(id, tipo.getNome(), status);
        this.numeroSerie = numeroSerie;
        this.dataAquisicao = dataAquisicao;
        this.tipo = tipo;
    }

    // Métodos Setters e Getters
    public String getNumeroSerie() {
        return numeroSerie;
    }
    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public LocalDate getDataAquisicao() {
        return dataAquisicao;
    }
    public void setDataAquisicao(LocalDate dataAquisicao) {
        this.dataAquisicao = dataAquisicao;
    }

    public TipoMaquina getTipo() {
        return tipo;
    }
    public void setTipo(TipoMaquina tipo) {
        this.tipo = tipo;
        setNome(tipo.getNome());
    }

    public String getFabricante() {
        return tipo.getFabricantePadrao();
    }

    @Override
    public String toString() {
        return "[" + getId() + "] " + getNome() + " (série " + numeroSerie + ")";
    }
}
