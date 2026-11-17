import classes.*;
import exception.*;
import service.SistemaService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class App extends JFrame {

    private SistemaService service;

    // Modelos das Tabelas
    private DefaultTableModel modeloMaquinas;
    private DefaultTableModel modeloTecnicos;
    private DefaultTableModel modeloPecas;
    private DefaultTableModel modeloManutencoes;

    public App() {
        // Cria/abre o banco de dados (manutencao.db) e garante que as tabelas existem
        try {
            service = new SistemaService();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(null,
                    "Erro ao iniciar o banco de dados:\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        setTitle("🏭 Sistema de Gestão de Manutenção Industrial");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ==========================================
        // BARRA DE MENUS (TOPO) COM A OPÇÃO DE AJUDA
        // ==========================================
        JMenuBar barraMenu = new JMenuBar();
        JMenu menuAjuda = new JMenu("Ajuda");

        JMenuItem itemTutorial = new JMenuItem("Como usar o sistema (?)");
        itemTutorial.addActionListener(e -> exibirTutorial());
        menuAjuda.add(itemTutorial);

        JMenuItem itemSobre = new JMenuItem("Sobre");
        itemSobre.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Sistema de Gestão de Manutenção Industrial\nVersão 2.0 (banco de dados SQLite)\n\nDesenvolvido para a disciplina de POO\nEngenharia de Controle e Automação - UNESP",
                "Sobre", JOptionPane.INFORMATION_MESSAGE));
        menuAjuda.add(itemSobre);

        barraMenu.add(menuAjuda);
        setJMenuBar(barraMenu);

        // ==========================================
        // PAINEL ESQUERDO (BOTÕES)
        // ==========================================
        JPanel painelBotoes = new JPanel();
        painelBotoes.setLayout(new GridLayout(14, 1, 5, 5));
        painelBotoes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        painelBotoes.setPreferredSize(new Dimension(240, 0));

        JButton btnCadTipoMaquina = new JButton("Cadastrar Tipo de Máquina");
        JButton btnCadTipoTecnico = new JButton("Cadastrar Tipo de Técnico");
        JButton btnCadMaquina = new JButton("Cadastrar Máquina");
        JButton btnCadTecnico = new JButton("Cadastrar Técnico");
        JButton btnCadPeca = new JButton("Cadastrar Peça");
        JButton btnExcluirMaquina = new JButton("Excluir Máquina");
        JButton btnRegistrarFalha = new JButton("Registrar Falha");
        JButton btnAbrirManutencao = new JButton("Abrir Manutenção Corretiva");
        JButton btnAbrirPreventiva = new JButton("Abrir Manutenção Preventiva");
        JButton btnAtribuirTecnico = new JButton("Atribuir Técnico");
        JButton btnAdicionarPeca = new JButton("Usar Peça na Manutenção");
        JButton btnConcluirManutencao = new JButton("Concluir Manutenção");
        JButton btnAtualizar = new JButton("🔄 Atualizar Tabelas");

        // Ações
        btnCadTipoMaquina.addActionListener(e -> cadastrarTipoMaquina());
        btnCadTipoTecnico.addActionListener(e -> cadastrarTipoTecnico());
        btnCadMaquina.addActionListener(e -> cadastrarMaquina());
        btnCadTecnico.addActionListener(e -> cadastrarTecnico());
        btnCadPeca.addActionListener(e -> cadastrarPeca());
        btnExcluirMaquina.addActionListener(e -> excluirMaquina());
        btnRegistrarFalha.addActionListener(e -> registrarFalha());
        btnAbrirManutencao.addActionListener(e -> abrirManutencao());
        btnAbrirPreventiva.addActionListener(e -> abrirPreventiva());
        btnAtribuirTecnico.addActionListener(e -> atribuirTecnico());
        btnAdicionarPeca.addActionListener(e -> adicionarPecaManutencao());
        btnConcluirManutencao.addActionListener(e -> concluirManutencao());
        btnAtualizar.addActionListener(e -> atualizarTabelas());

        painelBotoes.add(btnCadTipoMaquina);
        painelBotoes.add(btnCadTipoTecnico);
        painelBotoes.add(btnCadMaquina);
        painelBotoes.add(btnCadTecnico);
        painelBotoes.add(btnCadPeca);
        painelBotoes.add(btnExcluirMaquina);
        painelBotoes.add(btnRegistrarFalha);
        painelBotoes.add(btnAbrirManutencao);
        painelBotoes.add(btnAbrirPreventiva);
        painelBotoes.add(btnAtribuirTecnico);
        painelBotoes.add(btnAdicionarPeca);
        painelBotoes.add(btnConcluirManutencao);
        painelBotoes.add(btnAtualizar);

        // ==========================================
        // PAINEL DIREITO (TABELAS)
        // ==========================================
        JTabbedPane abas = new JTabbedPane();

        modeloMaquinas = new DefaultTableModel(new String[]{"ID", "Nº Série", "Tipo", "Fabricante", "Status"}, 0);
        abas.addTab("Máquinas", new JScrollPane(new JTable(modeloMaquinas)));

        modeloTecnicos = new DefaultTableModel(new String[]{"ID", "Nome", "Cargo", "Especialidade", "Valor/hora (R$)", "Telefone", "Status"}, 0);
        abas.addTab("Técnicos", new JScrollPane(new JTable(modeloTecnicos)));

        modeloPecas = new DefaultTableModel(new String[]{"ID", "Nome", "Categoria", "Qtd Estoque", "Valor (R$)"}, 0);
        abas.addTab("Estoque de Peças", new JScrollPane(new JTable(modeloPecas)));

        modeloManutencoes = new DefaultTableModel(new String[]{"ID", "Tipo", "Máquina", "Técnico", "Status", "Custo Total (R$)"}, 0);
        abas.addTab("Manutenções", new JScrollPane(new JTable(modeloManutencoes)));

        add(painelBotoes, BorderLayout.WEST);
        add(abas, BorderLayout.CENTER);

        atualizarTabelas();
    }

    // ==========================================
    // MÉTODO DO TUTORIAL (O "?")
    // ==========================================
    private void exibirTutorial() {
        String textoAjuda =
            "BEM-VINDO AO SISTEMA DE MANUTENÇÃO INDUSTRIAL!\n" +
            "==================================================\n\n" +
            "Os dados ficam gravados no banco de dados (manutencao.db).\n" +
            "Os IDs são gerados pelo sistema: consulte-os nas tabelas.\n\n" +
            "1. MOLDES (primeiro de tudo):\n" +
            "   - Cadastre um Tipo de Máquina (nome e fabricante padrão).\n" +
            "   - Cadastre um Tipo de Técnico (cargo, especialidade e valor da hora).\n\n" +
            "2. CADASTROS BÁSICOS:\n" +
            "   - Cadastre a Máquina (escolha o tipo e informe o nº de série).\n" +
            "   - Cadastre o Técnico (escolha o tipo e informe nome e telefone).\n" +
            "   - Cadastre a Peça (nome, quantidade em estoque e valor).\n\n" +
            "3. REGISTRAR PROBLEMA:\n" +
            "   - Clique em 'Registrar Falha' e informe o ID da máquina.\n" +
            "   - A máquina ficará com o status 'QUEBRADA'.\n\n" +
            "4. ABRIR MANUTENÇÃO:\n" +
            "   - Corretiva (após uma falha) ou Preventiva (com periodicidade).\n" +
            "   - O sistema gera o ID da manutenção.\n\n" +
            "5. ALOCAR TÉCNICO:\n" +
            "   - Informe o ID da manutenção e o ID do técnico.\n" +
            "   - O técnico ficará 'OCUPADO'.\n\n" +
            "6. USAR PEÇAS (opcional):\n" +
            "   - Informe o ID da manutenção, o ID da peça e a quantidade.\n" +
            "   - O estoque será reduzido automaticamente!\n\n" +
            "7. CONCLUIR MANUTENÇÃO:\n" +
            "   - Informe o ID da manutenção.\n" +
            "   - O custo total será calculado.\n" +
            "   - A máquina volta a 'ATIVA' e o técnico a 'DISPONÍVEL'.\n\n" +
            "DICA: tudo é gravado no banco na hora. Use 'Atualizar Tabelas' para recarregar a tela.";

        JTextArea textArea = new JTextArea(textoAjuda);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(580, 440));

        JOptionPane.showMessageDialog(this, scrollPane, "Tutorial de Uso - Ajuda", JOptionPane.INFORMATION_MESSAGE);
    }

    private void atualizarTabelas() {
        try {
            modeloMaquinas.setRowCount(0);
            modeloTecnicos.setRowCount(0);
            modeloPecas.setRowCount(0);
            modeloManutencoes.setRowCount(0);

            for (Maquina m : service.getMaquinas()) {
                modeloMaquinas.addRow(new Object[]{m.getId(), m.getNumeroSerie(), m.getNome(), m.getFabricante(), m.getStatus()});
            }
            for (Tecnico t : service.getTecnicos()) {
                modeloTecnicos.addRow(new Object[]{t.getId(), t.getNome(), t.getCargo(), t.getEspecialidade(),
                        String.format("%.2f", t.getValorHora()), t.getTelefone(), t.isDisponivel() ? "DISPONÍVEL" : "OCUPADO"});
            }
            for (Peca p : service.getPecas()) {
                modeloPecas.addRow(new Object[]{p.getId(), p.getNome(), p.getCategoria(), p.getQuantidadeEstoque(), String.format("%.2f", p.getValorUnitario())});
            }
            for (Manutencao m : service.getManutencoes()) {
                String nomeMaquina = (m.getMaquina() != null) ? m.getMaquina().toString() : "N/A";
                String nomeTecnico = (m.getTecnico() != null) ? m.getTecnico().getNome() : "Não atribuído";
                modeloManutencoes.addRow(new Object[]{m.getId(), m.getTipo(), nomeMaquina, nomeTecnico, m.getStatus(), String.format("%.2f", m.getCusto())});
            }
        } catch (RuntimeException e) {
            mostrarErro(e);
        }
    }

    // --- FUNÇÕES AUXILIARES ---
    private void mostrarErro(Exception e) {
        String msg;
        if (e instanceof NumberFormatException) {
            msg = "Digite um número válido.";
        } else {
            msg = e.getMessage();
        }
        JOptionPane.showMessageDialog(this, "Erro: " + msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    // Lê um número inteiro digitado; devolve null se o usuário cancelar
    private Integer lerInteiro(String mensagem) {
        String texto = JOptionPane.showInputDialog(this, mensagem);
        if (texto == null) return null;
        return Integer.parseInt(texto.trim());
    }

    // Lê um número decimal (aceita vírgula); devolve null se o usuário cancelar
    private Double lerDecimal(String mensagem) {
        String texto = JOptionPane.showInputDialog(this, mensagem);
        if (texto == null) return null;
        return Double.parseDouble(texto.trim().replace(',', '.'));
    }

    // --- MÉTODOS DE AÇÃO (JANELAS DE CADASTRO) ---
    private void cadastrarTipoMaquina() {
        try {
            String nome = JOptionPane.showInputDialog(this, "Nome do tipo de máquina (ex: Torno CNC):");
            if (nome == null || nome.isBlank()) return;
            String fabricante = JOptionPane.showInputDialog(this, "Fabricante padrão:");
            if (fabricante == null) return;

            service.cadastrarTipoMaquina(new TipoMaquina(0, nome.trim(), fabricante.trim()));
            JOptionPane.showMessageDialog(this, "Tipo de máquina cadastrado!");
        } catch (RuntimeException e) {
            mostrarErro(e);
        }
    }

    private void cadastrarTipoTecnico() {
        try {
            String cargo = JOptionPane.showInputDialog(this, "Cargo (ex: Mecânico Sênior):");
            if (cargo == null || cargo.isBlank()) return;
            String especialidade = JOptionPane.showInputDialog(this, "Especialidade:");
            if (especialidade == null) return;
            Double valorHora = lerDecimal("Valor da hora de trabalho (R$):");
            if (valorHora == null) return;

            service.cadastrarTipoTecnico(new TipoTecnico(0, cargo.trim(), especialidade.trim(), valorHora));
            JOptionPane.showMessageDialog(this, "Tipo de técnico cadastrado!");
        } catch (RuntimeException e) {
            mostrarErro(e);
        }
    }

    private void cadastrarMaquina() {
        try {
            List<TipoMaquina> tipos = service.getTiposMaquina();
            if (tipos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cadastre um Tipo de Máquina antes.", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Object escolhido = JOptionPane.showInputDialog(this, "Tipo da máquina:", "Cadastrar Máquina",
                    JOptionPane.QUESTION_MESSAGE, null, tipos.toArray(), tipos.get(0));
            if (escolhido == null) return;
            String serie = JOptionPane.showInputDialog(this, "Número de série:");
            if (serie == null || serie.isBlank()) return;

            service.cadastrarMaquina(new Maquina(0, serie.trim(), "ATIVA", LocalDate.now(), (TipoMaquina) escolhido));
            atualizarTabelas();
        } catch (RuntimeException e) {
            mostrarErro(e);
        }
    }

    private void cadastrarTecnico() {
        try {
            List<TipoTecnico> tipos = service.getTiposTecnico();
            if (tipos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cadastre um Tipo de Técnico antes.", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Object escolhido = JOptionPane.showInputDialog(this, "Tipo do técnico:", "Cadastrar Técnico",
                    JOptionPane.QUESTION_MESSAGE, null, tipos.toArray(), tipos.get(0));
            if (escolhido == null) return;
            String nome = JOptionPane.showInputDialog(this, "Nome do Técnico:");
            if (nome == null || nome.isBlank()) return;
            String telefone = JOptionPane.showInputDialog(this, "Telefone:");
            if (telefone == null) return;

            service.cadastrarTecnico(new Tecnico(0, nome.trim(), telefone.trim(), (TipoTecnico) escolhido));
            atualizarTabelas();
        } catch (RuntimeException e) {
            mostrarErro(e);
        }
    }

    private void cadastrarPeca() {
        try {
            String nome = JOptionPane.showInputDialog(this, "Nome da Peça:");
            if (nome == null || nome.isBlank()) return;
            Integer qtd = lerInteiro("Quantidade em estoque:");
            if (qtd == null) return;
            Double valor = lerDecimal("Valor unitário (R$):");
            if (valor == null) return;

            service.cadastrarPeca(new Peca(0, nome.trim(), "Descrição", "Geral", qtd, 5, valor));
            atualizarTabelas();
        } catch (RuntimeException e) {
            mostrarErro(e);
        }
    }

    private void excluirMaquina() {
        try {
            Integer id = lerInteiro("ID da Máquina que deseja excluir:");
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
        } catch (RuntimeException e) {
            mostrarErro(e);
        }
    }

    private void registrarFalha() {
        try {
            Integer idMaquina = lerInteiro("ID da Máquina quebrada:");
            if (idMaquina == null) return;
            String descricao = JOptionPane.showInputDialog(this, "O que aconteceu?");
            if (descricao == null) return;

            service.registrarFalha(descricao, idMaquina);
            JOptionPane.showMessageDialog(this, "Falha registrada! Máquina paralisada.");
            atualizarTabelas();
        } catch (MaquinaInexistenteException | RuntimeException e) {
            mostrarErro(e);
        }
    }

    private void abrirManutencao() {
        try {
            Integer idMaquina = lerInteiro("ID da Máquina:");
            if (idMaquina == null) return;
            String descricao = JOptionPane.showInputDialog(this, "Descrição do serviço:");
            if (descricao == null) return;
            String causa = JOptionPane.showInputDialog(this, "Causa raiz:");
            if (causa == null) return;

            int idManutencao = service.abrirCorretiva(descricao, idMaquina, causa, "ALTA");
            JOptionPane.showMessageDialog(this, "Ordem de serviço aberta! ID da manutenção: " + idManutencao);
            atualizarTabelas();
        } catch (MaquinaInexistenteException | MaquinaInativaException | RuntimeException e) {
            mostrarErro(e);
        }
    }

    private void abrirPreventiva() {
        try {
            Integer idMaquina = lerInteiro("ID da Máquina:");
            if (idMaquina == null) return;
            String descricao = JOptionPane.showInputDialog(this, "Descrição do serviço:");
            if (descricao == null) return;
            Integer dias = lerInteiro("Periodicidade (em dias):");
            if (dias == null) return;

            int idManutencao = service.abrirPreventiva(descricao, idMaquina, dias);
            JOptionPane.showMessageDialog(this, "Manutenção preventiva aberta! ID da manutenção: " + idManutencao);
            atualizarTabelas();
        } catch (MaquinaInexistenteException | MaquinaInativaException | RuntimeException e) {
            mostrarErro(e);
        }
    }

    private void atribuirTecnico() {
        try {
            Integer idManutencao = lerInteiro("ID da Manutenção:");
            if (idManutencao == null) return;
            Integer idTecnico = lerInteiro("ID do Técnico:");
            if (idTecnico == null) return;

            service.atribuirTecnico(idManutencao, idTecnico);
            JOptionPane.showMessageDialog(this, "Técnico alocado!");
            atualizarTabelas();
        } catch (TecnicoIndisponivelException | ManutencaoInexistenteException
                 | ManutencaoFinalizadaException | RuntimeException e) {
            mostrarErro(e);
        }
    }

    private void adicionarPecaManutencao() {
        try {
            Integer idManutencao = lerInteiro("ID da Manutenção:");
            if (idManutencao == null) return;
            Integer idPeca = lerInteiro("ID da Peça:");
            if (idPeca == null) return;
            Integer qtd = lerInteiro("Quantidade utilizada:");
            if (qtd == null) return;

            service.adicionarPecaNaManutencao(idManutencao, idPeca, qtd);
            JOptionPane.showMessageDialog(this, "Peça adicionada e estoque atualizado!");
            atualizarTabelas();
        } catch (EstoqueInsuficienteException | ManutencaoInexistenteException
                 | ManutencaoFinalizadaException | PecaInexistenteException | RuntimeException e) {
            mostrarErro(e);
        }
    }

    private void concluirManutencao() {
        try {
            Integer idManutencao = lerInteiro("ID da Manutenção:");
            if (idManutencao == null) return;

            double custo = service.concluirManutencao(idManutencao);
            JOptionPane.showMessageDialog(this, "Manutenção concluída!\nCusto total: R$ " + String.format("%.2f", custo));
            atualizarTabelas();
        } catch (ManutencaoInexistenteException | ManutencaoFinalizadaException | RuntimeException e) {
            mostrarErro(e);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            App app = new App();
            app.setVisible(true);
        });
    }
}
