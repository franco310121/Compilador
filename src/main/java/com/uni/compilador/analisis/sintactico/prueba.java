package com.uni.compilador.analisis.sintactico;

import com.uni.compilador.analisis.lexico.*;

import java.util.List;
import java.util.Map;

public class prueba {

    public static void main(String[] args) throws Exception {
        String codigo = """
    score 
    start
        when (edad)
            start
                play "hola"
            
    end
    """;

        AnalizadorLexico lexer = new AnalizadorLexico(codigo);
        lexer.analizar();
        List<Token> tokens = lexer.getTokens();

        Map<String, Regla> gramatica = LectorGramatica.cargarDesdeArchivo("src/main/java/com/uni/compilador/analisis/sintactico/gramatica.json");

        ParserAST parser = new ParserAST(gramatica, tokens);
        NodoAST arbol = parser.parse("programa");

        if (arbol != null) {
            System.out.println(ASTPrinter.imprimir(arbol));
        } else if (!parser.getErrores().isEmpty()) {
            System.out.println(parser.getErrores().get(0));
        }




    }
}
