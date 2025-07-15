package com.uni.compilador.analisis.backend;

import java.util.*;

public class TacGenerator {

    private static int tempCounter = 1;
    private static int labelCounter = 1;
    private static Deque<String> blockStack = new ArrayDeque<>();

    public static String generateTAC(String code) {
        // Reiniciar contadores para cada nueva generación
        tempCounter = 1;
        labelCounter = 1;
        blockStack.clear();

        StringBuilder output = new StringBuilder();
        String[] lines = code.split("\\n");
        boolean inFunction = false;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            // FUNCIONES
            if (line.startsWith("func")) {
                String[] parts = line.split("\\s+");
                String currentFunction = parts[1];
                output.append(currentFunction).append(":\n");
                inFunction = true;
                continue;
            }

            // BLOQUE PRINCIPAL
            if (line.startsWith("score")) {
                output.append("main:\n");
                continue;
            }

            // INICIO/FIN DE BLOQUE
            if (line.equals("start")) continue;

            if (line.equals("end")) {
                if (!blockStack.isEmpty()) {
                    String blk = blockStack.pop();
                    if (blk.startsWith("WHEN")) {
                        String lblEnd = blk.split(":")[1];
                        output.append(lblEnd).append(":\n");
                    } else if (blk.startsWith("LOOP")) {
                        String[] parts = blk.split(":");
                        String lblStart = parts[1];
                        String lblEnd = parts[2];
                        output.append("    goto ").append(lblStart).append("\n");
                        output.append(lblEnd).append(":\n");
                    }
                }
                if (inFunction) inFunction = false;
                continue;
            }

            // DECLARACIONES
            if (line.startsWith("note")) {
                output.append("    # ").append(line).append("\n");
                continue;
            }

            // ASIGNACIONES
            if (line.startsWith("tune")) {
                if (line.contains("call")) {
                    // Ejemplo: tune resultado = call promedio(10, 20)
                    String varName = line.split("\\s+")[1];
                    String funcCall = line.substring(line.indexOf("call") + 5).trim();

                    String funcName = funcCall.substring(0, funcCall.indexOf("(")).trim();
                    String paramsStr = funcCall.substring(funcCall.indexOf("(") + 1, funcCall.indexOf(")"));
                    String[] params = paramsStr.split(",");

                    for (String p : params) {
                        if (!p.trim().isEmpty())
                            output.append("    param ").append(p.trim()).append("\n");
                    }
                    String temp = newTemp();
                    output.append("    ").append(temp).append(" = call ").append(funcName)
                          .append(", ").append(params.length).append("\n");
                    output.append("    ").append(varName).append(" = ").append(temp).append("\n");

                } else if (line.contains("=")) {
                    String[] parts = line.replace("tune", "").split("=");
                    String left = parts[0].trim();
                    String right = parts[1].trim();

                    // ✅ NUEVO: soportar múltiples sumandos antes de la división
                    if (right.contains("/") && right.contains("+")) {
                        String[] divParts = right.split("/");
                        String sumExpr = divParts[0].replace("(", "").replace(")", "").trim();
                        String divisor = divParts[1].trim();

                        // Dividir la parte de sumas en términos separados
                        String[] addTerms = sumExpr.split("\\+");

                        // Generar sumas acumuladas
                        String lastTemp = addTerms[0].trim();
                        for (int i = 1; i < addTerms.length; i++) {
                            String next = addTerms[i].trim();
                            String tSum = newTemp();
                            output.append("    ").append(tSum).append(" = ")
                                  .append(lastTemp).append(" + ").append(next).append("\n");
                            lastTemp = tSum; // acumular
                        }

                        // Ahora dividir el resultado acumulado
                        String tDiv = newTemp();
                        output.append("    ").append(tDiv).append(" = ")
                              .append(lastTemp).append(" / ").append(divisor).append("\n");
                        output.append("    ").append(left).append(" = ").append(tDiv).append("\n");

                    } else if (right.contains("+")) {
                        // ✅ Sumas simples sin división (a + b + c)
                        String[] addTerms = right.replace("(", "").replace(")", "").trim().split("\\+");
                        String lastTemp = addTerms[0].trim();
                        for (int i = 1; i < addTerms.length; i++) {
                            String next = addTerms[i].trim();
                            String tSum = newTemp();
                            output.append("    ").append(tSum).append(" = ")
                                  .append(lastTemp).append(" + ").append(next).append("\n");
                            lastTemp = tSum;
                        }
                        output.append("    ").append(left).append(" = ").append(lastTemp).append("\n");

                    } else {
                        // ✅ Asignación directa
                        output.append("    ").append(left).append(" = ").append(right).append("\n");
                    }
                }
                continue;
            }

            // RETORNO
            if (line.startsWith("give")) {
                String returnVar = line.replace("give", "").trim();
                output.append("    return ").append(returnVar).append("\n");
                continue;
            }

            // IMPRESIÓN
            if (line.startsWith("play")) {
                String arg = line.substring(5).trim();
                output.append("    param ").append(arg).append("\n");
                output.append("    call print, 1\n");
                continue;
            }

            // CONDICIONAL WHEN
            if (line.startsWith("when")) {
                String cond = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim();
                String lblTrue = newLabel();
                String lblEnd = newLabel();

                String t = newTemp();
                output.append("    ").append(t).append(" = ").append(cond).append("\n");
                output.append("    if ").append(t).append(" goto ").append(lblTrue).append("\n");
                output.append("    goto ").append(lblEnd).append("\n");
                output.append(lblTrue).append(":\n");

                blockStack.push("WHEN:" + lblEnd);
                continue;
            }

            // BUCLE LOOP
            if (line.startsWith("loop")) {
                String cond = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim();
                String lblStart = newLabel();
                String lblEnd = newLabel();

                output.append(lblStart).append(":\n");
                String t = newTemp();
                output.append("    ").append(t).append(" = ").append(cond).append("\n");
                output.append("    if_false ").append(t).append(" goto ").append(lblEnd).append("\n");

                blockStack.push("LOOP:" + lblStart + ":" + lblEnd);
                continue;
            }
        }

        return output.toString();
    }

    private static String newTemp() {
        return "t" + (tempCounter++);
    }

    private static String newLabel() {
        return "L" + (labelCounter++);
    }
}
