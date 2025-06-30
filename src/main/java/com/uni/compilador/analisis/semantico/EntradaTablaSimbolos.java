package com.uni.compilador.analisis.semantico;

public class EntradaTablaSimbolos {
    private final String nombre;
    private final String tipo;
    private final String categoria;
    private final int alcance;
    private final int linea;
    private final String valor;

    public EntradaTablaSimbolos(String nombre, String tipo, String categoria, int alcance, int linea, String valor) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.categoria = categoria;
        this.alcance = alcance;
        this.linea = linea;
        this.valor = valor;
    }

    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public String getCategoria() { return categoria; }
    public int getAlcance() { return alcance; }
    public int getLinea() { return linea; }
    public String getValor() { return valor; }

    @Override
    public String toString() {
        return String.format("%-12s %-8s %-10s %-7d %-6d %s",
                nombre, tipo, categoria, alcance, linea, valor != null ? valor : "-");
    }
}