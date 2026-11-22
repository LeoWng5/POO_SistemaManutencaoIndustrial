import classes.*;
import service.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.metal.MetalLookAndFeel;
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
    static Color BG_APP, BG_CARD, PRIMARY, PRIMARY_D, ACCENT, DANGER, SUCCESS, TEXT, MUTED, BORDER, TAB_BG, HOVER;

    /** Cor de face clássica do Look and Feel CrossPlatform (Metal). */
    static final Color CLASSIC_BG = resolverCorClassica();

    /** Resolve a cor de face da paleta atual do L&F (com fallback seguro). */
    private static Color resolverCorClassica() {
        try {
            Class<?> metal = Class.forName("javax.swing.plaf.metal.MetalLookAndFeel");
            Object control = metal.getMethod("getControl").invoke(null); // face do Metal
            return (Color) control;
        } catch (Throwable ignore) {
            return new Color(0xC0, 0xC0, 0xC0); // fallback: cinza clássico genérico
        }
    }

    /** Cache da cor clássica obtida em tempo real para os painéis das fichas/diálogos. */
    private Color classicBgCache;

    /** Cor clássica ativa (L&F/Metal) usada nos cards flutuantes e nos diálogos clássicos. */
    private Color classicBg() {
        if (classicBgCache == null) {
            Color ui = UIManager.getColor("Panel.background");
            classicBgCache = (ui != null) ? ui : CLASSIC_BG;
        }
        return classicBgCache;
    }

    static {
        aplicarCores(false);
    }

    private static void aplicarCores(boolean dark) {
        if (dark) {
            BG_APP    = new Color(0x0F172A);
            BG_CARD   = new Color(0x1E293B);
            TEXT      = new Color(0xF1F5F9);
            MUTED     = new Color(0x94A3B8);
            BORDER    = new Color(0x334155);
            TAB_BG    = new Color(0x0F172A);
            HOVER     = new Color(0x334155);
            PRIMARY   = new Color(0x3B82F6);
            PRIMARY_D = new Color(0x2563EB);
        } else {
            BG_APP    = new Color(0xF8FAFC);
            BG_CARD   = Color.WHITE;
            TEXT      = new Color(0x0F172A);
            MUTED     = new Color(0x64748B);
            BORDER    = new Color(0xE2E8F0);
            TAB_BG    = new Color(0xF1F5F9);
            HOVER     = new Color(0xE2E8F0);
            PRIMARY   = new Color(0x2563EB);
            PRIMARY_D = new Color(0x1D4ED8);
        }
        ACCENT  = new Color(0xF59E0B);
        DANGER  = new Color(0xEF4444);
        SUCCESS = new Color(0x10B981);
    }

    private static void aplicarTemaGlobal() {
        if (!darkMode) return;
        UIManager.put("Panel.background", BG_CARD);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Label.background", BG_CARD);
        UIManager.put("TextField.background", BG_APP);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", TEXT);
        UIManager.put("TextField.selectionBackground", PRIMARY);
        UIManager.put("TextField.selectionForeground", Color.WHITE);
        UIManager.put("PasswordField.background", BG_APP);
        UIManager.put("PasswordField.foreground", TEXT);
        UIManager.put("PasswordField.caretForeground", TEXT);
        UIManager.put("TextArea.background", BG_CARD);
        UIManager.put("TextArea.foreground", TEXT);
        UIManager.put("TextArea.caretForeground", TEXT);
        UIManager.put("ComboBox.background", BG_APP);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ComboBox.selectionBackground", PRIMARY);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("Button.background", TAB_BG);
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("Button.select", PRIMARY_D);
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));
        UIManager.put("OptionPane.background", BG_CARD);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("OptionPane.messageBackground", BG_CARD);
        UIManager.put("ScrollPane.background", BG_CARD);
        UIManager.put("ScrollPane.border", new LineBorder(BORDER, 1));
        UIManager.put("Viewport.background", BG_CARD);
        UIManager.put("TabbedPane.background", BG_APP);
        UIManager.put("TabbedPane.foreground", TEXT);
        UIManager.put("TabbedPane.contentAreaColor", BG_CARD);
    }

    static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    static final Font F_SUBT  = new Font("Segoe UI", Font.BOLD, 13);
    static final Font F_BODY  = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font F_BIG   = new Font("Segoe UI", Font.BOLD, 34);

    private static final int LIMITE_SAUDE_PREVENTIVA = 30;

    private SistemaService service;

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

        setTitle("🏭 Gestão de Manutenção Industrial — v3.0");
        setSize(1366, 820);
        setMinimumSize(new Dimension(1150, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_APP);

        add(buildRibbon(), BorderLayout.NORTH);
        add(buildMain(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        atualizarTudo();
    }

    // ==========================================
    // HELPERS DE ESTILIZAÇÃO E DIÁLOGOS
    // ==========================================

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

    private void estilizarSpinner(JSpinner sp) {
        sp.setBackground(BG_APP);
        sp.setForeground(TEXT);
        sp.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(2, 4, 2, 2)));
        if (sp.getEditor() instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) sp.getEditor()).getTextField();
            tf.setBackground(BG_APP);
            tf.setForeground(TEXT);
            tf.setCaretColor(TEXT);
            tf.setBorder(new EmptyBorder(2, 4, 2, 4));
        }
        for (Component c : sp.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                b.setBackground(TAB_BG);
                b.setForeground(TEXT);
                b.setBorder(new EmptyBorder(0, 2, 0, 2));
            }
        }
    }

    private void estilizarCombo(JComboBox<?> cb) {
        cb.setBackground(BG_APP);
        cb.setForeground(TEXT);
        cb.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(2, 6, 2, 6)));
        
        for (Component c : cb.getComponents()) {
            if (c instanceof JButton) {
                c.setBackground(BG_APP);
            }
        }
        
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(new EmptyBorder(4, 8, 4, 8));
                if (isSelected) {
                    lbl.setBackground(PRIMARY);
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(BG_APP);
                    lbl.setForeground(TEXT);
                }
                return lbl;
            }
        });
    }

    private void estilizarTabs(JTabbedPane tabs) {
        tabs.setBackground(BG_APP);
        tabs.setForeground(TEXT);
        tabs.setFont(F_SUBT);
        if (!darkMode) return;

        tabs.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                g.setColor(isSelected ? BG_CARD : TAB_BG);
                g.fillRect(x, y, w, h);
            }
            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                g.setColor(isSelected ? PRIMARY : TEXT);
                g.setFont(font);
                g.drawString(title, textRect.x, textRect.y + metrics.getAscent());
            }
            @Override
            protected void paintContentBorderTopEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
                g.setColor(BORDER);
                g.drawLine(x, y, x + w, y);
            }
            @Override
            protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) { }
            @Override
            protected void paintContentBorderRightEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) { }
            @Override
            protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) { }
            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) { }
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) { }
        });
    }

    private void estilizarCampo(JTextField f) {
        f.setBackground(BG_APP);
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(4, 6, 4, 6)));
    }

    private void estilizarDialog(Component c) {
        c.setBackground(BG_CARD);
        if (c instanceof JLabel) {
            ((JLabel) c).setForeground(TEXT);
            ((JLabel) c).setBackground(BG_CARD);
        }
        if (c instanceof JTextArea) {
            ((JTextArea) c).setBackground(BG_CARD);
            ((JTextArea) c).setForeground(TEXT);
            ((JTextArea) c).setCaretColor(TEXT);
        }
        if (c instanceof JButton) {
            estilizarBotao((JButton) c, TAB_BG, TEXT);
        }
        if (c instanceof JTextField) {
            estilizarCampo((JTextField) c);
        }
        if (c instanceof JCheckBox) {
            ((JCheckBox) c).setBackground(BG_CARD);
            ((JCheckBox) c).setForeground(TEXT);
        }
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                estilizarDialog(child);
            }
        }
    }

    private void mostrarDialog(Object mensagem, String titulo, int tipo) {
        JOptionPane pane = new JOptionPane(mensagem, tipo);
        JDialog dlg = pane.createDialog(this, titulo);
        estilizarDialog(dlg.getContentPane());
        dlg.setVisible(true);
    }

    private void mostrarInfo(String msg) {
        mostrarDialog(msg, "Informação", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarErro(Exception e) {
        String msg = (e instanceof NumberFormatException) ? "Digite um número válido." : e.getMessage();
        mostrarDialog("Erro: " + msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private String mostrarInput(String mensagem) {
        JTextField txt = new JTextField();
        estilizarCampo(txt);
        Object[] msg = {mensagem, txt};
        JOptionPane pane = new JOptionPane(msg, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dlg = pane.createDialog(this, "Entrada");
        estilizarDialog(dlg.getContentPane());
        dlg.setVisible(true);
        
        Object selectedValue = pane.getValue();
        if(selectedValue == null) return null;
        if(selectedValue instanceof Integer && (Integer)selectedValue == JOptionPane.OK_OPTION) {
            return txt.getText();
        }
        return null;
    }

    private boolean mostrarConfirmacao(String mensagem) {
        JOptionPane pane = new JOptionPane(mensagem, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
        JDialog dlg = pane.createDialog(this, "Confirmação");
        estilizarDialog(dlg.getContentPane());
        dlg.setVisible(true);
        
        Object selectedValue = pane.getValue();
        if(selectedValue == null) return false;
        if(selectedValue instanceof Integer) return (Integer)selectedValue == JOptionPane.YES_OPTION;
        return false;
    }

    // ==========================================
    // LÓGICA DA INTERFACE
    // ==========================================

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
            chkDark.setOpaque(true);
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
        JLabel sub = new JLabel("v3.0 — SQLite", SwingConstants.CENTER);
        sub.setForeground(new Color(0xDBEAFE));
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
                    cardCentral.show(painelCards, "DASH");
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
                    p.add(ribbonBtn("🗑️ Excluir Registro", e -> abrirTelaExclusao()));
                    p.add(ribbonBtn("⏰ Alterar Data", e -> alterarDataSistema()));
                    p.add(ribbonBtn("📊 Relatório Gerencial", e -> abrirRelatorioGerencial()));
                    p.add(ribbonBtn("Histórico", e -> abrirRelatorioHistorico()));
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
        b.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(10, 14, 10, 14)));
        b.addActionListener(a);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(HOVER); }
            public void mouseExited(MouseEvent e)  { b.setBackground(BG_CARD); }
        });
        return b;
    }

    private JPanel buildMain() {
        JLayeredPane layeredPane = new JLayeredPane();
        
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
        desktopPane.setOpaque(false); 

        painelCards.setBounds(0, 0, 1366, 700);
        desktopPane.setBounds(0, 0, 1366, 700);

        layeredPane.add(painelCards, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(desktopPane, JLayeredPane.PALETTE_LAYER);

        layeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                painelCards.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
                desktopPane.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
            }
        });

        cardCentral.show(painelCards, "DASH");

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(layeredPane, BorderLayout.CENTER);
        return wrapper;
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
                NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
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

    private JPanel buildTabelasPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_APP);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JTabbedPane tabs = new JTabbedPane();
        estilizarTabs(tabs);

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

        modeloPecas = new DefaultTableModel(new String[]{"ID", "Nome", "Categoria", "Qtd. Estoque", "Valor Unit. (R$)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabPec = criarTabela(modeloPecas);
        sorterPecas = new TableRowSorter<>(modeloPecas);
        tabPec.setRowSorter(sorterPecas);
        tabs.addTab("Estoque de Peças", painelComBusca(tabPec, sorterPecas));

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
        table.setRowHeight(28);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT);
        table.setGridColor(BORDER);
        table.setSelectionBackground(PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(TAB_BG);
        header.setForeground(TEXT);
        header.setFont(F_SUBT);
        header.setPreferredSize(new Dimension(0, 32));
        
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setBackground(TAB_BG);
                lbl.setForeground(TEXT);
                lbl.setFont(F_SUBT);
                lbl.setHorizontalAlignment(JLabel.LEFT);
                lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
                return lbl;
            }
        };
        header.setDefaultRenderer(headerRenderer);
        
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
                if (isSelected) {
                    lbl.setBackground(PRIMARY);
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(BG_CARD);
                    lbl.setForeground(TEXT);
                }
                return lbl;
            }
        });
        
        return table;
    }

    private JScrollPane criarScroll(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(new LineBorder(BORDER, 1));
        return sp;
    }

    // ==========================================================
    // CARDS FLUTUANTES — LOOK AND FEEL CROSSPLATFORM (METAL)
    // ==========================================================
    private void abrirFichaMaquina(Maquina m) {
        JInternalFrame frame = new JInternalFrame("Máquina: " + m.getNumeroSerie(), true, true, true, true);
        frame.setSize(420, 480);
        frame.setLocation(50 + (desktopPane.getAllFrames().length * 20), 50 + (desktopPane.getAllFrames().length * 20));
        // Deixa o fundo clássico do L&F ativo pintar o contentPane.
        frame.getContentPane().setBackground(classicBg());
        
        JPanel info = new JPanel(new GridLayout(0, 2, 8, 6));
        info.setBackground(classicBg());
        info.setBorder(new EmptyBorder(10, 10, 10, 10));
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
        info.add(lblClassic("Tipo:")); info.add(lblClassic(m.getNome()));
        info.add(lblClassic("Status:")); info.add(lblClassic(m.getStatus()));
        info.add(lblClassic("Saúde:")); info.add(lblClassic(m.getSaudeEquipamento() + "%"));
        info.add(lblClassic("Valor:")); info.add(lblClassic(nf.format(m.getValorAquisicao())));
        info.add(lblClassic("Residual:")); info.add(lblClassic(nf.format(m.getValorResidual())));

        JLabel foto = new JLabel("Sem foto", SwingConstants.CENTER);
        foto.setForeground(Color.BLACK);
        foto.setPreferredSize(new Dimension(240, 180));
        // Borda inferior afundada no estilo dos diálogos clássicos.
        foto.setBorder(new BevelBorder(BevelBorder.LOWERED));
        foto.setOpaque(true);
        foto.setBackground(classicBg());
        try {
            String path = m.getFoto();
            ImageIcon ic = carregarMiniatura(path, 240, 180);
            if (ic != null) { foto.setIcon(ic); foto.setText(""); }
        } catch (Throwable ignore) {}

        JPanel acoes = new JPanel(new GridLayout(0, 1, 0, 8));
        acoes.setBackground(classicBg());
        acoes.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Botões com aparência nativa do L&F ativo (não aplicamos estilizarBotao).
        JButton btnFalha = new JButton("Registrar Falha");
        JButton btnTec = new JButton("Atribuir Técnico");
        JButton btnPeca = new JButton("Usar Peça");
        JButton btnConc = new JButton("Concluir Manutenção");

        acoes.add(btnFalha); acoes.add(btnTec); acoes.add(btnPeca); acoes.add(btnConc);
        
        frame.getContentPane().add(info, BorderLayout.NORTH);
        frame.getContentPane().add(foto, BorderLayout.CENTER);
        frame.getContentPane().add(acoes, BorderLayout.SOUTH);
        
        desktopPane.add(frame);
        frame.setVisible(true);
    }

    private void abrirFichaTecnico(Tecnico t) {
        JInternalFrame frame = new JInternalFrame("Técnico: " + t.getNome(), true, true, true, true);
        frame.setSize(380, 380);
        frame.setLocation(100 + (desktopPane.getAllFrames().length * 20), 100 + (desktopPane.getAllFrames().length * 20));
        frame.getContentPane().setBackground(classicBg());
        
        JPanel info = new JPanel(new GridLayout(0, 2, 8, 6));
        info.setBackground(classicBg());
        info.setBorder(new EmptyBorder(10, 10, 10, 10));
        info.add(lblClassic("Especialidade:")); info.add(lblClassic(t.getEspecialidade()));
        info.add(lblClassic("Telefone:")); info.add(lblClassic(t.getTelefone()));
        info.add(lblClassic("Status:")); info.add(lblClassic(t.isDisponivel() ? "DISPONÍVEL" : "OCUPADO"));

        JLabel foto = new JLabel("Sem foto", SwingConstants.CENTER);
        foto.setForeground(Color.BLACK);
        foto.setPreferredSize(new Dimension(240, 180));
        foto.setBorder(new BevelBorder(BevelBorder.LOWERED));
        foto.setOpaque(true);
        foto.setBackground(classicBg());
        try {
            String path = t.getFoto();
            ImageIcon ic = carregarMiniatura(path, 240, 180);
            if (ic != null) { foto.setIcon(ic); foto.setText(""); }
        } catch (Throwable ignore) {}
        
        frame.getContentPane().add(info, BorderLayout.NORTH);
        frame.getContentPane().add(foto, BorderLayout.CENTER);
        desktopPane.add(frame);
        frame.setVisible(true);
    }

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
                mostrarDialog("Preventiva automática aberta para as máquinas:\n\n" + String.join("\n", avisos), "Alerta de Manutenção", JOptionPane.WARNING_MESSAGE);
                atualizarTabelas(); 
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

    private void cadastrarTipoMaquina() {
        try {
            JTextField txtNome = new JTextField();
            JTextField txtFab = new JTextField();
            JSpinner spVida = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
            estilizarCampo(txtNome); estilizarCampo(txtFab); estilizarSpinner(spVida);

            List<Peca> todasPecas = service.getPecas();

            // ---- Peças recomendadas: checklist pesquisável (scroll fácil de listas grandes) ----
            JPanel painelPecas = new JPanel(new GridLayout(0, 2, 4, 4));
            painelPecas.setBackground(classicBg());
            List<JCheckBox> chks = new ArrayList<>();
            List<JSpinner> spins = new ArrayList<>();

            for (Peca p : todasPecas) {
                JCheckBox chk = new JCheckBox(p.getNome());
                chk.setName("pec#" + p.getId());         // identifica a peça para o filtro
                chk.setBackground(classicBg()); chk.setForeground(Color.BLACK);
                JSpinner sp = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
                estilizarSpinner(sp);
                painelPecas.add(chk); painelPecas.add(sp);
                chks.add(chk); spins.add(sp);
            }

            JScrollPane scrollPecas = new JScrollPane(painelPecas);
            scrollPecas.setPreferredSize(new Dimension(460, 210));
            scrollPecas.setBorder(null);
            scrollPecas.getViewport().setBackground(classicBg());
            scrollPecas.getVerticalScrollBar().setUnitIncrement(16);
            scrollPecas.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            // Busca em tempo real: filtra as peças pelo nome enquanto digita
            JTextField txtBusca = new JTextField();
            estilizarCampo(txtBusca);
            JLabel lblConta = new JLabel(todasPecas.isEmpty() ? "Nenhuma peça cadastrada." : "Nenhum marcado · 0 de " + todasPecas.size() + " peças exibidas");
            lblConta.setFont(F_SUBT); lblConta.setForeground(MUTED);

            JPanel barraBusca = new JPanel(new BorderLayout(6, 0));
            barraBusca.setBackground(classicBg());
            barraBusca.add(lblClassic("Buscar:"), BorderLayout.WEST);
            barraBusca.add(txtBusca, BorderLayout.CENTER);
            barraBusca.add(lblConta, BorderLayout.EAST);

            // Seleção rápida para listas grandes
            JPanel barraBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
            barraBotoes.setBackground(classicBg());
            JButton btnTodos = new JButton("Marcar todos");   estilizarBotao(btnTodos, classicBg(), Color.BLACK);
            JButton btnLimpar = new JButton("Desmarcar todos"); estilizarBotao(btnLimpar, classicBg(), Color.BLACK);
            barraBotoes.add(btnTodos); barraBotoes.add(btnLimpar);

            // Bloco composto: busca no topo, lista rolável no meio, botões embaixo
            JPanel blocoPecas = new JPanel(new BorderLayout(0, 4));
            blocoPecas.setBackground(classicBg());
            blocoPecas.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(6, 6, 6, 6)));
            blocoPecas.add(barraBusca, BorderLayout.NORTH);
            blocoPecas.add(scrollPecas, BorderLayout.CENTER);
            blocoPecas.add(barraBotoes, BorderLayout.SOUTH);

            txtBusca.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { filtrar(txtBusca.getText()); }
                @Override public void removeUpdate(DocumentEvent e) { filtrar(txtBusca.getText()); }
                @Override public void changedUpdate(DocumentEvent e) { filtrar(txtBusca.getText()); }

                private void filtrar(String termo) {
                    String q = termo.toLowerCase().trim();
                    int visiveis = 0, marcados = 0;
                    for (int i = 0; i < chks.size(); i++) {
                        JCheckBox c = chks.get(i);
                        boolean match = c.getText().toLowerCase().contains(q);
                        c.setVisible(match);   // esconde/reexibe a linha na grade
                        if (match) visiveis++;
                        if (c.isSelected()) marcados++;
                    }
                    // Reaplica o mesmo esquema para os spinners ao lado dos checkboxes ocultos
                    for (int j = 0; j < spins.size(); j++) {
                        JSpinner sp = spins.get(j);
                        boolean match = chks.get(j).getText().toLowerCase().contains(q);
                        sp.setVisible(match);
                    }
                    lblConta.setText((marcados == 0 ? "Nenhum marcado" : marcados + " marcado" + (marcados > 1 ? "s" : ""))
                            + " · " + visiveis + " de " + chks.size() + " peças exibidas");
                    painelPecas.revalidate();
                    painelPecas.repaint();
                }
            });

            btnTodos.addActionListener(e -> {
                chks.forEach(c -> c.setSelected(true));
                atualizarContadorSelecao(lblConta, chks);
            });
            btnLimpar.addActionListener(e -> {
                chks.forEach(c -> c.setSelected(false));
                atualizarContadorSelecao(lblConta, chks);
            });

            // ---- Formulário principal ----
            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(classicBg());
            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(4, 4, 4, 4); g.weightx = 1; g.gridx = 0;

            g.gridy = 0; form.add(lblClassic("Nome do Tipo:"), g);
            g.gridy = 1; form.add(txtNome, g);
            g.gridy = 2; form.add(lblClassic("Fabricante:"), g);
            g.gridy = 3; form.add(txtFab, g);
            g.gridy = 4; form.add(lblClassic("Vida Útil (anos):"), g);
            g.gridy = 5; form.add(spVida, g);
            g.gridy = 6; form.add(lblClassic("Peças Recomendadas:"), g);
            g.gridy = 7; form.add(blocoPecas, g);

            JOptionPane pane = new JOptionPane(form, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
            JDialog dlg = pane.createDialog(this, "Novo Tipo de Máquina");
            estilizarDialog(dlg.getContentPane());
            dlg.setVisible(true);

            if (pane.getValue() == null || (Integer) pane.getValue() != JOptionPane.OK_OPTION) return;

            String nome = txtNome.getText().trim();
            if (nome.isEmpty()) return;

            TipoMaquina t = new TipoMaquina(0, nome, txtFab.getText().trim(), (int) spVida.getValue());
            for (int i = 0; i < todasPecas.size(); i++) {
                if (chks.get(i).isSelected()) {
                    t.adicionarPecaRecomendada(todasPecas.get(i), (int) spins.get(i).getValue());
                }
            }

            service.cadastrarTipoMaquina(t);
            mostrarInfo("Tipo cadastrado!");
            atualizarTudo();
        } catch (Exception e) { mostrarErro(e); }
    }

    private void cadastrarMaquina() {
        try {
            List<TipoMaquina> tipos = service.getTiposMaquina();
            if (tipos.isEmpty()) { mostrarDialog("Cadastre um Tipo antes.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
            
            JDialog dlg = new JDialog(this, "Cadastrar Máquina", true);
            dlg.setSize(500, 640); dlg.setLocationRelativeTo(this);
            dlg.getContentPane().setBackground(BG_CARD); dlg.setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridBagLayout()); form.setBackground(BG_CARD);
            form.setBorder(new EmptyBorder(20, 24, 20, 24));
            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(6, 4, 6, 4); g.weightx = 1; g.gridx = 0;

            g.gridy = 0; form.add(lbl("Tipo:"), g);
            JComboBox<TipoMaquina> cbTipo = new JComboBox<>(tipos.toArray(new TipoMaquina[0]));
            estilizarCombo(cbTipo);
            g.gridy = 1; form.add(cbTipo, g);

            g.gridy = 2; form.add(lbl("Data de Aquisição:"), g);
            JSpinner spData = new JSpinner(new SpinnerDateModel());
            spData.setEditor(new JSpinner.DateEditor(spData, "dd/MM/yyyy"));
            spData.setValue(java.sql.Date.valueOf(RelogioSistema.hoje()));
            estilizarSpinner(spData);
            g.gridy = 3; form.add(spData, g);

            g.gridy = 4; form.add(lbl("Valor de Aquisição (R$):"), g);
            JTextField txtValor = new JTextField("0,00"); estilizarCampo(txtValor); g.gridy = 5; form.add(txtValor, g);

            g.gridy = 6; form.add(lbl("Número de Série:"), g);
            JTextField txtSerie = new JTextField(); estilizarCampo(txtSerie); g.gridy = 7; form.add(txtSerie, g);

            g.gridy = 8; form.add(lbl("Foto (opcional):"), g);
            JPanel fotoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            fotoPanel.setBackground(BG_CARD);
            JLabel preview = new JLabel("Sem foto", SwingConstants.CENTER);
            preview.setForeground(MUTED); preview.setPreferredSize(new Dimension(160, 100));
            preview.setBorder(new LineBorder(BORDER, 1, true)); preview.setOpaque(true); preview.setBackground(TAB_BG);
            JButton btnFoto = new JButton("Escolher..."); estilizarBotao(btnFoto, BG_APP, TEXT);
            final String[] fotoPath = {null};
            btnFoto.addActionListener(e -> {
                String p = escolherFoto(dlg);
                if (p != null) {
                    fotoPath[0] = p;
                    ImageIcon ic = carregarMiniatura(p, 160, 100);
                    if (ic != null) { preview.setIcon(ic); preview.setText(""); }
                }
            });
            fotoPanel.add(preview); fotoPanel.add(btnFoto);
            g.gridy = 9; form.add(fotoPanel, g);

            JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT)); botoes.setBackground(BG_CARD);
            JButton btnSalvar = new JButton("Salvar"); estilizarBotao(btnSalvar, PRIMARY, Color.WHITE);
            btnSalvar.addActionListener(e -> {
                try {
                    String serie = txtSerie.getText().trim();
                    if (serie.isEmpty()) throw new RuntimeException("Informe a série.");
                    LocalDate data = new java.sql.Date(((Date) spData.getValue()).getTime()).toLocalDate();
                    if (data.isAfter(RelogioSistema.hoje())) throw new RuntimeException("Data futura não permitida.");
                    double valor = Double.parseDouble(txtValor.getText().trim().replace(',', '.'));
                    
                    service.cadastrarMaquina(new Maquina(0, serie, "ATIVA", data, valor, (TipoMaquina) cbTipo.getSelectedItem(), fotoPath[0]));
                    dlg.dispose(); atualizarTudo();
                } catch (Exception ex) { mostrarErro(ex); }
            });
            botoes.add(btnSalvar);

            dlg.add(new JScrollPane(form), BorderLayout.CENTER);
            dlg.add(botoes, BorderLayout.SOUTH);
            dlg.setVisible(true);
        } catch (Exception e) { mostrarErro(e); }
    }

    private void cadastrarTecnico() {
        try {
            List<TipoTecnico> tipos = service.getTiposTecnico();
            if (tipos.isEmpty()) { mostrarDialog("Cadastre um Tipo de Técnico antes.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
            
            JDialog dlg = new JDialog(this, "Cadastrar Técnico", true);
            dlg.setSize(500, 480); dlg.setLocationRelativeTo(this);
            dlg.getContentPane().setBackground(BG_CARD); dlg.setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridBagLayout()); form.setBackground(BG_CARD);
            form.setBorder(new EmptyBorder(20, 24, 20, 24));
            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(6, 4, 6, 4); g.weightx = 1; g.gridx = 0;

            g.gridy = 0; form.add(lbl("Especialidade:"), g);
            JComboBox<TipoTecnico> cbTipo = new JComboBox<>(tipos.toArray(new TipoTecnico[0]));
            estilizarCombo(cbTipo);
            g.gridy = 1; form.add(cbTipo, g);

            g.gridy = 2; form.add(lbl("Nome:"), g);
            JTextField txtNome = new JTextField(); estilizarCampo(txtNome); g.gridy = 3; form.add(txtNome, g);

            g.gridy = 4; form.add(lbl("Telefone:"), g);
            JTextField txtTel = new JTextField(); estilizarCampo(txtTel); g.gridy = 5; form.add(txtTel, g);

            g.gridy = 6; form.add(lbl("Foto (opcional):"), g);
            JPanel fotoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            fotoPanel.setBackground(BG_CARD);
            JLabel preview = new JLabel("Sem foto", SwingConstants.CENTER);
            preview.setForeground(MUTED); preview.setPreferredSize(new Dimension(160, 100));
            preview.setBorder(new LineBorder(BORDER, 1, true)); preview.setOpaque(true); preview.setBackground(TAB_BG);
            JButton btnFoto = new JButton("Escolher..."); estilizarBotao(btnFoto, BG_APP, TEXT);
            final String[] fotoPath = {null};
            btnFoto.addActionListener(e -> {
                String p = escolherFoto(dlg);
                if (p != null) {
                    fotoPath[0] = p;
                    ImageIcon ic = carregarMiniatura(p, 160, 100);
                    if (ic != null) { preview.setIcon(ic); preview.setText(""); }
                }
            });
            fotoPanel.add(preview); fotoPanel.add(btnFoto);
            g.gridy = 7; form.add(fotoPanel, g);

            JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT)); botoes.setBackground(BG_CARD);
            JButton btnSalvar = new JButton("Salvar"); estilizarBotao(btnSalvar, PRIMARY, Color.WHITE);
            btnSalvar.addActionListener(e -> {
                try {
                    String nome = txtNome.getText().trim();
                    if (nome.isEmpty()) throw new RuntimeException("Informe o nome.");
                    service.cadastrarTecnico(new Tecnico(0, nome, txtTel.getText().trim(), (TipoTecnico) cbTipo.getSelectedItem(), fotoPath[0]));
                    dlg.dispose(); atualizarTudo();
                } catch (Exception ex) { mostrarErro(ex); }
            });
            botoes.add(btnSalvar);

            dlg.add(new JScrollPane(form), BorderLayout.CENTER);
            dlg.add(botoes, BorderLayout.SOUTH);
            dlg.setVisible(true);
        } catch (Exception e) { mostrarErro(e); }
    }

    private void cadastrarPeca() {
        try {
            JDialog dlg = new JDialog(this, "Cadastrar Peça", true);
            dlg.setSize(400, 300); dlg.setLocationRelativeTo(this);
            dlg.getContentPane().setBackground(BG_CARD); dlg.setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridBagLayout()); form.setBackground(BG_CARD);
            form.setBorder(new EmptyBorder(20, 24, 20, 24));
            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(6, 4, 6, 4); g.weightx = 1; g.gridx = 0;

            g.gridy = 0; form.add(lbl("Nome da Peça:"), g);
            JTextField txtNome = new JTextField(); estilizarCampo(txtNome); g.gridy = 1; form.add(txtNome, g);

            g.gridy = 2; form.add(lbl("Qtd. Estoque:"), g);
            JTextField txtQtd = new JTextField("0"); estilizarCampo(txtQtd); g.gridy = 3; form.add(txtQtd, g);

            g.gridy = 4; form.add(lbl("Valor Unitário (R$):"), g);
            JTextField txtValor = new JTextField("0,00"); estilizarCampo(txtValor); g.gridy = 5; form.add(txtValor, g);

            JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT)); botoes.setBackground(BG_CARD);
            JButton btnSalvar = new JButton("Salvar"); estilizarBotao(btnSalvar, PRIMARY, Color.WHITE);
            btnSalvar.addActionListener(e -> {
                try {
                    String nome = txtNome.getText().trim();
                    if (nome.isEmpty()) throw new RuntimeException("Informe o nome.");
                    int qtd = Integer.parseInt(txtQtd.getText().trim());
                    double valor = Double.parseDouble(txtValor.getText().trim().replace(',', '.'));
                    service.cadastrarPeca(new Peca(0, nome, "Geral", "Geral", qtd, 5, valor));
                    dlg.dispose(); atualizarTudo();
                } catch (Exception ex) { mostrarErro(ex); }
            });
            botoes.add(btnSalvar);

            dlg.add(new JScrollPane(form), BorderLayout.CENTER);
            dlg.add(botoes, BorderLayout.SOUTH);
            dlg.setVisible(true);
        } catch (Exception e) { mostrarErro(e); }
    }

    private void cadastrarTipoTecnico() {
        try {
            String esp = mostrarInput("Especialidade (ex: Mecânico, Eletricista):");
            if (esp == null || esp.isBlank()) return;
            service.cadastrarTipoTecnico(new TipoTecnico(0, esp.trim()));
            mostrarInfo("Tipo de técnico cadastrado!");
            atualizarTudo();
        } catch (RuntimeException e) { mostrarErro(e); }
    }

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
        estilizarSpinner(spinner);

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

    private void registrarFalha() {
        try {
            Integer idMaq = lerInteiro("ID da Máquina quebrada:");
            if (idMaq == null) return;
            String desc = mostrarInput("O que aconteceu?");
            if (desc == null) return;
            service.registrarFalha(desc, idMaq);
            atualizarTudo();
        } catch (Exception e) { mostrarErro(e); }
    }

    private void abrirManutencao() {
        try {
            Integer idMaq = lerInteiro("ID da Máquina:");
            if (idMaq == null) return;
            String desc = mostrarInput("Descrição do serviço:");
            String causa = mostrarInput("Causa raiz:");
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

        private void abrirTelaExclusao() {
        String[] opcoes = {"Máquina", "Técnico", "Peça do Estoque", "Tipo de Máquina", "Manutenção"};
        String tipo = (String) JOptionPane.showInputDialog(this, 
                "O que você deseja excluir do sistema?\n(Esta ação não pode ser desfeita)", 
                "Exclusão de Registro", 
                JOptionPane.WARNING_MESSAGE, null, opcoes, opcoes[0]);
        
        if (tipo == null) return; // Cancelou

        try {
            Integer id = lerInteiro("Informe o ID do registro do tipo [" + tipo + "] que deseja excluir:");
            if (id == null) return;

            if (!mostrarConfirmacao("Tem certeza que deseja excluir o " + tipo + " de ID " + id + "?")) return;

            boolean sucesso = false;
            switch (tipo) {
                case "Máquina" -> sucesso = service.excluirMaquina(id);
                case "Técnico" -> sucesso = service.excluirTecnico(id);
                case "Peça do Estoque" -> sucesso = service.excluirPeca(id);
                case "Tipo de Máquina" -> sucesso = service.excluirTipoMaquina(id);
                case "Manutenção" -> sucesso = service.excluirManutencao(id);
            }

            if (sucesso) {
                mostrarInfo(tipo + " excluído com sucesso!");
                atualizarTudo();
            } else {
                mostrarDialog("Registro não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
            }

        } catch (RuntimeException e) {
            mostrarErro(e);
        }
    }

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
            if (selecionadas.isEmpty()) { mostrarDialog("Selecione ao menos uma.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
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
            
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("relatorio_gerencial.txt"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try (PrintWriter pw = new PrintWriter(new FileWriter(fc.getSelectedFile()))) {
                    pw.print(sb.toString());
                }
                mostrarInfo("Relatório salvo com sucesso!");
            }
        } catch (Exception e) { mostrarErro(e); }
    }

    private JScrollPane areaTexto(String texto, int w, int h) {
        JTextArea ta = new JTextArea(texto);
        ta.setEditable(false);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
        ta.setBackground(BG_CARD); ta.setForeground(TEXT);
        ta.setCaretColor(TEXT); ta.setCaretPosition(0);
        ta.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(w, h));
        sp.setBorder(new LineBorder(BORDER, 1));
        sp.getViewport().setBackground(BG_CARD);
        return sp;
    }

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
        mostrarDialog(areaTexto(sb.toString(), 600, 400), "Custos", JOptionPane.INFORMATION_MESSAGE);
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
        mostrarDialog(areaTexto(sb.toString(), 780, 420), "Histórico", JOptionPane.INFORMATION_MESSAGE);
    }

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
          
          + "2. O RELÓGIO DO SISTEMA E A DEPRECIAÇÃO\n"
          + " • O sistema calcula a saúde do equipamento baseada na depreciação real.\n"
          + " • Fórmula: Saúde = 100 - (Dias de Uso / (Vida Útil do Tipo * 365)) * 100.\n"
          + " • Para testar o futuro, vá em Administração → ⏰ Alterar Data.\n"
          + " • Você pode avançar +1 mês ou +1 ano. A barra inferior ficará LARANJA\n"
          + "   indicando 'DATA SIMULADA'. Para voltar, clique em 'Voltar para Hoje'.\n\n"
          
          + "3. INTELIGÊNCIA PREDITIVA\n"
          + " • Não existe mais o botão manual de abrir preventiva.\n"
          + " • O sistema vigia as máquinas. Se uma máquina ATIVA atingir\n"
          + "   a saúde abaixo de 30%, o sistema abrirá uma Preventiva sozinho!\n"
          + " • Na tela Preditiva, você vê as barras vermelhas e amarelas de alerta.\n\n"
          
          + "4. AS FICHAS FLUTUANTES\n"
          + " • Vá na aba 'Dados' e dê DUPLO CLIQUE em uma Máquina ou Técnico.\n"
          + " • Abrirá uma janela interna que você pode arrastar, minimizar ou maximizar.\n"
          + " • Nessas fichas, você pode realizar ações diretas (ex: registrar falha ou\n"
          + "   alocar um técnico) sem precisar voltar aos menus superiores.\n\n"
          
          + "5. A LUPINHA (BUSCA)\n"
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

        mostrarDialog(areaTexto(texto, 800, 600), "Central de Ajuda v3.0", JOptionPane.INFORMATION_MESSAGE);
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

    private Integer lerInteiro(String mensagem) {
        String t = mostrarInput(mensagem);
        if (t == null) return null;
        return Integer.parseInt(t.trim());
    }

    private JLabel lbl(String texto) {
        JLabel l = new JLabel(texto); l.setForeground(TEXT); return l;
    }

    /** Label usado nas fichas clássicas: texto preto sobre a cor de face clara padrão. */
    private JLabel lblClassic(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(Color.BLACK);
        return l;
    }

    /** Atualiza o rótulo de contagem da seleção no diálogo de Tipo de Máquina. */
    private void atualizarContadorSelecao(JLabel lbl, List<JCheckBox> chks) {
        int total = chks.size(), visiveis = 0, marcados = 0;
        for (JCheckBox c : chks) {
            if (c.isVisible()) {
                visiveis++;
                if (c.isSelected()) marcados++;
            }
        }
        lbl.setText((marcados == 0 ? "Nenhum marcado" : marcados + " marcado" + (marcados > 1 ? "s" : ""))
                + " · " + visiveis + " de " + total + " peças exibidas");
    }

    public static void main(String[] args) {
        // ====================================================================
        // LOOK AND FEEL CROSSPLATFORM (METAL) — JANELAS CLÁSSICAS
        // É EXATAMENTE o L&F "Metal" do próprio JDK: barras de título, bordas
        // 3D, botões e componentes com a cara clássica dos softwares antigos.
        // Os JInternalFrame (cards flutuantes) herdam esse visual automaticamente.
        // ====================================================================
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception | Error ignore) {
            // Fallback: qualquer Look and Feel disponível nesta JVM
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored2) { }
        }

        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }
}