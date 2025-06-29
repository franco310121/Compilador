package com.uni.compilador.analisis.sintactico;

import java.util.ArrayList;
import java.util.List;

public class NodoAST {
    private final String etiqueta;
    private final List<NodoAST> hijos;

    public NodoAST(String etiqueta, List<NodoAST> hijos) {
        this.etiqueta = etiqueta;
        this.hijos    = hijos;
    }

    /** Crea un nodo hoja. */
    public NodoAST(String etiqueta) {
        this.etiqueta = etiqueta;
        this.hijos    = new ArrayList<>();   // evita NullPointerException
    }

    public String getEtiqueta()      { return etiqueta; }
    public List<NodoAST> getHijos()  { return hijos;   }

    @Override
    public String toString() { return etiqueta; }
}
