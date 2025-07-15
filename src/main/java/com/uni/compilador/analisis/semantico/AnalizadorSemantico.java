package com.uni.compilador.analisis.semantico;

import com.uni.compilador.analisis.sintactico.NodoAST;
import java.util.*;

public class AnalizadorSemantico {

    private final List<EntradaTablaSimbolos> tabla = new ArrayList<>();
    private final Map<String, FuncionDefinida> funciones = new HashMap<>();
    private final List<String> errores = new ArrayList<>();
    private final Deque<String> dentroDeFuncion = new ArrayDeque<>();
    private int alcanceActual = 0;

    public void analizar(NodoAST raiz) {
        registrarFuncionesSimuladas();
        recorrer(raiz);
    }

    public List<EntradaTablaSimbolos> getTabla() {
        return tabla;
    }

    public List<String> getErrores() {
        return errores;
    }

    private void recorrer(NodoAST nodo) {
        if (nodo == null) {
            return;
        }

        switch (nodo.getEtiqueta()) {
            case "declaracion" ->
                manejarDeclaracion(nodo);
            case "asignacion" ->
                manejarAsignacion(nodo);
            case "condicional", "bucle" ->
                manejarControl(nodo);
            case "llamadaFuncion" ->
                manejarLlamadaFuncion(nodo);
            case "retorno" ->
                manejarRetorno(nodo);
            case "impresion" ->
                manejarImpresion(nodo);
            case "lectura" ->
                manejarLectura(nodo);
            case "funcion" ->
                manejarDefinicionFuncion(nodo);
            case "bloque", "bloquePrincipal" -> {
                alcanceActual++;
                nodo.getHijos().forEach(this::recorrer);
                alcanceActual--;
            }
            default ->
                nodo.getHijos().forEach(this::recorrer);
        }
    }

    private void manejarDefinicionFuncion(NodoAST nodo) {
        String nombre = nodo.getHijos().get(1).getEtiqueta();
        NodoAST parametrosNodo = nodo.getHijos().get(3);
        NodoAST tipoNodo = nodo.getHijos().get(5);
        String tipoRetorno = "unknown";

        if (tipoNodo.getEtiqueta().equals("flechaTipo") && tipoNodo.getHijos().size() == 2) {
            NodoAST tipoReal = tipoNodo.getHijos().get(1);
            tipoRetorno = tipoReal.getHijos().isEmpty() ? tipoReal.getEtiqueta() : tipoReal.getHijos().get(0).getEtiqueta();
        }

        List<String> tiposParametros = extraerTiposParametros(parametrosNodo);

        if (funciones.containsKey(nombre)) {
            errores.add("Función '" + nombre + "' ya está definida.");
            return;
        }

        funciones.put(nombre, new FuncionDefinida(nombre, tipoRetorno, tiposParametros));

        // Registrar variables parámetros en la tabla
        dentroDeFuncion.push(nombre);
        alcanceActual++;
        registrarParametros(parametrosNodo);
        recorrer(nodo.getHijos().get(6));
        alcanceActual--;
        dentroDeFuncion.pop();
    }

    private List<String> extraerTiposParametros(NodoAST nodo) {
        List<String> tipos = new ArrayList<>();
        if (nodo == null) {
            return tipos;
        }

        if (nodo.getEtiqueta().equals("parametro")) {
            tipos.add(nodo.getHijos().get(1).getHijos().get(0).getEtiqueta());
        } else {
            for (NodoAST hijo : nodo.getHijos()) {
                tipos.addAll(extraerTiposParametros(hijo));
            }
        }
        return tipos;
    }

    private void registrarParametros(NodoAST nodo) {
        if (nodo == null) {
            return;
        }

        if (nodo.getEtiqueta().equals(",")) {
            nodo.getHijos().forEach(this::registrarParametros);
            return;
        }

        if (nodo.getEtiqueta().equals("parametro")) {
            String nombre = nodo.getHijos().get(0).getEtiqueta();
            String tipo = nodo.getHijos().get(1).getHijos().get(0).getEtiqueta();
            tabla.add(new EntradaTablaSimbolos(nombre, tipo, "parametro", alcanceActual, 0, null));
            return;
        }

        nodo.getHijos().forEach(this::registrarParametros);
    }

    private void manejarDeclaracion(NodoAST nodo) {
        String tipo = nodo.getHijos().get(1).getHijos().get(0).getEtiqueta();
        String nombre = nodo.getHijos().get(2).getEtiqueta();
        if (yaExisteEnAmbito(nombre, alcanceActual)) {
            errores.add("Variable '" + nombre + "' ya declarada en este ámbito.");
        }
        tabla.add(new EntradaTablaSimbolos(nombre, tipo, "variable", alcanceActual, 0, null));
    }

    private void manejarAsignacion(NodoAST nodo) {
        String nombre = nodo.getHijos().get(1).getEtiqueta();
        NodoAST expresionNodo = nodo.getHijos().get(3);

        EntradaTablaSimbolos variable = buscarVariable(nombre);
        if (variable == null) {
            errores.add("Variable '" + nombre + "' no ha sido declarada.");
            return;
        }

        String tipoValor = inferirTipo(expresionNodo);
        if (!esTipoCompatible(variable.getTipo(), tipoValor)) {
            errores.add(String.format("Tipo incompatible: se esperaba '%s' pero se encontró '%s' en asignación a '%s'.",
                    variable.getTipo(), tipoValor, nombre));
            return;
        }

        String valor = obtenerValorLiteral(expresionNodo);
        if (valor != null) {
            tabla.remove(variable);
            tabla.add(new EntradaTablaSimbolos(
                    variable.getNombre(), variable.getTipo(), variable.getCategoria(),
                    variable.getAlcance(), variable.getLinea(), valor));
        }
    }

    private void manejarControl(NodoAST nodo) {
        if (nodo.getHijos().size() < 3) {
            return;
        }
        NodoAST condicion = nodo.getHijos().get(2);
        String tipo = inferirTipo(condicion);
        if (!tipo.equals("logic")) {
            errores.add(String.format("Condición inválida en '%s': se esperaba 'logic' pero se encontró '%s'.",
                    nodo.getEtiqueta(), tipo));
        }
        nodo.getHijos().forEach(this::recorrer);
    }

    private void manejarLlamadaFuncion(NodoAST nodo) {
        if (nodo.getHijos().size() < 4) {
            return;
        }
        String nombreFunc = nodo.getHijos().get(1).getEtiqueta();
        NodoAST listaArgs = nodo.getHijos().get(3);

        FuncionDefinida fn = funciones.get(nombreFunc);
        if (fn == null) {
            errores.add("Función '" + nombreFunc + "' no está definida.");
            return;
        }

        List<String> reales = obtenerTiposArgumentos(listaArgs);
        List<String> esper = fn.getTiposParametros();

        // AQUÍ SE VERIFICA LA CANTIDAD
        if (reales.size() != esper.size()) {
            errores.add("Cantidad incorrecta de argumentos en '" + nombreFunc + "'. Se esperaban "
                    + esper.size() + " pero se recibieron " + reales.size() + ".");
            return;
        }

        // Verificar tipos uno a uno
        for (int i = 0; i < reales.size(); i++) {
            if (!esTipoCompatible(esper.get(i), reales.get(i))) {
                errores.add(String.format("Tipo de argumento %d inválido en '%s': se esperaba '%s', se encontró '%s'.",
                        i + 1, nombreFunc, esper.get(i), reales.get(i)));
            }
        }
    }

    private List<String> obtenerTiposArgumentos(NodoAST nodo) {
        List<String> tipos = new ArrayList<>();
        if (nodo == null) {
            return tipos;
        }

        // Recorremos TODOS los hijos y recogemos solo los que son expresiones primarias o literales
        Queue<NodoAST> cola = new LinkedList<>();
        cola.add(nodo);

        while (!cola.isEmpty()) {
            NodoAST actual = cola.poll();

            // Un argumento válido: literal, variable, llamada a función
            if (esNodoArgumento(actual)) {
                tipos.add(inferirTipo(actual));
            }

            // Seguir recorriendo
            for (NodoAST hijo : actual.getHijos()) {
                cola.add(hijo);
            }
        }
        return tipos;
    }

    private boolean esNodoArgumento(NodoAST nodo) {
        if (nodo == null) {
            return false;
        }
        String et = nodo.getEtiqueta();

        // Literal o variable
        if (nodo.getHijos().isEmpty()) {
            return et.matches("-?\\d+(\\.\\d+)?") || et.matches("\".*\"") || et.equals("yes") || et.equals("no") || buscarVariable(et) != null;
        }

        // Expresión primaria directa
        if (et.equals("expPrimaria") || et.equals("expresionSimple") || et.equals("expresion")) {
            // Si solo tiene un hijo, es un argumento directo
            if (nodo.getHijos().size() == 1 && nodo.getHijos().get(0).getHijos().isEmpty()) {
                return true;
            }
        }

        // Llamada a función como argumento
        if (et.equals("llamadaFuncion")) {
            return true;
        }

        return false;
    }

    private void manejarImpresion(NodoAST nodo) {
        NodoAST expresion = nodo.getHijos().get(1);
        String tipo = inferirTipo(expresion);
        if (!tipo.equals("text")) {
            errores.add("La instrucción 'play' solo puede imprimir valores de tipo 'text', se encontró '" + tipo + "'.");
        }
    }

    private void manejarLectura(NodoAST nodo) {
        String nombre = nodo.getHijos().get(1).getEtiqueta();
        EntradaTablaSimbolos variable = buscarVariable(nombre);
        if (variable == null) {
            errores.add("Variable '" + nombre + "' no ha sido declarada para lectura.");
        }
    }

    private void manejarRetorno(NodoAST nodo) {
        if (dentroDeFuncion.isEmpty()) {
            errores.add("Sentencia 'give' fuera de una función.");
            return;
        }

        String funcion = dentroDeFuncion.peek();
        String esperado = funciones.get(funcion).getTipoRetorno();
        String real = inferirTipo(nodo.getHijos().get(1));

        if (!esTipoCompatible(esperado, real)) {
            errores.add("Tipo de retorno incompatible en '" + funcion
                    + "': se esperaba '" + esperado + "', se encontró '" + real + "'.");
        }
    }

    private String inferirTipo(NodoAST n) {
        if (n == null) {
            return "unknown";
        }

        if (n.getEtiqueta().equals("expPrimaria") && n.getHijos().size() == 3
                && (n.getHijos().get(0).getEtiqueta().equals("(") || n.getHijos().get(0).getEtiqueta().equals("token_abrir"))
                && (n.getHijos().get(2).getEtiqueta().equals(")") || n.getHijos().get(2).getEtiqueta().equals("token_cerrar"))) {
            return inferirTipo(n.getHijos().get(1));
        }

        if (n.getEtiqueta().equals("expPrimaria") && n.getHijos().size() == 1) {
            return inferirTipo(n.getHijos().get(0));
        }

        if (n.getHijos().isEmpty()) {
            String val = n.getEtiqueta();
            if (val.matches("-?\\d+(\\.\\d+)?")) {
                return "number";
            }
            if (val.matches("\".*\"")) {
                return "text";
            }
            if (val.equals("yes") || val.equals("no")) {
                return "logic";
            }
            if (val.startsWith("[") && val.endsWith("]")) {
                return "list";
            }
            EntradaTablaSimbolos v = buscarVariable(val);
            if (v != null) {
                return v.getTipo();
            }
            return "unknown";
        }

        if (n.getEtiqueta().equals("llamadaFuncion")) {
            String nombreFunc = n.getHijos().get(1).getEtiqueta();
            FuncionDefinida f = funciones.get(nombreFunc);
            if (f != null) {
                return f.getTipoRetorno();
            }
            return "unknown";
        }

        if (n.getEtiqueta().equals("expresion") || n.getEtiqueta().equals("expresionSimple")) {
            if (n.getHijos().size() == 1) {
                return inferirTipo(n.getHijos().get(0));
            }
            String tipoIzq = inferirTipo(n.getHijos().get(0));
            NodoAST opNodo = n.getHijos().get(1);
            String op = opNodo.getHijos().isEmpty() ? opNodo.getEtiqueta() : opNodo.getHijos().get(0).getEtiqueta();
            String tipoDer = inferirTipo(n.getHijos().get(2));
            if (esOperadorLogico(op)) {
                return "logic";
            }
            if (esOperadorAritmetico(op) && tipoIzq.equals("number") && tipoDer.equals("number")) {
                return "number";
            }
            return "unknown";
        }

        if (n.getHijos().size() == 1) {
            return inferirTipo(n.getHijos().get(0));
        }

        return "unknown";
    }

    private boolean esOperadorAritmetico(String op) {
        return "+-*/".contains(op);
    }

    private boolean esOperadorLogico(String op) {
        return op.equals("and") || op.equals("or");
    }

    private boolean esTipoCompatible(String esperado, String real) {
        return esperado.equals(real);
    }

    private EntradaTablaSimbolos buscarVariable(String nombre) {
        for (int i = alcanceActual; i >= 0; i--) {
            for (EntradaTablaSimbolos e : tabla) {
                if (e.getNombre().equals(nombre) && e.getAlcance() == i) {
                    return e;
                }
            }
        }
        return null;
    }

    private boolean yaExisteEnAmbito(String nombre, int amb) {
        return tabla.stream().anyMatch(e -> e.getNombre().equals(nombre) && e.getAlcance() == amb);
    }

    private String obtenerValorLiteral(NodoAST n) {
        if (n == null) {
            return null;
        }
        if (n.getHijos().isEmpty()) {
            return n.getEtiqueta();
        }
        if (n.getHijos().size() == 1) {
            return obtenerValorLiteral(n.getHijos().get(0));
        }
        return null;
    }

    private void registrarFuncionesSimuladas() {
        funciones.put("imprimir", new FuncionDefinida("imprimir", "void", List.of("text")));
        funciones.put("procesar", new FuncionDefinida("procesar", "number", List.of("number", "text")));
    }
}
