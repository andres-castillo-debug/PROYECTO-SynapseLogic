package sinapsis.algoritmos;

import sinapsis.estructura.GrafoDirigido;
import sinapsis.estructura.HashTableNeurotransmisores;
import sinapsis.modelo.Neurotransmisor;
import sinapsis.modelo.Sinapsis;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilidades para carga y guardado de archivos CSV.
 * Maneja el archivo de sinapsis (grafo) y el diccionario de neurotransmisores.
 *
 * @author SynapseLogic Team
 * @version 1.0
 */
public class CargadorCSV {

    /**
     * Resultado de la carga de un archivo.
     */
    public static class ResultadoCarga {
        /** true si la carga fue exitosa */
        public boolean exitoso;
        /** Mensaje descriptivo del resultado */
        public String mensaje;
        /** Número de registros cargados */
        public int registrosCargados;
        /** Lista de errores encontrados (líneas con formato incorrecto) */
        public List<String> errores;

        public ResultadoCarga() {
            errores = new ArrayList<>();
            registrosCargados = 0;
        }
    }

    /**
     * Carga el grafo de la red sináptica desde un archivo CSV.
     * Formato: origen,destino,distancia,ID_Neurotransmisor,coheficiente_eficiencia_sináptica
     *
     * @param ruta  Ruta al archivo CSV
     * @param grafo El grafo donde se cargarán los datos (se limpia antes)
     * @return ResultadoCarga con el resultado de la operación
     */
    public static ResultadoCarga cargarGrafo(String ruta, GrafoDirigido grafo) {
        ResultadoCarga resultado = new ResultadoCarga();
        grafo.limpiar();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(ruta), StandardCharsets.UTF_8))) {

            String linea;
            int numLinea = 0;
            boolean primeraLinea = true;

            while ((linea = br.readLine()) != null) {
                numLinea++;
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                // Saltar encabezado
                if (primeraLinea) {
                    primeraLinea = false;
                    if (linea.toLowerCase().startsWith("origen") ||
                        linea.toLowerCase().startsWith("id_neurona")) {
                        continue;
                    }
                }

                String[] partes = linea.split(",");
                if (partes.length < 5) {
                    resultado.errores.add("Línea " + numLinea + ": formato inválido -> " + linea);
                    continue;
                }

                try {
                    String origen = partes[0].trim();
                    String destino = partes[1].trim();
                    double distancia = Double.parseDouble(partes[2].trim());
                    String idNT = partes[3].trim();
                    double k = Double.parseDouble(partes[4].trim());

                    Sinapsis s = new Sinapsis(origen, destino, distancia, idNT, k);
                    grafo.agregarSinapsis(s);
                    resultado.registrosCargados++;
                } catch (NumberFormatException e) {
                    resultado.errores.add("Línea " + numLinea + ": valores numéricos inválidos -> " + linea);
                }
            }

            resultado.exitoso = true;
            resultado.mensaje = "Carga exitosa. " + resultado.registrosCargados +
                                " sinapsis cargadas, " + grafo.getNumNeuronas() + " neuronas.";
            if (!resultado.errores.isEmpty()) {
                resultado.mensaje += " (" + resultado.errores.size() + " líneas con errores ignoradas)";
            }

        } catch (FileNotFoundException e) {
            resultado.exitoso = false;
            resultado.mensaje = "Archivo no encontrado: " + ruta;
        } catch (IOException e) {
            resultado.exitoso = false;
            resultado.mensaje = "Error de lectura: " + e.getMessage();
        }

        return resultado;
    }

    /**
     * Carga el diccionario de neurotransmisores desde un archivo CSV.
     * Formato: id,nombre,efecto,velocidad,descripcion
     *
     * @param ruta Ruta al archivo CSV
     * @param ht   La Hash Table donde se cargarán los datos
     * @return ResultadoCarga con el resultado de la operación
     */
    public static ResultadoCarga cargarNeurotransmisores(String ruta,
                                                          HashTableNeurotransmisores ht) {
        ResultadoCarga resultado = new ResultadoCarga();
        ht.limpiar();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(ruta), StandardCharsets.UTF_8))) {

            String linea;
            int numLinea = 0;

            while ((linea = br.readLine()) != null) {
                numLinea++;
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                // Saltar encabezado
                if (numLinea == 1 && linea.toLowerCase().startsWith("id")) continue;

                // Manejo de comillas en la descripción (campo con coma interna)
                String[] partes = parsearCSVConComillas(linea);

                if (partes.length < 5) {
                    resultado.errores.add("Línea " + numLinea + ": formato inválido -> " + linea);
                    continue;
                }

                try {
                    String id = partes[0].trim();
                    String nombre = partes[1].trim();
                    String efecto = partes[2].trim();
                    double velocidad = Double.parseDouble(partes[3].trim());
                    String descripcion = partes[4].trim().replace("\"", "");

                    Neurotransmisor nt = new Neurotransmisor(id, nombre, efecto, velocidad, descripcion);
                    ht.insertar(nt);
                    resultado.registrosCargados++;
                } catch (NumberFormatException e) {
                    resultado.errores.add("Línea " + numLinea + ": velocidad inválida -> " + linea);
                }
            }

            resultado.exitoso = true;
            resultado.mensaje = "Diccionario cargado: " + resultado.registrosCargados + " neurotransmisores.";

        } catch (FileNotFoundException e) {
            resultado.exitoso = false;
            resultado.mensaje = "Archivo no encontrado: " + ruta;
        } catch (IOException e) {
            resultado.exitoso = false;
            resultado.mensaje = "Error de lectura: " + e.getMessage();
        }

        return resultado;
    }

    /**
     * Guarda el grafo actual en un archivo CSV.
     *
     * @param ruta  Ruta donde se guardará el archivo
     * @param grafo El grafo a guardar
     * @return ResultadoCarga con el resultado de la operación
     */
    public static ResultadoCarga guardarGrafo(String ruta, GrafoDirigido grafo) {
        ResultadoCarga resultado = new ResultadoCarga();

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(ruta), StandardCharsets.UTF_8))) {

            pw.println("origen,destino,distancia,ID_Neurotransmisor,coheficiente_eficiencia_sináptica");

            for (Sinapsis s : grafo.getTodasSinapsis()) {
                pw.printf("%s,%s,%.4f,%s,%.6f%n",
                    s.getIdOrigen(), s.getIdDestino(),
                    s.getDistancia(), s.getIdNeurotransmisor(), s.getK());
                resultado.registrosCargados++;
            }

            resultado.exitoso = true;
            resultado.mensaje = "Archivo guardado: " + resultado.registrosCargados + " sinapsis.";

        } catch (IOException e) {
            resultado.exitoso = false;
            resultado.mensaje = "Error al guardar: " + e.getMessage();
        }

        return resultado;
    }

    /**
     * Parsea una línea CSV respetando campos entre comillas.
     *
     * @param linea Línea CSV a parsear
     * @return Array de campos
     */
    private static String[] parsearCSVConComillas(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean dentroComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '"') {
                dentroComillas = !dentroComillas;
            } else if (c == ',' && !dentroComillas) {
                campos.add(actual.toString());
                actual = new StringBuilder();
            } else {
                actual.append(c);
            }
        }
        campos.add(actual.toString());
        return campos.toArray(new String[0]);
    }
}
