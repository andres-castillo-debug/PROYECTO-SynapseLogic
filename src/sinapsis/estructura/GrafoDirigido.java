package sinapsis.estructura;

import sinapsis.modelo.Neurona;
import sinapsis.modelo.Sinapsis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Grafo dirigido que representa la red sináptica neuronal.
 * Implementado mediante lista de adyacencia.
 *
 * <p>Los nodos son objetos {@link Neurona} y las aristas son objetos
 * {@link Sinapsis}. El grafo es dirigido: una arista de A a B no implica
 * una arista de B a A (impulso nervioso unidireccional).</p>
 *
 * <p>Implementación propia sin uso de librerías de estructuras de datos.</p>
 *
 * @author SynapseLogic Team
 * @version 1.0
 */
public class GrafoDirigido {

    /**
     * Mapa de neuronas: ID -> Neurona.
     * Permite acceso O(1) a cada neurona.
     */
    private Map<String, Neurona> neuronas;

    /**
     * Lista de adyacencia: ID_origen -> lista de Sinapsis salientes.
     */
    private Map<String, List<Sinapsis>> listaAdyacencia;

    /**
     * Lista de todas las sinapsis del grafo.
     */
    private List<Sinapsis> todasSinapsis;

    /**
     * Constructor: inicializa el grafo vacío.
     */
    public GrafoDirigido() {
        neuronas = new HashMap<>();
        listaAdyacencia = new HashMap<>();
        todasSinapsis = new ArrayList<>();
    }

    /**
     * Agrega una neurona al grafo si no existe ya.
     *
     * @param neurona La neurona a agregar
     */
    public void agregarNeurona(Neurona neurona) {
        String id = neurona.getId();
        if (!neuronas.containsKey(id)) {
            neuronas.put(id, neurona);
            listaAdyacencia.put(id, new ArrayList<>());
        }
    }

    /**
     * Agrega una neurona nueva dado su ID.
     *
     * @param id Identificador de la nueva neurona
     * @return true si fue agregada, false si ya existía
     */
    public boolean agregarNeuronaPorId(String id) {
        id = id.trim();
        if (neuronas.containsKey(id)) return false;
        agregarNeurona(new Neurona(id));
        return true;
    }

    /**
     * Elimina una neurona y todas sus sinapsis (entrantes y salientes).
     *
     * @param id ID de la neurona a eliminar
     * @return true si fue eliminada, false si no existía
     */
    public boolean eliminarNeurona(String id) {
        id = id.trim();
        if (!neuronas.containsKey(id)) return false;

        // Eliminar sinapsis salientes
        listaAdyacencia.remove(id);
        boolean removeIf = todasSinapsis.removeIf(s -> s.getIdOrigen().equals(id));

        // Eliminar sinapsis entrantes desde otras neuronas
        String idFinal = id;
        for (List<Sinapsis> lista : listaAdyacencia.values()) {
            lista.removeIf(s -> s.getIdDestino().equals(idFinal));
        }
        todasSinapsis.removeIf(s -> s.getIdDestino().equals(idFinal));

        neuronas.remove(id);
        return true;
    }

    /**
     * Agrega una sinapsis (arista dirigida) entre dos neuronas.
     * Si las neuronas no existen, las crea automáticamente.
     *
     * @param sinapsis La sinapsis a agregar
     */
    public void agregarSinapsis(Sinapsis sinapsis) {
        String origen = sinapsis.getIdOrigen().trim();
        String destino = sinapsis.getIdDestino().trim();

        // Crear neuronas si no existen
        if (!neuronas.containsKey(origen)) agregarNeurona(new Neurona(origen, sinapsis.getK()));
        if (!neuronas.containsKey(destino)) agregarNeurona(new Neurona(destino));

        listaAdyacencia.get(origen).add(sinapsis);
        todasSinapsis.add(sinapsis);
    }

    /**
     * Verifica si existe una neurona con el ID dado.
     *
     * @param id ID a verificar
     * @return true si existe
     */
    public boolean existeNeurona(String id) {
        return neuronas.containsKey(id.trim());
    }

    /**
     * Obtiene una neurona por su ID.
     *
     * @param id ID de la neurona
     * @return La neurona, o null si no existe
     */
    public Neurona getNeurona(String id) {
        return neuronas.get(id.trim());
    }

    /**
     * Retorna la lista de sinapsis salientes de una neurona.
     *
     * @param idNeurona ID de la neurona origen
     * @return Lista de sinapsis (vacía si no existe o no tiene conexiones)
     */
    public List<Sinapsis> getSinapsisDesde(String idNeurona) {
        List<Sinapsis> lista = listaAdyacencia.get(idNeurona.trim());
        return lista != null ? lista : new ArrayList<>();
    }

    /**
     * Retorna todos los IDs de las neuronas del grafo.
     *
     * @return Colección de IDs de neuronas
     */
    public java.util.Collection<String> getIdsNeuronas() {
        return neuronas.keySet();
    }

    /**
     * Retorna todas las neuronas del grafo.
     *
     * @return Colección de neuronas
     */
    public java.util.Collection<Neurona> getNeuronas() {
        return neuronas.values();
    }

    /**
     * Retorna todas las sinapsis del grafo.
     *
     * @return Lista de todas las sinapsis
     */
    public List<Sinapsis> getTodasSinapsis() {
        return todasSinapsis;
    }

    /**
     * Aplica fatiga a todas las sinapsis del grafo multiplicando k por 1.2.
     * Esto simula el deterioro cognitivo por fatiga.
     */
    public void aplicarFatiga() {
        for (Sinapsis s : todasSinapsis) {
            s.aplicarFatiga();
        }
    }

    /**
     * Limpia el grafo completamente.
     */
    public void limpiar() {
        neuronas.clear();
        listaAdyacencia.clear();
        todasSinapsis.clear();
    }

    /**
     * Retorna el número de neuronas en el grafo.
     *
     * @return Cantidad de nodos
     */
    public int getNumNeuronas() { return neuronas.size(); }

    /**
     * Retorna el número de sinapsis en el grafo.
     *
     * @return Cantidad de aristas
     */
    public int getNumSinapsis() { return todasSinapsis.size(); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GrafoDirigido [").append(getNumNeuronas())
          .append(" neuronas, ").append(getNumSinapsis()).append(" sinapsis]\n");
        for (String id : neuronas.keySet()) {
            sb.append("  ").append(id).append(" -> ");
            for (Sinapsis s : getSinapsisDesde(id)) {
                sb.append(s.getIdDestino()).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
