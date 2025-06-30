package com.uni.compilador.analisis.lexico;

import java.util.*;

public class AnalizadorLexico {

    private final String fuente;
    private final List<Token> tokens = new ArrayList<>();
    private final List<ErrorLexico> errores = new ArrayList<>();

    private int posicion = 0;
    private int linea = 1;
    private int columna = 1;

    private static final Set<String> PALABRAS_CLAVE = Set.of(
            // Declaración de variables y tipos
            "note", "tune", "number", "text", "logic", "list",

            // Entrada / salida
            "play", "listen",

            // Control de flujo
            "when", "otherwise", "loop", "break", "continue",

            // Funciones
            "func", "theme", "give", "call",

            // Estructura de bloques
            "start", "end",

            // Booleanos y lógica
            "yes", "no", "and", "or", "not",

            // Operaciones con listas
            "length", "add", "remove",

            // Programa principal y metainformación
            "score", "track", "export"
    );


    private static final Map<String, TipoToken> OPERADORES = Map.ofEntries(
            // Operadores aritméticos
            Map.entry("+", TipoToken.OPERADOR_ARITMETICO),
            Map.entry("-", TipoToken.OPERADOR_ARITMETICO),
            Map.entry("*", TipoToken.OPERADOR_ARITMETICO),
            Map.entry("/", TipoToken.OPERADOR_ARITMETICO),
            Map.entry("%", TipoToken.OPERADOR_ARITMETICO),

            // Operadores de comparación
            Map.entry("==", TipoToken.OPERADOR_COMPARACION),
            Map.entry("!=", TipoToken.OPERADOR_COMPARACION),
            Map.entry(">", TipoToken.OPERADOR_COMPARACION),
            Map.entry("<", TipoToken.OPERADOR_COMPARACION),
            Map.entry(">=", TipoToken.OPERADOR_COMPARACION),
            Map.entry("<=", TipoToken.OPERADOR_COMPARACION),

            // Operadores lógicos
            Map.entry("and", TipoToken.OPERADOR_LOGICO),
            Map.entry("or", TipoToken.OPERADOR_LOGICO),
            Map.entry("not", TipoToken.OPERADOR_LOGICO),

            // Operadores de asignación
            Map.entry("=", TipoToken.OPERADOR_ASIGNACION),
            Map.entry("+=", TipoToken.OPERADOR_ASIGNACION),
            Map.entry("-=", TipoToken.OPERADOR_ASIGNACION),
            Map.entry("*=", TipoToken.OPERADOR_ASIGNACION),
            Map.entry("/=", TipoToken.OPERADOR_ASIGNACION),

            // Otros operadores posibles
            Map.entry("->", TipoToken.FLECHA),
            Map.entry(".", TipoToken.SIMBOLO)
    );

    private static final Map<Character, TipoToken> SIMBOLOS = Map.ofEntries(
            // Símbolos básicos de estructura
            Map.entry(';', TipoToken.SIMBOLO),
            Map.entry('(', TipoToken.SIMBOLO),
            Map.entry(')', TipoToken.SIMBOLO),
            Map.entry('{', TipoToken.SIMBOLO),
            Map.entry('}', TipoToken.SIMBOLO),
            Map.entry(',', TipoToken.SIMBOLO)
    );


    public AnalizadorLexico(String fuente) {
        this.fuente = fuente;
    }

    public void analizar() {
        while (!estaAlFinal()) {
            char actual = siguiente();

            if (Character.isWhitespace(actual)) {
                if (actual == '\n') {
                    linea++;
                    columna = 1;
                } else {
                    columna++;
                }
                continue;
            }

            if (Character.isLetter(actual)) {
                escanearIdentificador(actual);
            } else if (Character.isDigit(actual)) {
                escanearNumero(actual);
            } else if (actual == '"') {
                escanearCadena();
            } else {
                // Manejo de comentarios de una línea
                if (actual == '/' && !estaAlFinal() && verSiguiente() == '/') {
                    siguiente(); // consumir el segundo '/'
                    columna += 2;
                    // Avanzar hasta el final de la línea o del archivo
                    while (!estaAlFinal() && siguiente() != '\n') {
                        columna++;
                    }
                    linea++;
                    columna = 1;
                    continue;
                }

                // Intentar extraer operadores de uno o dos caracteres
                String operador = String.valueOf(actual);
                if (!estaAlFinal()) {
                    char siguienteChar = verSiguiente();
                    String posibleDoble = operador + siguienteChar;
                    if (OPERADORES.containsKey(posibleDoble)) {
                        tokens.add(new Token(OPERADORES.get(posibleDoble), posibleDoble, linea, columna));
                        siguiente(); // consumir el segundo carácter
                        columna += 2;
                        continue;
                    }
                }

                if (OPERADORES.containsKey(operador)) {
                    tokens.add(new Token(OPERADORES.get(operador), operador, linea, columna));
                    columna++;
                } else if (SIMBOLOS.containsKey(actual)) {
                    tokens.add(new Token(SIMBOLOS.get(actual), operador, linea, columna));
                    columna++;
                } else {
                    errores.add(new ErrorLexico("Carácter no válido: '" + actual + "'", linea, columna));
                    columna++;
                }
            }
        }

        tokens.add(new Token(TipoToken.FIN_ARCHIVO, "", linea, columna));
    }


    private void escanearIdentificador(char inicial) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(inicial);
        int startCol = columna;
        columna++;

        while (!estaAlFinal() && Character.isLetterOrDigit(verSiguiente())) {
            buffer.append(siguiente());
            columna++;
        }

        String valor = buffer.toString();
        TipoToken tipo = PALABRAS_CLAVE.contains(valor) ? TipoToken.PALABRA_CLAVE : TipoToken.IDENTIFICADOR;
        tokens.add(new Token(tipo, valor, linea, startCol));
    }

    private void escanearNumero(char inicial) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(inicial);
        int startCol = columna;
        columna++;

        while (!estaAlFinal() && Character.isDigit(verSiguiente())) {
            buffer.append(siguiente());
            columna++;
        }

        tokens.add(new Token(TipoToken.NUMERO, buffer.toString(), linea, startCol));
    }

    private void escanearCadena() {
        int startCol = columna;
        StringBuilder buffer = new StringBuilder();
        columna++; // avanzar desde la comilla inicial

        while (!estaAlFinal()) {
            char actual = siguiente();
            if (actual == '"') {
                columna++; // por la comilla de cierre
                tokens.add(new Token(TipoToken.CADENA, buffer.toString(), linea, startCol));
                return;
            } else if (actual == '\n') {
                errores.add(new ErrorLexico("Cadena sin cerrar antes de salto de línea", linea, columna));
                linea++;
                columna = 1;
                return;
            } else {
                buffer.append(actual);
                columna++;
            }
        }

        // Si llega aquí es porque terminó el archivo sin cerrar la cadena
        errores.add(new ErrorLexico("Cadena sin cerrar al final del archivo", linea, startCol));
    }


    private char siguiente() {
        return fuente.charAt(posicion++);
    }

    private char verSiguiente() {
        return fuente.charAt(posicion);
    }

    private boolean estaAlFinal() {
        return posicion >= fuente.length();
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public List<ErrorLexico> getErrores() {
        return errores;
    }
}