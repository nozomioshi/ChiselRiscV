# [vle](../src/main/scala/vle)

- unit-stride: Contiguous memory access from the base address
- strided: Access the address by incrementing the base address at a certain interval
- indexed: Specify the address offset directly

In this implementation, only the simplest `unit-stride` is supported.
Meanwhile, suppose the Effective Element Width(EEW) = Selected Element Width(SEW).

I-Type Instructions.

```asm
vle8 .v vd, (rs1)
vle16.v vd, (rs1)
vle32.v vd, (rs1)
vle64.v vd, (rs1)
```

`rs1` stores the base address of the memory, and the result is writed back to vector register `vd`.

The instructions are dependent on SEW because there may be some instruction using different SEW.

    [I-Tpye]
    +------------------------------------------------------------------------------------------------------+
    | 31 30 29  28   27 26  25  24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00 |
    +-----------------------------------------+--------------+--------+--------------+---------------------+
    | imm_i                                   | rs1          | funct3 | rd           | opcode              |
    +-----------------------------------------+--------------+--------+--------------+---------------------+

    [VLE]
    +------------------------------------------------------------------------------------------------------+
    | 31 30 29  28   27 26  25  24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00 |
    +---------+-----+-----+----+--------------+--------------+--------+--------------+---------------------+
    | nf      | mew | mop | vm | lumop        | rs1          | width  | vd           | 0000111             |
    +---------+-----+-----+----+--------------+--------------+--------+--------------+---------------------+

In the implementation, the `vm` is always set to 1 which means unmasked.
