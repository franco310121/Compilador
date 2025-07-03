package com.uni.compilador;

import javax.swing.*;
import com.uni.compilador.interfaz.Compilador;

public class VentanaPrincipal extends JFrame{
    
    public VentanaPrincipal() {
        Compilador panelForm = new Compilador();
        setContentPane(panelForm.getContentPane());
        setTitle("Compilador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1310, 900);
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
    } 
}
