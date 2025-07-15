package com.uni.compilador.analisis.backend;

import java.util.*;

public class TacOptimizer {

    public static String optimize(String tacCode) {
        StringBuilder optimized = new StringBuilder();
        String[] lines = tacCode.split("\\n");

        // Para rastrear asignaciones triviales (propagación de copias y constantes)
        Map<String, String> copyPropagation = new HashMap<>();
        Map<String, Integer> constantPropagation = new HashMap<>();

        for (String line : lines) {
            String trimmed = line.trim();

            // Ignorar etiquetas y comentarios
            if (trimmed.isEmpty() || trimmed.endsWith(":") || trimmed.startsWith("#")) {
                optimized.append(line).append("\n");
                continue;
            }

            // Procesar asignaciones
            if (trimmed.contains("=")) {
                String[] parts = trimmed.split("=");
                String left = parts[0].trim();
                String right = parts[1].trim();

                // Eliminar paréntesis innecesarios
                right = right.replace("(", "").replace(")", "").trim();

                // Propagar constantes y copias previas
                right = propagateValues(right, copyPropagation, constantPropagation);

                // Simplificación algebraica
                right = simplifyExpression(right);

                // Intentar evaluar constante si es posible
                String evaluated = tryEvaluateConstant(right);
                if (evaluated != null) {
                    right = evaluated;
                }

                // Guardar propagación si es una asignación simple
                if (isSimpleValue(right)) {
                    copyPropagation.put(left, right);
                    if (isNumeric(right)) {
                        constantPropagation.put(left, Integer.parseInt(right));
                    } else {
                        constantPropagation.remove(left);
                    }
                } else {
                    copyPropagation.remove(left);
                    constantPropagation.remove(left);
                }

                optimized.append("    ").append(left).append(" = ").append(right).append("\n");
            } else {
                // Instrucciones sin asignación (param, call, return)
                optimized.append(line).append("\n");
            }
        }

        return optimized.toString();
    }

    private static String propagateValues(String expr, Map<String, String> copies, Map<String, Integer> consts) {
        String[] tokens = expr.split(" ");
        StringBuilder result = new StringBuilder();
        for (String token : tokens) {
            String t = token.trim();
            if (copies.containsKey(t)) {
                result.append(copies.get(t)).append(" ");
            } else {
                result.append(t).append(" ");
            }
        }
        return result.toString().trim();
    }

    private static boolean isSimpleValue(String expr) {
        return !expr.contains("+") && !expr.contains("*") && !expr.contains("/") && !expr.contains("call");
    }

    private static boolean isNumeric(String s) {
        try {
            Integer.parseInt(s.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String simplifyExpression(String expr) {
        expr = expr.trim();

        // x + 0 -> x, 0 + x -> x
        if (expr.matches(".+ \\+ 0")) return expr.replace(" + 0", "");
        if (expr.matches("0 \\+ .+")) return expr.replace("0 + ", "");

        // x - 0 -> x
        if (expr.matches(".+ - 0")) return expr.replace(" - 0", "");

        // x * 1 -> x, 1 * x -> x
        if (expr.matches(".+ \\* 1")) return expr.replace(" * 1", "");
        if (expr.matches("1 \\* .+")) return expr.replace("1 * ", "");

        // x / 1 -> x
        if (expr.matches(".+ / 1")) return expr.replace(" / 1", "");

        // x * 0 -> 0, 0 * x -> 0
        if (expr.matches(".+ \\* 0") || expr.matches("0 \\* .+")) return "0";

        // x / x -> 1 (si no es 0)
        String[] parts = expr.split(" ");
        if (parts.length == 3 && parts[1].equals("/") && parts[0].equals(parts[2]) && !parts[0].equals("0")) {
            return "1";
        }

        // x - x -> 0
        if (parts.length == 3 && parts[1].equals("-") && parts[0].equals(parts[2])) {
            return "0";
        }

        return expr;
    }

    private static String tryEvaluateConstant(String expr) {
        try {
            String[] parts = expr.split(" ");
            if (parts.length == 3) {
                if (isNumeric(parts[0]) && isNumeric(parts[2])) {
                    int a = Integer.parseInt(parts[0]);
                    int b = Integer.parseInt(parts[2]);
                    switch (parts[1]) {
                        case "+": return String.valueOf(a + b);
                        case "-": return String.valueOf(a - b);
                        case "*": return String.valueOf(a * b);
                        case "/": return b != 0 ? String.valueOf(a / b) : expr;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
