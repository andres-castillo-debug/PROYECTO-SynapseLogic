package sinapsis.interfaz;

import sinapsis.algoritmos.AlgoritmosConectividad.ResultadoConectividad;
import sinapsis.estructura.GrafoDirigido;
import sinapsis.estructura.HashTableNeurotransmisores;
import sinapsis.modelo.Neurotransmisor;
import sinapsis.modelo.Sinapsis;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * Panel de visualización interactiva del grafo sináptico.
 * Renderiza neuronas como nodos y sinapsis como aristas dirigidas.
 *
 * <p>Características:
 * <ul>
 *   <li>Nodos arrastrables por el usuario (distribución manual)</li>
 *   <li>Aristas con flechas indicando dirección del impulso</li>
 *   <li>Colores según tipo de neurotransmisor</li>
 *   <li>Resaltado de zonas aisladas en rojo</li>
 *   <li>Etiquetas con distancia, NT y factor k</li>
 * </ul>
 * </p>
 *
 * @author SynapseLogic Team
 * @version 1.0
 */
public class PanelGrafo extends JPanel {

    // ---- Colores ----
    private static final Color COL_FONDO        = new Color(8, 14, 26);
    private static final Color COL_NODO         = new Color(30, 80, 150);
    private static final Color COL_NODO_BORDE   = new Color(0, 180, 216);
    private static final Color COL_NODO_FUENTE  = new Color(0, 180, 216);
    private static final Color COL_NODO_AISLADO = new Color(180, 40, 40);
    private static final Color COL_NODO_ALCAN   = new Color(30, 130, 80);
    private static final Color COL_TEXTO_NODO   = Color.WHITE;
    private static final Color COL_ARISTA_GLU   = new Color(80, 200, 80);   // excitatorio
    private static final Color COL_ARISTA_GABA  = new Color(220, 80, 80);   // inhibitorio
    private static final Color COL_ARISTA_DEF   = new Color(150, 180, 220); // modulador/default
    private static final Color COL_TEXTO_ARISTA = new Color(200, 220, 255);
    private static final Color COL_GRID         = new Color(20, 30, 50);

    private static final int RADIO_NODO = 26;

    // ---- Datos ----
    private GrafoDirigido grafo;
    private HashTableNeurotransmisores hashTable;
    private ResultadoConectividad resultadoConectividad;

    /** Posiciones de cada nodo: ID → Point */
    private Map<String, Point> posiciones = new HashMap<>();

    // ---- Arrastre ----
    private String nodoArrastrado = null;
    private int offsetX, offsetY;

    // ---- Tooltip ----
    private String tooltipTexto = null;
    private Point tooltipPos = null;

    /**
     * Constructor del panel de grafo.
     */
    public PanelGrafo() {
        setBackground(COL_FONDO);
        setPreferredSize(new Dimension(800, 500));
        configurarEventos();
    }

    /**
     * Configura los listeners de ratón para arrastre e interacción.
     */
    private void configurarEventos() {
        MouseAdapter ma = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                nodoArrastrado = getNodoEnPunto(e.getX(), e.getY());
                if (nodoArrastrado != null) {
                    Point pos = posiciones.get(nodoArrastrado);
                    offsetX = e.getX() - pos.x;
                    offsetY = e.getY() - pos.y;
                }
            }

            public void mouseReleased(MouseEvent e) {
                nodoArrastrado = null;
            }

            public void mouseDragged(MouseEvent e) {
                if (nodoArrastrado != null) {
                    int nx = Math.max(RADIO_NODO, Math.min(getWidth() - RADIO_NODO, e.getX() - offsetX));
                    int ny = Math.max(RADIO_NODO, Math.min(getHeight() - RADIO_NODO, e.getY() - offsetY));
                    posiciones.put(nodoArrastrado, new Point(nx, ny));
                    repaint();
                }
            }

            public void mouseMoved(MouseEvent e) {
                String nodo = getNodoEnPunto(e.getX(), e.getY());
                if (nodo != null && grafo != null) {
                    List<Sinapsis> salientes = grafo.getSinapsisDesde(nodo);
                    tooltipTexto = "Neurona: " + nodo +
                        " | k=" + String.format("%.4f", grafo.getNeurona(nodo).getK()) +
                        " | conexiones: " + salientes.size();
                    tooltipPos = new Point(e.getX() + 12, e.getY() - 8);
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    tooltipTexto = null;
                    setCursor(Cursor.getDefaultCursor());
                }
                repaint();
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    /**
     * Actualiza el grafo a visualizar y calcula posiciones iniciales.
     *
     * @param grafo     El grafo de la red
     * @param hashTable Hash Table de neurotransmisores
     */
    public void setGrafo(GrafoDirigido grafo, HashTableNeurotransmisores hashTable) {
        this.grafo = grafo;
        this.hashTable = hashTable;
        this.resultadoConectividad = null;

        // Calcular posiciones iniciales en círculo
        calcularPosicionesCirculo();
        repaint();
    }

    /**
     * Establece el resultado de conectividad para resaltar zonas aisladas.
     *
     * @param resultado El resultado de BFS/DFS
     */
    public void setResultadoConectividad(ResultadoConectividad resultado) {
        this.resultadoConectividad = resultado;
        repaint();
    }

    /**
     * Distribuye los nodos en un círculo centrado en el panel.
     */
    private void calcularPosicionesCirculo() {
        if (grafo == null) return;

        List<String> ids = new ArrayList<>(grafo.getIdsNeuronas());
        Collections.sort(ids);
        int n = ids.size();
        if (n == 0) return;

        int cx = getPreferredSize().width / 2;
        int cy = getPreferredSize().height / 2;
        double radio = Math.min(cx, cy) * 0.72;

        // Conservar posiciones existentes
        for (int i = 0; i < n; i++) {
            String id = ids.get(i);
            if (!posiciones.containsKey(id)) {
                double angulo = 2 * Math.PI * i / n - Math.PI / 2;
                int x = (int) (cx + radio * Math.cos(angulo));
                int y = (int) (cy + radio * Math.sin(angulo));
                posiciones.put(id, new Point(x, y));
            }
        }

        // Eliminar posiciones de nodos ya no existentes
        posiciones.keySet().retainAll(grafo.getIdsNeuronas());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        pintarFondo(g2);

        if (grafo == null || grafo.getNumNeuronas() == 0) {
            pintarMensajeVacio(g2);
            return;
        }

        // Recalcular posiciones si hay nodos sin posición
        for (String id : grafo.getIdsNeuronas()) {
            if (!posiciones.containsKey(id)) calcularPosicionesCirculo();
        }

        pintarAristas(g2);
        pintarNodos(g2);
        pintarTooltip(g2);
    }

    private void pintarFondo(Graphics2D g2) {
        g2.setColor(COL_FONDO);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Grid sutil
        g2.setColor(COL_GRID);
        g2.setStroke(new BasicStroke(0.5f));
        for (int x = 0; x < getWidth(); x += 40) g2.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += 40) g2.drawLine(0, y, getWidth(), y);
    }

    private void pintarMensajeVacio(Graphics2D g2) {
        g2.setColor(new Color(60, 80, 120));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        String msg = "Carga un archivo CSV o usa el ejemplo para visualizar la red neuronal";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g2.setColor(new Color(40, 60, 100));
        String sub = "Archivo → Cargar Grafo  |  Botón '⚡ Ejemplo'";
        fm = g2.getFontMetrics();
        g2.drawString(sub, (getWidth() - fm.stringWidth(sub)) / 2, getHeight() / 2 + 28);
    }

    /**
     * Dibuja todas las aristas (sinapsis) del grafo.
     *
     * @param g2 Contexto gráfico
     */
    private void pintarAristas(Graphics2D g2) {
        if (hashTable == null) return;

        for (Sinapsis s : grafo.getTodasSinapsis()) {
            Point pOrigen = posiciones.get(s.getIdOrigen());
            Point pDestino = posiciones.get(s.getIdDestino());
            if (pOrigen == null || pDestino == null) continue;

            // Color según tipo de neurotransmisor
            Neurotransmisor nt = hashTable.buscar(s.getIdNeurotransmisor());
            Color colorArista = COL_ARISTA_DEF;
            if (nt != null) {
                String efecto = nt.getEfecto().toLowerCase();
                if (efecto.contains("excit")) colorArista = COL_ARISTA_GLU;
                else if (efecto.contains("inhib")) colorArista = COL_ARISTA_GABA;
                else colorArista = COL_ARISTA_DEF;
            }

            // Dibujar arista curva si hay arista inversa para evitar solapamiento
            boolean hayInversa = tieneAristaInversa(s.getIdOrigen(), s.getIdDestino());
            dibujarArista(g2, pOrigen, pDestino, colorArista, s, hayInversa);
        }
    }

    /**
     * Dibuja una arista dirigida con punta de flecha.
     */
    private void dibujarArista(Graphics2D g2, Point origen, Point destino,
                                Color color, Sinapsis sinapsis, boolean curva) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        double dx = destino.x - origen.x;
        double dy = destino.y - origen.y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 1) return;

        // Punto donde termina la arista (en el borde del nodo destino)
        double tx = destino.x - (dx / dist) * RADIO_NODO;
        double ty = destino.y - (dy / dist) * RADIO_NODO;
        // Punto donde empieza (en el borde del nodo origen)
        double sx = origen.x + (dx / dist) * RADIO_NODO;
        double sy = origen.y + (dy / dist) * RADIO_NODO;

        if (curva) {
            // Línea curva para evitar solapamiento con arista inversa
            double perpX = -dy / dist * 30;
            double perpY = dx / dist * 30;
            double ctrlX = (sx + tx) / 2 + perpX;
            double ctrlY = (sy + ty) / 2 + perpY;

            Path2D path = new Path2D.Double();
            path.moveTo(sx, sy);
            path.quadTo(ctrlX, ctrlY, tx, ty);
            g2.draw(path);

            // Flecha en curva (aproximada)
            dibujarFlecha(g2, ctrlX, ctrlY, tx, ty, color);

            // Etiqueta
            double mx = (sx + ctrlX + tx) / 3;
            double my = (sy + ctrlY + ty) / 3;
            pintarEtiquetaArista(g2, sinapsis, (int) mx, (int) my);
        } else {
            g2.draw(new Line2D.Double(sx, sy, tx, ty));
            dibujarFlecha(g2, sx, sy, tx, ty, color);

            // Etiqueta en el centro
            int mx = (int) ((sx + tx) / 2);
            int my = (int) ((sy + ty) / 2);
            pintarEtiquetaArista(g2, sinapsis, mx, my);
        }
    }

    /**
     * Dibuja la punta de flecha en el extremo de una arista.
     */
    private void dibujarFlecha(Graphics2D g2, double x1, double y1,
                                double x2, double y2, Color color) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double angulo = Math.atan2(dy, dx);
        int largo = 10;
        double apertura = Math.PI / 7;

        int[] xs = {
            (int) x2,
            (int) (x2 - largo * Math.cos(angulo - apertura)),
            (int) (x2 - largo * Math.cos(angulo + apertura))
        };
        int[] ys = {
            (int) y2,
            (int) (y2 - largo * Math.sin(angulo - apertura)),
            (int) (y2 - largo * Math.sin(angulo + apertura))
        };

        g2.setColor(color);
        g2.fillPolygon(xs, ys, 3);
    }

    private void pintarEtiquetaArista(Graphics2D g2, Sinapsis s, int mx, int my) {
        String label = String.format("d:%.2f (%s) k:%.2f", s.getDistancia(),
            s.getIdNeurotransmisor(), s.getK());

        g2.setFont(new Font("Consolas", Font.PLAIN, 10));
        FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(label);

        // Fondo semi-transparente
        g2.setColor(new Color(8, 14, 26, 180));
        g2.fillRoundRect(mx - w / 2 - 3, my - 10, w + 6, 13, 4, 4);

        g2.setColor(COL_TEXTO_ARISTA);
        g2.drawString(label, mx - w / 2, my);
    }

    /**
     * Dibuja todos los nodos (neuronas) del grafo.
     *
     * @param g2 Contexto gráfico
     */
    private void pintarNodos(Graphics2D g2) {
        String fuente = resultadoConectividad != null ? resultadoConectividad.fuente : null;

        for (String id : grafo.getIdsNeuronas()) {
            Point pos = posiciones.get(id);
            if (pos == null) continue;

            // Determinar color del nodo
            Color colorNodo = COL_NODO;
            Color colorBorde = COL_NODO_BORDE;

            if (resultadoConectividad != null) {
                if (id.equals(fuente)) {
                    colorNodo = COL_NODO_FUENTE;
                    colorBorde = Color.WHITE;
                } else if (resultadoConectividad.aisladas.contains(id)) {
                    colorNodo = COL_NODO_AISLADO;
                    colorBorde = new Color(255, 80, 80);
                } else if (resultadoConectividad.alcanzables.contains(id)) {
                    colorNodo = COL_NODO_ALCAN;
                    colorBorde = new Color(80, 220, 130);
                }
            }

            // Sombra
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillOval(pos.x - RADIO_NODO + 3, pos.y - RADIO_NODO + 3,
                RADIO_NODO * 2, RADIO_NODO * 2);

            // Gradiente del nodo
            RadialGradientPaint grad = new RadialGradientPaint(
                new Point2D.Float(pos.x - 5, pos.y - 5),
                RADIO_NODO,
                new float[]{0f, 1f},
                new Color[]{colorNodo.brighter(), colorNodo.darker()}
            );
            g2.setPaint(grad);
            g2.fillOval(pos.x - RADIO_NODO, pos.y - RADIO_NODO, RADIO_NODO * 2, RADIO_NODO * 2);

            // Borde
            g2.setColor(colorBorde);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(pos.x - RADIO_NODO, pos.y - RADIO_NODO, RADIO_NODO * 2, RADIO_NODO * 2);

            // Texto del ID
            g2.setColor(COL_TEXTO_NODO);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(id, pos.x - fm.stringWidth(id) / 2, pos.y + fm.getAscent() / 2 - 1);

            // Indicador de zona aislada
            if (resultadoConectividad != null && resultadoConectividad.aisladas.contains(id)) {
                g2.setColor(new Color(255, 100, 100));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                g2.drawString("AISLADA", pos.x - 18, pos.y + RADIO_NODO + 11);
            }
        }
    }

    private void pintarTooltip(Graphics2D g2) {
        if (tooltipTexto == null || tooltipPos == null) return;

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(tooltipTexto) + 10;
        int h = 20;
        int tx = Math.min(tooltipPos.x, getWidth() - w - 4);
        int ty = Math.max(tooltipPos.y, h + 4);

        g2.setColor(new Color(20, 30, 50, 220));
        g2.fillRoundRect(tx, ty - h + 2, w, h, 6, 6);
        g2.setColor(COL_NODO_BORDE);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(tx, ty - h + 2, w, h, 6, 6);
        g2.setColor(COL_TEXTO_ARISTA);
        g2.drawString(tooltipTexto, tx + 5, ty - 3);
    }

    /**
     * Verifica si existe una arista en dirección inversa (B → A dado A → B).
     *
     * @param origen  ID del nodo origen
     * @param destino ID del nodo destino
     * @return true si existe arista inversa
     */
    private boolean tieneAristaInversa(String origen, String destino) {
        if (grafo == null) return false;
        for (Sinapsis s : grafo.getSinapsisDesde(destino)) {
            if (s.getIdDestino().equals(origen)) return true;
        }
        return false;
    }

    /**
     * Determina qué nodo (si alguno) está en las coordenadas dadas.
     *
     * @param px Coordenada X del cursor
     * @param py Coordenada Y del cursor
     * @return ID del nodo, o null si no hay nodo en ese punto
     */
    private String getNodoEnPunto(int px, int py) {
        for (Map.Entry<String, Point> entry : posiciones.entrySet()) {
            Point p = entry.getValue();
            double dist = Math.sqrt(Math.pow(px - p.x, 2) + Math.pow(py - p.y, 2));
            if (dist <= RADIO_NODO) return entry.getKey();
        }
        return null;
    }
}
