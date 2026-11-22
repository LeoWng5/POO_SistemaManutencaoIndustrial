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

    // Renovação de vida útil (efeito da manutenção preventiva)
    private LocalDate dataBaseDepreciacao;   // null = conta a partir da data de aquisição
    private Integer vidaUtilAtualAnos;       // null = usa a vida útil do tipo

    public Maquina(int id, String numeroSerie, String status, LocalDate dataAquisicao,
                   double valorAquisicao, TipoMaquina tipo, String caminhoFoto) {
        super(id, tipo.getNome(), status);
        this.numeroSerie = numeroSerie;
        this.dataAquisicao = dataAquisicao;
        this.valorAquisicao = valorAquisicao;
        this.tipo = tipo;
        this.caminhoFoto = caminhoFoto;
    }

    /** Construtor usado pelo DAO: inclui os dados da última renovação de vida útil. */
    public Maquina(int id, String numeroSerie, String status, LocalDate dataAquisicao,
                   double valorAquisicao, TipoMaquina tipo, String caminhoFoto,
                   LocalDate dataBaseDepreciacao, Integer vidaUtilAtualAnos) {
        this(id, numeroSerie, status, dataAquisicao, valorAquisicao, tipo, caminhoFoto);
        this.dataBaseDepreciacao = dataBaseDepreciacao;
        this.vidaUtilAtualAnos = vidaUtilAtualAnos;
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

    public LocalDate getDataBaseDepreciacao() { return dataBaseDepreciacao; }
    public Integer getVidaUtilAtualAnos() { return vidaUtilAtualAnos; }

    /** Data a partir da qual a depreciação é contada. */
    public LocalDate getInicioDepreciacao() {
        return dataBaseDepreciacao != null ? dataBaseDepreciacao : dataAquisicao;
    }

    /** Vida útil considerada hoje: a renovada pela preventiva ou, se não houver, a do tipo. */
    public int getVidaUtilEfetivaAnos() {
        if (vidaUtilAtualAnos != null && vidaUtilAtualAnos > 0) return vidaUtilAtualAnos;
        return (tipo != null && tipo.getVidaUtilAnos() > 0) ? tipo.getVidaUtilAnos() : 5;
    }

    /**
     * Efeito de uma manutenção preventiva: a saúde volta a 100% e a máquina
     * passa a depreciar a partir de hoje, ao longo da nova estimativa (1 a 20 anos).
     */
    public void renovarVidaUtil(int anos) {
        if (anos < 1 || anos > 20) {
            throw new IllegalArgumentException("A estimativa deve ficar entre 1 e 20 anos.");
        }
        this.dataBaseDepreciacao = RelogioSistema.hoje();
        this.vidaUtilAtualAnos = anos;
    }

    /**
     * Saúde = 100 - (dias desde o início da depreciação / (vida útil efetiva * 365)) * 100,
     * limitada entre 0 e 100. Usa a data "de hoje" do RelogioSistema.
     */
    public int getSaudeEquipamento() {
        long vidaUtilDias = (long) getVidaUtilEfetivaAnos() * 365L;
        long diasDeUso = ChronoUnit.DAYS.between(getInicioDepreciacao(), RelogioSistema.hoje());
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