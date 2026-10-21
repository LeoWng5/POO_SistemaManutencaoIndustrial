import classes.*;
import exception.*;
import service.SistemaService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class App extends JFrame {

    private SistemaService service = new SistemaService();

    // Modelos das Tabelas
    private DefaultTableModel modeloMaquinas;
    private DefaultTableModel modeloTecnicos;
    private DefaultTableModel modeloPecas;
    private DefaultTableModel modeloManutencoes;

    public App() {
        service.carregar();

        setTitle("🏭 Sistema de Gestão de Manutenção Industrial");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- PAINEL ESQUERDO (BOTÕES) ---
        JPanel painelBotoes = new JPanel();
        painelBotoes.setLayout(new GridLayout(12, 1, 5, 5)); // 12 linhas, 1 coluna
        painelBotoes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        painelBotoes.setPreferredSize(new Dimension(220, 0));

        JButton btnCadMaquina = new JButton("Cadastrar Máquina");
        JButton btnCadTecnico = new JButton("Cadastrar Técnico");
        JButton btnCadPeca = new JButton("Cadastrar Peça");
        JButton btnExcluirMaquina = new JButton("Excluir Máquina");
        JButton btnRegistrarFalha = new JButton("Registrar Falha");
        JButton btnAbrirManutencao = new JButton("Abrir Manutenção");
        JButton btnAtribuirTecnico = new JButton("Atribuir Técnico");
        JButton btnAdicionarPeca = new JButton("Usar Peça na Manutenção");
        JButton btnConcluirManutencao = new JButton("Concluir Manutenção");
        JButton btnAtualizar = new JButton("🔄 Atualizar Tabelas");
        JButton btnSalvar = new JButton("💾 Salvar Dados");

        // Adicionando ações aos botões
        btnCadMaquina.addActionListener(e -> cadastrarMaquina());
        btnCadTecnico.addActionListener(e -> cadastrarTecnico());
        btnCadPeca.addActionListener(e -> cadastrarPeca());
        btnExcluirMaquina.addActionListener(e -> excluirMaquina());
        btnRegistrarFalha.addActionListener(e -> registrarFalha());
        btnAbrirManutencao.addActionListener(e -> abrirManutencao());
        btnAtribuirTecnico.addActionListener(e -> atribuirTecnico());
        btnAdicionarPeca.addActionListener(e -> adicionarPecaManutencao());
        btnConcluirManutencao.addActionListener(e -> concluirManutencao());
        btnAtualizar.addActionListener(e -> atualizarTabelas());
        btnSalvar.addActionListener(e -> {
            service.salvar();
            JOptionPane.showMessageDialog(this, "Dados salvos com sucesso!");
        });

        painelBotoes.add(btnCadMaquina);
        painelBotoes.add(btnCadTecnico);
        painelBotoes.add(btnCadPeca);
        painelBotoes.add(btnExcluirMaquina);
        painelBotoes.add(btnRegistrarFalha);
        painelBotoes.add(btnAbrirManutencao);
        painelBotoes.add(btnAtribuirTecnico);
        painelBotoes.add(btnAdicionarPeca);
        painelBotoes.add(btnConcluirManutencao);
        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnSalvar);

        // --- PAINEL DIREITO (TABELAS) ---
        JTabbedPane abas = new JTabbedPane();

        // Tabela de Máquinas
        modeloMaquinas = new DefaultTableModel(new String[]{"ID", "Nome", "Modelo", "Fabricante", "Status"}, 0);
        abas.addTab("Máquinas", new JScrollPane(new JTable(modeloMaquinas)));

        // Tabela de Técnicos
        modeloTecnicos = new DefaultTableModel(new String[]{"ID", "Nome", "Especialidade", "Telefone", "Status"}, 0);
        abas.addTab("Técnicos", new JScrollPane(new JTable(modeloTecnicos)));

        // Tabela de Peças
        modeloPecas = new DefaultTableModel(new String[]{"ID", "Nome", "Categoria", "Qtd Estoque", "Valor (R$)"}, 0);
        abas.addTab("Estoque de Peças", new JScrollPane(new JTable(modeloPecas)));

        // Tabela de Manutenções
        modeloManutencoes = new DefaultTableModel(new String[]{"ID", "Máquina", "Técnico", "Status", "Custo Total (R$)"}, 0);
        abas.addTab("Manutenções", new JScrollPane(new JTable(modeloManutencoes)));

        // Adicionando os painéis à janela principal
        add(painelBotoes, BorderLayout.WEST);
        add(abas, BorderLayout.CENTER);

        // Carrega os dados nas tabelas logo ao abrir
        atualizarTabelas();
    }

    // --- MÉTODO PARA PUXAR OS DADOS E ATUALIZAR AS TABELAS ---
    private void atualizarTabelas() {
        // Limpa as tabelas
        modeloMaquinas.setRowCount(0);
        modeloTecnicos.setRowCount(0);
        modeloPecas.setRowCount(0);
        modeloManutencoes.setRowCount(0);

        // Preenche Máquinas
        for (Maquina m : service.getMaquinas()) {
            modeloMaquinas.addRow(new Object[]{m.getId(), m.getNome(), m.getModelo(), m.getFabricante(), m.getStatus()});
        }

        // Preenche Técnicos
        for (Tecnico t : service.getTecnicos()) {
            modeloTecnicos.addRow(new Object[]{t.getId(), t.getNome(), t.getEspecialidade(), t.getTelefone(), t.isDisponivel() ? "DISPONÍVEL" : "OCUPADO"});
        }

        // Preenche Peças
        for (Peca p : service.getPecas()) {
            modeloPecas.addRow(new Object[]{p.getId(), p.getNome(), p.getCategoria(), p.getQuantidadeEstoque(), String.format("%.2f", p.getValorUnitario())});
        }

        // Preenche Manutenções
        for (Manutencao m : service.getManutencoes()) {
            String nomeMaquina = (m.getMaquina() != null) ? m.getMaquina().getNome() : "N/A";
            String nomeTecnico = (m.getTecnico() != null) ? m.getTecnico().getNome() : "Não atribuído";
            modeloManutencoes.addRow(new Object[]{m.getId(), nomeMaquina, nomeTecnico, m.getStatus(), String.format("%.2f", m.getCusto())});
        }
    }

    // --- MÉTODOS DE AÇÃO (JANELAS DE CADASTRO) ---
    
    private void cadastrarMaquina() {
        String id = JOptionPane.showInputDialog(this, "ID da Máquina (ex: M1):");
        if (id == null || id.isEmpty()) return;
        String nome = JOptionPane.showInputDialog(this, "Nome da Máquina:");
        if (nome == null) return;
        String modelo = JOptionPane.showInputDialog(this, "Modelo:");
        if (modelo == null) return;
        String fabricante = JOptionPane.showInputDialog(this, "Fabricante:");
        if (fabricante == null) return;

        service.cadastrarMaquina(new Maquina(id, nome, "ATIVA", modelo, fabricante, LocalDate.now()));
        atualizarTabelas(); // Atualiza na hora!
    }

    private void cadastrarTecnico() {
        String id = JOptionPane.showInputDialog(this, "ID do Técnico (ex: T1):");
        if (id == null || id.isEmpty()) return;
        String nome = JOptionPane.showInputDialog(this, "Nome do Técnico:");
        if (nome == null) return;
        String especialidade = JOptionPane.showInputDialog(this, "Especialidade:");
        if (especialidade == null) return;
        String telefone = JOptionPane.showInputDialog(this, "Telefone:");
        if (telefone == null) return;

        service.cadastrarTecnico(new Tecnico(id, nome, especialidade, telefone));
        atualizarTabelas();
    }

    private void cadastrarPeca() {
        try {
            String id = JOptionPane.showInputDialog(this, "ID da Peça (ex: P1):");
            if (id == null || id.isEmpty()) return;
            String nome = JOptionPane.showInputDialog(this, "Nome da Peça:");
            if (nome == null) return;
            int qtd = Integer.parseInt(JOptionPane.showInputDialog(this, "Quantidade em estoque:"));
            double valor = Double.parseDouble(JOptionPane.showInputDialog(this, "Valor unitário (R$):"));

            service.cadastrarPeca(new Peca(id, nome, "Descrição", "Geral", qtd, 5, valor));
            atualizarTabelas();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Erro: Digite números válidos.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirMaquina() {
        String id = JOptionPane.showInputDialog(this, "ID da Máquina que deseja excluir:");
        if (id == null) return;

        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja excluir a máquina " + id + "?", "Atenção", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (service.excluirMaquina(id)) {
                JOptionPane.showMessageDialog(this, "Máquina excluída!");
                atualizarTabelas();
            } else {
                JOptionPane.showMessageDialog(this, "Máquina não encontrada.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void registrarFalha() {
        String idMaquina = JOptionPane.showInputDialog(this, "ID da Máquina quebrada:");
        if (idMaquina == null) return;
        String descricao = JOptionPane.showInputDialog(this, "O que aconteceu?");
        if (descricao == null) return;

        try {
            service.registrarFalha(descricao, idMaquina);
            JOptionPane.showMessageDialog(this, "Falha registrada! Máquina paralisada.");
            atualizarTabelas();
        } catch (MaquinaInexistenteException e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirManutencao() {
        String idMaquina = JOptionPane.showInputDialog(this, "ID da Máquina:");
        if (idMaquina == null) return;
        String descricao = JOptionPane.showInputDialog(this, "Descrição do serviço:");
        if (descricao == null) return;
        String causa = JOptionPane.showInputDialog(this, "Causa raiz:");
        if (causa == null) return;

        try {
            service.abrirCorretiva(descricao, idMaquina, causa, "ALTA");
            JOptionPane.showMessageDialog(this, "Ordem de serviço aberta!");
            atualizarTabelas();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atribuirTecnico() {
        String idManutencao = JOptionPane.showInputDialog(this, "ID da Manutenção:");
        if (idManutencao == null) return;
        String idTecnico = JOptionPane.showInputDialog(this, "ID do Técnico:");
        if (idTecnico == null) return;

        try {
            service.atribuirTecnico(idManutencao, idTecnico);
            JOptionPane.showMessageDialog(this, "Técnico alocado!");
            atualizarTabelas();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void adicionarPecaManutencao() {
        try {
            String idManutencao = JOptionPane.showInputDialog(this, "ID da Manutenção:");
            if (idManutencao == null) return;
            String idPeca = JOptionPane.showInputDialog(this, "ID da Peça:");
            if (idPeca == null) return;
            int qtd = Integer.parseInt(JOptionPane.showInputDialog(this, "Quantidade utilizada:"));

            service.adicionarPecaNaManutencao(idManutencao, idPeca, qtd);
            JOptionPane.showMessageDialog(this, "Peça adicionada e estoque atualizado!");
            atualizarTabelas();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Erro: A quantidade deve ser um número.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void concluirManutencao() {
        String idManutencao = JOptionPane.showInputDialog(this, "ID da Manutenção:");
        if (idManutencao == null) return;

        try {
            service.concluirManutencao(idManutencao);
            JOptionPane.showMessageDialog(this, "Manutenção concluída!");
            atualizarTabelas();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Inicia a interface gráfica na thread correta do Swing
        SwingUtilities.invokeLater(() -> {
            App app = new App();
            app.setVisible(true);
        });
    }
}