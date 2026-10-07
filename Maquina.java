package classes;

import java.time.LocalDate;

public class Maquina extends Equipamento {
    private String modelo;
    private String fabricante;
    private LocalDate dataAquisicao;
    
    //Constructor de Maquina
    public Maquina(String id, String nome, String status,
                   String modelo, String fabricante, LocalDate dataAquisicao){
        super(id, nome, status);
        this.modelo = modelo;
        this.fabricante = fabricante;
    }

    //Métodos Setters e Getters
    public String getModelo(){
        return modelo;
    }
    public void setModelo(String modelo){
        this.modelo=modelo;
    }


    public String getFabricante(){
        return fabricante;
    }
    public void setFabricante(String fabricante){
        this.fabricante = fabricante;
    }
    
    
    public LocalDate getDataAquisicao(){
        return dataAquisicao;
    }
    public void setDataAquisicao(LocalDate dataAquisicao){
        this.dataAquisicao = dataAquisicao;
    }
}
