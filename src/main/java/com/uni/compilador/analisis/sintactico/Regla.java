package com.uni.compilador.analisis.sintactico;

import java.util.List;

public class Regla {
    private String nombre;
    private List<List<String>> producciones;

    public Regla(String nombre, List<List<String>> producciones) {
        this.nombre = nombre;
        this.producciones = producciones;
    }

    public String getNombre() {
        return nombre;
    }

    public List<List<String>> getProducciones() {
        return producciones;
    }
}
