package service;

import classes.*;
import exception.*;
import java.io.*;
import java.util.*;

public class SistemaService {
    private List<Maquina> maquinas = new ArrayList<>();
    private List<Tecnico> tecnicos = new ArrayList<>();
    private List<Peca> pecas = new ArrayList<>();
    private List<Falha> falhas = new ArrayList<>();
    private List<Manutencao> manutencoes = new ArrayList<>();

    public void cadastrarMaquina(Maquina m) { maquinas.add(m); }
    public void cadastrarTecnico(Tecnico t) { tecnicos.add(t); }
    public void cadastrarPeca(Peca p) { pecas.add(p); }

    public Maquina buscarMaquina(String id) throws MaquinaInexistenteException {
        for (Maquina m : maquinas) {
            if (m.getId().equals(id)) return m;
        }
        throw new MaquinaInexistenteException("Máquina com ID " + id + " não encontrada.");
    }

    public Tecnico buscarTecnico(String id) {
        for (Tecnico t : tecnicos) {
            if (t.getId().equals(id)) return t;
        }
        return null;
    }

    public Peca buscarPeca(String id) {
        for (Peca p : pecas) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    public void registrarFalha(String descricao, String idMaquina) throws MaquinaInexistenteException {
        Maquina m = buscarMaquina(idMaquina);
        falhas.add(new Falha("F" + (falhas.size() + 1), descricao, m));
        m.setStatus("QUEBRADA");
    }

    public void abrirCorretiva(String descricao, String idMaquina, String causa, String gravidade) 
            throws MaquinaInexistenteException, MaquinaInativaException {
        Maquina m = buscarMaquina(idMaquina);
        if (m.getStatus().equalsIgnoreCase("INATIVA")) {
            throw new MaquinaInativaException("Não é possível manutenção em máquina inativa.");
        }
        manutencoes.add(new ManutencaoCorretiva("M" + (manutencoes.size() + 1), descricao, m, causa, gravidade));
    }

    public void atribuirTecnico(String idManutencao, String idTecnico) throws TecnicoIndisponivelException {
        Tecnico t = buscarTecnico(idTecnico);
        if (t == null || !t.isDisponivel()) {
            throw new TecnicoIndisponivelException("Técnico indisponível ou não encontrado.");
        }
        
        for (Manutencao m : manutencoes) {
            if (m.getId().equals(idManutencao)) {
                t.ocupar();
                m.setTecnico(t);
                m.setStatus("EM_EXECUCAO");
                return;
            }
        }
    }

    public void concluirManutencao(String idManutencao) throws ManutencaoFinalizadaException {
        for (Manutencao m : manutencoes) {
            if (m.getId().equals(idManutencao)) {
                if (m.getStatus().equals("CONCLUIDA")) {
                    throw new ManutencaoFinalizadaException("Essa manutenção já foi finalizada.");
                }
                m.concluir();
                if (m.getTecnico() != null) m.getTecnico().liberar();
                m.getMaquina().setStatus("ATIVA");
                return;
            }
        }
    }

    public void listarManutencoes() {
        for (Manutencao m : manutencoes) m.exibirDetalhes();
    }

    // Persistência
    public void salvar() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("dados.dat"))) {
            oos.writeObject(maquinas); oos.writeObject(tecnicos);
            oos.writeObject(pecas); oos.writeObject(falhas); oos.writeObject(manutencoes);
        } catch (IOException e) { System.out.println("Erro ao salvar: " + e.getMessage()); }
    }

    @SuppressWarnings("unchecked")
    public void carregar() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("dados.dat"))) {
            maquinas = (List<Maquina>) ois.readObject();
            tecnicos = (List<Tecnico>) ois.readObject();
            pecas = (List<Peca>) ois.readObject();
            falhas = (List<Falha>) ois.readObject();
            manutencoes = (List<Manutencao>) ois.readObject();
        } catch (Exception e) { /* Primeira execução */ }
    }
}