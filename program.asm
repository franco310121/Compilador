global  _start
global  promedio

section .bss
resultado       resd    1
buffer          resb    16

section .text
promedio:
    push    rbp
    mov     rbp, rsp
    mov     eax, edi
    add     eax, esi
    shr     eax, 1
    pop     rbp
    ret

_start:
    mov     edi, 20
    mov     esi, 20
    call    promedio
    mov     [resultado], eax

    mov     rsi, buffer
    mov     rax, [resultado]
    call    int_to_str

    mov     rax, 1
    mov     rdi, 1
    mov     rdx, rbx
    syscall

    mov     eax, 60
    xor     edi, edi
    syscall

int_to_str:
    mov     rcx, 10
    xor     rbx, rbx
    mov     rdi, rsi
.reverse:
    xor     rdx, rdx
    div     rcx
    add     dl, '0'
    dec     rsi
    mov     [rsi], dl
    inc     rbx
    test    eax, eax
    jnz     .reverse
    ret
