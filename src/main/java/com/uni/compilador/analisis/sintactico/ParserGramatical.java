package com.uni.compilador.analisis.sintactico;

import com.uni.compilador.analisis.lexico.Token;
import com.uni.compilador.analisis.lexico.TipoToken;


import java.util.List;
import java.util.Map;

public class ParserGramatical {

    private final Map<String, Regla> reglas;
    private final List<Token> tokens;
    private int posicion = 0;

    public ParserGramatical(Map<String, Regla> reglas, List<Token> tokens) {
        this.reglas = reglas;
        this.tokens = tokens;
    }

    public boolean parse(String nombreRegla) {
        return matchRegla(nombreRegla);
    }

    private boolean matchRegla(String nombre) {
        if (!reglas.containsKey(nombre)) return false;

        int inicio = posicion;
        for (List<String> produccion : reglas.get(nombre).getProducciones()) {
            posicion = inicio; // reinicia por intento de alternativa
            if (matchProduccion(produccion)) {
                return true;
            }
        }

        posicion = inicio;
        return false;
    }

    private boolean matchProduccion(List<String> produccion) {
        for (String simbolo : produccion) {
            if (esNoTerminal(simbolo)) {
                if (!matchRegla(simbolo)) return false;
            } else {
                if (!matchTerminal(simbolo)) return false;
            }
        }
        return true;
    }

    private boolean matchTerminal(String simboloEsperado) {
        if (posicion >= tokens.size()) return false;

        Token token = tokens.get(posicion);

        // Comparación contra tipo o valor
        boolean coincide = simboloEsperado.equals(token.getValor()) ||
                simboloEsperado.equals(token.getTipo().name());

        if (coincide) {
            posicion++;
            return true;
        }

        return false;
    }

    private boolean esNoTerminal(String simbolo) {
        return reglas.containsKey(simbolo);
    }
}