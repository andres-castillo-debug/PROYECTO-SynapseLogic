package sinapsis.estructura;

import sinapsis.modelo.Neurotransmisor;

/**
 * Implementación manual de una Tabla Hash para el diccionario de
 * neurotransmisores. Utiliza encadenamiento (chaining) para resolver
 * colisiones. Las operaciones de inserción y búsqueda son O(1) en promedio.
 *
 * <p>No utiliza ninguna librería de estructuras de datos de Java.
 * Implementación propia desde cero.</p>
 *
 * @author SynapseLogic Team
 * @version 1.0
 */
public class HashTableNeurotransmisores {

    /** Tamaño inicial de la tabla hash */
    private static final int CAPACIDAD = 64;

    /** Arreglo de listas enlazadas (buckets) */
    private NodoHash[] tabla;

    /** Número de elementos almacenados */
    private int tamaño;

    /**
     * Nodo interno de la lista enlazada para encadenamiento.
     */
    private static class NodoHash {
        String clave;
        Neurotransmisor valor;
        NodoHash siguiente;

        NodoHash(String clave, Neurotransmisor valor) {
            this.clave = clave;
            this.valor = valor;
            this.siguiente = null;
        }
    }

    /**
     * Constructor: inicializa la tabla con capacidad fija.
     */
    public HashTableNeurotransmisores() {
        tabla = new NodoHash[CAPACIDAD];
        tamaño = 0;
    }

    /**
     * Función de dispersión (hash) basada en suma ponderada de caracteres.
     * Garantiza distribución uniforme para las claves de neurotransmisores.
     *
     * @param clave La clave string (ID del neurotransmisor)
     * @return Índice en el arreglo [0, CAPACIDAD)
     */
    private int funcionHash(String clave) {
        int hash = 0;
        int primo = 31;
        for (int i = 0; i < clave.length(); i++) {
            hash = (hash * primo + clave.charAt(i)) % CAPACIDAD;
        }
        return Math.abs(hash);
    }

    /**
     * Inserta un neurotransmisor en la tabla hash. Complejidad O(1) amortizado.
     * Si ya existe una clave igual, sobreescribe el valor.
     *
     * @param nt El neurotransmisor a insertar
     */
    public void insertar(Neurotransmisor nt) {
        String clave = nt.getId().trim().toUpperCase();
        int indice = funcionHash(clave);

        NodoHash actual = tabla[indice];
        while (actual != null) {
            if (actual.clave.equals(clave)) {
                actual.valor = nt; // actualizar si ya existe
                return;
            }
            actual = actual.siguiente;
        }

        // Insertar al inicio del bucket (O(1))
        NodoHash nuevo = new NodoHash(clave, nt);
        nuevo.siguiente = tabla[indice];
        tabla[indice] = nuevo;
        tamaño++;
    }

    /**
     * Busca un neurotransmisor por su ID. Complejidad O(1) amortizado.
     *
     * @param id Identificador del neurotransmisor (ej: "GLU")
     * @return El objeto Neurotransmisor, o null si no existe
     */
    public Neurotransmisor buscar(String id) {
        if (id == null) return null;
        String clave = id.trim().toUpperCase();
        int indice = funcionHash(clave);

        NodoHash actual = tabla[indice];
        while (actual != null) {
            if (actual.clave.equals(clave)) return actual.valor;
            actual = actual.siguiente;
        }
        return null;
    }

    /**
     * Elimina un neurotransmisor de la tabla.
     *
     * @param id ID del neurotransmisor a eliminar
     * @return true si fue eliminado, false si no existía
     */
    public boolean eliminar(String id) {
        String clave = id.trim().toUpperCase();
        int indice = funcionHash(clave);

        NodoHash actual = tabla[indice];
        NodoHash anterior = null;

        while (actual != null) {
            if (actual.clave.equals(clave)) {
                if (anterior == null) {
                    tabla[indice] = actual.siguiente;
                } else {
                    anterior.siguiente = actual.siguiente;
                }
                tamaño--;
                return true;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return false;
    }

    /**
     * Retorna todos los neurotransmisores almacenados en la tabla.
     *
     * @return Arreglo con todos los neurotransmisores
     */
    public Neurotransmisor[] obtenerTodos() {
        Neurotransmisor[] resultado = new Neurotransmisor[tamaño];
        int idx = 0;
        for (int i = 0; i < CAPACIDAD; i++) {
            NodoHash actual = tabla[i];
            while (actual != null) {
                resultado[idx++] = actual.valor;
                actual = actual.siguiente;
            }
        }
        return resultado;
    }

    /**
     * Limpia todos los elementos de la tabla.
     */
    public void limpiar() {
        tabla = new NodoHash[CAPACIDAD];
        tamaño = 0;
    }

    /**
     * @return Número de elementos almacenados
     */
    public int getTamaño() { return tamaño; }
}
