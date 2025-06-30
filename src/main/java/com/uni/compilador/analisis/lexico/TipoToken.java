package com.uni.compilador.analisis.lexico;

public enum TipoToken {
    IDENTIFICADOR,
    NUMERO,
    PALABRA_CLAVE,

    // Operadores diferenciados
    OPERADOR_ARITMETICO,
    OPERADOR_COMPARACION,
    OPERADOR_ASIGNACION,
    OPERADOR_LOGICO,

    SIMBOLO,
    CADENA,
    FLECHA,
    FIN_ARCHIVO,
    DESCONOCIDO
}