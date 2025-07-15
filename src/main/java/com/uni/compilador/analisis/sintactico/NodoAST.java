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

    public String getValor() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getValor'");
    }

    public String getNombreReal() {
        // Si es hoja y no es "identificador", devuelve la etiqueta
        if (getHijos().isEmpty() && !getEtiqueta().equals("identificador")) {
            return getEtiqueta();
        }
        // Si tiene hijos, busca el primer hijo hoja que no sea "identificador"
        for (NodoAST hijo : getHijos()) {
            String nombre = hijo.getNombreReal();
            if (!nombre.equals("identificador")) {
                return nombre;
            }
        }
        // Si no encuentra nada mejor, devuelve la etiqueta
        return getEtiqueta();
    }
}
