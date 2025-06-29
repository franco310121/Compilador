package com.uni.compilador.analisis.sintactico;

public class ErrorSintactico {
    private final int posicion;
    private final String esperado;
    private final String encontrado;
    private final int linea;
    private final int columna;

    public ErrorSintactico(int posicion, String esperado, String encontrado, int linea, int columna) {
        this.posicion = posicion;
        this.esperado = esperado;
        this.encontrado = encontrado;
        this.linea = linea;
        this.columna = columna;
    }

    @Override
    public String toString() {
        return String.format(
                "[Error de sintaxis] Línea %d, Columna %d: se esperaba \"%s\", pero se encontró \"%s\"",
                linea, columna, esperado, encontrado
        );
    }
}
