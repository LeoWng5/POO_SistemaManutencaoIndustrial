package classes;

import java.time.LocalDateTime;

public class Falha {
    private int id;
    private LocalDateTime dataHora;
    private String descricao;
    private Maquina maquina;

    // Falha nova: a data e a hora são as de agora
    public Falha(int id, String descricao, Maquina maquina) {
        this(id, descricao, maquina, LocalDateTime.now());
    }

    // Usado ao carregar do banco
    public Falha(int id, String descricao, Maquina maquina, LocalDateTime dataHora) {
        this.id = id;
        this.descricao = descricao;
        this.maquina = maquina;
        this.dataHora = dataHora;
    }

    // Métodos Setters e Getters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Maquina getMaquina() {
        return maquina;
    }
    public void setMaquina(Maquina maquina) {
        this.maquina = maquina;
    }
}
