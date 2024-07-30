# [sw](../src/main/scala/sw)

S-Type instruction is used to store a 32-bit value from a register to memory.

    [S-Type]
    +-------------------------------------------------------------------------------------------------+
    | 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00 |
    +---------------------+--------------+--------------+--------+--------------+---------------------+
    | imm_s(11:5)         | rs2          | rs1          | funct3 | imm_s(4:0)   | opcode              |
    +---------------------+--------------+--------------+--------+--------------+---------------------+


The immediate value is used to calculate the address of the memory location where the value is stored.
The immediate value is sign-extended to 32 bits.

```asm
sw rs2, offset(rs1)
```

Data comes from Rs2.
Memory address = `rs1` + sign-extended `imm_s`

## Test

> pcReg      : 0x00000004 \
> inst       : 0x00602823 \
> rs1Addr    :   0 \
> rs2Addr    :   6 \
> wbAddr     :  16 \
> rs1Data    : 0x00000000 \
> rs2Data    : 0x22222222 \
> wbData     : 0x22222222 \
> dmem.addr  :          16 \
> dmem.wEn   :  1 \
> dmem.wData : 0x22222222

`wbData` may take the value of last byte of hex file in the last clock.
It doesn't mean the core is working incorrectly.
But it is better to add `00`s at the end of hex file to avoid this situation.
