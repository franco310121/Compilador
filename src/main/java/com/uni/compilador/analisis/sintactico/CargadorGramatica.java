package com.uni.compilador.analisis.sintactico;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

public class CargadorGramatica {

    public static Map<String, Regla> cargarDesdeJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // Parseamos el JSON a un Map raw
        Map<String, List<List<String>>> datos = mapper.readValue(json, Map.class);

        Map<String, Regla> mapaReglas = new HashMap<>();

        for (Map.Entry<String, List<List<String>>> entrada : datos.entrySet()) {
            String nombreRegla = entrada.getKey();
            List<List<String>> producciones = new ArrayList<>();

            for (List<String> produccion : entrada.getValue()) {
                producciones.add(new ArrayList<>(produccion));
            }

            mapaReglas.put(nombreRegla, new Regla(nombreRegla, producciones));
        }

        return mapaReglas;
    }
}
