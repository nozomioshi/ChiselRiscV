# [riscvtests](../src/main/scala/riscvtests)

## Arithmetic

### AddSub

    [R-Type]
    +-------------------------------------------------------------------------------------------------+
    | 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00 |
    +---------------------+--------------+--------------+--------+--------------+---------------------+
    | funct7              | rs2          | rs1          | funct3 | rd           | opcode              |
    +---------------------+--------------+--------------+--------+--------------+---------------------+

```asm
add rd, rs1, rs2
sub rd, rs1, rs2
```

### Addi

I-Type Instruction.

```asm
addi rd, rs1, imm_i
```

## Logical

R-Type Instructions.

```asm
and rd, rs1, rs2
or  rd, rs1, rs2
xor rd, rs1, rs2
```

I-Type Instructions.

```asm
andi rd, rs1, imm_i
ori  rd, rs1, imm_i
xori rd, rs1, imm_i
```

## Shift

All the shift instructions use the lower 5 bits of shift amount.

R-Type Instructions.

```asm
sll rd, rs1, rs2
srl rd, rs1, rs2
sra rd, rs1, rs2
```

I-Type Instructions.

```asm
slli rd, rs1, shamt
srli rd, rs1, shamt
srai rd, rs1, shamt
```
