# [vse](../src/main/scala/vse)

I-Type Instructions.

```asm
vse8.v vs3, (rs1)
vse16.v vs3, (rs1)
vse32.v vs3, (rs1)
vse64.v vs3, (rs1)
```

    [I-Tpye]
    +------------------------------------------------------------------------------------------------------+
    | 31 30 29  28   27 26  25  24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00 |
    +-----------------------------------------+--------------+--------+--------------+---------------------+
    | imm_i                                   | rs1          | funct3 | rd           | opcode              |
    +-----------------------------------------+--------------+--------+--------------+---------------------+

    [VSE]
    +------------------------------------------------------------------------------------------------------+
    | 31 30 29  28   27 26  25  24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00 |
    +---------+-----+-----+----+--------------+--------------+--------+--------------+---------------------+
    | nf      | mew | mop | vm | sumop        | rs1          | width  | vs3          | 0100111             |
    +---------+-----+-----+----+--------------+--------------+--------+--------------+---------------------+
    