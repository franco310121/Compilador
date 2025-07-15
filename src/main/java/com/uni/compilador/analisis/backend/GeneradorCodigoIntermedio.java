package com.uni.compilador.analisis.backend;

import com.uni.compilador.analisis.sintactico.NodoAST;
import java.util.*;

public class GeneradorCodigoIntermedio {

    private final List<String> instrucciones = new ArrayList<>();
    private int contadorTemporales = 0;

    public List<String> generar(NodoAST arbol) {
        instrucciones.clear();
        generarDesdeNodo(arbol);

        System.out.println("[INTERMEDIO - Código en 3 direcciones]");
        instrucciones.forEach(System.out::println);
        return instrucciones;
    }

    private void generarDesdeNodo(NodoAST nodo) {
        if (nodo == null) return;
        String etiqueta = nodo.getEtiqueta();

        switch (etiqueta) {
            case "programa", "listafunciones" -> nodo.getHijos().forEach(this::generarDesdeNodo);
            case "funcion" -> generarFuncion(nodo);
            case "score" -> generarMain(nodo);
            case "bloque", "bloquePrincipal", "listainstrucciones" ->
                    nodo.getHijos().forEach(this::generarDesdeNodo);
            case "instruccion" -> generarInstruccion(nodo);
            default -> nodo.getHijos().forEach(this::generarDesdeNodo);
        }
    }

    private void generarFuncion(NodoAST nodo) {
        String nombreFuncion = nodo.getHijos().get(1).getEtiqueta();
        instrucciones.add(nombreFuncion + ":");

        NodoAST parametrosNodo = nodo.getHijos().get(3);
        generarParametros(parametrosNodo);

        NodoAST bloque = nodo.getHijos().get(6);
        if (bloque != null) bloque.getHijos().forEach(this::generarDesdeNodo);
    }

    private void generarParametros(NodoAST nodo) {
        if (nodo == null) return;
        if (nodo.getEtiqueta().equals(",")) {
            nodo.getHijos().forEach(this::generarParametros);
            return;
        }
        if (nodo.getEtiqueta().equals("parametro")) {
            String nombre = nodo.getHijos().get(0).getEtiqueta();
            instrucciones.add("param " + nombre);
            return;
        }
        nodo.getHijos().forEach(this::generarParametros);
    }

    private void generarMain(NodoAST nodo) {
        instrucciones.add("main:");
        if (nodo.getHijos().isEmpty()) return;

        NodoAST bloque = (nodo.getHijos().size() > 1)
                ? nodo.getHijos().get(1)
                : nodo.getHijos().get(0);

        if (bloque != null) bloque.getHijos().forEach(this::generarDesdeNodo);
    }

    private void generarInstruccion(NodoAST nodo) {
        if (nodo.getHijos().isEmpty()) return;
        String tipoInstr = nodo.getHijos().get(0).getEtiqueta();

        switch (tipoInstr) {
            case "declaracion" -> generarDeclaracion(nodo.getHijos().get(0));
            case "asignacion" -> generarAsignacion(nodo.getHijos().get(0));
            case "retorno" -> generarRetorno(nodo.getHijos().get(0));
        }
    }

    private void generarDeclaracion(NodoAST nodo) {
        String nombre = nodo.getHijos().get(2).getEtiqueta();
        instrucciones.add(nombre + " = 0");
    }

    private void generarAsignacion(NodoAST nodo) {
        String variable = nodo.getHijos().get(1).getEtiqueta();
        NodoAST expr = nodo.getHijos().get(3);

        String valor = generarExpresion(expr);
        instrucciones.add(variable + " = " + valor);
    }

    private void generarRetorno(NodoAST nodo) {
        String expr = generarExpresion(nodo.getHijos().get(1));
        instrucciones.add("return " + expr);
    }

    private String generarLlamadaFuncion(NodoAST nodo) {
        String nombreFunc = "";
        NodoAST argsNodo = null;

        // RADICAL: Busca el primer hijo hoja del nodo "identificador" y usa su etiqueta
        for (NodoAST hijo : nodo.getHijos()) {
            if (hijo.getEtiqueta().equals("identificador")) {
                NodoAST actual = hijo;
                while (!actual.getHijos().isEmpty()) {
                    actual = actual.getHijos().get(0);
                }
                nombreFunc = actual.getEtiqueta();
            } else if (hijo.getEtiqueta().equals("listaArgumentos")) {
                argsNodo = hijo;
            }
        }

        // Generar código para cada argumento si existen
        List<String> args = obtenerArgumentos(argsNodo);
        for (String arg : args) {
            instrucciones.add("param " + arg);
        }

        // Temporal para almacenar el resultado del call
        String temp = nuevoTemporal();
        instrucciones.add(temp + " = call " + nombreFunc);
        return temp;
    }

    private List<String> obtenerArgumentos(NodoAST nodo) {
        List<String> args = new ArrayList<>();
        recolectarArgs(nodo, args);
        return args;
    }

    private void recolectarArgs(NodoAST nodo, List<String> args) {
        if (nodo == null) return;
        if (nodo.getEtiqueta().equals(",")) {
            nodo.getHijos().forEach(child -> recolectarArgs(child, args));
            return;
        }
        if (nodo.getEtiqueta().startsWith("exp")) {
            args.add(generarExpresion(nodo));
            return;
        }
        nodo.getHijos().forEach(child -> recolectarArgs(child, args));
    }

    private String generarExpresion(NodoAST nodo) {
        if (nodo == null) return "";

        String et = nodo.getEtiqueta();

        //  es una llamada a función dentro de una expresión
        if (et.equals("llamadaFuncion")) {
            return generarLlamadaFuncion(nodo);
        }

        // expresiones binarias
        if (et.equals("expresion") || et.equals("expresionSimple")) {
            if (nodo.getHijos().size() == 1) {
                return generarExpresion(nodo.getHijos().get(0));
            } else if (nodo.getHijos().size() == 3) {
                String izq = generarExpresion(nodo.getHijos().get(0));

                NodoAST nodoOperador = nodo.getHijos().get(1);
                String op = nodoOperador.getHijos().isEmpty()
                        ? nodoOperador.getEtiqueta()
                        : nodoOperador.getHijos().get(0).getEtiqueta();
                String operador = mapearOperador(op);

                String der = generarExpresion(nodo.getHijos().get(2));

                String t = nuevoTemporal();
                instrucciones.add(t + " = " + izq + " " + operador + " " + der);
                return t;
            }
        }

        // expresión primaria
        if (et.equals("expPrimaria")) {
            if (nodo.getHijos().size() == 3 && nodo.getHijos().get(0).getEtiqueta().equals("(")) {
                return generarExpresion(nodo.getHijos().get(1));
            }
            // Si el hijo es una llamada a función, procesar correctamente
            NodoAST hijo0 = nodo.getHijos().get(0);
            if (hijo0.getEtiqueta().equals("llamadaFuncion")) {
                return generarExpresion(hijo0);
            }
            return hijo0.getEtiqueta();
        }

        return et;
    }

    private String mapearOperador(String op) {
        return switch (op) {
            case "+", "mas" -> "+";
            case "-", "menos" -> "-";
            case "*", "por" -> "*";
            case "/", "div" -> "/";
            default -> op;
        };
    }

    private String nuevoTemporal() {
        contadorTemporales++;
        return "t" + contadorTemporales;
    }

    private String obtenerNombreFuncion(NodoAST identificador) {
        return identificador.getNombreReal();
    }
}
