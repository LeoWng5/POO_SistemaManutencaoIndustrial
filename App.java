import classes.*;
import exception.*;
import service.*;
import service.SistemaService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.File;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Interface Gráfica 2.0 — Dashboard estilo ERP / Office Ribbon.
 * Apenas camada de visualização: todo o backend (Service/DAO) é reaproveitado.
 */
public class App extends JFrame {

    // ============================================================
    // PALETA E FONTES (identidade visual "Office/ERP")
    // ============================================================
    static final Color BG_APP    = new Color(0xF4F6F9);
    static final Color BG_CARD   = Color.WHITE;
    static final Color PRIMARY   = new Color(0x1E5F9E);
    static final Color PRIMARY_D = new Color(0x154975);
    static final Color ACCENT    = new Color(0xE67E22);
    static final Color DANGER    = new Color(0xE74C3C);
    static final Color SUCCESS   = new Color(0x27AE60);
    static final Color TEXT      = new Color(0x2C3E50);
    static final Color MUTED     = new Color(0x95A5A6);
    static final Color BORDER    = new Color(0xDDE2E8);
    static final Color TAB_BG    = new Color(0xEDF1F6);

    static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    static final Font F_SUBT  = new Font("Segoe UI", Font.BOLD, 13);
    static final Font F_BODY  = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font F_BIG   = new Font("Segoe UI", Font.BOLD, 34);

    // ============================================================
    // ESTADO
    // ============================================================
    private SistemaService service;

    private JPanel painelCentral;
    private CardLayout cardCentral;

    private DashboardPanel dashboardPanel;
    private JPanel painelPreditiva;
    private JPanel painelPreditivaLista;
    private JPanel painelTabelas;

    private DefaultTableModel modeloMaquinas;
    private DefaultTableModel modeloTecnicos;
    private DefaultTableModel modeloPecas;
    private DefaultTableModel modeloManutencoes;

    // ============================================================
    // CONSTRUTOR
    // ============================================================
    public App() {
        try {
            service = new SistemaService();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(null,
                "Erro ao iniciar o banco de dados:\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        if (!fazerLogin()) {
            dispose();
            System.exit(0);
            return;
        }

        setTitle("🏭 Gestão de Manutenção Industrial — v2.0");
        setSize(1366, 820);
        setMinimumSize(new Dimension(1150, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_APP);

        JPanel main = buildMain();     // cria cardCentral antes do ribbon
        add(buildRibbon(), BorderLayout.NORTH);
        add(main, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        atualizarTudo();
    }

    // ============================================================
    // LOGIN (RBAC)
    // ============================================================
    private boolean fazerLogin() {
        JDialog dlg = new JDialog((Frame) null, "Acesso ao Sistema", true);
        dlg.setSize(420, 360);
        dlg.setLocationRelativeTo(null);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(BG_CARD);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setPreferredSize(new Dimension(0, 90));
        JLabel t = new JLabel("GESTÃO DE MANUTENÇÃO", SwingConstants.CENTER);
        t.setForeground(Color.WHITE);
        t.setFont(new Font("Segoe UI", Font.BOLD, 20));
        JLabel sub = new JLabel("v2.0 — SQLite", SwingConstants.CENTER);
        sub.setForeground(new Color(0xCCE1F5));
        sub.setFont(F_BODY);
        header.add(t, BorderLayout.CENTER);
        header.add(sub, BorderLayout.SOUTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_CARD);
        form.setBorder(new EmptyBorder(20, 32, 10, 32));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 0);
        g.weightx = 1; g.gridx = 0;

        g.gridy = 0; form.add(new JLabel("Usuário"), g);
        JTextField txtUser = new JTextField();
        g.gridy = 1; form.add(txtUser, g);

        g.gridy = 2; form.add(new JLabel("Senha"), g);
        JPasswordField txtPass = new JPasswordField();
        g.gridy = 3; form.add(txtPass, g);

        JLabel lblErro = new JLabel(" ");
        lblErro.setForeground(DANGER);
        lblErro.setFont(F_BODY);
        g.gridy = 4; form.add(lblErro, g);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        footer.setBackground(BG_CARD);
        JButton btnSair = new JButton("Sair");
        JButton btnEntrar = new JButton("Entrar");
        btnEntrar.setBackground(PRIMARY);
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setFocusPainted(false);
        footer.add(btnSair);
        footer.add(btnEntrar);

        final boolean[] ok = {false};

        Runnable tentar = () -> {
            String u = txtUser.getText().trim();
            String s = new String(txtPass.getPassword());
            if (u.isEmpty() || s.isEmpty()) { lblErro.setText("Informe usuário e senha."); return; }
            try {
                service.realizarLogin(u, s);   // [ADAPTE] se retornar boolean, cheque aqui
                ok[0] = true;
                dlg.dispose();
            } catch (Exception ex) {
                lblErro.setText("Falha: " + ex.getMessage());
            }
        };
        btnEntrar.addActionListener(e -> tentar.run());
        txtPass.addActionListener(e -> tentar.run());
        btnSair.addActionListener(e -> dlg.dispose());

        dlg.add(header, BorderLayout.NORTH);
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
        return ok[0];
    }

    private boolean isAdmin() {
        try { return Sessao.getInstance().isAdmin(); }
        catch (Throwable t) { return false; }
    }

    // ============================================================
    // RIBBON (estilo Office)
    // ============================================================
    private JPanel buildRibbon() {
        JPanel ribbon = new JPanel();
        ribbon.setLayout(new BoxLayout(ribbon, BoxLayout.Y_AXIS));
        ribbon.setBackground(Color.WHITE);
        ribbon.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));

        // faixa de abas
        JPanel tabStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabStrip.setBackground(TAB_BG);

        // área de botões (CardLayout)
        JPanel btnArea = new JPanel(new CardLayout());
        btnArea.setBackground(BG_CARD);
        btnArea.setPreferredSize(new Dimension(0, 92));

        String[] tabs = {"Início", "Cadastros", "Manutenção", "Estoque", "Administração"};
        JButton[] tabBtns = new JButton[tabs.length];

        for (int i = 0; i < tabs.length; i++) {
            final String nome = tabs[i];
            JButton tb = new JButton(nome);
            tb.setFont(F_SUBT);
            tb.setFocusPainted(false);
            tb.setBorder(new EmptyBorder(8, 18, 8, 18));
            tb.setBackground(TAB_BG);
            tb.setForeground(TEXT);
            tabBtns[i] = tb;
            tabStrip.add(tb);

            btnArea.add(buildRibbonPage(nome), nome);

            tb.addActionListener(e -> {
                ((CardLayout) btnArea.getLayout()).show(btnArea, nome);
                for (JButton x : tabBtns) {
                    x.setBackground(TAB_BG);
                    x.setForeground(TEXT);
                }
                tb.setBackground(Color.WHITE);
                tb.setForeground(PRIMARY);
                if (nome.equals("Início") && cardCentral != null) {
                    cardCentral.show(painelCentral, "DASH");
                }
            });
        }
        tabBtns[0].setBackground(Color.WHITE);
        tabBtns[0].setForeground(PRIMARY);

        ribbon.add(tabStrip);
        ribbon.add(btnArea);
        return ribbon;
    }

    private JPanel buildRibbonPage(String tab) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        p.setBackground(BG_CARD);

        switch (tab) {
            case "Início" -> {
                p.add(ribbonBtn("🏠 Dashboard", e -> cardCentral.show(painelCentral, "DASH")));
                p.add(ribbonBtn("📋 Dados", e -> cardCentral.show(painelCentral, "DADOS")));
                p.add(ribbonBtn("⚠ Preditiva", e -> cardCentral.show(painelCentral, "PREDITIVA")));
                p.add(ribbonBtn("🔄 Atualizar Tudo", e -> atualizarTudo()));
                p.add(ribbonBtn("❓ Tutorial", e -> exibirTutorial()));
            }
            case "Cadastros" -> {
                p.add(ribbonBtn("Tipo de Máquina", e -> cadastrarTipoMaquina()));
                p.add(ribbonBtn("Tipo de Técnico", e -> cadastrarTipoTecnico()));
                p.add(ribbonBtn("Máquina", e -> cadastrarMaquina()));
                p.add(ribbonBtn("Técnico", e -> cadastrarTecnico()));
                p.add(ribbonBtn("Peça", e -> cadastrarPeca()));
            }
            case "Manutenção" -> {
                p.add(ribbonBtn("Registrar Falha", e -> registrarFalha()));
                p.add(ribbonBtn("Abrir Corretiva", e -> abrirManutencao()));
                p.add(ribbonBtn("Abrir Preventiva", e -> abrirPreventiva()));
                p.add(ribbonBtn("Atribuir Técnico", e -> atribuirTecnico()));
                p.add(ribbonBtn("Usar Peça", e -> adicionarPecaManutencao()));
                p.add(ribbonBtn("Concluir", e -> concluirManutencao()));
            }
            case "Estoque" -> {
                p.add(ribbonBtn("Cadastrar Peça", e -> cadastrarPeca()));
                p.add(ribbonBtn("Ver Estoque", e -> cardCentral.show(painelCentral, "DADOS")));
                p.add(ribbonBtn("Relatório de Custo", e -> abrirRelatorioCusto()));
            }
            case "Administração" -> {
                if (isAdmin()) {
                    p.add(ribbonBtn("Excluir Máquina", e -> excluirMaquina()));
                    p.add(ribbonBtn("Relatório Histórico", e -> abrirRelatorioHistorico()));
                } else {
                    JLabel l = new JLabel("⚠ Acesso restrito ao perfil ADMINISTRADOR.");
                    l.setForeground(MUTED);
                    l.setFont(F_BODY);
                    p.add(l);
                }
            }
        }
        return p;
    }

    private JButton ribbonBtn(String text, ActionListener a) {
        JButton b = new JButton(text);
        b.setFont(F_BODY);
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(10, 14, 10, 14)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(a);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(0xEEF3F9)); }
            public void mouseExited(MouseEvent e) { b.setBackground(Color.WHITE); }
        });
        return b;
    }

    // ============================================================
    // ÁREA CENTRAL (CardLayout: DASH | PREDITIVA | DADOS)
    // ============================================================
    private JPanel buildMain() {
        painelCentral = new JPanel();
        cardCentral = new CardLayout();
        painelCentral.setLayout(cardCentral);
        painelCentral.setBackground(BG_APP);

        dashboardPanel = new DashboardPanel();
        painelPreditiva = buildPreditivaPanel();
        painelTabelas = buildTabelasPanel();

        painelCentral.add(dashboardPanel, "DASH");
        painelCentral.add(painelPreditiva, "PREDITIVA");
        painelCentral.add(painelTabelas, "DADOS");

        cardCentral.show(painelCentral, "DASH");
        return painelCentral;
    }

    // ============================================================
    // DASHBOARD
    // ============================================================
    class DashboardPanel extends JPanel {
        private final BarChartPanel chartMaquinas;
        private final BarChartPanel chartTecnicos;
        private final JLabel lblCustoTotal;

        DashboardPanel() {
            setLayout(new BorderLayout(16, 16));
            setBackground(BG_APP);
            setBorder(new EmptyBorder(20, 24, 20, 24));

            JLabel titulo = new JLabel("Dashboard de Manutenção");
            titulo.setFont(F_TITLE);
            titulo.setForeground(TEXT);

            JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
            grid.setOpaque(false);

            chartMaquinas = new BarChartPanel(new String[]{"Operando", "Quebradas"},
                    new Color[]{SUCCESS, DANGER});
            grid.add(wrapCard("Status das Máquinas", chartMaquinas));

            chartTecnicos = new BarChartPanel(new String[]{"Disponíveis", "Ocupados"},
                    new Color[]{SUCCESS, ACCENT});
            grid.add(wrapCard("Equipe Técnica", chartTecnicos));

            // Card de custos
            JPanel custoCard = new JPanel(new BorderLayout(0, 6));
            custoCard.setBackground(BG_CARD);
            custoCard.setBorder(new CompoundBorder(
                    new LineBorder(BORDER, 1, true), new EmptyBorder(16, 20, 16, 20)));
            JLabel lc = new JLabel("Custo Acumulado");
            lc.setFont(F_SUBT); lc.setForeground(MUTED);
            lblCustoTotal = new JLabel("R$ 0,00");
            lblCustoTotal.setFont(F_BIG);
            lblCustoTotal.setForeground(PRIMARY);
            custoCard.add(lc, BorderLayout.NORTH);
            custoCard.add(lblCustoTotal, BorderLayout.CENTER);

            // Card preditivo
            JPanel predCard = new JPanel(new BorderLayout(0, 6));
            predCard.setBackground(BG_CARD);
            predCard.setBorder(new CompoundBorder(
                    new LineBorder(BORDER, 1, true), new EmptyBorder(16, 20, 16, 20)));
            JLabel lp = new JLabel("Alerta Preditivo");
            lp.setFont(F_SUBT); lp.setForeground(MUTED);
            JLabel lpInfo = new JLabel("Veja quais equipamentos estão mais próximos da falha.");
            lpInfo.setFont(F_BODY); lpInfo.setForeground(TEXT);
            JButton btnVer = new JButton("Abrir Lista →");
            btnVer.setBackground(PRIMARY);
            btnVer.setForeground(Color.WHITE);
            btnVer.setFocusPainted(false);
            btnVer.addActionListener(e -> cardCentral.show(painelCentral, "PREDITIVA"));
            predCard.add(lp, BorderLayout.NORTH);
            predCard.add(lpInfo, BorderLayout.CENTER);
            JPanel bwrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            bwrap.setOpaque(false);
            bwrap.add(btnVer);
            predCard.add(bwrap, BorderLayout.SOUTH);

            grid.add(custoCard);
            grid.add(predCard);

            add(titulo, BorderLayout.NORTH);
            add(grid, BorderLayout.CENTER);
        }

        private JPanel wrapCard(String titulo, JComponent conteudo) {
            JPanel c = new JPanel(new BorderLayout(0, 8));
            c.setBackground(BG_CARD);
            c.setBorder(new CompoundBorder(
                    new LineBorder(BORDER, 1, true), new EmptyBorder(16, 20, 16, 20)));
            JLabel t = new JLabel(titulo);
            t.setFont(F_SUBT);
            t.setForeground(TEXT);
            c.add(t, BorderLayout.NORTH);
            c.add(conteudo, BorderLayout.CENTER);
            return c;
        }

        void atualizar() {
            try {
                int operando  = service.getTotalMaquinasPorStatus("ATIVA");
                int quebradas = service.getTotalMaquinasPorStatus("QUEBRADA");
                chartMaquinas.setValores(new int[]{operando, quebradas});

                int disp = service.getTotalTecnicosPorDisponibilidade(true);
                int ocup = service.getTotalTecnicosPorDisponibilidade(false);
                chartTecnicos.setValores(new int[]{disp, ocup});

                double total = 0;
                for (Manutencao m : service.getManutencoes()) total += m.getCusto();

                NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
                lblCustoTotal.setText(nf.format(total));
            } catch (Exception ignore) { }
        }
    }

    /** Gráfico de barras pintado com Graphics2D (sem dependências externas). */
    class BarChartPanel extends JComponent {
        private final String[] labels;
        private final Color[] cores;
        private int[] valores;

        BarChartPanel(String[] labels, Color[] cores) {
            this.labels = labels;
            this.cores = cores;
            this.valores = new int[labels.length];
            setPreferredSize(new Dimension(220, 150));
            setOpaque(false);
        }

        void setValores(int[] v) {
            this.valores = v;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int n = labels.length;
            int max = 1;
            for (int v : valores) max = Math.max(max, v);

            int espaco = 24;
            int barW = (w - espaco * (n + 1)) / n;
            int baseY = h - 26;

            for (int i = 0; i < n; i++) {
                int x = espaco + i * (barW + espaco);
                int barH = (int) ((double) valores[i] / max * (baseY - 30));
                if (barH < 2) barH = 2;

                g2.setColor(cores[i]);
                g2.fill(new RoundRectangle2D.Float(x, baseY - barH, barW, barH, 10, 10));

                g2.setColor(TEXT);
                g2.setFont(F_SUBT);
                String num = String.valueOf(valores[i]);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(num, x + (barW - fm.stringWidth(num)) / 2, baseY - barH - 6);

                g2.setColor(MUTED);
                g2.setFont(F_BODY);
                fm = g2.getFontMetrics();
                g2.drawString(labels[i], x + (barW - fm.stringWidth(labels[i])) / 2, baseY + 18);
            }
            g2.dispose();
        }
    }

    // ============================================================
    // TELA PREDITIVA
    // ============================================================
    private JPanel buildPreditivaPanel() {
        JPanel painel = new JPanel(new BorderLayout(0, 12));
        painel.setBackground(BG_APP);
        painel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel titulo = new JLabel("Manutenção Preditiva — Saúde dos Equipamentos");
        titulo.setFont(F_TITLE);
        titulo.setForeground(TEXT);

        JLabel sub = new JLabel("Equipamentos ordenados do mais crítico para o menos crítico (depreciação em 5 anos).");
        sub.setFont(F_BODY);
        sub.setForeground(MUTED);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(titulo);
        header.add(Box.createRigidArea(new Dimension(0, 4)));
        header.add(sub);

        painelPreditivaLista = new JPanel();
        painelPreditivaLista.setLayout(new BoxLayout(painelPreditivaLista, BoxLayout.Y_AXIS));
        painelPreditivaLista.setBackground(BG_APP);

        JScrollPane scroll = new JScrollPane(painelPreditivaLista);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(BG_APP);

        painel.add(header, BorderLayout.NORTH);
        painel.add(scroll, BorderLayout.CENTER);
        return painel;
    }

    private void atualizarPreditiva() {
        painelPreditivaLista.removeAll();
        try {
            List<Maquina> ms = new ArrayList<>(service.getMaquinas());
            ms.sort(Comparator.comparingDouble(Maquina::getSaudeEquipamento));
            if (ms.isEmpty()) {
                JLabel vazio = new JLabel("Nenhum equipamento cadastrado.");
                vazio.setForeground(MUTED);
                vazio.setFont(F_BODY);
                painelPreditivaLista.add(vazio);
            }
            for (Maquina m : ms) {
                double s = m.getSaudeEquipamento();
                painelPreditivaLista.add(cardPreditiva(m, s));
                painelPreditivaLista.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        } catch (Exception e) {
            mostrarErro(e);
        }
        painelPreditivaLista.revalidate();
        painelPreditivaLista.repaint();
    }

    private JPanel cardPreditiva(Maquina m, double saude) {
        Color cor = saude < 30 ? DANGER : saude < 60 ? ACCENT : SUCCESS;
        String alerta = saude < 30 ? "⚠ Troca iminente" : saude < 60 ? "Monitorar" : "OK";

        JPanel c = new JPanel(new BorderLayout(12, 6));
        c.setBackground(BG_CARD);
        c.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true),
                new EmptyBorder(12, 16, 12, 16)));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));

        JLabel nome = new JLabel(m.getNome() + "   ·   S/N " + m.getNumeroSerie());
        nome.setFont(F_SUBT);
        nome.setForeground(TEXT);

        JProgressBar pb = new JProgressBar(0, 100);
        pb.setValue((int) Math.max(0, Math.min(100, saude)));
        pb.setForeground(cor);
        pb.setBackground(new Color(0xEEEEEE));
        pb.setStringPainted(true);
        pb.setString(String.format("%.0f%% de saúde", saude));
        pb.setBorderPainted(false);

        JLabel lblAlerta = new JLabel(alerta);
        lblAlerta.setForeground(cor);
        lblAlerta.setFont(F_SUBT);

        JPanel dir = new JPanel(new BorderLayout(8, 4));
        dir.setOpaque(false);
        dir.add(lblAlerta, BorderLayout.NORTH);
        dir.add(pb, BorderLayout.CENTER);

        c.add(nome, BorderLayout.WEST);
        c.add(dir, BorderLayout.CENTER);
        return c;
    }

    // ============================================================
    // TABELAS
    // ============================================================
    private JPanel buildTabelasPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_APP);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(F_SUBT);

        modeloMaquinas = new DefaultTableModel(
                new String[]{"ID", "Nº Série", "Tipo", "Fabricante", "Status", "Saúde (%)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabMaq = new JTable(modeloMaquinas);
        tabMaq.setRowHeight(24);
        tabMaq.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tabMaq.getSelectedRow();
                    if (row >= 0) {
                        int id = (int) modeloMaquinas.getValueAt(row, 0);
                        try {
                            service.getMaquinas().stream()
                                .filter(x -> x.getId() == id).findFirst()
                                .ifPresent(App.this::mostrarDetalhesMaquina);
                        } catch (Exception ignore) {}
                    }
                }
            }
        });
        tabs.addTab("Máquinas", new JScrollPane(tabMaq));

        modeloTecnicos = new DefaultTableModel(
                new String[]{"ID", "Nome", "Especialidade", "Telefone", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabTec = new JTable(modeloTecnicos);
        tabTec.setRowHeight(24);
        tabTec.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tabTec.getSelectedRow();
                    if (row >= 0) {
                        int id = (int) modeloTecnicos.getValueAt(row, 0);
                        try {
                            service.getTecnicos().stream()
                                .filter(x -> x.getId() == id).findFirst()
                                .ifPresent(App.this::mostrarDetalhesTecnico);
                        } catch (Exception ignore) {}
                    }
                }
            }
        });
        tabs.addTab("Técnicos", new JScrollPane(tabTec));

        modeloPecas = new DefaultTableModel(
                new String[]{"ID", "Nome", "Categoria", "Qtd. Estoque", "Valor Unit. (R$)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabPec = new JTable(modeloPecas);
        tabPec.setRowHeight(24);
        tabs.addTab("Estoque de Peças", new JScrollPane(tabPec));

        modeloManutencoes = new DefaultTableModel(
                new String[]{"ID", "Tipo", "Máquina", "Técnico", "Status", "Custo (R$)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabMan = new JTable(modeloManutencoes);
        tabMan.setRowHeight(24);
        tabs.addTab("Manutenções", new JScrollPane(tabMan));

        p.add(tabs, BorderLayout.CENTER);
        return p;
    }

    // ============================================================
    // REFRESH GERAL
    // ============================================================
    private void atualizarTudo() {
        atualizarTabelas();
        if (dashboardPanel != null) dashboardPanel.atualizar();
        if (painelPreditivaLista != null) atualizarPreditiva();
    }

    private void atualizarTabelas() {
        try {
            modeloMaquinas.setRowCount(0);
            modeloTecnicos.setRowCount(0);
            modeloPecas.setRowCount(0);
            modeloManutencoes.setRowCount(0);

            for (Maquina m : service.getMaquinas()) {
                modeloMaquinas.addRow(new Object[]{
                    m.getId(), m.getNumeroSerie(), m.getNome(), m.getFabricante(),
                    m.getStatus(), String.format("%.0f%%", m.getSaudeEquipamento())
                });
            }
            for (Tecnico t : service.getTecnicos()) {
                modeloTecnicos.addRow(new Object[]{
                    t.getId(), t.getNome(), t.getEspecialidade(), t.getTelefone(),
                    t.isDisponivel() ? "DISPONÍVEL" : "OCUPADO"
                });
            }
            for (Peca p : service.getPecas()) {
                modeloPecas.addRow(new Object[]{
                    p.getId(), p.getNome(), p.getCategoria(),
                    p.getQuantidadeEstoque(), String.format("%.2f", p.getValorUnitario())
                });
            }
            for (Manutencao m : service.getManutencoes()) {
                String nomeMaq = (m.getMaquina() != null) ? m.getMaquina().toString() : "N/A";
                String nomeTec = (m.getTecnico() != null) ? m.getTecnico().getNome() : "Não atribuído";
                modeloManutencoes.addRow(new Object[]{
                    m.getId(), m.getTipo(), nomeMaq, nomeTec, m.getStatus(),
                    String.format("%.2f", m.getCusto())
                });
            }
        } catch (RuntimeException e) {
            mostrarErro(e);
        }
    }

    // ============================================================
    // STATUS BAR
    // ============================================================
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0xF0F3F7));
        bar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(6, 16, 6, 16)));

        String perfil = isAdmin() ? "ADMINISTRADOR" : "OPERADOR";
        JLabel lbl = new JLabel("👤 Sessão: " + perfil);
        lbl.setFont(F_BODY);
        lbl.setForeground(MUTED);
        bar.add(lbl, BorderLayout.WEST);

        JLabel data = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        data.setFont(F_BODY);
        data.setForeground(MUTED);
        bar.add(data, BorderLayout.EAST);
        return bar;
    }

    // ============================================================
    // HELPERS
    // ============================================================
    private void mostrarErro(Exception e) {
        String msg = (e instanceof NumberFormatException) ? "Digite um número válido." : e.getMessage();
        JOptionPane.showMessageDialog(this, "Erro: " + msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private Integer lerInteiro(String mensagem) {
        String t = JOptionPane.showInputDialog(this, mensagem);
        if (t == null) return null;
        return Integer.parseInt(t.trim());
    }

    private Double lerDecimal(String mensagem) {
        String t = JOptionPane.showInputDialog(this, mensagem);
        if (t == null) return null;
        return Double.parseDouble(t.trim().replace(',', '.'));
    }

    private ImageIcon carregarMiniatura(String path, int w, int h) {
        if (path == null || path.isBlank() || !new File(path).exists()) return null;
        try {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) { return null; }
    }

    private String escolherFoto(Component parent) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Selecione uma foto");
        fc.setFileFilter(new FileNameExtensionFilter("Imagens (jpg, png, gif)", "jpg", "jpeg", "png", "gif"));
        if (fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    // ============================================================
    // DETALHES (miniaturas)
    // ============================================================
    private void mostrarDetalhesMaquina(Maquina m) {
        JDialog d = new JDialog(this, "Detalhes da Máquina", true);
        d.setSize(560, 340);
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(BG_CARD);

        JPanel info = new JPanel(new GridLayout(0, 2, 8, 6));
        info.setBackground(BG_CARD);
        info.setBorder(new EmptyBorder(16, 16, 16, 16));
        info.add(new JLabel("ID:"));        info.add(new JLabel(String.valueOf(m.getId())));
        info.add(new JLabel("Tipo:"));      info.add(new JLabel(m.getNome()));
        info.add(new JLabel("S/N:"));       info.add(new JLabel(m.getNumeroSerie()));
        info.add(new JLabel("Fabricante:"));info.add(new JLabel(m.getFabricante()));
        info.add(new JLabel("Status:"));    info.add(new JLabel(m.getStatus()));
        info.add(new JLabel("Saúde:"));     info.add(new JLabel(String.format("%.0f%%", m.getSaudeEquipamento())));

        JLabel foto = new JLabel("Sem foto", SwingConstants.CENTER);
        foto.setPreferredSize(new Dimension(240, 180));
        foto.setBorder(new LineBorder(BORDER, 1, true));
        foto.setOpaque(true);
        foto.setBackground(new Color(0xF5F7FA));
        try {
            String path = m.getFoto(); // [ADAPTE] se o getter tiver outro nome
            ImageIcon ic = carregarMiniatura(path, 240, 180);
            if (ic != null) { foto.setIcon(ic); foto.setText(""); }
        } catch (Throwable ignore) {}

        d.add(info, BorderLayout.CENTER);
        d.add(foto, BorderLayout.EAST);
        d.setVisible(true);
    }

    private void mostrarDetalhesTecnico(Tecnico t) {
        JDialog d = new JDialog(this, "Detalhes do Técnico", true);
        d.setSize(560, 340);
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(BG_CARD);

        JPanel info = new JPanel(new GridLayout(0, 2, 8, 6));
        info.setBackground(BG_CARD);
        info.setBorder(new EmptyBorder(16, 16, 16, 16));
        info.add(new JLabel("ID:"));           info.add(new JLabel(String.valueOf(t.getId())));
        info.add(new JLabel("Nome:"));         info.add(new JLabel(t.getNome()));
        info.add(new JLabel("Especialidade:"));info.add(new JLabel(t.getEspecialidade()));
        info.add(new JLabel("Telefone:"));     info.add(new JLabel(t.getTelefone()));
        info.add(new JLabel("Disponível:"));   info.add(new JLabel(t.isDisponivel() ? "Sim" : "Não"));

        JLabel foto = new JLabel("Sem foto", SwingConstants.CENTER);
        foto.setPreferredSize(new Dimension(240, 180));
        foto.setBorder(new LineBorder(BORDER, 1, true));
        foto.setOpaque(true);
        foto.setBackground(new Color(0xF5F7FA));
        try {
            String path = t.getFoto(); // [ADAPTE]
            ImageIcon ic = carregarMiniatura(path, 240, 180);
            if (ic != null) { foto.setIcon(ic); foto.setText(""); }
        } catch (Throwable ignore) {}

        d.add(info, BorderLayout.CENTER);
        d.add(foto, BorderLayout.EAST);
        d.setVisible(true);
    }

    // ============================================================
    // AÇÕES — CADASTROS
    // ============================================================
    private void cadastrarTipoMaquina() {
        try {
            String nome = JOptionPane.showInputDialog(this, "Nome do tipo de máquina (ex: Torno CNC):");
            if (nome == null || nome.isBlank()) return;
            String fab = JOptionPane.showInputDialog(this, "Fabricante padrão:");
            if (fab == null) return;
            service.cadastrarTipoMaquina(new TipoMaquina(0, nome.trim(), fab.trim()));
            JOptionPane.showMessageDialog(this, "Tipo de máquina cadastrado!");
            atualizarTudo();
        } catch (RuntimeException e) { mostrarErro(e); }
    }

    private void cadastrarTipoTecnico() {
        try {
            String esp = JOptionPane.showInputDialog(this, "Especialidade (ex: Mecânico, Eletricista):");
            if (esp == null || esp.isBlank()) return;
            service.cadastrarTipoTecnico(new TipoTecnico(0, esp.trim()));
            JOptionPane.showMessageDialog(this, "Tipo de técnico cadastrado!");
            atualizarTudo();
        } catch (RuntimeException e) { mostrarErro(e); }
    }

    /** Formulário moderno de Máquina com JFileChooser e preview. */
    private void cadastrarMaquina() {
        try {
            List<TipoMaquina> tipos = service.getTiposMaquina();
            if (tipos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cadastre um Tipo de Máquina antes.",
                        "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JDialog dlg = new JDialog(this, "Cadastrar Máquina", true);
            dlg.setSize(500, 460);
            dlg.setLocationRelativeTo(this);
            dlg.getContentPane().setBackground(BG_CARD);
            dlg.setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(BG_CARD);
            form.setBorder(new EmptyBorder(20, 24, 20, 24));
            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL;
            g.insets = new Insets(6, 4, 6, 4);
            g.weightx = 1; g.gridx = 0;

            g.gridy = 0; form.add(new JLabel("Tipo de Máquina:"), g);
            JComboBox<TipoMaquina> cbTipo = new JComboBox<>(tipos.toArray(new TipoMaquina[0]));
            g.gridy = 1; form.add(cbTipo, g);

            g.gridy = 2; form.add(new JLabel("Número de Série:"), g);
            JTextField txtSerie = new JTextField();
            g.gridy = 3; form.add(txtSerie, g);

            g.gridy = 4; form.add(new JLabel("Foto (opcional):"), g);
            JPanel fotoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            fotoPanel.setBackground(BG_CARD);
            JLabel preview = new JLabel("Sem foto", SwingConstants.CENTER);
            preview.setPreferredSize(new Dimension(160, 100));
            preview.setBorder(new LineBorder(BORDER, 1, true));
            preview.setOpaque(true);
            preview.setBackground(new Color(0xF5F7FA));
            JButton btnFoto = new JButton("Escolher...");
            final String[] fotoPath = {null};
            btnFoto.addActionListener(e -> {
                String p = escolherFoto(dlg);
                if (p != null) {
                    fotoPath[0] = p;
                    ImageIcon ic = carregarMiniatura(p, 160, 100);
                    if (ic != null) { preview.setIcon(ic); preview.setText(""); }
                }
            });
            fotoPanel.add(preview);
            fotoPanel.add(btnFoto);
            g.gridy = 5; form.add(fotoPanel, g);

            JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            botoes.setBackground(BG_CARD);
            JButton btnCancelar = new JButton("Cancelar");
            JButton btnSalvar = new JButton("Salvar");
            btnSalvar.setBackground(PRIMARY);
            btnSalvar.setForeground(Color.WHITE);
            btnSalvar.setFocusPainted(false);
            botoes.add(btnCancelar);
            botoes.add(btnSalvar);

            btnCancelar.addActionListener(e -> dlg.dispose());
            btnSalvar.addActionListener(e -> {
                try {
                    String serie = txtSerie.getText().trim();
                    if (serie.isEmpty()) throw new RuntimeException("Informe o número de série.");
                    TipoMaquina tipo = (TipoMaquina) cbTipo.getSelectedItem();
                    service.cadastrarMaquina(new Maquina(0, serie, "ATIVA",
                            LocalDate.now(), tipo, fotoPath[0]));
                    dlg.dispose();
                    JOptionPane.showMessageDialog(this, "Máquina cadastrada!");
                    atualizarTudo();
                } catch (RuntimeException ex) { mostrarErro(ex); }
            });

            dlg.add(new JScrollPane(form), BorderLayout.CENTER);
            dlg.add(botoes, BorderLayout.SOUTH);
            dlg.setVisible(true);
        } catch (RuntimeException e) { mostrarErro(e); }
    }

    /** Formulário moderno de Técnico com foto. */
    private void cadastrarTecnico() {
        try {
            List<TipoTecnico> tipos = service.getTiposTecnico();
            if (tipos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cadastre um Tipo de Técnico antes.",
                        "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JDialog dlg = new JDialog(this, "Cadastrar Técnico", true);
            dlg.setSize(500, 480);
            dlg.setLocationRelativeTo(this);
            dlg.getContentPane().setBackground(BG_CARD);
            dlg.setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(BG_CARD);
            form.setBorder(new EmptyBorder(20, 24, 20, 24));
            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL;
            g.insets = new Insets(6, 4, 6, 4);
            g.weightx = 1; g.gridx = 0;

            g.gridy = 0; form.add(new JLabel("Especialidade:"), g);
            JComboBox<TipoTecnico> cbTipo = new JComboBox<>(tipos.toArray(new TipoTecnico[0]));
            g.gridy = 1; form.add(cbTipo, g);

            g.gridy = 2; form.add(new JLabel("Nome:"), g);
            JTextField txtNome = new JTextField();
            g.gridy = 3; form.add(txtNome, g);

            g.gridy = 4; form.add(new JLabel("Telefone:"), g);
            JTextField txtTel = new JTextField();
            g.gridy = 5; form.add(txtTel, g);

            g.gridy = 6; form.add(new JLabel("Foto (opcional):"), g);
            JPanel fotoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            fotoPanel.setBackground(BG_CARD);
            JLabel preview = new JLabel("Sem foto", SwingConstants.CENTER);
            preview.setPreferredSize(new Dimension(160, 100));
            preview.setBorder(new LineBorder(BORDER, 1, true));
            preview.setOpaque(true);
            preview.setBackground(new Color(0xF5F7FA));
            JButton btnFoto = new JButton("Escolher...");
            final String[] fotoPath = {null};
            btnFoto.addActionListener(e -> {
                String p = escolherFoto(dlg);
                if (p != null) {
                    fotoPath[0] = p;
                    ImageIcon ic = carregarMiniatura(p, 160, 100);
                    if (ic != null) { preview.setIcon(ic); preview.setText(""); }
                }
            });
            fotoPanel.add(preview);
            fotoPanel.add(btnFoto);
            g.gridy = 7; form.add(fotoPanel, g);

            JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            botoes.setBackground(BG_CARD);
            JButton btnCancelar = new JButton("Cancelar");
            JButton btnSalvar = new JButton("Salvar");
            btnSalvar.setBackground(PRIMARY);
            btnSalvar.setForeground(Color.WHITE);
            btnSalvar.setFocusPainted(false);
            botoes.add(btnCancelar);
            botoes.add(btnSalvar);

            btnCancelar.addActionListener(e -> dlg.dispose());
            btnSalvar.addActionListener(e -> {
                try {
                    String nome = txtNome.getText().trim();
                    if (nome.isEmpty()) throw new RuntimeException("Informe o nome do técnico.");
                    TipoTecnico tipo = (TipoTecnico) cbTipo.getSelectedItem();
                    service.cadastrarTecnico(new Tecnico(0, nome, txtTel.getText().trim(), tipo, fotoPath[0]));
                    dlg.dispose();
                    JOptionPane.showMessageDialog(this, "Técnico cadastrado!");
                    atualizarTudo();
                } catch (RuntimeException ex) { mostrarErro(ex); }
            });

            dlg.add(new JScrollPane(form), BorderLayout.CENTER);
            dlg.add(botoes, BorderLayout.SOUTH);
            dlg.setVisible(true);
        } catch (RuntimeException e) { mostrarErro(e); }
    }

    private void cadastrarPeca() {
        try {
            String nome = JOptionPane.showInputDialog(this, "Nome da Peça:");
            if (nome == null || nome.isBlank()) return;
            Integer qtd = lerInteiro("Quantidade em estoque:");
            if (qtd == null) return;
            Double val = lerDecimal("Valor unitário (R$):");
            if (val == null) return;
            service.cadastrarPeca(new Peca(0, nome.trim(), "Descrição", "Geral", qtd, 5, val));
            atualizarTudo();
        } catch (RuntimeException e) { mostrarErro(e); }
    }

    // ============================================================
    // AÇÕES — MANUTENÇÃO
    // ============================================================
    private void excluirMaquina() {
        if (!isAdmin()) {
            JOptionPane.showMessageDialog(this, "Apenas ADMINISTRADOR pode excluir.",
                    "Acesso negado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Integer id = lerInteiro("ID da Máquina que deseja excluir:");
            if (id == null) return;
            if (JOptionPane.showConfirmDialog(this,
                    "Confirmar exclusão da máquina " + id + "?",
                    "Atenção", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (service.excluirMaquina(id)) {
                    JOptionPane.showMessageDialog(this, "Máquina excluída!");
                    atualizarTudo();
                } else {
                    JOptionPane.showMessageDialog(this, "Máquina não encontrada.",
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (RuntimeException e) { mostrarErro(e); }
    }

    private void registrarFalha() {
        try {
            Integer idMaq = lerInteiro("ID da Máquina quebrada:");
            if (idMaq == null) return;
            String desc = JOptionPane.showInputDialog(this, "O que aconteceu?");
            if (desc == null) return;
            service.registrarFalha(desc, idMaq);
            JOptionPane.showMessageDialog(this, "Falha registrada!");
            atualizarTudo();
        } catch (MaquinaInexistenteException | RuntimeException e) { mostrarErro(e); }
    }

    private void abrirManutencao() {
        try {
            Integer idMaq = lerInteiro("ID da Máquina:");
            if (idMaq == null) return;
            String desc = JOptionPane.showInputDialog(this, "Descrição do serviço:");
            if (desc == null) return;
            String causa = JOptionPane.showInputDialog(this, "Causa raiz:");
            if (causa == null) return;
            int id = service.abrirCorretiva(desc, idMaq, causa, "ALTA");
            JOptionPane.showMessageDialog(this, "Ordem de serviço aberta! ID: " + id);
            atualizarTudo();
        } catch (MaquinaInexistenteException | MaquinaInativaException | RuntimeException e) { mostrarErro(e); }
    }

    private void abrirPreventiva() {
        try {
            Integer idMaq = lerInteiro("ID da Máquina:");
            if (idMaq == null) return;
            String desc = JOptionPane.showInputDialog(this, "Descrição do serviço:");
            if (desc == null) return;
            Integer dias = lerInteiro("Periodicidade (dias):");
            if (dias == null) return;
            int id = service.abrirPreventiva(desc, idMaq, dias);
            JOptionPane.showMessageDialog(this, "Manutenção preventiva aberta! ID: " + id);
            atualizarTudo();
        } catch (MaquinaInexistenteException | MaquinaInativaException | RuntimeException e) { mostrarErro(e); }
    }

    private void atribuirTecnico() {
        try {
            Integer idMan = lerInteiro("ID da Manutenção:");
            if (idMan == null) return;
            Integer idTec = lerInteiro("ID do Técnico:");
            if (idTec == null) return;
            service.atribuirTecnico(idMan, idTec);
            JOptionPane.showMessageDialog(this, "Técnico alocado!");
            atualizarTudo();
        } catch (TecnicoIndisponivelException | ManutencaoInexistenteException
                 | ManutencaoFinalizadaException | RuntimeException e) { mostrarErro(e); }
    }

    private void adicionarPecaManutencao() {
        try {
            Integer idMan = lerInteiro("ID da Manutenção:");
            if (idMan == null) return;
            Integer idPec = lerInteiro("ID da Peça:");
            if (idPec == null) return;
            Integer qtd = lerInteiro("Quantidade utilizada:");
            if (qtd == null) return;
            service.adicionarPecaNaManutencao(idMan, idPec, qtd);
            JOptionPane.showMessageDialog(this, "Peça adicionada e estoque atualizado!");
            atualizarTudo();
        } catch (EstoqueInsuficienteException | ManutencaoInexistenteException
                 | ManutencaoFinalizadaException | RuntimeException e) { mostrarErro(e); }
    }

    private void concluirManutencao() {
        try {
            Integer idMan = lerInteiro("ID da Manutenção:");
            if (idMan == null) return;
            double custo = service.concluirManutencao(idMan);
            JOptionPane.showMessageDialog(this,
                    "Manutenção concluída!\nCusto total: R$ " + String.format("%.2f", custo));
            atualizarTudo();
        } catch (ManutencaoInexistenteException | ManutencaoFinalizadaException | RuntimeException e) { mostrarErro(e); }
    }

    // ============================================================
    // RELATÓRIOS
    // ============================================================
    private void abrirRelatorioCusto() {
        StringBuilder sb = new StringBuilder("RELATÓRIO DE CUSTO ACUMULADO\n\n");
        double total = 0;
        try {
            for (Manutencao m : service.getManutencoes()) {
                String sn = (m.getMaquina() != null) ? m.getMaquina().getNumeroSerie() : "-";
                sb.append(String.format("#%03d [%s] %-18s  R$ %,.2f%n",
                        m.getId(), m.getTipo(), sn, m.getCusto()));
                total += m.getCusto();
            }
        } catch (Exception e) { mostrarErro(e); return; }
        sb.append("\n──────────────────────────────\n");
        sb.append(String.format("TOTAL ACUMULADO:   R$ %,.2f%n", total));

        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(600, 400));
        JOptionPane.showMessageDialog(this, sp, "Relatório de Custo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void abrirRelatorioHistorico() {
        StringBuilder sb = new StringBuilder("HISTÓRICO DE MANUTENÇÕES\n\n");
        try {
            for (Manutencao m : service.getManutencoes()) {
                String sn = (m.getMaquina() != null) ? m.getMaquina().getNumeroSerie() : "-";
                String tec = (m.getTecnico() != null) ? m.getTecnico().getNome() : "—";
                sb.append(String.format("#%03d  Tipo: %-10s  Máquina: %-12s  Técnico: %-18s  Status: %s%n",
                        m.getId(), m.getTipo(), sn, tec, m.getStatus()));
            }
        } catch (Exception e) { mostrarErro(e); return; }

        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(780, 420));
        JOptionPane.showMessageDialog(this, sp, "Histórico de Manutenções", JOptionPane.INFORMATION_MESSAGE);
    }

    // ============================================================
    // TUTORIAL
    // ============================================================
    private void exibirTutorial() {
        String texto =
            "BEM-VINDO AO SISTEMA DE MANUTENÇÃO INDUSTRIAL v2.0\n"
          + "==================================================\n\n"
          + "NOVO LAYOUT:\n"
          + "• Barra superior com abas (Início / Cadastros / Manutenção / Estoque / Administração)\n"
          + "• Dashboard com gráficos e custo acumulado\n"
          + "• Tela 'Preditiva' ordena equipamentos por saúde\n\n"
          + "FLUXO RECOMENDADO:\n"
          + "1) Cadastros → Tipo de Máquina / Tipo de Técnico\n"
          + "2) Cadastros → Máquina / Técnico / Peça  (com foto)\n"
          + "3) Manutenção → Registrar Falha (máquina fica QUEBRADA)\n"
          + "4) Manutenção → Abrir Corretiva OU Abrir Preventiva\n"
          + "5) Manutenção → Atribuir Técnico / Usar Peça\n"
          + "6) Manutenção → Concluir (custo calculado, máquina volta a ATIVA)\n\n"
          + "PERFIL OPERADOR: pode cadastrar, mas não exclui.\n"
          + "PERFIL ADM: acesso completo.";

        JTextArea ta = new JTextArea(texto);
        ta.setEditable(false);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
        ta.setCaretPosition(0);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(620, 460));
        JOptionPane.showMessageDialog(this, sp, "Tutorial", JOptionPane.INFORMATION_MESSAGE);
    }

    // ============================================================
    // MAIN
    // ============================================================
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignore) {}
        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }
}