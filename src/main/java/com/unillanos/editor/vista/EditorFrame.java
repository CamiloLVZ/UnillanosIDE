package com.unillanos.editor.vista;

import com.unillanos.editor.controller.EditorController;
import com.unillanos.plugin.PluginDescriptor;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class EditorFrame extends JFrame {

    private final EditorController controller;

    private JButton btnCargarComponente;
    private DefaultListModel<PluginDescriptor> listModel;
    private JList<PluginDescriptor> listComponentes;
    private JButton btnEjecutarComponente;

    private JButton btnCargarArchivoInicial;
    private JTextArea txtArchivoInicial;
    private JTextArea txtArchivoProcesado;
    private JLabel lblPosicionInicial;

    private JTextArea txtSalidaMensajes;

    public EditorFrame(EditorController controller) {
        this.controller = controller;
        initLookAndFeel();
        initComponents();
        initLayout();
        initEvents();
        bindController();
    }

    private void initLookAndFeel() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
    }

    private void initComponents() {
        btnCargarComponente   = new JButton("Cargar Archivo");
        btnEjecutarComponente = new JButton("Ejecutar componente");
        listModel       = new DefaultListModel<>();
        listComponentes = new JList<>(listModel);
        listComponentes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listComponentes.setBackground(Color.WHITE);
        btnCargarArchivoInicial = new JButton("Cargar Archivo");
        txtArchivoInicial = buildTextArea();
        txtArchivoProcesado = buildTextArea();
        txtArchivoProcesado.setEditable(false);
        lblPosicionInicial = new JLabel("ln 1, col 1");
        lblPosicionInicial.setFont(new Font("Monospaced", Font.PLAIN, 11));
        lblPosicionInicial.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        txtSalidaMensajes = buildTextArea();
        txtSalidaMensajes.setEditable(false);
        txtSalidaMensajes.setBackground(Color.WHITE);
    }

    private JTextArea buildTextArea() {
        JTextArea ta = new JTextArea();
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        return ta;
    }

    private void initLayout() {
        setTitle("Editor de Texto - Unillanos V1.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(870, 530);
        setLocationRelativeTo(null);
        setResizable(true);
        Color bg = new Color(220, 215, 201);
        getContentPane().setBackground(bg);
        JPanel mainPanel = new JPanel(new BorderLayout(6, 6));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        mainPanel.setBackground(bg);
        mainPanel.add(buildLeftPanel(bg),   BorderLayout.WEST);
        mainPanel.add(buildRightPanel(bg),  BorderLayout.CENTER);
        mainPanel.add(buildBottomPanel(bg), BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    private JPanel buildLeftPanel(Color bg) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(bg);
        panel.setPreferredSize(new Dimension(230, 0));
        JPanel pCargar = titledPanel("Cargar componente", bg);
        pCargar.add(stretchButton(btnCargarComponente));
        JPanel pLista = titledPanel("Componentes cargados", bg);
        pLista.setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane(listComponentes);
        scroll.setPreferredSize(new Dimension(210, 200));
        pLista.add(scroll, BorderLayout.CENTER);
        JPanel pEjecutar = titledPanel("Ejecutar componente", bg);
        pEjecutar.add(stretchButton(btnEjecutarComponente));
        panel.add(pCargar);
        panel.add(Box.createVerticalStrut(4));
        panel.add(pLista);
        panel.add(Box.createVerticalStrut(4));
        panel.add(pEjecutar);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel buildRightPanel(Color bg) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 6, 6));
        panel.setBackground(bg);

        JPanel pInicial = titledPanel("Archivo Inicial", bg);
        pInicial.setLayout(new BorderLayout(4, 4));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setBackground(bg);
        btnRow.add(btnCargarArchivoInicial);
        pInicial.add(btnRow, BorderLayout.NORTH);
        JPanel textoPanelInicial = new JPanel(new BorderLayout());
        textoPanelInicial.setBackground(bg);
        LineNumberComponent lineNumberInicial = new LineNumberComponent(txtArchivoInicial);
        JScrollPane scrollInicial = new JScrollPane(txtArchivoInicial);
        scrollInicial.setRowHeaderView(lineNumberInicial);
        textoPanelInicial.add(scrollInicial, BorderLayout.CENTER);
        textoPanelInicial.add(lblPosicionInicial, BorderLayout.SOUTH);
        pInicial.add(textoPanelInicial, BorderLayout.CENTER);

        JPanel pProcesado = titledPanel("Archivo procesado", bg);
        pProcesado.setLayout(new BorderLayout());
        JScrollPane scrollProcesado = new JScrollPane(txtArchivoProcesado);
        scrollProcesado.setRowHeaderView(new LineNumberComponent(txtArchivoProcesado));
        pProcesado.add(scrollProcesado, BorderLayout.CENTER);

        panel.add(pInicial);
        panel.add(pProcesado);
        return panel;
    }

    private JPanel buildBottomPanel(Color bg) {
        JPanel panel = titledPanel("Salida de mensajes", bg);
        panel.setLayout(new BorderLayout());
        JScrollPane scrollMensajes = new JScrollPane(txtSalidaMensajes);
        scrollMensajes.setPreferredSize(new Dimension(0, 120));
        scrollMensajes.setRowHeaderView(new LineNumberComponent(txtSalidaMensajes));
        panel.add(scrollMensajes, BorderLayout.CENTER);
        return panel;
    }

    private JPanel titledPanel(String title, Color bg) {
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.setBackground(bg);
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(160, 150, 130)),
                title, TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Dialog", Font.PLAIN, 11), new Color(80, 60, 40));
        p.setBorder(border);
        return p;
    }

    private JButton stretchButton(JButton btn) {
        btn.setPreferredSize(new Dimension(200, 23));
        return btn;
    }

    // ── Eventos: solo abren diálogos y delegan al controller ─────────── //

    private void initEvents() {
        btnCargarComponente.addActionListener((ActionEvent e) -> onCargarComponente());
        btnEjecutarComponente.addActionListener((ActionEvent e) -> onEjecutarComponente());
        btnCargarArchivoInicial.addActionListener((ActionEvent e) -> onCargarArchivoInicial());
        listComponentes.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) onEjecutarComponente();
            }
        });
        txtArchivoInicial.addCaretListener(e ->
                updateCaretPosition(txtArchivoInicial, lblPosicionInicial));
    }

    private void onCargarComponente() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Seleccionar componente (plugin)");
        fc.setFileFilter(new FileNameExtensionFilter("Plugin package (*.jar)", "jar"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        controller.loadPlugin(fc.getSelectedFile());
    }

    private void onEjecutarComponente() {
        controller.executePlugin(
                listComponentes.getSelectedValue(),
                txtArchivoInicial.getText());
    }

    private void onCargarArchivoInicial() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Cargar archivo inicial");
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        controller.loadTextFile(fc.getSelectedFile());
    }

    // ── Callbacks del controller → actualizan la vista ───────────────── //

    private void bindController() {
        controller.setOnPluginLoaded(descriptor -> {
            for (int i = 0; i < listModel.size(); i++) {
                if (listModel.get(i).getId().equals(descriptor.getId())) return;
            }
            listModel.addElement(descriptor);
            listComponentes.setSelectedIndex(listModel.size() - 1);
        });

        controller.setOnPluginExecuted(txtArchivoProcesado::setText);

        controller.setOnFileLoaded(content -> {
            txtArchivoInicial.setText(content);
            txtArchivoInicial.setCaretPosition(0);
        });

        controller.setOnError((title, msg) -> SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE)));

        controller.setOnStatusMessage(msg -> SwingUtilities.invokeLater(() -> appendStatusMessage(msg)));

        List<String> historialMensajes = controller.getMessageHistory();
        for (int i = 0; i < historialMensajes.size(); i++) {
            appendStatusMessage(historialMensajes.get(i));
        }
    }

    private void appendStatusMessage(String message) {
        if (txtSalidaMensajes.getDocument().getLength() > 0) {
            txtSalidaMensajes.append("\n");
        }
        txtSalidaMensajes.append(message);
        txtSalidaMensajes.setCaretPosition(txtSalidaMensajes.getDocument().getLength());
    }

    private void updateCaretPosition(JTextComponent textComponent, JLabel label) {
        try {
            int caretPos = textComponent.getCaretPosition();
            Element root = textComponent.getDocument().getDefaultRootElement();
            int line = root.getElementIndex(caretPos);
            int col  = caretPos - root.getElement(line).getStartOffset();
            label.setText(String.format("ln %d, col %d", line + 1, col + 1));
        } catch (Exception ex) {
            label.setText("ln 1, col 1");
        }
    }
}
