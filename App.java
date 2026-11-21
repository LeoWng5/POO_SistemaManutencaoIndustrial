import classes.*;
import exception.*;
import service.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicButtonUI;
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

    private JLabel lblDataStatus;

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

        aplicarTemaGlobal();

        setTitle("🏭 Gestão de Manutenção Industrial — v2.0");
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
        b.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(6, 14, 6, 14)));
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
        JLabel sub = new JLabel("v2.0 — SQLite", SwingConstants.CENTER);
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
        chkDark.addActionListener(e -> {
            darkMode = chkDark.isSelected();
            temaLogin.run();
        });

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
        try { return Sessao.getInstance().isAdmin(); }
        catch (Throwable t) { return false; }
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
                for (JButton x : tabBtns) {
                    x.setBackground(TAB_BG);
                    x.setForeground(TEXT);
                }
                tb.setBackground(BG_CARD);
                tb.setForeground(PRIMARY);
                if (nome.equals("Início") && cardCentral != null) {
                    cardCentral.show(painelCentral, "DASH");
                }
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
                    p.add(ribbonBtn("⏰ Alterar Data", e -> alterarDataSistema()));
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
        estilizarBotao(b, BG_CARD, TEXT);
        b.setFont(F_BODY);
        b.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(10, 14, 10, 14)));
        b.addActionListener(a);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(TAB_BG); }
            public void mouseExited(MouseEvent e) { b.setBackground(BG_CARD); }
        });
        return b;
    }

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

            JPanel predCard = new JPanel(new BorderLayout(0, 6));
            predCard.setBackground(BG_CARD);
            predCard.setBorder(new CompoundBorder(
                    new LineBorder(BORDER, 1, true), new EmptyBorder(16, 20, 16, 20)));
            JLabel lp = new JLabel("Alerta Preditivo");
            lp.setFont(F_SUBT); lp.setForeground(MUTED);
            JLabel lpInfo = new JLabel("Veja quais equipamentos estão mais próximos da falha.");
            lpInfo.setFont(F_BODY); lpInfo.setForeground(TEXT);
            JButton btnVer = new JButton("Abrir Lista →");
            estilizarBotao(btnVer, PRIMARY, Color.WHITE);
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

        void setValores(int[] v) { this.valores = v; repaint(); }

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

    private JPanel buildPreditivaPanel() {
        JPanel painel = new JPanel(new BorderLayout(0, 12));
        painel.setBackground(BG_APP);
        painel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel titulo = new JLabel("Manutenção Preditiva — Saúde dos Equipamentos");
        titulo.setFont(F_TITLE);
        titulo.setForeground(TEXT);

        JLabel sub = new JLabel("Equipamentos ordenados do mais crítico para o menos crítico "
                + "(saúde calculada pela vida útil do tipo).");
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
            ms.sort(Comparator.comparingInt(Maquina::getSaudeEquipamento));
            if (ms.isEmpty()) {
                JLabel vazio = new JLabel("Nenhum equipamento cadastrado.");
                vazio.setForeground(MUTED);
                vazio.setFont(F_BODY);
                painelPreditivaLista.add(vazio);
            }
            for (Maquina m : ms) {
                int s = m.getSaudeEquipamento();
                painelPreditivaLista.add(cardPreditiva(m, s));
                painelPreditivaLista.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        } catch (Exception e) {
            mostrarErro(e);
        }
        painelPreditivaLista.revalidate();
        painelPreditivaLista.repaint();
    }

    private JPanel cardPreditiva(Maquina m, int saude) {
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
        pb.setValue(saude);
        pb.setForeground(cor);
        pb.setBackground(BORDER);
        pb.setStringPainted(true);
        pb.setString(saude + "% de saúde");
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

    private JPanel buildTabelasPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_APP);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(F_SUBT);
        tabs.setBackground(BG_CARD);
        tabs.setForeground(TEXT);

        modeloMaquinas = new DefaultTableModel(
                new String[]{"ID", "Nº Série", "Tipo", "Fabricante", "Status", "Saúde (%)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabMaq = criarTabela(modeloMaquinas);
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
        tabs.addTab("Máquinas", criarScroll(tabMaq));

        modeloTecnicos = new DefaultTableModel(
                new String[]{"ID", "Nome", "Especialidade", "Telefone", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabTec = criarTabela(modeloTecnicos);
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
        tabs.addTab("Técnicos", criarScroll(tabTec));

        modeloPecas = new DefaultTableModel(
                new String[]{"ID", "Nome", "Categoria", "Qtd. Estoque", "Valor Unit. (R$)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabPec = criarTabela(modeloPecas);
        tabs.addTab("Estoque de Peças", criarScroll(tabPec));

        modeloManutencoes = new DefaultTableModel(
                new String[]{"ID", "Tipo", "Máquina", "Técnico", "Status", "Custo (R$)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabMan = criarTabela(modeloManutencoes);
        tabs.addTab("Manutenções", criarScroll(tabMan));

        p.add(tabs, BorderLayout.CENTER);
        return p;
    }

    private JTable criarTabela(DefaultTableModel modelo) {
        JTable table = new JTable(modelo);
        table.setRowHeight(24);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT);
        table.setGridColor(BORDER);
        table.setSelectionBackground(PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setBackground(TAB_BG);
        table.getTableHeader().setForeground(TEXT);
        table.getTableHeader().setFont(F_SUBT);
        return table;
    }

    private JScrollPane criarScroll(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(new LineBorder(BORDER, 1));
        return sp;
    }

    private void atualizarTudo() {
        atualizarTabelas();
        if (dashboardPanel != null) dashboardPanel.atualizar();
        if (painelPreditivaLista != null) atualizarPreditiva();
        atualizarLabelData();
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
                    m.getStatus(), m.getSaudeEquipamento() + "%"
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

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(TAB_BG);
        bar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(6, 16, 6, 16)));

        String perfil = isAdmin() ? "ADMINISTRADOR" : "OPERADOR";
        JLabel lbl = new JLabel("👤 Sessão: " + perfil);
        lbl.setFont(F_BODY);
        lbl.setForeground(MUTED);
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
            lblDataStatus.setText("DATA SIMULADA: " + fmt);
            lblDataStatus.setForeground(ACCENT);
        } else {
            lblDataStatus.setText(fmt);
            lblDataStatus.setForeground(MUTED);
        }
    }

    private void alterarDataSistema() {
        if (!isAdmin()) {
            JOptionPane.showMessageDialog(this, "Apenas ADMINISTRADOR pode alterar a data.",
                    "Acesso negado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JDialog dlg = new JDialog(this, "Alterar Data do Sistema", true);
        dlg.setSize(420, 260);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(BG_CARD);

        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd/MM/yyyy");
        spinner.setEditor(editor);
        spinner.setValue(java.sql.Date.valueOf(RelogioSistema.hoje()));
        editor.getTextField().setBackground(BG_APP);
        editor.getTextField().setForeground(TEXT);

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.setBackground(BG_CARD);
        topo.setBorder(new EmptyBorder(12, 16, 4, 16));
        JLabel l = new JLabel("Data:");
        l.setForeground(TEXT);
        topo.add(l);
        topo.add(spinner);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botoes.setBackground(BG_CARD);
        botoes.setBorder(new EmptyBorder(0, 12, 0, 12));
        JButton btnMes = new JButton("+1 mês");
        estilizarBotao(btnMes, BG_APP, TEXT);
        btnMes.addActionListener(e -> {
            Calendar c = Calendar.getInstance();
            c.setTime((Date) spinner.getValue());
            c.add(Calendar.MONTH, 1);
            spinner.setValue(c.getTime());
        });
        JButton btnAno = new JButton("+1 ano");
        estilizarBotao(btnAno, BG_APP, TEXT);
        btnAno.addActionListener(e -> {
            Calendar c = Calendar.getInstance();
            c.setTime((Date) spinner.getValue());
            c.add(Calendar.YEAR, 1);
            spinner.setValue(c.getTime());
        });
        JButton btnReset = new JButton("Voltar para hoje");
        estilizarBotao(btnReset, BG_APP, TEXT);
        btnReset.addActionListener(e -> spinner.setValue(java.sql.Date.valueOf(LocalDate.now())));
        botoes.add(btnMes); botoes.add(btnAno); botoes.add(btnReset);

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        rodape.setBackground(BG_CARD);
        JButton btnCancelar = new JButton("Cancelar");
        estilizarBotao(btnCancelar, BG_APP, TEXT);
        btnCancelar.addActionListener(e -> dlg.dispose());
        JButton btnOk = new JButton("Aplicar");
        estilizarBotao(btnOk, PRIMARY, Color.WHITE);
        btnOk.addActionListener(e -> {
            Date d = (Date) spinner.getValue();
            LocalDate ld = new java.sql.Date(d.getTime()).toLocalDate();
            RelogioSistema.setData(ld);
            dlg.dispose();
            atualizarTudo();
        });
        rodape.add(btnCancelar);
        rodape.add(btnOk);

        dlg.add(topo, BorderLayout.NORTH);
        dlg.add(botoes, BorderLayout.CENTER);
        dlg.add(rodape, BorderLayout.SOUTH);
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

    private JLabel lbl(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(TEXT);
        return l;
    }

    private void estilizarCampo(JTextField f) {
        f.setBackground(BG_APP);
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
    }

    private void mostrarDetalhesMaquina(Maquina m) {
        JDialog d = new JDialog(this, "Detalhes da Máquina", true);
        d.setSize(620, 440);
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(BG_CARD);

        JPanel info = new JPanel(new GridLayout(0, 2, 8, 6));
        info.setBackground(BG_CARD);
        info.setBorder(new EmptyBorder(16, 16, 16, 16));
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        info.add(lbl("ID:"));                info.add(lbl(String.valueOf(m.getId())));
        info.add(lbl("Tipo:"));              info.add(lbl(m.getNome()));
        info.add(lbl("S/N:"));               info.add(lbl(m.getNumeroSerie()));
        info.add(lbl("Fabricante:"));        info.add(lbl(m.getFabricante()));
        info.add(lbl("Status:"));            info.add(lbl(m.getStatus()));
        info.add(lbl("Saúde:"));             info.add(lbl(m.getSaudeEquipamento() + "%"));
        info.add(lbl("Data de Aquisição:")); info.add(lbl(m.getDataAquisicao().format(df)));
        info.add(lbl("Valor de Aquisição:"));info.add(lbl(nf.format(m.getValorAquisicao())));
        info.add(lbl("Valor Residual:"));    info.add(lbl(nf.format(m.getValorResidual())));

        JLabel foto = new JLabel("Sem foto", SwingConstants.CENTER);
        foto.setForeground(MUTED);
        foto.setPreferredSize(new Dimension(240, 180));
        foto.setBorder(new LineBorder(BORDER, 1, true));
        foto.setOpaque(true);
        foto.setBackground(TAB_BG);
        try {
            String path = m.getFoto();
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
        info.add(lbl("ID:"));            info.add(lbl(String.valueOf(t.getId())));
        info.add(lbl("Nome:"));          info.add(lbl(t.getNome()));
        info.add(lbl("Especialidade:")); info.add(lbl(t.getEspecialidade()));
        info.add(lbl("Telefone:"));      info.add(lbl(t.getTelefone()));
        info.add(lbl("Disponível:"));    info.add(lbl(t.isDisponivel() ? "Sim" : "Não"));

        JLabel foto = new JLabel("Sem foto", SwingConstants.CENTER);
        foto.setForeground(MUTED);
        foto.setPreferredSize(new Dimension(240, 180));
        foto.setBorder(new LineBorder(BORDER, 1, true));
        foto.setOpaque(true);
        foto.setBackground(TAB_BG);
        try {
            String path = t.getFoto();
            ImageIcon ic = carregarMiniatura(path, 240, 180);
            if (ic != null) { foto.setIcon(ic); foto.setText(""); }
        } catch (Throwable ignore) {}

        d.add(info, BorderLayout.CENTER);
        d.add(foto, BorderLayout.EAST);
        d.setVisible(true);
    }

    private void cadastrarTipoMaquina() {
        try {
            JTextField txtNome = new JTextField();
            JTextField txtFab = new JTextField();
            JSpinner spVida = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
            Object[] msg = {
                "Nome do tipo de máquina:", txtNome,
                "Fabricante padrão:", txtFab,
                "Vida útil (anos):", spVida
            };
            int r = JOptionPane.showConfirmDialog(this, msg, "Novo Tipo de Máquina",
                    JOptionPane.OK_CANCEL_OPTION);
            if (r != JOptionPane.OK_OPTION) return;
            String nome = txtNome.getText().trim();
            if (nome.isEmpty()) return;
            service.cadastrarTipoMaquina(new TipoMaquina(0, nome,
                    txtFab.getText().trim(), (int) spVida.getValue()));
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

    private void cadastrarMaquina() {
        try {
            List<TipoMaquina> tipos = service.getTiposMaquina();
            if (tipos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cadastre um Tipo de Máquina antes.",
                        "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JDialog dlg = new JDialog(this, "Cadastrar Máquina", true);
            dlg.setSize(500, 640);
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

            g.gridy = 0; form.add(lbl("Tipo de Máquina:"), g);
            JComboBox<TipoMaquina> cbTipo = new JComboBox<>(tipos.toArray(new TipoMaquina[0]));
            cbTipo.setBackground(BG_APP); cbTipo.setForeground(TEXT);
            g.gridy = 1; form.add(cbTipo, g);

            g.gridy = 2; form.add(lbl("Data de Aquisição:"), g);
            SpinnerDateModel dateModel = new SpinnerDateModel();
            JSpinner spData = new JSpinner(dateModel);
            JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spData, "dd/MM/yyyy");
            spData.setEditor(dateEditor);
            spData.setValue(java.sql.Date.valueOf(RelogioSistema.hoje()));
            dateEditor.getTextField().setBackground(BG_APP);
            dateEditor.getTextField().setForeground(TEXT);
            g.gridy = 3; form.add(spData, g);

            g.gridy = 4; form.add(lbl("Valor de Aquisição (R$):"), g);
            JTextField txtValor = new JTextField("0,00");
            estilizarCampo(txtValor);
            g.gridy = 5; form.add(txtValor, g);

            g.gridy = 6; form.add(lbl("Número de Série:"), g);
            JTextField txtSerie = new JTextField();
            estilizarCampo(txtSerie);
            g.gridy = 7; form.add(txtSerie, g);

            g.gridy = 8; form.add(lbl("Foto (opcional):"), g);
            JPanel fotoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            fotoPanel.setBackground(BG_CARD);
            JLabel preview = new JLabel("Sem foto", SwingConstants.CENTER);
            preview.setForeground(MUTED);
            preview.setPreferredSize(new Dimension(160, 100));
            preview.setBorder(new LineBorder(BORDER, 1, true));
            preview.setOpaque(true);
            preview.setBackground(TAB_BG);
            JButton btnFoto = new JButton("Escolher...");
            estilizarBotao(btnFoto, BG_APP, TEXT);
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
            g.gridy = 9; form.add(fotoPanel, g);

            JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            botoes.setBackground(BG_CARD);
            JButton btnCancelar = new JButton("Cancelar");
            estilizarBotao(btnCancelar, BG_APP, TEXT);
            JButton btnSalvar = new JButton("Salvar");
            estilizarBotao(btnSalvar, PRIMARY, Color.WHITE);
            botoes.add(btnCancelar);
            botoes.add(btnSalvar);

            btnCancelar.addActionListener(e -> dlg.dispose());
            btnSalvar.addActionListener(e -> {
                try {
                    String serie = txtSerie.getText().trim();
                    if (serie.isEmpty()) throw new RuntimeException("Informe o número de série.");
                    TipoMaquina tipo = (TipoMaquina) cbTipo.getSelectedItem();

                    Date d = (Date) spData.getValue();
                    LocalDate dataAq = new java.sql.Date(d.getTime()).toLocalDate();
                    if (dataAq.isAfter(RelogioSistema.hoje())) {
                        throw new RuntimeException("Data de aquisição não pode ser futura.");
                    }

                    double valor;
                    try {
                        valor = Double.parseDouble(txtValor.getText().trim().replace(',', '.'));
                    } catch (NumberFormatException ex) {
                        throw new RuntimeException("Valor de aquisição inválido.");
                    }
                    if (valor < 0) throw new RuntimeException("Valor não pode ser negativo.");

                    service.cadastrarMaquina(new Maquina(0, serie, "ATIVA",
                            dataAq, valor, tipo, fotoPath[0]));
                    dlg.dispose();
                    JOptionPane.showMessageDialog(this, "Máquina cadastrada!");
                    atualizarTudo();
                } catch (RuntimeException ex) { mostrarErro(ex); }
            });

            dlg.add(criarScrollForm(form), BorderLayout.CENTER);
            dlg.add(botoes, BorderLayout.SOUTH);
            dlg.setVisible(true);
        } catch (RuntimeException e) { mostrarErro(e); }
    }

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

            g.gridy = 0; form.add(lbl("Especialidade:"), g);
            JComboBox<TipoTecnico> cbTipo = new JComboBox<>(tipos.toArray(new TipoTecnico[0]));
            cbTipo.setBackground(BG_APP); cbTipo.setForeground(TEXT);
            g.gridy = 1; form.add(cbTipo, g);

            g.gridy = 2; form.add(lbl("Nome:"), g);
            JTextField txtNome = new JTextField();
            estilizarCampo(txtNome);
            g.gridy = 3; form.add(txtNome, g);

            g.gridy = 4; form.add(lbl("Telefone:"), g);
            JTextField txtTel = new JTextField();
            estilizarCampo(txtTel);
            g.gridy = 5; form.add(txtTel, g);

            g.gridy = 6; form.add(lbl("Foto (opcional):"), g);
            JPanel fotoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            fotoPanel.setBackground(BG_CARD);
            JLabel preview = new JLabel("Sem foto", SwingConstants.CENTER);
            preview.setForeground(MUTED);
            preview.setPreferredSize(new Dimension(160, 100));
            preview.setBorder(new LineBorder(BORDER, 1, true));
            preview.setOpaque(true);
            preview.setBackground(TAB_BG);
            JButton btnFoto = new JButton("Escolher...");
            estilizarBotao(btnFoto, BG_APP, TEXT);
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
            estilizarBotao(btnCancelar, BG_APP, TEXT);
            JButton btnSalvar = new JButton("Salvar");
            estilizarBotao(btnSalvar, PRIMARY, Color.WHITE);
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

            dlg.add(criarScrollForm(form), BorderLayout.CENTER);
            dlg.add(botoes, BorderLayout.SOUTH);
            dlg.setVisible(true);
        } catch (RuntimeException e) { mostrarErro(e); }
    }

    private JScrollPane criarScrollForm(JPanel form) {
        JScrollPane sp = new JScrollPane(form);
        sp.setBorder(null);
        sp.getViewport().setBackground(BG_CARD);
        return sp;
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

    private JScrollPane areaTexto(String texto, int w, int h) {
        JTextArea ta = new JTextArea(texto);
        ta.setEditable(false);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
        ta.setBackground(BG_CARD);
        ta.setForeground(TEXT);
        ta.setCaretPosition(0);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(w, h));
        return sp;
    }

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

        JOptionPane.showMessageDialog(this, areaTexto(sb.toString(), 600, 400),
                "Relatório de Custo", JOptionPane.INFORMATION_MESSAGE);
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

        JOptionPane.showMessageDialog(this, areaTexto(sb.toString(), 780, 420),
                "Histórico de Manutenções", JOptionPane.INFORMATION_MESSAGE);
    }

        private void exibirTutorial() {
        String texto =
            "═══════════════════════════════════════════════════════════════════════\n"
          + "                 CENTRAL DE AJUDA E DÚVIDAS (FAQ)\n"
          + "             Sistema de Manutenção Industrial v2.0\n"
          + "═══════════════════════════════════════════════════════════════════════\n\n"
          
          + "1. COMO FUNCIONA O RELÓGIO E A SIMULAÇÃO DE DATA?\n"
          + "   • Por padrão, o sistema usa a data real do seu computador.\n"
          + "   • Para testar cenários futuros, vá em: Administração → ⏰ Alterar Data.\n"
          + "   • Lá você pode usar os atalhos (+1 mês, +1 ano) ou escolher uma data exata.\n"
          + "   • Quando a data está simulada, a barra inferior fica LARANJA com o aviso\n"
          + "     'DATA SIMULADA'. Para voltar ao normal, clique em 'Voltar para hoje'.\n"
          + "   • ATENÇÃO: A data simulada afeta todo o sistema (cadastros e manutenções).\n\n"
          
          + "2. QUAL A DIFERENÇA ENTRE O PERFIL ADM E O OPERADOR?\n"
          + "   • ADMINISTRADOR (ADM): Tem acesso total. É o único que pode:\n"
          + "       - Alterar a data do sistema (simulação);\n"
          + "       - Excluir máquinas do sistema;\n"
          + "       - Emitir o Relatório Histórico completo.\n"
          + "   • OPERADOR: Pode cadastrar máquinas, técnicos, peças, registrar falhas,\n"
          + "     atribuir técnicos, usar peças e concluir manutenções.\n\n"
          
          + "3. COMO FUNCIONA A SAÚDE E A DEPRECIAÇÃO DA MÁQUINA?\n"
          + "   • A saúde do equipamento é calculada automaticamente baseada na idade dele.\n"
          + "   • Fórmula: 100 − (dias de uso ÷ (vida útil × 365)) × 100.\n"
          + "   • A 'vida útil' é definida lá no cadastro do 'Tipo de Máquina' (padrão 5 anos).\n"
          + "   • Uma máquina com 4 anos de uso (e vida útil de 5 anos) terá 20% de saúde.\n"
          + "   • Se a data do sistema for avançada, a saúde cai. Se voltar para hoje,\n"
          + "     a saúde é recalculada com a data correta.\n\n"
          
          + "4. QUAL A DIFERENÇA ENTRE MANUTENÇÃO CORRETIVA E PREVENTIVA?\n"
          + "   • CORRETIVA: Aberta quando a máquina quebra (você clica em 'Registrar Falha'\n"
          + "     e depois em 'Abrir Corretiva'). A máquina fica com status QUEBRADA.\n"
          + "   • PREVENTIVA: Aberta para evitar que a máquina quebre. Possui uma data\n"
          + "     de próxima manutenção programada (periodicidade).\n\n"
          
          + "5. COMO CONCLUIR UMA MANUTENÇÃO?\n"
          + "   • Vá em 'Manutenção → Concluir'. Informe o ID da manutenção.\n"
          + "   • O sistema calculará o custo final (mão de obra + peças usadas),\n"
          + "     devolverá a máquina para o status ATIVA e liberará o técnico.\n\n"
          
          + "6. FLUXO BÁSICO RECOMENDADO (PASSO A PASSO)\n"
          + "   Passo 1: Cadastros → Tipo de Máquina (define vida útil e peças padrão)\n"
          + "   Passo 2: Cadastros → Tipo de Técnico, Máquina, Técnico e Peças.\n"
          + "   Passo 3: Manutenção → Registrar Falha (se a máquina quebrou).\n"
          + "   Passo 4: Manutenção → Abrir Corretiva ou Abrir Preventiva.\n"
          + "   Passo 5: Manutenção → Atribuir Técnico (técnico fica OCUPADO).\n"
          + "   Passo 6: Manutenção → Usar Peça (baixa no estoque).\n"
          + "   Passo 7: Manutenção → Concluir (custo gerado e máquina consertada).\n"
          + "═══════════════════════════════════════════════════════════════════════";

        JOptionPane.showMessageDialog(this, areaTexto(texto, 720, 520),
                "Tutorial e Ajuda", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignore) {}
        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }
}