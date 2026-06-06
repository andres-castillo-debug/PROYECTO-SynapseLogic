package sinapsis.modelo;

/**
 * Representa una neurona en la red sináptica.
 * Es el nodo del grafo dirigido.
 *
 * @author SynapseLogic Team
 * @version 1.0
 */
public class Neurona {

    /** Identificador único de la neurona */
    private String id;

    /** Factor de atenuación k (eficiencia sináptica). Inicialmente 1.0 */
    private double k;

    /**
     * Constructor de Neurona.
     *
     * @param id Identificador único de la neurona
     */
    public Neurona(String id) {
        this.id = id;
        this.k = 1.0;
    }

    /**
     * Constructor de Neurona con factor k inicial.
     *
     * @param id Identificador único de la neurona
     * @param k  Factor de atenuación sináptica
     */
    public Neurona(String id, double k) {
        this.id = id;
        this.k = k;
    }

    /**
     * Obtiene el identificador de la neurona.
     *
     * @return id de la neurona
     */
    public String getId() {
        return id;
    }

    /**
     * Obtiene el factor de atenuación k de la neurona.
     *
     * @return factor k
     */
    public double getK() {
        return k;
    }

    /**
     * Establece el factor de atenuación k.
     *
     * @param k nuevo factor de atenuación
     */
    public void setK(double k) {
        this.k = k;
    }

    /**
     * Simula fatiga multiplicando k por 1.2.
     */
    public void aplicarFatiga() {
        this.k *= 1.2;
    }

    @Override
    public String toString() {
        return "Neurona{id='" + id + "', k=" + String.format("%.4f", k) + "}";
    }
}
