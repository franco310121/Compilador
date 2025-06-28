package com.uni.compilador.analisis.lexico;

public class ErrorLexico {
    private final String mensaje;
    private final int linea;
    private final int columna;

    public ErrorLexico(String mensaje, int linea, int columna) {
        this.mensaje = mensaje;
        this.linea = linea;
        this.columna = columna;
    }

    public String getMensaje() {
        return mensaje;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    @Override
    public String toString() {
        return "Error léxico: " + mensaje + " en línea " + linea + ", columna " + columna;
    }
}