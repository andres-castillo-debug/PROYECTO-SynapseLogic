package sinapsis.modelo;

/**
 * Representa una sinapsis (arista dirigida) entre dos neuronas.
 * Contiene la información de la conexión incluyendo distancia,
 * el tipo de neurotransmisor y el coeficiente de eficiencia sináptica k.
 *
 * @author SynapseLogic Team
 * @version 1.0
 */
public class Sinapsis {

    /** Neurona de origen (presináptica) */
    private String idOrigen;

    /** Neurona de destino (postsináptica) */
    private String idDestino;

    /** Distancia sináptica obtenida del CSV */
    private double distancia;

    /** Identificador del neurotransmisor (ej: "GLU", "GABA") */
    private String idNeurotransmisor;

    /** Factor de eficiencia sináptica k */
    private double k;

    /**
     * Constructor de Sinapsis.
     *
     * @param idOrigen           ID de la neurona de origen
     * @param idDestino          ID de la neurona de destino
     * @param distancia          Distancia sináptica
     * @param idNeurotransmisor  Código del neurotransmisor
     * @param k                  Coeficiente de eficiencia sináptica
     */
    public Sinapsis(String idOrigen, String idDestino, double distancia,
                    String idNeurotransmisor, double k) {
        this.idOrigen = idOrigen;
        this.idDestino = idDestino;
        this.distancia = distancia;
        this.idNeurotransmisor = idNeurotransmisor;
        this.k = k;
    }

    /**
     * Calcula el peso de la arista usando la fórmula W = d / (v * k),
     * donde v se obtiene del neurotransmisor correspondiente.
     *
     * @param velocidad Factor de velocidad del neurotransmisor
     * @return Peso calculado de la arista
     */
    public double calcularPeso(double velocidad) {
        if (velocidad <= 0 || k <= 0) return Double.MAX_VALUE;
        return distancia / (velocidad * k);
    }

    // ---- Getters y Setters ----

    /** @return ID de la neurona origen */
    public String getIdOrigen() { return idOrigen; }

    /** @return ID de la neurona destino */
    public String getIdDestino() { return idDestino; }

    /** @return Distancia sináptica */
    public double getDistancia() { return distancia; }

    /** @return ID del neurotransmisor */
    public String getIdNeurotransmisor() { return idNeurotransmisor; }

    /** @return Factor k de eficiencia sináptica */
    public double getK() { return k; }

    /**
     * Establece el factor k.
     *
     * @param k nuevo factor de atenuación
     */
    public void setK(double k) { this.k = k; }

    /**
     * Aplica fatiga multiplicando k por 1.2.
     */
    public void aplicarFatiga() {
        this.k *= 1.2;
    }

    @Override
    public String toString() {
        return idOrigen + " -> " + idDestino +
               " [d=" + distancia + ", NT=" + idNeurotransmisor +
               ", k=" + String.format("%.4f", k) + "]";
    }
}
