package classes;

/**
 * Molde (template) de técnico, criado pelo ADM.
 * Define o cargo, a especialidade e o valor da hora de trabalho.
 */
public class TipoTecnico {
    private int id;
    private String cargo;
    private String especialidade;
    private double valorHora;

    public TipoTecnico(int id, String cargo, String especialidade, double valorHora) {
        this.id = id;
        this.cargo = cargo;
        this.especialidade = especialidade;
        this.valorHora = valorHora;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }

    public double getValorHora() { return valorHora; }
    public void setValorHora(double valorHora) { this.valorHora = valorHora; }

    @Override
    public String toString() {
        return String.format("%s - %s (R$ %.2f/h)", cargo, especialidade, valorHora);
    }
}
