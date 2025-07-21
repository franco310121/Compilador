/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.uni.compilador.interfaz;
import java.awt.Font;
import java.io.InputStream;

public class FuenteUtil {
    public static Font cargarFuente(float tamaño) {
        try {
            InputStream is = FuenteUtil.class.getResourceAsStream("/fonts/JetBrainsMono-Regular.ttf");
            if (is == null) {
                throw new RuntimeException("No se encontró el archivo de fuente.");
            }
            Font fuente = Font.createFont(Font.TRUETYPE_FONT, is);
            return fuente.deriveFont(tamaño);
        } catch (Exception e) {
            e.printStackTrace();
            return new Font("SansSerif", Font.PLAIN, (int)tamaño);
        }
    }
}
