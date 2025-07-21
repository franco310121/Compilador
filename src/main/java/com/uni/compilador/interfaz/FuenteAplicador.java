/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.uni.compilador.interfaz;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

/**
 *
 * @author Usuario
 */
public class FuenteAplicador {

    public static void aplicarFuente(Container contenedor, Font fuente) {
        for (Component comp : contenedor.getComponents()) {
            comp.setFont(fuente);
            if (comp instanceof Container) {
                aplicarFuente((Container) comp, fuente); // Recursividad para contenedores internos
            }
        }
    }
}
