package classes;

import java.time.LocalDateTime;

public class Falha {
    private String id;
    private LocalDateTime dataHora;
    private String descricao;
    private Maquina maquina;

    // Constructor de Falha
    public Falha(String id, String descricao, Maquina maquina) {
        this.id = id;
        this.descricao = descricao;
        this.maquina = maquina;
        this.dataHora = LocalDateTime.now();
    }

    // Métodos Setters e Getters
    public String getId() {
        return id;
    }
    public void setId(String id){
        this.id=id;
    }


    public LocalDateTime getDataHora(){
        return dataHora;
    }
    public void setDataHora(LocalDateTime dataHora){
        this.dataHora = dataHora;
    }


    public String getDescricao(){
        return descricao;
    }
    public void setDescricao(String descricao){
        this.descricao = descricao;
    }
    

    public Maquina getMaquina(){
        return maquina;
    }
    public void setMaquina(Maquina maquina){
        this.maquina=maquina;
    }
}

