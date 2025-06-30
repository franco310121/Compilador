package com.uni.compilador.analisis.sintactico;

import com.uni.compilador.analisis.lexico.Token;
import java.util.*;

public class ParserAST {

    private final Map<String, Regla> reglas;
    private final List<Token> tokens;
    private int posicion = 0;

    private final List<ErrorSintactico> errores = new ArrayList<>();
    private ErrorSintactico mejorError = null;
    private int mejorPos = -1;

    public ParserAST(Map<String, Regla> reglas, List<Token> tokens) {
        this.reglas = reglas;
        this.tokens = tokens;
    }

    public NodoAST parse(String reglaInicial) {
        posicion = 0;
        errores.clear();
        mejorError = null;
        mejorPos = -1;
        NodoAST nodo = construirNodo(reglaInicial);
        if (mejorError != null) {
            errores.add(mejorError);
        }
        return nodo;
    }

    private NodoAST construirNodo(String nombreRegla) {
        if (!reglas.containsKey(nombreRegla)) return null;

        int posicionInicial = posicion;

        for (List<String> produccion : reglas.get(nombreRegla).getProducciones()) {
            int posTemp = posicion;
            List<NodoAST> hijos = new ArrayList<>();
            boolean exito = true;

            for (String simbolo : produccion) {
                if (esNoTerminal(simbolo)) {
                    NodoAST hijo = construirNodo(simbolo);
                    if (hijo != null) {
                        hijos.add(hijo);
                    } else {
                        registrarMejorError(posicion, simbolo);
                        exito = false;
                        break;
                    }
                } else {
                    if (posicion < tokens.size()) {
                        Token actual = tokens.get(posicion);
                        if (simbolo.equals(actual.getValor()) || simbolo.equals(actual.getTipo().name())) {
                            String etiquetaHoja = actual.getTipo().name().equals("CADENA")
                                    ? "\"" + actual.getValor() + "\""        // conserva comillas
                                    : actual.getValor();
                            hijos.add(new NodoAST(etiquetaHoja));
                            posicion++;
                        } else {
                            registrarMejorError(posicion, simbolo);
                            exito = false;
                            break;
                        }
                    } else {
                        registrarMejorError(posicion, simbolo);
                        exito = false;
                        break;
                    }
                }
            }

            if (exito) {
                return new NodoAST(nombreRegla, hijos);
            } else {
                posicion = posTemp;
            }
        }

        posicion = posicionInicial;
        return null;
    }

    private void registrarMejorError(int pos, String esperado) {
        if (pos >= mejorPos) {
            String encontrado = (pos < tokens.size()) ? tokens.get(pos).getValor() : "EOF";
            int linea = (pos < tokens.size()) ? tokens.get(pos).getLinea() : -1;
            int columna = (pos < tokens.size()) ? tokens.get(pos).getColumna() : -1;
            mejorError = new ErrorSintactico(pos, esperado, encontrado, linea, columna);
            mejorPos = pos;
        }
    }


    private boolean esNoTerminal(String simbolo) {
        return reglas.containsKey(simbolo);
    }

    public List<ErrorSintactico> getErrores() {
        return errores;
    }
}
