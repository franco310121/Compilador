package com.uni.compilador.analisis.semantico;

import java.util.List;

public class FuncionDefinida {
    private final String nombre;
    private final String tipoRetorno;
    private final List<String> tiposParametros;

    public FuncionDefinida(String nombre, String tipoRetorno, List<String> tiposParametros) {
        this.nombre = nombre;
        this.tipoRetorno = tipoRetorno;
        this.tiposParametros = tiposParametros;
    }

    public String getNombre()             { return nombre; }
    public String getTipoRetorno()        { return tipoRetorno; }
    public List<String> getTiposParametros() { return tiposParametros; }
}

