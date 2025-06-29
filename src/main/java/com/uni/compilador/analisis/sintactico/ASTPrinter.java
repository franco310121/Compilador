package com.uni.compilador.analisis.sintactico;

import java.util.List;

public class ASTPrinter {

    public static String imprimir(NodoAST nodo) {
        StringBuilder sb = new StringBuilder();
        imprimirRecursivo(nodo, "", true, sb);
        return sb.toString();
    }

    private static void imprimirRecursivo(NodoAST nodo, String prefijo, boolean esUltimo, StringBuilder sb) {
        sb.append(prefijo);
        sb.append(esUltimo ? "└── " : "├── ");

        if (nodo.getHijos().isEmpty()) {
            sb.append('"').append(nodo.getEtiqueta()).append('"').append("\n");
        } else {
            sb.append("[").append(nodo.getEtiqueta().toUpperCase()).append("]").append("\n");

            List<NodoAST> hijos = nodo.getHijos();
            for (int i = 0; i < hijos.size(); i++) {
                boolean ultimoHijo = (i == hijos.size() - 1);
                imprimirRecursivo(hijos.get(i), prefijo + (esUltimo ? "    " : "│   "), ultimoHijo, sb);
            }
        }
    }
}