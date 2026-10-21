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

    // --- MÉTODOS DE CADASTRO ---
    public void cadastrarMaquina(Maquina m) { maquinas.add(m); }
    public void cadastrarTecnico(Tecnico t) { tecnicos.add(t); }
    public void cadastrarPeca(Peca p) { pecas.add(p); }

    // --- MÉTODO DE EXCLUSÃO ---
    public boolean excluirMaquina(String id) {
        for (int i = 0; i < maquinas.size(); i++) {
            if (maquinas.get(i).getId().equals(id)) {
                maquinas.remove(i);
                return true;
            }
        }
        return false;
    }

    // --- MÉTODOS DE BUSCA ---
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

    // --- REGRAS DE NEGÓCIO ---
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

    public void adicionarPecaNaManutencao(String idManutencao, String idPeca, int quantidade) 
            throws EstoqueInsuficienteException, Exception {
        
        Manutencao manutencao = null;
        for (Manutencao m : manutencoes) {
            if (m.getId().equals(idManutencao)) {
                manutencao = m;
                break;
            }
        }
        
        if (manutencao == null) throw new Exception("Manutenção não encontrada.");
        if (manutencao.getStatus().equals("CONCLUIDA")) throw new Exception("Manutenção já concluída.");

        Peca peca = buscarPeca(idPeca);
        if (peca == null) throw new Exception("Peça não encontrada no estoque.");

        manutencao.adicionarPeca(peca, quantidade); 
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
                
                System.out.println("Manutenção " + m.getId() + " concluída!");
                System.out.println("Custo total (Mão de obra + Peças): R$ " + String.format("%.2f", m.getCusto()));
                return;
            }
        }
    }

    // --- LISTAGENS ---
    public void listarManutencoes() {
        if (manutencoes.isEmpty()) { System.out.println("Nenhuma manutenção registrada."); return; }
        for (Manutencao m : manutencoes) m.exibirDetalhes();
    }

    public void listarMaquinas() {
        if (maquinas.isEmpty()) { System.out.println("Nenhuma máquina cadastrada."); return; }
        System.out.println("--- INVENTÁRIO DE MÁQUINAS ---");
        for (Maquina m : maquinas) {
            System.out.println("[" + m.getId() + "] " + m.getNome() + " | Modelo: " + m.getModelo() + " | Status: " + m.getStatus());
        }
    }

    public void listarTecnicos() {
        if (tecnicos.isEmpty()) { System.out.println("Nenhum técnico cadastrado."); return; }
        System.out.println("--- EQUIPE DE TÉCNICOS ---");
        for (Tecnico t : tecnicos) {
            System.out.println("[" + t.getId() + "] " + t.getNome() + " | Especialidade: " + t.getEspecialidade() + " | Status: " + (t.isDisponivel() ? "DISPONÍVEL" : "OCUPADO"));
        }
    }

    public void listarPecas() {
        if (pecas.isEmpty()) { System.out.println("Nenhuma peça no estoque."); return; }
        System.out.println("--- INVENTÁRIO DE PEÇAS ---");
        for (Peca p : pecas) {
            System.out.println("[" + p.getId() + "] " + p.getNome() + " | Qtd em estoque: " + p.getQuantidadeEstoque() + " | Valor: R$ " + p.getValorUnitario());
        }
    }

    // --- PERSISTÊNCIA ---
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