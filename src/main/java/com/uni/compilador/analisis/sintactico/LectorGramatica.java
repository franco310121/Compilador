package com.uni.compilador.analisis.sintactico;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LectorGramatica {

    public static Map<String, Regla> cargarDesdeArchivo(String rutaArchivo) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // Lee: Map<String, List<List<String>>> desde JSON
        Map<String, List<List<String>>> jsonMap = mapper.readValue(
                new File(rutaArchivo),
                new TypeReference<>() {}
        );

        Map<String, Regla> reglas = new HashMap<>();
        for (Map.Entry<String, List<List<String>>> entry : jsonMap.entrySet()) {
            reglas.put(entry.getKey(), new Regla(entry.getKey(), entry.getValue()));
        }

        return reglas;
    }
}