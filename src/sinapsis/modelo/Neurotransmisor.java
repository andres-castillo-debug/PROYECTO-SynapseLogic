package sinapsis.modelo;

/**
 * Representa un neurotransmisor con sus propiedades químicas.
 * Se almacena en la Hash Table del diccionario de neurotransmisores.
 *
 * @author SynapseLogic Team
 * @version 1.0
 */
public class Neurotransmisor {

    /** Identificador único del neurotransmisor (ej: "GLU", "GABA") */
    private String id;

    /** Nombre completo del neurotransmisor */
    private String nombre;

    /** Efecto: Excitatorio, Inhibitorio, Modulador */
    private String efecto;

    /** Factor de velocidad de transmisión */
    private double velocidad;

    /** Descripción breve del neurotransmisor */
    private String descripcion;

    /**
     * Constructor completo de Neurotransmisor.
     *
     * @param id          Código identificador (ej: "GLU")
     * @param nombre      Nombre completo
     * @param efecto      Tipo de efecto (Excitatorio/Inhibitorio/Modulador)
     * @param velocidad   Factor de velocidad de transmisión
     * @param descripcion Descripción breve
     */
    public Neurotransmisor(String id, String nombre, String efecto,
                           double velocidad, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.efecto = efecto;
        this.velocidad = velocidad;
        this.descripcion = descripcion;
    }

    /** @return ID del neurotransmisor */
    public String getId() { return id; }

    /** @return Nombre del neurotransmisor */
    public String getNombre() { return nombre; }

    /** @return Efecto del neurotransmisor */
    public String getEfecto() { return efecto; }

    /** @return Factor de velocidad */
    public double getVelocidad() { return velocidad; }

    /** @return Descripción del neurotransmisor */
    public String getDescripcion() { return descripcion; }

    @Override
    public String toString() {
        return id + " | " + nombre + " | " + efecto +
               " | v=" + velocidad + " | " + descripcion;
    }
}
