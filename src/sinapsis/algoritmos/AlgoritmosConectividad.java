package sinapsis.algoritmos;

import sinapsis.estructura.GrafoDirigido;
import sinapsis.estructura.HashTableNeurotransmisores;
import sinapsis.modelo.Sinapsis;

import java.util.*;

/**
 * Algoritmos de búsqueda BFS y DFS para la detección de zonas aisladas
 * en la red sináptica neuronal.
 *
 * <p>Dado un estímulo en una neurona fuente, BFS y DFS determinan qué
 * neuronas son alcanzables y cuáles son zonas aisladas (inalcanzables).</p>
 *
 * @author SynapseLogic Team
 * @version 1.0
 */
public class AlgoritmosConectividad {

    /**
     * Resultado de un análisis de conectividad.
     */
    public static class ResultadoConectividad {
        /** Neuronas alcanzables desde la fuente */
        public Set<String> alcanzables;
        /** Neuronas NO alcanzables (zonas aisladas) */
        public Set<String> aisladas;
        /** Algoritmo utilizado */
        public String algoritmo;
        /** Neurona fuente del análisis */
        public String fuente;
        /** Orden de visita de las neuronas */
        public List<String> ordenVisita;

        public ResultadoConectividad(String algoritmo, String fuente) {
            this.alcanzables = new LinkedHashSet<>();
            this.aisladas = new LinkedHashSet<>();
            this.ordenVisita = new ArrayList<>();
            this.algoritmo = algoritmo;
            this.fuente = fuente;
        }

        /**
         * @return true si la red es fuertemente conexa (no hay zonas aisladas)
         */
        public boolean esFuertementeConexo() {
            return aisladas.isEmpty();
        }
    }

    /**
     * Ejecuta BFS (Búsqueda por Amplitud) desde una neurona fuente.
     * Determina qué neuronas son alcanzables.
     *
     * @param grafo  El grafo de la red sináptica
     * @param ht     Hash Table de neurotransmisores (para calcular pesos si aplica)
     * @param fuente ID de la neurona fuente (donde se origina el estímulo)
     * @return ResultadoConectividad con alcanzables y aisladas
     */
    public static ResultadoConectividad bfs(GrafoDirigido grafo,
                                            HashTableNeurotransmisores ht,
                                            String fuente) {
        ResultadoConectividad resultado = new ResultadoConectividad("BFS", fuente);

        if (!grafo.existeNeurona(fuente)) {
            resultado.aisladas.addAll(grafo.getIdsNeuronas());
            return resultado;
        }

        Set<String> visitados = new LinkedHashSet<>();
        Queue<String> cola = new LinkedList<>();

        cola.offer(fuente);
        visitados.add(fuente);

        while (!cola.isEmpty()) {
            String actual = cola.poll();
            resultado.alcanzables.add(actual);
            resultado.ordenVisita.add(actual);

            for (Sinapsis s : grafo.getSinapsisDesde(actual)) {
                String vecino = s.getIdDestino();
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    cola.offer(vecino);
                }
            }
        }

        // Las neuronas no visitadas son zonas aisladas
        for (String id : grafo.getIdsNeuronas()) {
            if (!resultado.alcanzables.contains(id)) {
                resultado.aisladas.add(id);
            }
        }

        return resultado;
    }

    /**
     * Ejecuta DFS (Búsqueda por Profundidad) desde una neurona fuente.
     * Determina qué neuronas son alcanzables.
     *
     * @param grafo  El grafo de la red sináptica
     * @param ht     Hash Table de neurotransmisores
     * @param fuente ID de la neurona fuente
     * @return ResultadoConectividad con alcanzables y aisladas
     */
    public static ResultadoConectividad dfs(GrafoDirigido grafo,
                                            HashTableNeurotransmisores ht,
                                            String fuente) {
        ResultadoConectividad resultado = new ResultadoConectividad("DFS", fuente);

        if (!grafo.existeNeurona(fuente)) {
            resultado.aisladas.addAll(grafo.getIdsNeuronas());
            return resultado;
        }

        Set<String> visitados = new LinkedHashSet<>();
        dfsRecursivo(grafo, fuente, visitados, resultado.ordenVisita);

        resultado.alcanzables.addAll(visitados);

        for (String id : grafo.getIdsNeuronas()) {
            if (!visitados.contains(id)) {
                resultado.aisladas.add(id);
            }
        }

        return resultado;
    }

    /**
     * Función auxiliar recursiva del DFS.
     *
     * @param grafo     El grafo
     * @param actual    Nodo actual
     * @param visitados Conjunto de nodos ya visitados
     * @param orden     Lista con el orden de visita
     */
    private static void dfsRecursivo(GrafoDirigido grafo, String actual,
                                     Set<String> visitados, List<String> orden) {
        visitados.add(actual);
        orden.add(actual);

        for (Sinapsis s : grafo.getSinapsisDesde(actual)) {
            String vecino = s.getIdDestino();
            if (!visitados.contains(vecino)) {
                dfsRecursivo(grafo, vecino, visitados, orden);
            }
        }
    }

    /**
     * Verifica si la red es fuertemente conexa realizando BFS desde
     * cada nodo y verificando que todos sean alcanzables.
     *
     * @param grafo El grafo a analizar
     * @param ht    Hash Table de neurotransmisores
     * @return true si es fuertemente conexo
     */
    public static boolean esFuertementeConexo(GrafoDirigido grafo,
                                               HashTableNeurotransmisores ht) {
        if (grafo.getNumNeuronas() == 0) return true;

        for (String id : grafo.getIdsNeuronas()) {
            ResultadoConectividad r = bfs(grafo, ht, id);
            if (!r.esFuertementeConexo()) return false;
        }
        return true;
    }
}
