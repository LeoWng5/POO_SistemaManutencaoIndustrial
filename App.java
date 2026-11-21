import classes.*;
import service.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class App extends JFrame {

    static boolean darkMode = false;
    static Color BG_APP, BG_CARD, PRIMARY, PRIMARY_D, ACCENT, DANGER, SUCCESS, TEXT, MUTED, BORDER, TAB_BG;

    static {
        aplicarCores(false);
    }

    private static void aplicarCores(boolean dark) {
        if (dark) {
            BG_APP    = new Color(0x121212);
            BG_CARD   = new Color(0x1E1E1E);
            TEXT      = new Color(0xF1F1F1);
            MUTED     = new Color(0xA0A0A0);
            BORDER    = new Color(0x333333);
            TAB_BG    = new Color(0x2D2D2D);
            PRIMARY   = new Color(0x007ACC);
            PRIMARY_D = new Color(0x005A9E);
        } else {
            BG_APP    = new Color(0xF4F6F9);
            BG_CARD   = Color.WHITE;
            TEXT      = new Color(0x2C3E50);
            MUTED     = new Color(0x95A5A6);
            BORDER    = new Color(0xDDE2E8);
            TAB_BG    = new Color(0xEDF1F6);
            PRIMARY   = new Color(0x1E5F9E);
            PRIMARY_D = new Color(0x154975);
        }
        ACCENT  = new Color(0xE67E22);
        DANGER  = new Color(0xE74C3C);
        SUCCESS = new Color(0x27AE60);
    }

    private static void aplicarTemaGlobal() {
        if (!darkMode) return;
        UIManager.put("OptionPane.background", BG_CARD);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Panel.background", BG_CARD);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("TextField.background", BG_APP);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", TEXT);
        UIManager.put("ComboBox.background", BG_APP);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ScrollPane.background", BG_CARD);
    }

    static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    static final Font F_SUBT  = new Font("Segoe UI", Font.BOLD, 13);
    static final Font F_BODY  = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font F_BIG   = new Font("Segoe UI", Font.BOLD, 34);

    private static final int LIMITE_SAUDE_PREVENTIVA = 30;

    private SistemaService service;

    private JLayeredPane layeredPane;
    private JPanel painelCards;
    private CardLayout cardCentral;
    private JDesktopPane desktopPane;

    private DashboardPanel dashboardPanel;
    private JPanel painelPreditiva;
    private JPanel painelPreditivaLista;
    private JPanel painelTabelas;

    private DefaultTableModel modeloMaquinas;
    private DefaultTableModel modeloTecnicos;
    private DefaultTableModel modeloPecas;
    private DefaultTableModel modeloManutencoes;
    
    private TableRowSorter<DefaultTableModel> sorterMaquinas;
    private TableRowSorter<DefaultTableModel> sorterTecnicos;
    private TableRowSorter<DefaultTableModel> sorterPecas;
    private TableRowSorter<DefaultTableModel> sorterManutencoes;

    private JLabel lblDataStatus;

    public App() {
        try {
            service = new SistemaService();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(null, "Erro ao iniciar banco de dados:\n" + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        if (!fazerLogin()) {
            dispose();
            System.exit(0);
            return;
        }

        aplicarTemaGlobal();

        setTitle("🏭 Gestão de Manutenção Industrial — v2.5");
        setSize(1366, 820);
        setMinimumSize(new Dimension(1150, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_APP);

        JPanel main = buildMain();
        add(buildRibbon(), BorderLayout.NORTH);
        add(main, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        atualizarTudo();
    }

    private void estilizarBotao(JButton b, Color bg, Color fg) {
        if (b.getUI().getClass() != BasicButtonUI.class) {
            b.setUI(new BasicButtonUI());
        }
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(6, 14, 6, 14)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private boolean fazerLogin() {
        JDialog dlg = new JDialog((Frame) null, "Acesso ao Sistema", true);
        dlg.setSize(420, 410);
        dlg.setLocationRelativeTo(null);
        dlg.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new GridBagLayout());
        JPanel footer = new JPanel(new BorderLayout());
        JLabel[] labels = new JLabel[2];
        JTextField txtUser = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JCheckBox chkDark = new JCheckBox("Modo Escuro");
        JButton btnSair = new JButton("Sair");
        JButton btnEntrar = new JButton("Entrar");
        JLabel lblErro = new JLabel(" ");

        Runnable temaLogin = () -> {
            aplicarCores(darkMode);
            dlg.getContentPane().setBackground(BG_CARD);
            header.setBackground(PRIMARY);
            form.setBackground(BG_CARD);
            footer.setBackground(BG_CARD);

            txtUser.setBackground(BG_APP); txtUser.setForeground(TEXT); txtUser.setCaretColor(TEXT);
            txtPass.setBackground(BG_APP); txtPass.setForeground(TEXT); txtPass.setCaretColor(TEXT);
            txtUser.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(4, 6, 4, 6)));
            txtPass.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(4, 6, 4, 6)));

            for (JLabel l : labels) l.setForeground(TEXT);
            lblErro.setForeground(DANGER);
            chkDark.setBackground(BG_CARD);
            chkDark.setForeground(TEXT);

            estilizarBotao(btnSair, BG_APP, TEXT);
            estilizarBotao(btnEntrar, PRIMARY, Color.WHITE);
            btnEntrar.setFont(F_SUBT);

            dlg.getContentPane().repaint();
        };

        header.setPreferredSize(new Dimension(0, 90));
        JLabel t = new JLabel("GESTÃO DE MANUTENÇÃO", SwingConstants.CENTER);
        t.setForeground(Color.WHITE);
        t.setFont(new Font("Segoe UI", Font.BOLD, 20));
        JLabel sub = new JLabel("v2.5 — SQLite", SwingConstants.CENTER);
        sub.setForeground(new Color(0xCCE1F5));
        sub.setFont(F_BODY);
        header.add(t, BorderLayout.CENTER);
        header.add(sub, BorderLayout.SOUTH);

        form.setBorder(new EmptyBorder(20, 32, 10, 32));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 0);
        g.weightx = 1; g.gridx = 0;

        labels[0] = new JLabel("Usuário");
        labels[1] = new JLabel("Senha");
        lblErro.setFont(F_BODY);

        g.gridy = 0; form.add(labels[0], g);
        g.gridy = 1; form.add(txtUser, g);
        g.gridy = 2; form.add(labels[1], g);
        g.gridy = 3; form.add(txtPass, g);
        g.gridy = 4; form.add(lblErro, g);

        chkDark.setFont(F_BODY);
        chkDark.setFocusPainted(false);
        chkDark.addActionListener(e -> { darkMode = chkDark.isSelected(); temaLogin.run(); });

        JPanel pnlBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlBotoes.setOpaque(false);
        pnlBotoes.add(btnSair);
        pnlBotoes.add(btnEntrar);

        footer.setBorder(new EmptyBorder(10, 20, 15, 20));
        footer.add(chkDark, BorderLayout.WEST);
        footer.add(pnlBotoes, BorderLayout.EAST);

        final boolean[] ok = {false};
        Runnable tentar = () -> {
            String u = txtUser.getText().trim();
            String s = new String(txtPass.getPassword());
            if (u.isEmpty() || s.isEmpty()) { lblErro.setText("Informe usuário e senha."); return; }
            try {
                if (!service.realizarLogin(u, s)) {
                    lblErro.setText("Usuário ou senha inválidos.");
                    return;
                }
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

        temaLogin.run();
        dlg.setVisible(true);
        return ok[0];
    }

    private boolean isAdmin() {
        try { return Sessao.getInstance().isAdmin(); } catch (Throwable t) { return false; }
    }

    private JPanel buildRibbon() {
        JPanel ribbon = new JPanel();
        ribbon.setLayout(new BoxLayout(ribbon, BoxLayout.Y_AXIS));
        ribbon.setBackground(BG_CARD);
        ribbon.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));

        JPanel tabStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabStrip.setBackground(TAB_BG);

        JPanel btnArea = new JPanel(new CardLayout());
        btnArea.setBackground(BG_CARD);
        btnArea.setPreferredSize(new Dimension(0, 92));

        String[] tabs = {"Início", "Cadastros", "Manutenção", "Estoque", "Administração"};
        JButton[] tabBtns = new JButton[tabs.length];

        for (int i = 0; i < tabs.length; i++) {
            final String nome = tabs[i];
            JButton tb = new JButton(nome);
            estilizarBotao(tb, TAB_BG, TEXT);
            tb.setFont(F_SUBT);
            tb.setBorder(new EmptyBorder(8, 18, 8, 18));
            tabBtns[i] = tb;
            tabStrip.add(tb);

            btnArea.add(buildRibbonPage(nome), nome);

            tb.addActionListener(e -> {
                ((CardLayout) btnArea.getLayout()).show(btnArea, nome);
                for (JButton x : tabBtns) { x.setBackground(TAB_BG); x.setForeground(TEXT); }
                tb.setBackground(BG_CARD);
                tb.setForeground(PRIMARY);
                if (nome.equals("Início") && cardCentral != null) cardCentral.show(painelCards, "DASH");
            });
        }
        tabBtns[0].setBackground(BG_CARD);
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
                p.add(ribbonBtn("🏠 Dashboard", e -> cardCentral.show(painelCards, "DASH")));
                p.add(ribbonBtn("📋 Dados", e -> cardCentral.show(painelCards, "DADOS")));
                p.add(ribbonBtn("⚠ Preditiva", e -> cardCentral.show(painelCards, "PREDITIVA")));
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
                // O botão "Abrir Preventiva" foi removido daqui (Etapa 2)
                p.add(ribbonBtn("Atribuir Técnico", e -> atribuirTecnico()));
                p.add(ribbonBtn("Usar Peça", e -> adicionarPecaManutencao()));
                p.add(ribbonBtn("Concluir", e -> concluirManutencao()));
            }
            case "Estoque" -> {
                p.add(ribbonBtn("Cadastrar Peça", e -> cadastrarPeca()));
                p.add(ribbonBtn("Ver Estoque", e -> cardCentral.show(painelCards, "DADOS")));
                p.add(ribbonBtn("Relatório de Custo", e -> abrirRelatorioCusto()));
            }
            case "Administração" -> {
                if (isAdmin()) {
                    p.add(ribbonBtn("Excluir Máquina", e -> excluirMaquina()));
                    p.add(ribbonBtn("Relatório Histórico", e -> abrirRelatorioHistorico()));
                    p.add(ribbonBtn("⏰ Alterar Data", e -> alterarDataSistema()));
                    p.add(ribbonBtn("📊 Relatório Gerencial", e -> abrirRelatorioGerencial()));
                } else {
                    JLabel l = new JLabel("⚠ Acesso restrito ao perfil ADMINISTRADOR.");
                    l.setForeground(MUTED); l.setFont(F_BODY); p.add(l);
                }
            }
        }
        return p;
    }

    private JButton ribbonBtn(String text, ActionListener a) {
        JButton b = new JButton(text);
        estilizarBotao(b, BG_CARD, TEXT);
        b.setFont(F_BODY);
        b.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(10, 14, 10, 14)));
        b.addActionListener(a);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(TAB_BG); }
            public void mouseExited(MouseEvent e) { b.setBackground(BG_CARD); }
        });
        return b;
    }

    private JPanel buildMain() {
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));

        painelCards = new JPanel();
        cardCentral = new CardLayout();
        painelCards.setLayout(cardCentral);
        painelCards.setBackground(BG_APP);

        dashboardPanel = new DashboardPanel();
        painelPreditiva = buildPreditivaPanel();
        painelTabelas = buildTabelasPanel();

        painelCards.add(dashboardPanel, "DASH");
        painelCards.add(painelPreditiva, "PREDITIVA");
        painelCards.add(painelTabelas, "DADOS");

        desktopPane = new JDesktopPane();
        desktopPane.setOpaque(false); // Fundamental para ver o fundo

        layeredPane.add(desktopPane, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(painelCards, JLayeredPane.DEFAULT_LAYER);

        cardCentral.show(painelCards, "DASH");
        return painelCards; // O layout do JFrame cuidará de encaixar
    }

    // ============================================================
    // DASHBOARD E PREDITIVA
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
            titulo.setFont(F_TITLE); titulo.setForeground(TEXT);

            JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
            grid.setOpaque(false);

            chartMaquinas = new BarChartPanel(new String[]{"Operando", "Quebradas"}, new Color[]{SUCCESS, DANGER});
            grid.add(wrapCard("Status das Máquinas", chartMaquinas));

            chartTecnicos = new BarChartPanel(new String[]{"Disponíveis", "Ocupados"}, new Color[]{SUCCESS, ACCENT});
            grid.add(wrapCard("Equipe Técnica", chartTecnicos));

            JPanel custoCard = new JPanel(new BorderLayout(0, 6));
            custoCard.setBackground(BG_CARD);
            custoCard.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(16, 20, 16, 20)));
            JLabel lc = new JLabel("Custo Acumulado");
            lc.setFont(F_SUBT); lc.setForeground(MUTED);
            lblCustoTotal = new JLabel("R$ 0,00");
            lblCustoTotal.setFont(F_BIG); lblCustoTotal.setForeground(PRIMARY);
            custoCard.add(lc, BorderLayout.NORTH); custoCard.add(lblCustoTotal, BorderLayout.CENTER);

            JPanel predCard = new JPanel(new BorderLayout(0, 6));
            predCard.setBackground(BG_CARD);
            predCard.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(16, 20, 16, 20)));
            JLabel lp = new JLabel("Alerta Preditivo");
            lp.setFont(F_SUBT); lp.setForeground(MUTED);
            JLabel lpInfo = new JLabel("Veja quais equipamentos estão mais próximos da falha.");
            lpInfo.setFont(F_BODY); lpInfo.setForeground(TEXT);
            JButton btnVer = new JButton("Abrir Lista →");
            estilizarBotao(btnVer, PRIMARY, Color.WHITE);
            btnVer.addActionListener(e -> cardCentral.show(painelCards, "PREDITIVA"));
            predCard.add(lp, BorderLayout.NORTH); predCard.add(lpInfo, BorderLayout.CENTER);
            JPanel bwrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            bwrap.setOpaque(false); bwrap.add(btnVer); predCard.add(bwrap, BorderLayout.SOUTH);

            grid.add(custoCard);
            grid.add(predCard);

            add(titulo, BorderLayout.NORTH);
            add(grid, BorderLayout.CENTER);
        }

        private JPanel wrapCard(String titulo, JComponent conteudo) {
            JPanel c = new JPanel(new BorderLayout(0, 8));
            c.setBackground(BG_CARD);
            c.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(16, 20, 16, 20)));
            JLabel t = new JLabel(titulo); t.setFont(F_SUBT); t.setForeground(TEXT);
            c.add(t, BorderLayout.NORTH); c.add(conteudo, BorderLayout.CENTER);
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

    class BarChartPanel extends JComponent {
        private final String[] labels;
        private final Color[] cores;
        private int[] valores;

        BarChartPanel(String[] labels, Color[] cores) {
            this.labels = labels; this.cores = cores; this.valores = new int[labels.length];
            setPreferredSize(new Dimension(220, 150)); setOpaque(false);
        }
        void setValores(int[] v) { this.valores = v; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight(), n = labels.length, max = 1;
            for (int v : valores) max = Math.max(max, v);
            int espaco = 24, barW = (w - espaco * (n + 1)) / n, baseY = h - 26;

            for (int i = 0; i < n; i++) {
                int x = espaco + i * (barW + espaco);
                int barH = (int) ((double) valores[i] / max * (baseY - 30));
                if (barH < 2) barH = 2;

                g2.setColor(cores[i]);
                g2.fill(new RoundRectangle2D.Float(x, baseY - barH, barW, barH, 10, 10));

                g2.setColor(TEXT); g2.setFont(F_SUBT);
                String num = String.valueOf(valores[i]);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(num, x + (barW - fm.stringWidth(num)) / 2, baseY - barH - 6);

                g2.setColor(MUTED); g2.setFont(F_BODY);
                fm = g2.getFontMetrics();
                g2.drawString(labels[i], x + (barW - fm.stringWidth(labels[i])) / 2, baseY + 18);
            }
            g2.dispose();
        }
    }

    private JPanel buildPreditivaPanel() {
        JPanel painel = new JPanel(new BorderLayout(0, 12));
        painel.setBackground(BG_APP);
        painel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel titulo = new JLabel("Manutenção Preditiva — Saúde dos Equipamentos");
        titulo.setFont(F_TITLE); titulo.setForeground(TEXT);
        JLabel sub = new JLabel("Equipamentos ordenados do mais crítico para o menos crítico.");
        sub.setFont(F_BODY); sub.setForeground(MUTED);

        JPanel header = new JPanel();
        header.setOpaque(false); header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(titulo); header.add(Box.createRigidArea(new Dimension(0, 4))); header.add(sub);

        painelPreditivaLista = new JPanel();
        painelPreditivaLista.setLayout(new BoxLayout(painelPreditivaLista, BoxLayout.Y_AXIS));
        painelPreditivaLista.setBackground(BG_APP);

        JScrollPane scroll = new JScrollPane(painelPreditivaLista);
        scroll.setBorder(null); scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(BG_APP);

        painel.add(header, BorderLayout.NORTH);
        painel.add(scroll, BorderLayout.CENTER);
        return painel;
    }

    private void atualizarPreditiva() {
        painelPreditivaLista.removeAll();
        try {
            List<Maquina> ms = new ArrayList<>(service.getMaquinas());
            ms.sort(Comparator.comparingInt(Maquina::getSaudeEquipamento));
            if (ms.isEmpty()) {
                JLabel vazio = new JLabel("Nenhum equipamento cadastrado.");
                vazio.setForeground(MUTED); vazio.setFont(F_BODY);
                painelPreditivaLista.add(vazio);
            }
            for (Maquina m : ms) {
                painelPreditivaLista.add(cardPreditiva(m, m.getSaudeEquipamento()));
                painelPreditivaLista.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        } catch (Exception e) { mostrarErro(e); }
        painelPreditivaLista.revalidate();
        painelPreditivaLista.repaint();
    }

    private JPanel cardPreditiva(Maquina m, int saude) {
        Color cor = saude < 30 ? DANGER : saude < 60 ? ACCENT : SUCCESS;
        String alerta = saude < 30 ? "⚠ Troca iminente" : saude < 60 ? "Monitorar" : "OK";

        JPanel c = new JPanel(new BorderLayout(12, 6));
        c.setBackground(BG_CARD);
        c.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(12, 16, 12, 16)));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));

        JLabel nome = new JLabel(m.getNome() + "   ·   S/N " + m.getNumeroSerie());
        nome.setFont(F_SUBT); nome.setForeground(TEXT);

        JProgressBar pb = new JProgressBar(0, 100);
        pb.setValue(saude); pb.setForeground(cor); pb.setBackground(BORDER);
        pb.setStringPainted(true); pb.setString(saude + "% de saúde"); pb.setBorderPainted(false);

        JLabel lblAlerta = new JLabel(alerta);
        lblAlerta.setForeground(cor); lblAlerta.setFont(F_SUBT);

        JPanel dir = new JPanel(new BorderLayout(8, 4));
        dir.setOpaque(false); dir.add(lblAlerta, BorderLayout.NORTH); dir.add(pb, BorderLayout.CENTER);
        c.add(nome, BorderLayout.WEST); c.add(dir, BorderLayout.CENTER);
        return c;
    }

    // ============================================================
    // TABELAS COM BUSCA (LUPINHA)
    // ============================================================
    private JPanel buildTabelasPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_APP);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(F_SUBT); tabs.setBackground(BG_CARD); tabs.setForeground(TEXT);

        // ABA MÁQUINAS
        modeloMaquinas = new DefaultTableModel(new String[]{"ID", "Nº Série", "Tipo", "Fabricante", "Status", "Saúde (%)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabMaq = criarTabela(modeloMaquinas);
        sorterMaquinas = new TableRowSorter<>(modeloMaquinas);
        tabMaq.setRowSorter(sorterMaquinas);
        tabMaq.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = tabMaq.getSelectedRow();
                    if (viewRow >= 0) {
                        int id = (int) modeloMaquinas.getValueAt(tabMaq.convertRowIndexToModel(viewRow), 0);
                        try {
                            service.getMaquinas().stream().filter(x -> x.getId() == id).findFirst()
                                .ifPresent(App.this::abrirFichaMaquina);
                        } catch (Exception ignore) {}
                    }
                }
            }
        });
        tabs.addTab("Máquinas", painelComBusca(tabMaq, sorterMaquinas));

        // ABA TÉCNICOS
        modeloTecnicos = new DefaultTableModel(new String[]{"ID", "Nome", "Especialidade", "Telefone", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabTec = criarTabela(modeloTecnicos);
        sorterTecnicos = new TableRowSorter<>(modeloTecnicos);
        tabTec.setRowSorter(sorterTecnicos);
        tabTec.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = tabTec.getSelectedRow();
                    if (viewRow >= 0) {
                        int id = (int) modeloTecnicos.getValueAt(tabTec.convertRowIndexToModel(viewRow), 0);
                        try {
                            service.getTecnicos().stream().filter(x -> x.getId() == id).findFirst()
                                .ifPresent(App.this::abrirFichaTecnico);
                        } catch (Exception ignore) {}
                    }
                }
            }
        });
        tabs.addTab("Técnicos", painelComBusca(tabTec, sorterTecnicos));

        // ABA PEÇAS
        modeloPecas = new DefaultTableModel(new String[]{"ID", "Nome", "Categoria", "Qtd. Estoque", "Valor Unit. (R$)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabPec = criarTabela(modeloPecas);
        sorterPecas = new TableRowSorter<>(modeloPecas);
        tabPec.setRowSorter(sorterPecas);
        tabs.addTab("Estoque de Peças", painelComBusca(tabPec, sorterPecas));

        // ABA MANUTENÇÕES
        modeloManutencoes = new DefaultTableModel(new String[]{"ID", "Tipo", "Máquina", "Técnico", "Status", "Custo (R$)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabMan = criarTabela(modeloManutencoes);
        sorterManutencoes = new TableRowSorter<>(modeloManutencoes);
        tabMan.setRowSorter(sorterManutencoes);
        tabs.addTab("Manutenções", painelComBusca(tabMan, sorterManutencoes));

        p.add(tabs, BorderLayout.CENTER);
        return p;
    }

    private JPanel painelComBusca(JTable table, TableRowSorter<DefaultTableModel> sorter) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(BG_APP);
        
        JTextField txtBusca = new JTextField();
        estilizarCampo(txtBusca);
        txtBusca.setBorder(new CompoundBorder(new TitledBorder(new LineBorder(BORDER), "🔍 Buscar...", TitledBorder.LEFT, TitledBorder.TOP, F_BODY, MUTED), new EmptyBorder(2, 4, 2, 4)));
        
        txtBusca.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
            private void filtrar() {
                String text = txtBusca.getText();
                if (text.trim().length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });
        
        p.add(txtBusca, BorderLayout.NORTH);
        p.add(criarScroll(table), BorderLayout.CENTER);
        return p;
    }

    private JTable criarTabela(DefaultTableModel modelo) {
        JTable table = new JTable(modelo);
        table.setRowHeight(24); table.setBackground(BG_CARD); table.setForeground(TEXT);
        table.setGridColor(BORDER); table.setSelectionBackground(PRIMARY); table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setBackground(TAB_BG); table.getTableHeader().setForeground(TEXT); table.getTableHeader().setFont(F_SUBT);
        return table;
    }

    private JScrollPane criarScroll(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(BG_CARD); sp.setBorder(new LineBorder(BORDER, 1));
        return sp;
    }

    // ============================================================
    // FICHAS FLUTUANTES (JInternalFrame)
    // ============================================================
    private void abrirFichaMaquina(Maquina m) {
        JInternalFrame frame = new JInternalFrame("Máquina: " + m.getNumeroSerie(), true, true, true, true);
        frame.setSize(420, 480);
        frame.setLocation(50 + (desktopPane.getAllFrames().length * 20), 50 + (desktopPane.getAllFrames().length * 20));
        frame.getContentPane().setBackground(BG_CARD);
        
        JPanel info = new JPanel(new GridLayout(0, 2, 8, 6));
        info.setBackground(BG_CARD);
        info.setBorder(new EmptyBorder(10, 10, 10, 10));
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
        info.add(lbl("Tipo:")); info.add(lbl(m.getNome()));
        info.add(lbl("Status:")); info.add(lbl(m.getStatus()));
        info.add(lbl("Saúde:")); info.add(lbl(m.getSaudeEquipamento() + "%"));
        info.add(lbl("Valor:")); info.add(lbl(nf.format(m.getValorAquisicao())));
        info.add(lbl("Residual:")); info.add(lbl(nf.format(m.getValorResidual())));

        JPanel acoes = new JPanel(new GridLayout(0, 1, 0, 8));
        acoes.setBackground(BG_CARD);
        acoes.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JButton btnFalha = new JButton("Registrar Falha"); estilizarBotao(btnFalha, DANGER, Color.WHITE);
        JButton btnTec = new JButton("Atribuir Técnico"); estilizarBotao(btnTec, PRIMARY, Color.WHITE);
        JButton btnPeca = new JButton("Usar Peça"); estilizarBotao(btnPeca, ACCENT, Color.WHITE);
        JButton btnConc = new JButton("Concluir Manutenção"); estilizarBotao(btnConc, SUCCESS, Color.WHITE);

        acoes.add(btnFalha); acoes.add(btnTec); acoes.add(btnPeca); acoes.add(btnConc);
        
        frame.getContentPane().add(info, BorderLayout.NORTH);
        frame.getContentPane().add(acoes, BorderLayout.CENTER);
        desktopPane.add(frame);
        frame.setVisible(true);
    }

    private void abrirFichaTecnico(Tecnico t) {
        JInternalFrame frame = new JInternalFrame("Técnico: " + t.getNome(), true, true, true, true);
        frame.setSize(380, 300);
        frame.setLocation(100 + (desktopPane.getAllFrames().length * 20), 100 + (desktopPane.getAllFrames().length * 20));
        frame.getContentPane().setBackground(BG_CARD);
        
        JPanel info = new JPanel(new GridLayout(0, 2, 8, 6));
        info.setBackground(BG_CARD);
        info.setBorder(new EmptyBorder(10, 10, 10, 10));
        info.add(lbl("Especialidade:")); info.add(lbl(t.getEspecialidade()));
        info.add(lbl("Telefone:")); info.add(lbl(t.getTelefone()));
        info.add(lbl("Status:")); info.add(lbl(t.isDisponivel() ? "DISPONÍVEL" : "OCUPADO"));
        
        frame.getContentPane().add(info, BorderLayout.CENTER);
        desktopPane.add(frame);
        frame.setVisible(true);
    }

    // ============================================================
    // REFRESH E PREVENTIVA AUTOMÁTICA (ETAPA 2)
    // ============================================================
    private void atualizarTudo() {
        atualizarTabelas();
        if (dashboardPanel != null) dashboardPanel.atualizar();
        if (painelPreditivaLista != null) atualizarPreditiva();
        atualizarLabelData();
        verificarPreventivasAutomaticas();
    }

    private void verificarPreventivasAutomaticas() {
        try {
            List<Maquina> ms = service.getMaquinas();
            List<String> avisos = new ArrayList<>();
            for (Maquina m : ms) {
                if (m.getStatus().equals("ATIVA") && m.getSaudeEquipamento() < LIMITE_SAUDE_PREVENTIVA) {
                    if (!service.temPreventivaAberta(m.getId())) {
                        service.abrirPreventiva("Preventiva Automática (Saúde " + m.getSaudeEquipamento() + "%)", m.getId(), 180);
                        avisos.add(m.getNumeroSerie() + " (Saúde " + m.getSaudeEquipamento() + "%)");
                    }
                }
            }
            if (!avisos.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Preventiva automática aberta para as máquinas:\n\n" + String.join("\n", avisos),
                    "Alerta de Manutenção", JOptionPane.WARNING_MESSAGE);
                atualizarTabelas(); // Recarrega para mostrar as manutenções novas
            }
        } catch (Exception ignore) {}
    }

    private void atualizarTabelas() {
        try {
            modeloMaquinas.setRowCount(0); modeloTecnicos.setRowCount(0);
            modeloPecas.setRowCount(0); modeloManutencoes.setRowCount(0);

            for (Maquina m : service.getMaquinas()) {
                modeloMaquinas.addRow(new Object[]{ m.getId(), m.getNumeroSerie(), m.getNome(), m.getFabricante(), m.getStatus(), m.getSaudeEquipamento() + "%" });
            }
            for (Tecnico t : service.getTecnicos()) {
                modeloTecnicos.addRow(new Object[]{ t.getId(), t.getNome(), t.getEspecialidade(), t.getTelefone(), t.isDisponivel() ? "DISPONÍVEL" : "OCUPADO" });
            }
            for (Peca p : service.getPecas()) {
                modeloPecas.addRow(new Object[]{ p.getId(), p.getNome(), p.getCategoria(), p.getQuantidadeEstoque(), String.format("%.2f", p.getValorUnitario()) });
            }
            for (Manutencao m : service.getManutencoes()) {
                String nomeMaq = (m.getMaquina() != null) ? m.getMaquina().toString() : "N/A";
                String nomeTec = (m.getTecnico() != null) ? m.getTecnico().getNome() : "Não atribuído";
                modeloManutencoes.addRow(new Object[]{ m.getId(), m.getTipo(), nomeMaq, nomeTec, m.getStatus(), String.format("%.2f", m.getCusto()) });
            }
        } catch (RuntimeException e) { mostrarErro(e); }
    }

    // ============================================================
    // CADASTROS COM CHECKLIST (ETAPA 3)
    // ============================================================
    private void cadastrarTipoMaquina() {
        try {
            JTextField txtNome = new JTextField();
            JTextField txtFab = new JTextField();
            JSpinner spVida = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
            
            List<Peca> todasPecas = service.getPecas();
            JPanel painelPecas = new JPanel(new GridLayout(0, 2, 4, 4));
            painelPecas.setBackground(BG_CARD);
            List<JCheckBox> chks = new ArrayList<>();
            List<JSpinner> spins = new ArrayList<>();
            
            for (Peca p : todasPecas) {
                JCheckBox chk = new JCheckBox(p.getNome()); chk.setBackground(BG_CARD); chk.setForeground(TEXT);
                JSpinner sp = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
                painelPecas.add(chk); painelPecas.add(sp);
                chks.add(chk); spins.add(sp);
            }
            
            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(BG_CARD);
            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(4, 4, 4, 4); g.weightx = 1; g.gridx = 0;
            
            g.gridy = 0; form.add(lbl("Nome do Tipo:"), g);
            g.gridy = 1; form.add(txtNome, g);
            g.gridy = 2; form.add(lbl("Fabricante:"), g);
            g.gridy = 3; form.add(txtFab, g);
            g.gridy = 4; form.add(lbl("Vida Útil (anos):"), g);
            g.gridy = 5; form.add(spVida, g);
            g.gridy = 6; form.add(lbl("Peças Recomendadas:"), g);
            g.gridy = 7; form.add(new JScrollPane(painelPecas), g);
            
            int r = JOptionPane.showConfirmDialog(this, form, "Novo Tipo de Máquina", JOptionPane.OK_CANCEL_OPTION);
            if (r != JOptionPane.OK_OPTION) return;
            
            String nome = txtNome.getText().trim();
            if (nome.isEmpty()) return;
            
            TipoMaquina t = new TipoMaquina(0, nome, txtFab.getText().trim(), (int) spVida.getValue());
            for (int i = 0; i < todasPecas.size(); i++) {
                if (chks.get(i).isSelected()) {
                    t.adicionarPecaRecomendada(todasPecas.get(i), (int) spins.get(i).getValue());
                }
            }
            
            service.cadastrarTipoMaquina(t);
            JOptionPane.showMessageDialog(this, "Tipo cadastrado!");
            atualizarTudo();
        } catch (Exception e) { mostrarErro(e); }
    }

    private void cadastrarMaquina() {
        try {
            List<TipoMaquina> tipos = service.getTiposMaquina();
            if (tipos.isEmpty()) { JOptionPane.showMessageDialog(this, "Cadastre um Tipo antes."); return; }
            
            JDialog dlg = new JDialog(this, "Cadastrar Máquina", true);
            dlg.setSize(500, 600); dlg.setLocationRelativeTo(this);
            dlg.getContentPane().setBackground(BG_CARD); dlg.setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridBagLayout()); form.setBackground(BG_CARD);
            form.setBorder(new EmptyBorder(20, 24, 20, 24));
            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(6, 4, 6, 4); g.weightx = 1; g.gridx = 0;

            g.gridy = 0; form.add(lbl("Tipo:"), g);
            JComboBox<TipoMaquina> cbTipo = new JComboBox<>(tipos.toArray(new TipoMaquina[0]));
            g.gridy = 1; form.add(cbTipo, g);

            g.gridy = 2; form.add(lbl("Data de Aquisição:"), g);
            JSpinner spData = new JSpinner(new SpinnerDateModel());
            spData.setEditor(new JSpinner.DateEditor(spData, "dd/MM/yyyy"));
            spData.setValue(java.sql.Date.valueOf(RelogioSistema.hoje()));
            g.gridy = 3; form.add(spData, g);

            g.gridy = 4; form.add(lbl("Valor de Aquisição (R$):"), g);
            JTextField txtValor = new JTextField("0,00"); estilizarCampo(txtValor); g.gridy = 5; form.add(txtValor, g);

            g.gridy = 6; form.add(lbl("Número de Série:"), g);
            JTextField txtSerie = new JTextField(); estilizarCampo(txtSerie); g.gridy = 7; form.add(txtSerie, g);

            JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT)); botoes.setBackground(BG_CARD);
            JButton btnSalvar = new JButton("Salvar"); estilizarBotao(btnSalvar, PRIMARY, Color.WHITE);
            btnSalvar.addActionListener(e -> {
                try {
                    String serie = txtSerie.getText().trim();
                    if (serie.isEmpty()) throw new RuntimeException("Informe a série.");
                    LocalDate data = new java.sql.Date(((Date) spData.getValue()).getTime()).toLocalDate();
                    if (data.isAfter(RelogioSistema.hoje())) throw new RuntimeException("Data futura não permitida.");
                    double valor = Double.parseDouble(txtValor.getText().trim().replace(',', '.'));
                    
                    service.cadastrarMaquina(new Maquina(0, serie, "ATIVA", data, valor, (TipoMaquina) cbTipo.getSelectedItem(), null));
                    dlg.dispose(); atualizarTudo();
                } catch (Exception ex) { mostrarErro(ex); }
            });
            botoes.add(btnSalvar);

            dlg.add(new JScrollPane(form), BorderLayout.CENTER);
            dlg.add(botoes, BorderLayout.SOUTH);
            dlg.setVisible(true);
        } catch (Exception e) { mostrarErro(e); }
    }

    // ... (Métodos de cadastrarTecnico e cadastrarPeca mantidos iguais) ...

    // ============================================================
    // RELATÓRIO GERENCIAL POR MÁQUINA (ETAPA 6)
    // ============================================================
    private void abrirRelatorioGerencial() {
        JDialog dlg = new JDialog(this, "Gerar Relatório", true);
        dlg.setSize(400, 450); dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BG_CARD); dlg.setLayout(new BorderLayout());

        JPanel painelLista = new JPanel(new GridLayout(0, 1, 4, 4));
        painelLista.setBackground(BG_CARD);
        List<JCheckBox> chks = new ArrayList<>();
        
        try {
            for (Maquina m : service.getMaquinas()) {
                JCheckBox chk = new JCheckBox(m.getNumeroSerie() + " - " + m.getNome());
                chk.setBackground(BG_CARD); chk.setForeground(TEXT);
                painelLista.add(chk); chks.add(chk);
            }
        } catch (Exception e) { mostrarErro(e); return; }

        JButton btnTodos = new JButton("Selecionar Todas"); estilizarBotao(btnTodos, BG_APP, TEXT);
        btnTodos.addActionListener(e -> chks.forEach(c -> c.setSelected(true)));

        JButton btnGerar = new JButton("Gerar Relatório TXT"); estilizarBotao(btnGerar, PRIMARY, Color.WHITE);
        btnGerar.addActionListener(e -> {
            List<Maquina> selecionadas = new ArrayList<>();
            List<Maquina> todas = service.getMaquinas();
            for (int i = 0; i < chks.size(); i++) {
                if (chks.get(i).isSelected()) selecionadas.add(todas.get(i));
            }
            if (selecionadas.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Selecione ao menos uma."); return; }
            dlg.dispose();
            gerarTextoRelatorio(selecionadas);
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT)); top.setBackground(BG_CARD);
        top.add(btnTodos);
        
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bot.setBackground(BG_CARD);
        bot.add(btnGerar);

        dlg.add(top, BorderLayout.NORTH);
        dlg.add(new JScrollPane(painelLista), BorderLayout.CENTER);
        dlg.add(bot, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void gerarTextoRelatorio(List<Maquina> selecionadas) {
        try {
            StringBuilder sb = new StringBuilder("RELATÓRIO GERENCIAL DE ATIVOS\n\n");
            double totalGeral = 0;
            for (Maquina m : selecionadas) {
                sb.append("MÁQUINA: ").append(m.getNumeroSerie()).append(" (").append(m.getNome()).append(")\n");
                sb.append("Saúde Atual: ").append(m.getSaudeEquipamento()).append("%\n");
                sb.append("Manutenções:\n");
                double custoMaq = 0;
                for (Manutencao man : service.getManutencoesPorMaquina(m.getId())) {
                    sb.append(String.format("  #%03d [%s] R$ %.2f\n", man.getId(), man.getTipo(), man.getCusto()));
                    custoMaq += man.getCusto();
                }
                sb.append("Custo Total da Máquina: R$ ").append(String.format("%.2f", custoMaq)).append("\n");
                sb.append("--------------------------------------------------\n");
                totalGeral += custoMaq;
            }
            sb.append("TOTAL GERAL DO RELATÓRIO: R$ ").append(String.format("%.2f", totalGeral));
            
            // Exporta para TXT
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("relatorio_gerencial.txt"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try (PrintWriter pw = new PrintWriter(new FileWriter(fc.getSelectedFile()))) {
                    pw.print(sb.toString());
                }
                JOptionPane.showMessageDialog(this, "Relatório salvo com sucesso!");
            }
        } catch (Exception e) { mostrarErro(e); }
    }

    // ============================================================
    // MÉTODOS UTILITÁRIOS E DE APOIO
    // ============================================================
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(TAB_BG);
        bar.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, BORDER), new EmptyBorder(6, 16, 6, 16)));

        String perfil = isAdmin() ? "ADMINISTRADOR" : "OPERADOR";
        JLabel lbl = new JLabel("👤 Sessão: " + perfil);
        lbl.setFont(F_BODY); lbl.setForeground(MUTED);
        bar.add(lbl, BorderLayout.WEST);

        lblDataStatus = new JLabel();
        lblDataStatus.setFont(F_BODY);
        atualizarLabelData();
        bar.add(lblDataStatus, BorderLayout.EAST);
        return bar;
    }

    private void atualizarLabelData() {
        if (lblDataStatus == null) return;
        LocalDate d = RelogioSistema.hoje();
        String fmt = d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        if (RelogioSistema.estaSimulando()) {
            lblDataStatus.setText("DATA SIMULADA: " + fmt); lblDataStatus.setForeground(ACCENT);
        } else {
            lblDataStatus.setText(fmt); lblDataStatus.setForeground(MUTED);
        }
    }

    private void alterarDataSistema() {
        if (!isAdmin()) return;
        JDialog dlg = new JDialog(this, "Alterar Data", true);
        dlg.setSize(380, 200); dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout()); dlg.getContentPane().setBackground(BG_CARD);

        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        spinner.setValue(java.sql.Date.valueOf(RelogioSistema.hoje()));

        JPanel top = new JPanel(); top.setBackground(BG_CARD); top.add(lbl("Data:")); top.add(spinner);
        
        JPanel botoes = new JPanel(); botoes.setBackground(BG_CARD);
        JButton btnAno = new JButton("+1 ano"); estilizarBotao(btnAno, BG_APP, TEXT);
        btnAno.addActionListener(e -> {
            Calendar c = Calendar.getInstance(); c.setTime((Date) spinner.getValue()); c.add(Calendar.YEAR, 1); spinner.setValue(c.getTime());
        });
        JButton btnReset = new JButton("Hoje"); estilizarBotao(btnReset, BG_APP, TEXT);
        btnReset.addActionListener(e -> spinner.setValue(java.sql.Date.valueOf(LocalDate.now())));
        JButton btnOk = new JButton("Aplicar"); estilizarBotao(btnOk, PRIMARY, Color.WHITE);
        btnOk.addActionListener(e -> {
            LocalDate ld = new java.sql.Date(((Date) spinner.getValue()).getTime()).toLocalDate();
            RelogioSistema.setData(ld); dlg.dispose(); atualizarTudo();
        });
        botoes.add(btnAno); botoes.add(btnReset); botoes.add(btnOk);

        dlg.add(top, BorderLayout.CENTER); dlg.add(botoes, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void mostrarErro(Exception e) {
        String msg = (e instanceof NumberFormatException) ? "Digite um número válido." : e.getMessage();
        JOptionPane.showMessageDialog(this, "Erro: " + msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private Integer lerInteiro(String mensagem) {
        String t = JOptionPane.showInputDialog(this, mensagem);
        if (t == null) return null;
        return Integer.parseInt(t.trim());
    }

    private JLabel lbl(String texto) {
        JLabel l = new JLabel(texto); l.setForeground(TEXT); return l;
    }

    private void estilizarCampo(JTextField f) {
        f.setBackground(BG_APP); f.setForeground(TEXT); f.setCaretColor(TEXT);
    }

    // Ações soltas do Ribbon (Falhas, Manutenções, Peças)
    private void registrarFalha() {
        try {
            Integer idMaq = lerInteiro("ID da Máquina quebrada:");
            if (idMaq == null) return;
            String desc = JOptionPane.showInputDialog(this, "O que aconteceu?");
            if (desc == null) return;
            service.registrarFalha(desc, idMaq);
            atualizarTudo();
        } catch (Exception e) { mostrarErro(e); }
    }

    private void abrirManutencao() {
        try {
            Integer idMaq = lerInteiro("ID da Máquina:");
            if (idMaq == null) return;
            String desc = JOptionPane.showInputDialog(this, "Descrição do serviço:");
            String causa = JOptionPane.showInputDialog(this, "Causa raiz:");
            service.abrirCorretiva(desc, idMaq, causa, "ALTA");
            atualizarTudo();
        } catch (Exception e) { mostrarErro(e); }
    }

    private void atribuirTecnico() {
        try {
            Integer idMan = lerInteiro("ID da Manutenção:");
            if (idMan == null) return;
            Integer idTec = lerInteiro("ID do Técnico:");
            if (idTec == null) return;
            service.atribuirTecnico(idMan, idTec);
            atualizarTudo();
        } catch (Exception e) { mostrarErro(e); }
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
            atualizarTudo();
        } catch (Exception e) { mostrarErro(e); }
    }

    private void concluirManutencao() {
        try {
            Integer idMan = lerInteiro("ID da Manutenção:");
            if (idMan == null) return;
            service.concluirManutencao(idMan);
            atualizarTudo();
        } catch (Exception e) { mostrarErro(e); }
    }

    private void cadastrarTecnico() { /* ... Mantido igual ... */ }
    private void cadastrarTipoTecnico() { /* ... Mantido igual ... */ }
    private void cadastrarPeca() { /* ... Mantido igual ... */ }
    private void excluirMaquina() { /* ... Mantido igual ... */ }

    private void abrirRelatorioCusto() {
        StringBuilder sb = new StringBuilder("RELATÓRIO DE CUSTO ACUMULADO\n\n");
        double total = 0;
        try {
            for (Manutencao m : service.getManutencoes()) {
                sb.append(String.format("#%03d [%s] R$ %,.2f%n", m.getId(), m.getTipo(), m.getCusto()));
                total += m.getCusto();
            }
        } catch (Exception e) { mostrarErro(e); return; }
        sb.append("\nTOTAL: R$ ").append(String.format("%,.2f", total));
        JOptionPane.showMessageDialog(this, new JTextArea(sb.toString()), "Custos", JOptionPane.INFORMATION_MESSAGE);
    }

    private void abrirRelatorioHistorico() { /* ... Mantido igual ... */ }

        private void exibirTutorial() {
        String texto =
            "═══════════════════════════════════════════════════════════════════════\n"
          + "           CENTRAL DE AJUDA INTEGRADA - SISTEMA DE MANUTENÇÃO v3.0\n"
          + "═══════════════════════════════════════════════════════════════════════\n\n"
          
          + "🚀 NOVIDADES DA VERSÃO 3.0 (MEGA UPDATE)\n"
          + "Esta versão marca a transição de um protótipo para um sistema corporativo.\n"
          + " • Simulador de Tempo (Relógio do Sistema)\n"
          + " • Cálculo de Depreciação e Saúde Realista\n"
          + " • Abertura Automática de Manutenção Preventiva\n"
          + " • Fichas Flutuantes (JInternalFrame) para Máquinas e Técnicos\n"
          + " • Filtro de Busca (Lupinha) em todas as tabelas\n"
          + " • Gerador de Relatórios por Seleção com exportação TXT\n\n"
          
          + "1. PERFIS DE ACESSO E PERMISSÕES\n"
          + " • ADMINISTRADOR (admin/admin123): Acesso total. Exclusivo para ele:\n"
          + "   - Alterar a data do sistema (simulação);\n"
          + "   - Excluir máquinas;\n"
          + "   - Emitir Relatórios Gerenciais e Históricos.\n"
          + " • OPERADOR (operador/op123): Pode fazer todos os cadastros, registrar\n"
          + "   falhas, abrir manutenções, atribuir técnicos e usar peças.\n\n"
          
          + "2. O RELÓGIO DO SISTEMA E A DEPRECIAÇÃO (ETAPA 1)\n"
          + " • O sistema calcula a saúde do equipamento baseada na depreciação real.\n"
          + " • Fórmula: Saúde = 100 - (Dias de Uso / (Vida Útil do Tipo * 365)) * 100.\n"
          + " • Para testar o futuro, vá em Administração → ⏰ Alterar Data.\n"
          + " • Você pode avançar +1 mês ou +1 ano. A barra inferior ficará LARANJA\n"
          + "   indicando 'DATA SIMULADA'. Para voltar, clique em 'Voltar para Hoje'.\n\n"
          
          + "3. INTELIGÊNCIA PREDITIVA (ETAPA 2)\n"
          + " • Não existe mais o botão manual de abrir preventiva.\n"
          + " • O sistema vigia as máquinas. Se uma máquina ATIVA atingir\n"
          + "   a saúde abaixo de 30%, o sistema abrirá uma Preventiva sozinho!\n"
          + " • Na tela Preditiva, você vê as barras vermelhas e amarelas de alerta.\n\n"
          
          + "4. AS FICHAS FLUTUANTES (ETAPA 5)\n"
          + " • Vá na aba 'Dados' e dê DUPLO CLIQUE em uma Máquina ou Técnico.\n"
          + " • Abrirá uma janela interna que você pode arrastar, minimizar ou maximizar.\n"
          + " • Nessas fichas, você pode realizar ações diretas (ex: registrar falha ou\n"
          + "   alocar um técnico) sem precisar voltar aos menus superiores.\n\n"
          
          + "5. A LUPINHA (ETAPA 4)\n"
          + " • Em cima de todas as tabelas existe um campo de busca.\n"
          + " • Digite qualquer letra (ex: parte do número de série) e a tabela filtrará\n"
          + "   na hora, sem diferenciar maiúsculas de minúsculas.\n\n"
          
          + "6. FLUXO BÁSICO DE UTILIZAÇÃO (PASSO A PASSO)\n"
          + "   Passo 1: Vá em Cadastros → Tipo de Máquina. Defina a vida útil (ex: 5 anos)\n"
          + "            e marque no checklist quais peças são recomendadas para ele.\n"
          + "   Passo 2: Cadastre suas Máquinas (informando Data e Valor de Aquisição),\n"
          + "            Técnicos e Peças no estoque.\n"
          + "   Passo 3: Se a máquina quebrar, use 'Manutenção → Registrar Falha'.\n"
          + "   Passo 4: Abra uma Ordem de Serviço (Corretiva) para essa máquina.\n"
          + "   Passo 5: Atribua um Técnico disponível e dê baixa em Peças usadas.\n"
          + "   Passo 6: Conclua a manutenção. O sistema calculará o custo final\n"
          + "            (Mão de obra + Peças), liberará o técnico e consertará a máquina.\n"
          + "   Passo 7: Vá em Administração → 📊 Relatório Gerencial. Selecione as\n"
          + "            máquinas e exporte o arquivo TXT com todos os custos e métricas.\n\n"
          
          + "═══════════════════════════════════════════════════════════════════════\n"
          + " Desenvolvido para a disciplina de POO - UNESP Sorocaba (2026)\n"
          + "═══════════════════════════════════════════════════════════════════════";

        JTextArea ta = new JTextArea(texto);
        ta.setEditable(false);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
        ta.setBackground(BG_CARD);       // Fundo escuro/claro correto
        ta.setForeground(TEXT);          // Letra branca/escura correta
        ta.setCaretColor(TEXT);
        ta.setCaretPosition(0);

        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(800, 600)); // Janela mais espaçosa
        sp.setBorder(new LineBorder(BORDER, 1));

        JOptionPane.showMessageDialog(this, sp, "Central de Ajuda v3.0", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignore) {}
        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }
}