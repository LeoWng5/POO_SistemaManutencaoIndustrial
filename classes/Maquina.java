package classes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import service.RelogioSistema;

public class Maquina extends Equipamento {

    private String numeroSerie;
    private LocalDate dataAquisicao;
    private double valorAquisicao;
    private TipoMaquina tipo;
    private String caminhoFoto;

    public Maquina(int id, String numeroSerie, String status, LocalDate dataAquisicao,
                   double valorAquisicao, TipoMaquina tipo, String caminhoFoto) {
        super(id, tipo.getNome(), status);
        this.numeroSerie = numeroSerie;
        this.dataAquisicao = dataAquisicao;
        this.valorAquisicao = valorAquisicao;
        this.tipo = tipo;
        this.caminhoFoto = caminhoFoto;
    }

    /** Construtor antigo mantido para compatibilidade (valor = 0). */
    public Maquina(int id, String numeroSerie, String status, LocalDate dataAquisicao,
                   TipoMaquina tipo, String caminhoFoto) {
        this(id, numeroSerie, status, dataAquisicao, 0.0, tipo, caminhoFoto);
    }

    public String getNumeroSerie() { return numeroSerie; }
    public LocalDate getDataAquisicao() { return dataAquisicao; }
    public double getValorAquisicao() { return valorAquisicao; }
    public void setValorAquisicao(double valorAquisicao) { this.valorAquisicao = valorAquisicao; }
    public TipoMaquina getTipo() { return tipo; }
    public String getFabricante() { return tipo.getFabricantePadrao(); }
    public String getFoto() { return caminhoFoto; }
    public void setFoto(String caminhoFoto) { this.caminhoFoto = caminhoFoto; }

    /**
     * Saúde = 100 - (dias de uso / (vida útil em anos * 365)) * 100,
     * limitada entre 0 e 100. Usa a data "de hoje" do RelogioSistema.
     */
    public int getSaudeEquipamento() {
        int anos = (tipo != null && tipo.getVidaUtilAnos() > 0) ? tipo.getVidaUtilAnos() : 5;
        long vidaUtilDias = (long) anos * 365L;
        long diasDeUso = ChronoUnit.DAYS.between(this.dataAquisicao, RelogioSistema.hoje());
        if (diasDeUso <= 0) return 100;
        if (diasDeUso >= vidaUtilDias) return 0;
        double saude = 100.0 - ((double) diasDeUso / vidaUtilDias * 100.0);
        int s = (int) Math.round(saude);
        if (s < 0) s = 0;
        if (s > 100) s = 100;
        return s;
    }

    /** Valor residual estimado pela saúde atual (valor * saúde / 100). */
    public double getValorResidual() {
        return valorAquisicao * getSaudeEquipamento() / 100.0;
    }

    @Override
    public String toString() { return getNome() + " (Série: " + numeroSerie + ")"; }
}