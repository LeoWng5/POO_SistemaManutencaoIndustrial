package classes;

public class ManutencaoCorretiva extends Manutencao {
    private String causaFalha, gravidade;

    public ManutencaoCorretiva(int id, String descricao, Maquina maquina, String causaFalha, String gravidade) {
        super(id, descricao, maquina);
        this.causaFalha = causaFalha; this.gravidade = gravidade;
    }
    
    @Override
    public String getTipo() { return "CORRETIVA"; }
    
    public String getCausaFalha() { return causaFalha; }
    public void setCausaFalha(String causaFalha) { this.causaFalha = causaFalha; }
    public String getGravidade() { return gravidade; }
    public void setGravidade(String gravidade) { this.gravidade = gravidade; }
}