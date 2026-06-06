package sinapsis.interfaz;

import sinapsis.algoritmos.*;
import sinapsis.algoritmos.AlgoritmosConectividad.ResultadoConectividad;
import sinapsis.algoritmos.Dijkstra.ResultadoDijkstra;
import sinapsis.estructura.GrafoDirigido;
import sinapsis.estructura.HashTableNeurotransmisores;
import sinapsis.modelo.Neurona;
import sinapsis.modelo.Neurotransmisor;
import sinapsis.modelo.Sinapsis;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Ventana principal de la aplicación SynapseLogic.
 * Proporciona la interfaz gráfica completa para el análisis de
 * conectividad y transmisión neuronal.
 *
 * @author SynapseLogic Team
 * @version 1.0
 */
public class VentanaPrincipal extends JFrame {

    // ---- Colores del tema ----
    private static final Color COLOR_FONDO        = new Color(15, 20, 35);
    private static final Color COLOR_PANEL        = new Color(25, 35, 55);
    private static final Color COLOR_ACENTO       = new Color(0, 180, 216);
    private static final Color COLOR_ACENTO2      = new Color(72, 149, 239);
    private static final Color COLOR_TEXTO        = new Color(220, 230, 255);
    private static final Color COLOR_TEXTO_SEC    = new Color(140, 160, 200);
    private static final Color COLOR_EXCITATORIO  = new Color(100, 220, 100);
    private static final Color COLOR_INHIBITORIO  = new Color(220, 100, 100);
    private static final Color COLOR_MODULADOR    = new Color(220, 180, 60);
    private static final Color COLOR_AISLADO      = new Color(220, 80, 80);
    private static final Color COLOR_ALCANZABLE   = new Color(60, 200, 120);
    private static final Color COLOR_BOTON        = new Color(30, 45, 75);
    private static final Color COLOR_BOTON_HOVER  = new Color(0, 140, 180);
    private static final Color COLOR_ADVERTENCIA  = new Color(255, 160, 0);

    // ---- Datos del sistema ----
    private GrafoDirigido grafo;
    private HashTableNeurotransmisores hashTable;
    private String rutaArchivoActual = null;
    private boolean hayDatosSinGuardar = false;

    // ---- Componentes de la GUI ----
    private JTabbedPane pestanias;
    private PanelGrafo panelGrafo;
    private JTextArea areaResultados;
    private JLabel lblEstado;
    private JLabel lblEstadisticas;

    // Tab Grafo
    private JComboBox<String> cbFuente;
    private JComboBox<String> cbAlgoritmo;
    private JButton btnAnalizar;

    // Tab Dijkstra
    private JComboBox<String> cbOrigen;
    private JComboBox<String> cbDestino;
    private JTextArea areaDijkstra;

    // Tab Neurotransmisores
    private JTable tablaNT;
    private DefaultTableModel modeloTablaNT;

    // Tab Neuronas
    private JList<String> listaNeuronas;
    private DefaultListModel<String> modeloListaNeuronas;

    /**
     * Constructor principal: inicializa el sistema y la GUI.
     */
    public VentanaPrincipal() {
        grafo = new GrafoDirigido();
        hashTable = new HashTableNeurotransmisores();
        cargarDiccionarioPorDefecto();
        initUI();
    }

    /**
     * Inicializa todos los componentes de la interfaz gráfica.
     */
    private void initUI() {
        setTitle("SynapseLogic — Análisis de Conectividad Neuronal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 820);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);

        setJMenuBar(crearMenuBar());

        JPanel panelPrincipal = new JPanel(new BorderLayout(0, 0));
        panelPrincipal.setBackground(COLOR_FONDO);

        panelPrincipal.add(crearPanelSuperior(), BorderLayout.NORTH);
        panelPrincipal.add(crearPanelCentral(), BorderLayout.CENTER);
        panelPrincipal.add(crearBarraEstado(), BorderLayout.SOUTH);

        add(panelPrincipal);
        actualizarEstadisticas();
    }

    // ================================================================
    //  MENÚ BAR
    // ================================================================

    /**
     * Crea la barra de menú de la aplicación.
     *
     * @return JMenuBar configurada
     */
    private JMenuBar crearMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(COLOR_PANEL);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_ACENTO));

        // ---- Menú Archivo ----
        JMenu menuArchivo = crearMenu("Archivo");
        menuArchivo.add(crearMenuItem("Cargar Grafo (CSV)...", e -> cargarGrafo()));
        menuArchivo.add(crearMenuItem("Cargar Diccionario NT...", e -> cargarDiccionario()));
        menuArchivo.addSeparator();
        menuArchivo.add(crearMenuItem("Guardar Grafo...", e -> guardarGrafo()));
        menuArchivo.addSeparator();
        menuArchivo.add(crearMenuItem("Cargar Datos de Ejemplo", e -> cargarEjemplo()));
        menuArchivo.addSeparator();
        menuArchivo.add(crearMenuItem("Salir", e -> salir()));

        // ---- Menú Red Neuronal ----
        JMenu menuRed = crearMenu("Red Neuronal");
        menuRed.add(crearMenuItem("Agregar Neurona...", e -> agregarNeurona()));
        menuRed.add(crearMenuItem("Eliminar Neurona...", e -> eliminarNeurona()));
        menuRed.addSeparator();
        menuRed.add(crearMenuItem("Simular Fatiga Cognitiva", e -> simularFatiga()));
        menuRed.add(crearMenuItem("Ver Estadísticas", e -> mostrarEstadisticas()));

        // ---- Menú Ayuda ----
        JMenu menuAyuda = crearMenu("Ayuda");
        menuAyuda.add(crearMenuItem("Instrucciones de Uso", e -> mostrarAyuda()));
        menuAyuda.add(crearMenuItem("Acerca de...", e -> mostrarAcercaDe()));

        menuBar.add(menuArchivo);
        menuBar.add(menuRed);
        menuBar.add(menuAyuda);
        return menuBar;
    }

    private JMenu crearMenu(String titulo) {
        JMenu menu = new JMenu(titulo);
        menu.setForeground(COLOR_TEXTO);
        menu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return menu;
    }

    private JMenuItem crearMenuItem(String texto, ActionListener al) {
        JMenuItem item = new JMenuItem(texto);
        item.setBackground(COLOR_PANEL);
        item.setForeground(COLOR_TEXTO);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        item.addActionListener(al);
        return item;
    }

    // ================================================================
    //  PANEL SUPERIOR (Toolbar)
    // ================================================================

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_PANEL);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, COLOR_ACENTO));
        panel.setPreferredSize(new Dimension(0, 65));

        // Logo / Título
        JLabel lblTitulo = new JLabel("  🧠 SynapseLogic");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(COLOR_ACENTO);
        panel.add(lblTitulo, BorderLayout.WEST);

        // Botones de acción rápida
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        panelBotones.setBackground(COLOR_PANEL);

        panelBotones.add(crearBotonAccion("📂 Cargar Grafo", COLOR_ACENTO2, e -> cargarGrafo()));
        panelBotones.add(crearBotonAccion("📖 Cargar NT", COLOR_ACENTO2, e -> cargarDiccionario()));
        panelBotones.add(crearBotonAccion("💾 Guardar", new Color(60, 160, 80), e -> guardarGrafo()));
        panelBotones.add(crearBotonAccion("⚡ Ejemplo", COLOR_MODULADOR, e -> cargarEjemplo()));
        panelBotones.add(crearBotonAccion("😴 Fatiga", COLOR_ADVERTENCIA, e -> simularFatiga()));
        panelBotones.add(crearBotonAccion("➕ Neurona", COLOR_EXCITATORIO, e -> agregarNeurona()));
        panelBotones.add(crearBotonAccion("➖ Eliminar", COLOR_AISLADO, e -> eliminarNeurona()));

        panel.add(panelBotones, BorderLayout.EAST);
        return panel;
    }

    private JButton crearBotonAccion(String texto, Color color, ActionListener al) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color.darker());
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 1),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.addActionListener(al);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(color); }
            public void mouseExited(MouseEvent e) { btn.setBackground(color.darker()); }
        });
        return btn;
    }

    // ================================================================
    //  PANEL CENTRAL (Dividido)
    // ================================================================

    private JSplitPane crearPanelCentral() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            crearPanelIzquierdo(), crearPanelDerecho());
        split.setDividerLocation(830);
        split.setDividerSize(4);
        split.setBackground(COLOR_FONDO);
        split.setBorder(null);
        return split;
    }

    // ---- Panel Izquierdo: Grafo + Pestañas ----

    private JPanel crearPanelIzquierdo() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_FONDO);

        pestanias = new JTabbedPane();
        pestanias.setBackground(COLOR_PANEL);
        pestanias.setForeground(COLOR_TEXTO);
        pestanias.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Tab 1: Visualización del Grafo
        pestanias.addTab("🕸️ Red Neuronal", crearTabGrafo());
        // Tab 2: Algoritmos de conectividad
        pestanias.addTab("🔍 BFS / DFS", crearTabConectividad());
        // Tab 3: Dijkstra
        pestanias.addTab("⚡ Ruta Óptima", crearTabDijkstra());
        // Tab 4: Neurotransmisores
        pestanias.addTab("🧪 Neurotransmisores", crearTabNeurotransmisores());
        // Tab 5: Neuronas
        pestanias.addTab("🔵 Neuronas", crearTabNeuronas());

        panel.add(pestanias, BorderLayout.CENTER);
        return panel;
    }

    // ---- Tab Grafo ----

    private JPanel crearTabGrafo() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        panelGrafo = new PanelGrafo();
        panelGrafo.setPreferredSize(new Dimension(780, 480));
        JScrollPane scrollGrafo = new JScrollPane(panelGrafo);
        scrollGrafo.setBackground(COLOR_FONDO);
        scrollGrafo.setBorder(BorderFactory.createLineBorder(COLOR_ACENTO, 1));

        // Leyenda
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        leyenda.setBackground(COLOR_PANEL);
        leyenda.add(crearItemLeyenda("Excitatorio", COLOR_EXCITATORIO));
        leyenda.add(crearItemLeyenda("Inhibitorio", COLOR_INHIBITORIO));
        leyenda.add(crearItemLeyenda("Modulador", COLOR_MODULADOR));
        leyenda.add(crearItemLeyenda("Zona Aislada", COLOR_AISLADO));
        leyenda.add(crearItemLeyenda("Alcanzable", COLOR_ALCANZABLE));

        JLabel instrucciones = new JLabel("  Arrastra los nodos para reorganizar la red");
        instrucciones.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        instrucciones.setForeground(COLOR_TEXTO_SEC);
        leyenda.add(instrucciones);

        panel.add(scrollGrafo, BorderLayout.CENTER);
        panel.add(leyenda, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearItemLeyenda(String texto, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        item.setBackground(COLOR_PANEL);
        JPanel circulo = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(color);
                g.fillOval(0, 0, 14, 14);
            }
        };
        circulo.setPreferredSize(new Dimension(14, 14));
        circulo.setOpaque(false);
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(COLOR_TEXTO_SEC);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        item.add(circulo);
        item.add(lbl);
        return item;
    }

    // ---- Tab Conectividad BFS/DFS ----

    private JPanel crearTabConectividad() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Panel de controles
        JPanel controles = new JPanel(new GridBagLayout());
        controles.setBackground(COLOR_PANEL);
        controles.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_ACENTO, 1),
            new EmptyBorder(12, 12, 12, 12)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        controles.add(crearLabel("Neurona Fuente (Estímulo):"), gbc);
        gbc.gridx = 1;
        cbFuente = new JComboBox<>();
        estilizarCombo(cbFuente);
        controles.add(cbFuente, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        controles.add(crearLabel("Algoritmo:"), gbc);
        gbc.gridx = 1;
        cbAlgoritmo = new JComboBox<>(new String[]{"BFS (Amplitud)", "DFS (Profundidad)"});
        estilizarCombo(cbAlgoritmo);
        controles.add(cbAlgoritmo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        btnAnalizar = crearBotonPrincipal("🔍 Analizar Conectividad", e -> analizarConectividad());
        controles.add(btnAnalizar, gbc);

        // Área de resultados conectividad
        areaResultados = new JTextArea();
        areaResultados.setEditable(false);
        areaResultados.setFont(new Font("Consolas", Font.PLAIN, 12));
        areaResultados.setBackground(new Color(10, 15, 25));
        areaResultados.setForeground(COLOR_TEXTO);
        areaResultados.setCaretColor(COLOR_ACENTO);
        areaResultados.setText("Carga un archivo CSV y selecciona una neurona fuente para analizar la conectividad.\n");
        JScrollPane scroll = new JScrollPane(areaResultados);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_ACENTO, 1));

        panel.add(controles, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ---- Tab Dijkstra ----

    private JPanel crearTabDijkstra() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel controles = new JPanel(new GridBagLayout());
        controles.setBackground(COLOR_PANEL);
        controles.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_ACENTO, 1),
            new EmptyBorder(12, 12, 12, 12)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        controles.add(crearLabel("Neurona Origen:"), gbc);
        gbc.gridx = 1;
        cbOrigen = new JComboBox<>();
        estilizarCombo(cbOrigen);
        controles.add(cbOrigen, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        controles.add(crearLabel("Neurona Destino:"), gbc);
        gbc.gridx = 1;
        cbDestino = new JComboBox<>();
        estilizarCombo(cbDestino);
        controles.add(cbDestino, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        controles.add(crearBotonPrincipal("⚡ Calcular Ruta Óptima", e -> calcularRutaOptima()), gbc);

        areaDijkstra = new JTextArea();
        areaDijkstra.setEditable(false);
        areaDijkstra.setFont(new Font("Consolas", Font.PLAIN, 12));
        areaDijkstra.setBackground(new Color(10, 15, 25));
        areaDijkstra.setForeground(COLOR_TEXTO);
        areaDijkstra.setText("Selecciona las neuronas de origen y destino para calcular la ruta óptima.\n\nFórmula: W = d / (v × k)\n");
        JScrollPane scroll = new JScrollPane(areaDijkstra);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_ACENTO, 1));

        panel.add(controles, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ---- Tab Neurotransmisores ----

    private JPanel crearTabNeurotransmisores() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Barra búsqueda
        JPanel barraBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        barraBusqueda.setBackground(COLOR_PANEL);
        barraBusqueda.add(crearLabel("Buscar NT:"));
        JTextField txtBusqueda = new JTextField(15);
        estilizarTextField(txtBusqueda);
        barraBusqueda.add(txtBusqueda);
        JButton btnBuscar = crearBotonPequeño("Buscar", e -> buscarNT(txtBusqueda.getText()));
        barraBusqueda.add(btnBuscar);
        barraBusqueda.add(crearBotonPequeño("Mostrar Todos", e -> actualizarTablaNT()));

        // Tabla
        String[] columnas = {"ID", "Nombre", "Efecto", "Velocidad", "Descripción"};
        modeloTablaNT = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaNT = new JTable(modeloTablaNT);
        estilizarTabla(tablaNT);
        tablaNT.getColumnModel().getColumn(0).setPreferredWidth(60);
        tablaNT.getColumnModel().getColumn(1).setPreferredWidth(180);
        tablaNT.getColumnModel().getColumn(2).setPreferredWidth(90);
        tablaNT.getColumnModel().getColumn(3).setPreferredWidth(70);
        tablaNT.getColumnModel().getColumn(4).setPreferredWidth(280);

        // Renderer por efecto
        tablaNT.getColumnModel().getColumn(2).setCellRenderer((table, value, sel, foc, row, col) -> {
            JLabel lbl = new JLabel(value.toString(), SwingConstants.CENTER);
            lbl.setOpaque(true);
            String efecto = value.toString().toLowerCase();
            if (efecto.contains("excit")) { lbl.setBackground(COLOR_EXCITATORIO.darker()); lbl.setForeground(Color.WHITE); }
            else if (efecto.contains("inhib")) { lbl.setBackground(COLOR_INHIBITORIO.darker()); lbl.setForeground(Color.WHITE); }
            else { lbl.setBackground(COLOR_MODULADOR.darker()); lbl.setForeground(Color.WHITE); }
            return lbl;
        });

        JScrollPane scroll = new JScrollPane(tablaNT);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_ACENTO, 1));

        panel.add(barraBusqueda, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        actualizarTablaNT();
        return panel;
    }

    // ---- Tab Neuronas ----

    private JPanel crearTabNeuronas() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        modeloListaNeuronas = new DefaultListModel<>();
        listaNeuronas = new JList<>(modeloListaNeuronas);
        listaNeuronas.setBackground(new Color(10, 15, 25));
        listaNeuronas.setForeground(COLOR_TEXTO);
        listaNeuronas.setFont(new Font("Consolas", Font.PLAIN, 13));
        listaNeuronas.setSelectionBackground(COLOR_ACENTO2);
        listaNeuronas.setFixedCellHeight(28);
        JScrollPane scroll = new JScrollPane(listaNeuronas);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_ACENTO, 1));
        scroll.setPreferredSize(new Dimension(0, 300));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        botones.setBackground(COLOR_FONDO);
        botones.add(crearBotonPrincipal("➕ Agregar Neurona", e -> agregarNeurona()));
        botones.add(crearBotonPrincipal("➖ Eliminar Seleccionada", e -> eliminarNeurona()));
        botones.add(crearBotonPrincipal("🔄 Actualizar", e -> actualizarListaNeuronas()));

        panel.add(crearLabel("Neuronas en la red:"), BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    // ---- Panel Derecho: Resultados ----

    private JPanel crearPanelDerecho() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBackground(COLOR_FONDO);
        panel.setPreferredSize(new Dimension(440, 0));
        panel.setBorder(new EmptyBorder(8, 4, 8, 8));

        // Info del sistema
        JPanel panelInfo = new JPanel(new BorderLayout());
        panelInfo.setBackground(COLOR_PANEL);
        panelInfo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_ACENTO, 1),
            new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel lblTituloInfo = new JLabel("📊 Estado de la Red");
        lblTituloInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTituloInfo.setForeground(COLOR_ACENTO);

        lblEstadisticas = new JLabel("<html><br>Sin datos cargados.<br>Carga un archivo CSV para comenzar.</html>");
        lblEstadisticas.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEstadisticas.setForeground(COLOR_TEXTO_SEC);

        panelInfo.add(lblTituloInfo, BorderLayout.NORTH);
        panelInfo.add(lblEstadisticas, BorderLayout.CENTER);

        // Panel de acciones rápidas
        JPanel panelAcciones = new JPanel(new GridLayout(0, 1, 0, 6));
        panelAcciones.setBackground(COLOR_FONDO);
        panelAcciones.setBorder(new EmptyBorder(8, 0, 8, 0));

        panelAcciones.add(crearPanelAccionRapida("🔍 Analizar BFS/DFS",
            "Detecta zonas aisladas desde una neurona fuente", COLOR_ACENTO2,
            e -> { pestanias.setSelectedIndex(1); }));

        panelAcciones.add(crearPanelAccionRapida("⚡ Ruta Óptima (Dijkstra)",
            "Calcula la ruta más rápida entre dos neuronas", COLOR_EXCITATORIO,
            e -> { pestanias.setSelectedIndex(2); }));

        panelAcciones.add(crearPanelAccionRapida("😴 Simular Fatiga",
            "Multiplica todos los k por 1.2 (deterioro cognitivo)", COLOR_ADVERTENCIA,
            e -> simularFatiga()));

        panelAcciones.add(crearPanelAccionRapida("🧪 Diccionario NT",
            "Ver todos los neurotransmisores disponibles", COLOR_MODULADOR,
            e -> { pestanias.setSelectedIndex(3); }));

        // Log de eventos
        JLabel lblLog = new JLabel("📋 Log de Eventos:");
        lblLog.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLog.setForeground(COLOR_ACENTO);

        JTextArea areaLog = crearAreaLog();
        // Guardamos referencia global
        this.areaLog = areaLog;

        JScrollPane scrollLog = new JScrollPane(areaLog);
        scrollLog.setBorder(BorderFactory.createLineBorder(COLOR_ACENTO, 1));

        panel.add(panelInfo, BorderLayout.NORTH);
        panel.add(panelAcciones, BorderLayout.CENTER);

        JPanel panelLog = new JPanel(new BorderLayout(0, 4));
        panelLog.setBackground(COLOR_FONDO);
        panelLog.add(lblLog, BorderLayout.NORTH);
        panelLog.add(scrollLog, BorderLayout.CENTER);
        panel.add(panelLog, BorderLayout.SOUTH);

        return panel;
    }

    private JTextArea areaLog;

    private JTextArea crearAreaLog() {
        JTextArea area = new JTextArea(10, 0);
        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 11));
        area.setBackground(new Color(8, 12, 20));
        area.setForeground(new Color(100, 220, 120));
        area.setCaretColor(COLOR_ACENTO);
        area.setText("[Sistema] SynapseLogic iniciado.\n");
        return area;
    }

    private JPanel crearPanelAccionRapida(String titulo, String desc, Color color, ActionListener al) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(COLOR_PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTit.setForeground(color);

        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblDesc.setForeground(COLOR_TEXTO_SEC);

        JPanel centro = new JPanel(new GridLayout(2, 1));
        centro.setBackground(COLOR_PANEL);
        centro.add(lblTit);
        centro.add(lblDesc);

        JButton btn = new JButton("→");
        btn.setForeground(color);
        btn.setBackground(COLOR_PANEL);
        btn.setBorder(null);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.addActionListener(al);

        p.add(centro, BorderLayout.CENTER);
        p.add(btn, BorderLayout.EAST);
        p.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { al.actionPerformed(null); }
            public void mouseEntered(MouseEvent e) { p.setBackground(color.darker().darker()); centro.setBackground(color.darker().darker()); }
            public void mouseExited(MouseEvent e) { p.setBackground(COLOR_PANEL); centro.setBackground(COLOR_PANEL); }
        });
        return p;
    }

    // ================================================================
    //  BARRA DE ESTADO
    // ================================================================

    private JPanel crearBarraEstado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(10, 15, 30));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_ACENTO));
        panel.setPreferredSize(new Dimension(0, 28));

        lblEstado = new JLabel("  ✅ Sistema listo. Carga un archivo CSV para comenzar.");
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEstado.setForeground(COLOR_TEXTO_SEC);

        JLabel lblVersion = new JLabel("SynapseLogic v1.0  ");
        lblVersion.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVersion.setForeground(COLOR_TEXTO_SEC);

        panel.add(lblEstado, BorderLayout.WEST);
        panel.add(lblVersion, BorderLayout.EAST);
        return panel;
    }

    // ================================================================
    //  LÓGICA DE NEGOCIO
    // ================================================================

    /**
     * Carga el grafo desde un archivo CSV seleccionado por el usuario.
     */
    private void cargarGrafo() {
        if (hayDatosSinGuardar) {
            int resp = JOptionPane.showConfirmDialog(this,
                "Hay datos sin guardar en memoria.\n¿Deseas guardar antes de cargar un nuevo archivo?",
                "Datos sin guardar", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (resp == JOptionPane.YES_OPTION) guardarGrafo();
            else if (resp == JOptionPane.CANCEL_OPTION) return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Seleccionar archivo CSV de Red Sináptica");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos CSV", "csv", "txt"));
        if (rutaArchivoActual != null) fc.setCurrentDirectory(new File(rutaArchivoActual).getParentFile());

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String ruta = fc.getSelectedFile().getAbsolutePath();
            CargadorCSV.ResultadoCarga resultado = CargadorCSV.cargarGrafo(ruta, grafo);

            if (resultado.exitoso) {
                rutaArchivoActual = ruta;
                hayDatosSinGuardar = false;
                setEstado("✅ " + resultado.mensaje);
                log("[Carga] " + resultado.mensaje);
                actualizarTodo();

                if (!resultado.errores.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Carga completada con advertencias:\n" + String.join("\n", resultado.errores),
                        "Advertencias", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + resultado.mensaje,
                    "Error de carga", JOptionPane.ERROR_MESSAGE);
                setEstado("❌ Error al cargar archivo.");
            }
        }
    }

    /**
     * Carga el diccionario de neurotransmisores desde un archivo CSV.
     */
    private void cargarDiccionario() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Seleccionar Diccionario de Neurotransmisores (CSV)");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos CSV", "csv", "txt"));

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String ruta = fc.getSelectedFile().getAbsolutePath();
            CargadorCSV.ResultadoCarga resultado = CargadorCSV.cargarNeurotransmisores(ruta, hashTable);

            if (resultado.exitoso) {
                setEstado("✅ " + resultado.mensaje);
                log("[Diccionario] " + resultado.mensaje);
                actualizarTablaNT();
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + resultado.mensaje,
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Guarda el estado actual del grafo en un archivo CSV.
     */
    private void guardarGrafo() {
        if (grafo.getNumSinapsis() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos para guardar.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar Red Sináptica");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV", "csv"));
        if (rutaArchivoActual != null) fc.setSelectedFile(new File(rutaArchivoActual));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String ruta = fc.getSelectedFile().getAbsolutePath();
            if (!ruta.endsWith(".csv")) ruta += ".csv";
            CargadorCSV.ResultadoCarga resultado = CargadorCSV.guardarGrafo(ruta, grafo);
            if (resultado.exitoso) {
                rutaArchivoActual = ruta;
                hayDatosSinGuardar = false;
                setEstado("💾 " + resultado.mensaje);
                log("[Guardado] Archivo guardado: " + ruta);
            } else {
                JOptionPane.showMessageDialog(this, resultado.mensaje, "Error al guardar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Carga el dataset de ejemplo del proyecto (grafo de 6 neuronas).
     */
    private void cargarEjemplo() {
        if (hayDatosSinGuardar) {
            int resp = JOptionPane.showConfirmDialog(this,
                "Hay datos sin guardar. ¿Continuar y perder los cambios?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
            if (resp != JOptionPane.YES_OPTION) return;
        }

        grafo.limpiar();
        // Dataset de ejemplo del enunciado
        grafo.agregarSinapsis(new Sinapsis("1", "2", 0.85, "GLU", 1.0));
        grafo.agregarSinapsis(new Sinapsis("1", "3", 0.42, "DA",  1.0));
        grafo.agregarSinapsis(new Sinapsis("2", "4", 0.91, "GLU", 1.0));
        grafo.agregarSinapsis(new Sinapsis("3", "4", 0.15, "GABA",1.0));
        grafo.agregarSinapsis(new Sinapsis("4", "5", 0.77, "GLU", 1.0));
        grafo.agregarSinapsis(new Sinapsis("5", "6", 0.33, "DA",  1.0));
        grafo.agregarSinapsis(new Sinapsis("2", "6", 0.55, "ACH", 1.0));

        hayDatosSinGuardar = true;
        setEstado("⚡ Ejemplo cargado: 6 neuronas, 7 sinapsis.");
        log("[Ejemplo] Red de ejemplo del proyecto cargada correctamente.");
        actualizarTodo();
    }

    /**
     * Ejecuta BFS o DFS según la selección del usuario.
     */
    private void analizarConectividad() {
        if (grafo.getNumNeuronas() == 0) {
            JOptionPane.showMessageDialog(this, "Carga un archivo CSV primero.", "Sin datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String fuente = (String) cbFuente.getSelectedItem();
        if (fuente == null || fuente.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona una neurona fuente.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean usarBFS = cbAlgoritmo.getSelectedIndex() == 0;
        ResultadoConectividad resultado = usarBFS
            ? AlgoritmosConectividad.bfs(grafo, hashTable, fuente)
            : AlgoritmosConectividad.dfs(grafo, hashTable, fuente);

        // Mostrar en el área de resultados
        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════════════════\n");
        sb.append("  Análisis ").append(resultado.algoritmo)
          .append(" desde neurona fuente: ").append(fuente).append("\n");
        sb.append("══════════════════════════════════════════\n\n");

        sb.append("▶ Orden de visita:\n  ");
        sb.append(String.join(" → ", resultado.ordenVisita)).append("\n\n");

        sb.append("✅ Neuronas ALCANZABLES (").append(resultado.alcanzables.size()).append("):\n  ");
        sb.append(String.join(", ", resultado.alcanzables)).append("\n\n");

        if (resultado.aisladas.isEmpty()) {
            sb.append("🌐 RED FUERTEMENTE CONEXA\n");
            sb.append("   Todas las neuronas son alcanzables desde ").append(fuente).append(".\n");
        } else {
            sb.append("⚠️ ZONAS AISLADAS detectadas (").append(resultado.aisladas.size()).append("):\n  ");
            sb.append(String.join(", ", resultado.aisladas)).append("\n\n");
            sb.append("🔴 La red está FRAGMENTADA. Las zonas aisladas no reciben\n");
            sb.append("   señales desde la neurona fuente '").append(fuente).append("'.\n");
        }

        areaResultados.setText(sb.toString());

        // Actualizar visualización del grafo con zonas aisladas marcadas
        panelGrafo.setResultadoConectividad(resultado);
        panelGrafo.repaint();

        setEstado("🔍 Análisis " + resultado.algoritmo + " completado. "
            + resultado.aisladas.size() + " zonas aisladas detectadas.");
        log("[" + resultado.algoritmo + "] Fuente: " + fuente +
            " | Alcanzables: " + resultado.alcanzables.size() +
            " | Aisladas: " + resultado.aisladas.size());
    }

    /**
     * Ejecuta Dijkstra entre las neuronas seleccionadas.
     */
    private void calcularRutaOptima() {
        if (grafo.getNumNeuronas() == 0) {
            JOptionPane.showMessageDialog(this, "Carga un archivo CSV primero.", "Sin datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String origen = (String) cbOrigen.getSelectedItem();
        String destino = (String) cbDestino.getSelectedItem();

        if (origen == null || destino == null) {
            JOptionPane.showMessageDialog(this, "Selecciona origen y destino.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (origen.equals(destino)) {
            JOptionPane.showMessageDialog(this, "Origen y destino deben ser neuronas distintas.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ResultadoDijkstra resultado = Dijkstra.calcularRuta(grafo, hashTable, origen, destino);

        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════════════════\n");
        sb.append("  Ruta Óptima de Señal Neuronal\n");
        sb.append("  Fórmula: W = d / (v × k)\n");
        sb.append("══════════════════════════════════════════\n\n");
        sb.append("Origen:  ").append(origen).append("\n");
        sb.append("Destino: ").append(destino).append("\n\n");

        if (resultado.existeRuta) {
            sb.append("✅ Ruta encontrada:\n");
            sb.append("   ").append(String.join(" ⟶ ", resultado.ruta)).append("\n\n");
            sb.append("📊 Detalle de la transmisión:\n");
            for (String detalle : resultado.detalleSaltos) {
                sb.append("   ").append(detalle).append("\n");
            }
            sb.append("\n⏱️ Tiempo total de transmisión: ")
              .append(String.format("%.6f", resultado.costoTotal)).append(" u.t.\n");
        } else {
            sb.append("❌ No existe ruta entre ").append(origen)
              .append(" y ").append(destino).append(".\n");
            sb.append("   La señal no puede llegar al destino.\n");
        }

        areaDijkstra.setText(sb.toString());
        setEstado("⚡ Dijkstra: " + (resultado.existeRuta ? "Ruta encontrada. Costo: " +
            String.format("%.4f", resultado.costoTotal) : "Sin ruta posible."));
        log("[Dijkstra] " + origen + " → " + destino + ": " +
            (resultado.existeRuta ? "costo=" + String.format("%.4f", resultado.costoTotal) : "sin ruta"));
    }

    /**
     * Simula el deterioro cognitivo por fatiga multiplicando k × 1.2.
     */
    private void simularFatiga() {
        if (grafo.getNumSinapsis() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos en memoria.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int resp = JOptionPane.showConfirmDialog(this,
            "Esta operación multiplicará todos los factores k por 1.2,\n" +
            "simulando el deterioro cognitivo por fatiga.\n\n" +
            "¿Confirmas la operación?",
            "Simular Fatiga Cognitiva", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            grafo.aplicarFatiga();
            hayDatosSinGuardar = true;
            panelGrafo.repaint();
            setEstado("😴 Fatiga aplicada. Todos los factores k multiplicados por 1.2.");
            log("[Fatiga] Deterioro cognitivo simulado. k × 1.2 en todas las sinapsis.");
            JOptionPane.showMessageDialog(this,
                "Fatiga aplicada correctamente.\n\nTodos los factores k han sido multiplicados por 1.2.\n" +
                "Puedes volver a analizar la conectividad y recalcular rutas.",
                "Fatiga Aplicada", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Agrega una nueva neurona al grafo.
     */
    private void agregarNeurona() {
        String id = JOptionPane.showInputDialog(this,
            "Ingresa el ID de la nueva neurona:", "Agregar Neurona", JOptionPane.PLAIN_MESSAGE);
        if (id == null || id.trim().isEmpty()) return;
        id = id.trim();

        if (grafo.existeNeurona(id)) {
            JOptionPane.showMessageDialog(this, "Ya existe una neurona con ID: " + id,
                "Neurona duplicada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        grafo.agregarNeuronaPorId(id);
        hayDatosSinGuardar = true;
        actualizarTodo();
        setEstado("✅ Neurona '" + id + "' agregada.");
        log("[Agregar] Neurona '" + id + "' agregada al grafo.");
    }

    /**
     * Elimina una neurona del grafo (con todas sus sinapsis).
     */
    private void eliminarNeurona() {
        if (grafo.getNumNeuronas() == 0) {
            JOptionPane.showMessageDialog(this, "No hay neuronas en el grafo.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Selección desde lista desplegable
        String[] opciones = grafo.getIdsNeuronas().stream().sorted().toArray(String[]::new);
        String seleccionada = (String) JOptionPane.showInputDialog(this,
            "Selecciona la neurona a eliminar:", "Eliminar Neurona",
            JOptionPane.PLAIN_MESSAGE, null, opciones,
            listaNeuronas.getSelectedValue() != null ? listaNeuronas.getSelectedValue() : opciones[0]);

        if (seleccionada == null) return;

        int resp = JOptionPane.showConfirmDialog(this,
            "¿Eliminar neurona '" + seleccionada + "' y todas sus sinapsis?",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            boolean eliminada = grafo.eliminarNeurona(seleccionada);
            if (eliminada) {
                hayDatosSinGuardar = true;
                actualizarTodo();
                setEstado("🗑️ Neurona '" + seleccionada + "' eliminada.");
                log("[Eliminar] Neurona '" + seleccionada + "' eliminada del grafo.");
            }
        }
    }

    /**
     * Busca un neurotransmisor específico en la tabla.
     *
     * @param id ID a buscar
     */
    private void buscarNT(String id) {
        if (id == null || id.trim().isEmpty()) {
            actualizarTablaNT();
            return;
        }

        Neurotransmisor nt = hashTable.buscar(id.trim());
        modeloTablaNT.setRowCount(0);

        if (nt != null) {
            modeloTablaNT.addRow(new Object[]{
                nt.getId(), nt.getNombre(), nt.getEfecto(),
                String.format("%.1f", nt.getVelocidad()), nt.getDescripcion()
            });
            setEstado("🔍 Neurotransmisor '" + nt.getId() + "' encontrado.");
        } else {
            setEstado("❌ Neurotransmisor '" + id + "' no encontrado en el diccionario.");
            JOptionPane.showMessageDialog(this,
                "No se encontró el neurotransmisor con ID: " + id.toUpperCase(),
                "No encontrado", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Muestra estadísticas generales del grafo.
     */
    private void mostrarEstadisticas() {
        if (grafo.getNumNeuronas() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos cargados.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Contar tipos de NT
        Map<String, Integer> conteoNT = new HashMap<>();
        for (Sinapsis s : grafo.getTodasSinapsis()) {
            conteoNT.merge(s.getIdNeurotransmisor(), 1, Integer::sum);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("═══ Estadísticas de la Red Sináptica ═══\n\n");
        sb.append("Neuronas:    ").append(grafo.getNumNeuronas()).append("\n");
        sb.append("Sinapsis:    ").append(grafo.getNumSinapsis()).append("\n");
        sb.append("NT en dic.:  ").append(hashTable.getTamaño()).append("\n\n");
        sb.append("Neurotransmisores más usados:\n");
        conteoNT.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .forEach(e -> sb.append("  ").append(e.getKey()).append(": ").append(e.getValue()).append(" sinapsis\n"));

        JOptionPane.showMessageDialog(this, sb.toString(), "Estadísticas", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarAyuda() {
        JOptionPane.showMessageDialog(this,
            "═══ INSTRUCCIONES DE USO ═══\n\n" +
            "1. CARGAR DATOS:\n" +
            "   • Usa 'Archivo → Cargar Grafo' para cargar la red sináptica (CSV).\n" +
            "   • Usa 'Archivo → Cargar Diccionario NT' para el diccionario de neurotransmisores.\n" +
            "   • O usa 'Cargar Datos de Ejemplo' para ver una demo de 6 neuronas.\n\n" +
            "2. BFS / DFS (Detección de Zonas Aisladas):\n" +
            "   • Ve a la pestaña 'BFS / DFS'.\n" +
            "   • Selecciona la neurona fuente (donde se origina el estímulo).\n" +
            "   • Elige BFS o DFS y presiona 'Analizar Conectividad'.\n" +
            "   • Las zonas aisladas se marcan en ROJO en el grafo.\n\n" +
            "3. RUTA ÓPTIMA (Dijkstra):\n" +
            "   • Ve a la pestaña 'Ruta Óptima'.\n" +
            "   • Selecciona origen y destino.\n" +
            "   • El peso se calcula como W = d / (v × k).\n\n" +
            "4. FATIGA COGNITIVA:\n" +
            "   • Usa el botón 'Fatiga' para multiplicar todos los k × 1.2.\n" +
            "   • Luego recalcula BFS/DFS o Dijkstra para ver el efecto.\n\n" +
            "5. MODIFICAR LA RED:\n" +
            "   • Usa los botones '+ Neurona' y '- Eliminar' para modificar el grafo.\n" +
            "   • Arrastra los nodos del grafo visual para reorganizarlos.\n\n" +
            "6. GUARDAR:\n" +
            "   • Usa 'Archivo → Guardar Grafo' para persistir los cambios.",
            "Instrucciones de Uso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarAcercaDe() {
        JOptionPane.showMessageDialog(this,
            "SynapseLogic v1.0\n" +
            "Análisis de Conectividad y Transmisión Neuronal\n\n" +
            "Universidad Metropolitana\n" +
            "Departamento de Gestión de Proyectos y Sistemas\n" +
            "Estructuras de Datos — BPTSP06\n" +
            "Trimestre 2526-3\n\n" +
            "Estructuras implementadas:\n" +
            "  • Grafo dirigido (lista de adyacencia)\n" +
            "  • Hash Table propia (encadenamiento)\n" +
            "  • BFS, DFS, Dijkstra\n",
            "Acerca de SynapseLogic", JOptionPane.INFORMATION_MESSAGE);
    }

    private void salir() {
        if (hayDatosSinGuardar) {
            int resp = JOptionPane.showConfirmDialog(this,
                "Hay datos sin guardar. ¿Deseas guardar antes de salir?",
                "Salir", JOptionPane.YES_NO_CANCEL_OPTION);
            if (resp == JOptionPane.YES_OPTION) guardarGrafo();
            else if (resp == JOptionPane.CANCEL_OPTION) return;
        }
        System.exit(0);
    }

    // ================================================================
    //  ACTUALIZACIÓN DE COMPONENTES
    // ================================================================

    /**
     * Carga el diccionario de neurotransmisores por defecto en la Hash Table.
     */
    private void cargarDiccionarioPorDefecto() {
        String[][] datos = {
            {"GLU","Glutamato","Excitatorio","2.5","Principal mediador de información sensorial y motora."},
            {"GABA","Ácido Gamma-aminobutírico","Inhibitorio","1.2","Reduce la actividad neuronal; control del estrés."},
            {"ACH","Acetilcolina","Excitatorio","2.0","Fundamental para la memoria y activación muscular."},
            {"DA","Dopamina","Modulador","1.5","Regula el placer, recompensa y la motivación."},
            {"5HT","Serotonina","Modulador","1.0","Influye en el estado de ánimo y ciclo del sueño."},
            {"NE","Norepinefrina","Excitatorio","1.8","Relacionado con la atención y respuesta de alerta."},
            {"GLY","Glicina","Inhibitorio","1.1","Principal inhibidor en la médula espinal."},
            {"HIS","Histamina","Excitatorio","1.4","Regula el despertar y la atención."},
            {"ASP","Aspartato","Excitatorio","2.3","Estimulante cerebral similar al glutamato."},
            {"EPI","Epinefrina","Excitatorio","2.0","Respuesta de estrés agudo (adrenalina)."},
            {"NO","Óxido Nítrico","Modulador","3.0","Gas que difunde libremente (retroalimentación)."},
            {"CO","Monóxido de Carbono","Modulador","2.8","Mensajero gaseoso de larga distancia."},
            {"ATP","Adenosín trifosfato","Excitatorio","2.2","Co-transmisor en sinapsis rápidas."},
            {"ADEN","Adenosina","Inhibitorio","0.4","Induce el sueño y suprime el despertar."},
            {"OX","Oxitocina","Modulador","0.8","Vinculación social y confianza."},
            {"OREX","Orexina (Hipocretina)","Excitatorio","1.9","Mantiene el estado de vigilia."}
        };
        for (String[] d : datos) {
            hashTable.insertar(new Neurotransmisor(d[0], d[1], d[2],
                Double.parseDouble(d[3]), d[4]));
        }
    }

    /**
     * Actualiza todos los componentes de la interfaz con los datos actuales.
     */
    private void actualizarTodo() {
        actualizarCombos();
        actualizarListaNeuronas();
        actualizarEstadisticas();
        panelGrafo.setGrafo(grafo, hashTable);
        panelGrafo.repaint();
    }

    private void actualizarCombos() {
        List<String> ids = new ArrayList<>(grafo.getIdsNeuronas());
        Collections.sort(ids);

        Object selectedFuente = cbFuente.getSelectedItem();
        Object selectedOrigen = cbOrigen.getSelectedItem();
        Object selectedDestino = cbDestino.getSelectedItem();

        cbFuente.removeAllItems();
        cbOrigen.removeAllItems();
        cbDestino.removeAllItems();

        for (String id : ids) {
            cbFuente.addItem(id);
            cbOrigen.addItem(id);
            cbDestino.addItem(id);
        }

        if (selectedFuente != null) cbFuente.setSelectedItem(selectedFuente);
        if (selectedOrigen != null) cbOrigen.setSelectedItem(selectedOrigen);
        if (selectedDestino != null) cbDestino.setSelectedItem(selectedDestino);
    }

    private void actualizarListaNeuronas() {
        modeloListaNeuronas.clear();
        List<String> ids = new ArrayList<>(grafo.getIdsNeuronas());
        Collections.sort(ids);
        for (String id : ids) {
            Neurona n = grafo.getNeurona(id);
            modeloListaNeuronas.addElement(
                String.format("  [%s]  k=%.4f  |  conexiones salientes: %d",
                    id, n.getK(), grafo.getSinapsisDesde(id).size())
            );
        }
    }

    private void actualizarTablaNT() {
        modeloTablaNT.setRowCount(0);
        Neurotransmisor[] todos = hashTable.obtenerTodos();
        Arrays.sort(todos, Comparator.comparing(Neurotransmisor::getId));
        for (Neurotransmisor nt : todos) {
            modeloTablaNT.addRow(new Object[]{
                nt.getId(), nt.getNombre(), nt.getEfecto(),
                String.format("%.1f", nt.getVelocidad()), nt.getDescripcion()
            });
        }
    }

    private void actualizarEstadisticas() {
        if (grafo.getNumNeuronas() == 0) {
            lblEstadisticas.setText("<html><br>Sin datos cargados.<br>Carga un archivo CSV.</html>");
        } else {
            lblEstadisticas.setText(String.format(
                "<html><br>🔵 Neuronas: <b>%d</b><br>" +
                "🔗 Sinapsis: <b>%d</b><br>" +
                "🧪 Neurotransmisores en dic.: <b>%d</b><br>" +
                "📁 Archivo: <b>%s</b></html>",
                grafo.getNumNeuronas(), grafo.getNumSinapsis(), hashTable.getTamaño(),
                rutaArchivoActual != null ? new File(rutaArchivoActual).getName() : "—"
            ));
        }
    }

    // ================================================================
    //  HELPERS DE ESTILO
    // ================================================================

    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(COLOR_TEXTO);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return lbl;
    }

    private JButton crearBotonPrincipal(String texto, ActionListener al) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(COLOR_ACENTO.darker());
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_ACENTO, 1),
            BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(COLOR_ACENTO); }
            public void mouseExited(MouseEvent e) { btn.setBackground(COLOR_ACENTO.darker()); }
        });
        return btn;
    }

    private JButton crearBotonPequeño(String texto, ActionListener al) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(COLOR_TEXTO);
        btn.setBackground(COLOR_BOTON);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_ACENTO2, 1),
            BorderFactory.createEmptyBorder(3, 10, 3, 10)
        ));
        btn.setFocusPainted(false);
        btn.addActionListener(al);
        return btn;
    }

    private void estilizarCombo(JComboBox<String> cb) {
        cb.setBackground(COLOR_BOTON);
        cb.setForeground(COLOR_TEXTO);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBorder(BorderFactory.createLineBorder(COLOR_ACENTO, 1));
        cb.setPreferredSize(new Dimension(200, 30));
    }

    private void estilizarTextField(JTextField tf) {
        tf.setBackground(COLOR_BOTON);
        tf.setForeground(COLOR_TEXTO);
        tf.setCaretColor(COLOR_ACENTO);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_ACENTO, 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
    }

    private void estilizarTabla(JTable tabla) {
        tabla.setBackground(new Color(12, 18, 30));
        tabla.setForeground(COLOR_TEXTO);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setRowHeight(26);
        tabla.setGridColor(new Color(30, 45, 70));
        tabla.setSelectionBackground(COLOR_ACENTO2.darker());
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setShowHorizontalLines(true);
        tabla.setShowVerticalLines(false);
        JTableHeader header = tabla.getTableHeader();
        header.setBackground(COLOR_PANEL);
        header.setForeground(COLOR_ACENTO);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    private void setEstado(String mensaje) {
        lblEstado.setText("  " + mensaje);
    }

    private void log(String mensaje) {
        if (areaLog != null) {
            areaLog.append(mensaje + "\n");
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        }
    }

    // ================================================================
    //  MAIN
    // ================================================================

    /**
     * Punto de entrada de la aplicación.
     *
     * @param args Argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
