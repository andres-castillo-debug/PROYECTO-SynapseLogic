package sinapsis.algoritmos;

import sinapsis.estructura.GrafoDirigido;
import sinapsis.estructura.HashTableNeurotransmisores;
import sinapsis.modelo.Neurotransmisor;
import sinapsis.modelo.Sinapsis;

import java.util.*;

/**
 * Implementación del algoritmo de Dijkstra para calcular la ruta de
 * mayor activación (señal más rápida) entre dos neuronas.
 *
 El peso de cada arista se calcula como W = d / (v * k), donde:
 * <ul>
 *   <li>d = distancia sináptica</li>
 *   <li>v = velocidad del neurotransmisor (obtenida de la Hash Table)</li>
 *   <li>k = factor de eficiencia sináptica de la sinapsis</li>
 * </ul>
 * 
 *
 * @author SynapseLogic Team
 * @version 1.0
 */
public class Dijkstra {

    /**
     * Resultado del algoritmo de Dijkstra.
     */
    public static class ResultadoDijkstra {
        /** Ruta más corta de origen a destino (lista de IDs de neuronas) */
        public List<String> ruta;
        /** Costo total (tiempo de transmisión) */
        public double costoTotal;
        /** true si existe una ruta */
        public boolean existeRuta;
        /** Neurona de origen */
        public String origen;
        /** Neurona de destino */
        public String destino;
        /** Detalle de cada salto de la ruta */
        public List<String> detalleSaltos;

        public ResultadoDijkstra(String origen, String destino) {
            this.origen = origen;
            this.destino = destino;
            this.ruta = new ArrayList<>();
            this.detalleSaltos = new ArrayList<>();
            this.costoTotal = Double.MAX_VALUE;
            this.existeRuta = false;
        }
    }

    /**
     * Nodo auxiliar para la cola de prioridad de Dijkstra.
     */
    private static class NodoDijkstra implements Comparable<NodoDijkstra> {
        String id;
        double distancia;

        NodoDijkstra(String id, double distancia) {
            this.id = id;
            this.distancia = distancia;
        }

        @Override
        public int compareTo(NodoDijkstra otro) {
            return Double.compare(this.distancia, otro.distancia);
        }
    }

    /**
     * Ejecuta el algoritmo de Dijkstra para hallar la ruta más rápida
     * (menor tiempo de transmisión) entre dos neuronas.
     *
     * @param grafo   El grafo de la red sináptica
     * @param ht      Hash Table con neurotransmisores y sus velocidades
     * @param origen  ID de la neurona de origen
     * @param destino ID de la neurona de destino
     * @return ResultadoDijkstra con la ruta y el costo
     */
    public static ResultadoDijkstra calcularRuta(GrafoDirigido grafo,
                                                  HashTableNeurotransmisores ht,
                                                  String origen,
                                                  String destino) {
        ResultadoDijkstra resultado = new ResultadoDijkstra(origen, destino);

        if (!grafo.existeNeurona(origen) || !grafo.existeNeurona(destino)) {
            return resultado;
        }

        // Mapa de distancias mínimas
        Map<String, Double> dist = new HashMap<>();
        // Mapa de predecesores para reconstruir la ruta
        Map<String, String> predecesor = new HashMap<>();
        // Mapa de sinapsis usada para llegar a cada nodo (para el detalle)
        Map<String, Sinapsis> sinapsisUsada = new HashMap<>();

        // Inicializar distancias a infinito
        for (String id : grafo.getIdsNeuronas()) {
            dist.put(id, Double.MAX_VALUE);
        }
        dist.put(origen, 0.0);

        // Cola de prioridad (min-heap)
        PriorityQueue<NodoDijkstra> pq = new PriorityQueue<>();
        pq.offer(new NodoDijkstra(origen, 0.0));

        Set<String> procesados = new HashSet<>();

        while (!pq.isEmpty()) {
            NodoDijkstra actual = pq.poll();
            String idActual = actual.id;

            if (procesados.contains(idActual)) continue;
            procesados.add(idActual);

            // Si llegamos al destino, terminamos
            if (idActual.equals(destino)) break;

            // Explorar vecinos
            for (Sinapsis s : grafo.getSinapsisDesde(idActual)) {
                String vecino = s.getIdDestino();
                if (procesados.contains(vecino)) continue;

                // Calcular peso W = d / (v * k)
                double peso = calcularPeso(s, ht);
                double nuevaDist = dist.get(idActual) + peso;

                if (nuevaDist < dist.getOrDefault(vecino, Double.MAX_VALUE)) {
                    dist.put(vecino, nuevaDist);
                    predecesor.put(vecino, idActual);
                    sinapsisUsada.put(vecino, s);
                    pq.offer(new NodoDijkstra(vecino, nuevaDist));
                }
            }
        }

        // Reconstruir ruta si existe
        if (dist.get(destino) < Double.MAX_VALUE) {
            resultado.existeRuta = true;
            resultado.costoTotal = dist.get(destino);

            // Reconstruir path de atrás hacia adelante
            LinkedList<String> ruta = new LinkedList<>();
            String actual = destino;
            while (actual != null) {
                ruta.addFirst(actual);
                actual = predecesor.get(actual);
            }
            resultado.ruta = new ArrayList<>(ruta);

            // Generar detalle de saltos
            for (int i = 1; i < resultado.ruta.size(); i++) {
                String nodo = resultado.ruta.get(i);
                Sinapsis s = sinapsisUsada.get(nodo);
                if (s != null) {
                    Neurotransmisor nt = ht.buscar(s.getIdNeurotransmisor());
                    double v = (nt != null) ? nt.getVelocidad() : 1.0;
                    double w = calcularPeso(s, ht);
                    String detalle = String.format(
                        "%s → %s [d=%.4f, NT=%s (v=%.1f), k=%.4f, W=%.4f]",
                        s.getIdOrigen(), s.getIdDestino(),
                        s.getDistancia(), s.getIdNeurotransmisor(),
                        v, s.getK(), w
                    );
                    resultado.detalleSaltos.add(detalle);
                }
            }
        }

        return resultado;
    }

    /**
     * Calcula el peso de una sinapsis usando W = d / (v * k).
     *
     * @param s  La sinapsis
     * @param ht Hash Table de neurotransmisores
     * @return Peso calculado
     */
    private static double calcularPeso(Sinapsis s, HashTableNeurotransmisores ht) {
        Neurotransmisor nt = ht.buscar(s.getIdNeurotransmisor());
        double v = (nt != null) ? nt.getVelocidad() : 1.0;
        double k = s.getK();
        if (v <= 0 || k <= 0) return Double.MAX_VALUE / 2;
        return s.getDistancia() / (v * k);
    }
}
