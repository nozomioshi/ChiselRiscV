# [lw](../src/main/scala/lw)

I-Type instruction use a 12-bit immediate value extended to 32-bit by sign extension.

    [I-Tpye]
    +-------------------------------------------------------------------------------------------------+
    | 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00 |
    +------------------------------------+--------------+--------+--------------+---------------------+
    | imm_i                              | rs1          | funct3 | rd           | opcode              |
    +------------------------------------+--------------+--------+--------------+---------------------+

Load data from memory to Rd.

```asm
lw rd, offset(rs1)
```

ALU(algorithmic logic unit) is used to calculate the memory address.
Base address is `rs1` and offset is sign extended `imm_i`.
