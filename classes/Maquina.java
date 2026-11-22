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
    private long diasRecuperados;

    public Maquina(int id, String numeroSerie, String status, LocalDate dataAquisicao,
                   double valorAquisicao, TipoMaquina tipo, String caminhoFoto) {
        super(id, tipo.getNome(), status);
        this.numeroSerie = numeroSerie;
        this.dataAquisicao = dataAquisicao;
        this.valorAquisicao = valorAquisicao;
        this.tipo = tipo;
        this.caminhoFoto = caminhoFoto;
        this.diasRecuperados = Math.max(0, diasRecuperados);
    }

    /** Construtor antigo mantido para compatibilidade (valor = 0). */
    public Maquina(int id, String numeroSerie, String status, LocalDate dataAquisicao,
                   double valorAquisicao, TipoMaquina tipo, String caminhoFoto, long diasRecuperados) {
        this(id, numeroSerie, status, dataAquisicao, valorAquisicao, tipo, caminhoFoto);
        this.diasRecuperados = Math.max(0, diasRecuperados);
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
        long diasDeUso = getDiasDeUsoEfetivos();
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

    public long getDiasRecuperados() { return diasRecuperados; }

    /** Dias de uso que ainda contam para a depreciação (uso real menos o que foi recuperado). */
    public long getDiasDeUsoEfetivos() {
        long bruto = ChronoUnit.DAYS.between(this.dataAquisicao, RelogioSistema.hoje());
        return Math.max(0, bruto - diasRecuperados);
    }

    /**
     * Recupera até N anos de desgaste (efeito de uma manutenção preventiva).
     * Nunca recupera mais do que o desgaste existente, então a saúde fica no máximo em 100%.
     */
    public void rejuvenescer(int anos) {
        if (anos <= 0) throw new IllegalArgumentException("Informe pelo menos 1 ano.");
        long recuperar = Math.min(anos * 365L, getDiasDeUsoEfetivos());
        this.diasRecuperados += recuperar;
    }

    @Override
    public String toString() { return getNome() + " (Série: " + numeroSerie + ")"; }
}