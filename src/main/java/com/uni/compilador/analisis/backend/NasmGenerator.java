package com.uni.compilador.analisis.backend;

import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

public class NasmGenerator {

    private static final String[] ARG = {"edi", "esi", "edx", "ecx", "r8d", "r9d"};

    public String generate(String tac) {

        List<String> mainParams = new ArrayList<>();
        boolean collect = false;

        /* ───── 1. Recolectar parámetros de main ( param …) ───── */
        for (String ln : tac.split("\\n")) {
            ln = ln.trim();
            if (ln.equals("main:")) { collect = true; continue; }
            if (collect) {
                if (ln.startsWith("param")) {
                    mainParams.add(ln.split(" ")[1].trim());
                } else if (!ln.isEmpty() && !ln.startsWith("#")) {
                    break;                     // fin de params
                }
            }
        }

        /* ───── 2. Generar ASM ───── */
        StringBuilder asm = new StringBuilder();

        asm.append("global  _start\n");
        asm.append("global  promedio\n\n");

        asm.append("section .bss\n");
        asm.append("resultado       resd    1\n");
        asm.append("buffer          resb    16\n\n");

        asm.append("section .text\n");

        /* función promedio */
        asm.append("promedio:\n");
        asm.append("    push    rbp\n");
        asm.append("    mov     rbp, rsp\n");
        asm.append("    mov     eax, edi\n");
        asm.append("    add     eax, esi\n");
        asm.append("    shr     eax, 1\n");
        asm.append("    pop     rbp\n");
        asm.append("    ret\n\n");

        /* _start dinámico */
        asm.append("_start:\n");
        for (int i = 0; i < mainParams.size() && i < ARG.length; i++) {
            String val = mainParams.get(i);
            asm.append("    mov     ").append(ARG[i]).append(", ").append(val).append("\n");
        }
        asm.append("    call    promedio\n");
        asm.append("    mov     [resultado], eax\n\n");

        asm.append("    mov     rsi, buffer\n");
        asm.append("    mov     rax, [resultado]\n");
        asm.append("    call    int_to_str\n\n");

        asm.append("    mov     rax, 1\n");
        asm.append("    mov     rdi, 1\n");
        asm.append("    mov     rdx, rbx\n");
        asm.append("    syscall\n\n");

        asm.append("    mov     eax, 60\n");
        asm.append("    xor     edi, edi\n");
        asm.append("    syscall\n\n");

        /* rutina int_to_str (idéntica a la pedida) */
        asm.append("int_to_str:\n");
        asm.append("    mov     rcx, 10\n");
        asm.append("    xor     rbx, rbx\n");
        asm.append("    mov     rdi, rsi\n");
        asm.append(".reverse:\n");
        asm.append("    xor     rdx, rdx\n");
        asm.append("    div     rcx\n");
        asm.append("    add     dl, '0'\n");
        asm.append("    dec     rsi\n");
        asm.append("    mov     [rsi], dl\n");
        asm.append("    inc     rbx\n");
        asm.append("    test    eax, eax\n");
        asm.append("    jnz     .reverse\n");
        asm.append("    ret\n");

        return asm.toString();
    }

    public void write(String s, String path) throws Exception {
        Files.writeString(Path.of(path), s);
    }
}
