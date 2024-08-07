# [riscvtests](../src/main/scala/riscvtests)

## Instructions

### Arithmetic

#### AddSub

R-Type Instructions.

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

#### Addi

I-Type Instruction.

```asm
addi rd, rs1, imm_i
```

### Logical

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

### Shift

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

### Compare

R-Type Instructions.

```asm
slt  rd, rs1, rs2
sltu rd, rs1, rs2
```

I-Type Instructions.

```asm
slti  rd, rs1, imm_i
sltiu rd, rs1, imm_i
```

`u` for unsigned.

### Branch

B-Type Instructions.

    [B-Type]
    +-------------------------------------------------------------------------------------------------+
    | 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00 |
    +---------------------+--------------+--------------+--------+--------------+---------------------+
    | imm_b(12 + 10:5)    | rs2          | rs1          | funct3 | imm_b(4:1+11)| opcode              |
    +---------------------+--------------+--------------+--------+--------------+---------------------+

This kind of instructions is very similar to the S-Type instructions.

```asm
beq  rs1, rs2, offset
bne  rs1, rs2, offset
blt  rs1, rs2, offset
bge  rs1, rs2, offset
bltu rs1, rs2, offset
bgeu rs1, rs2, offset
```

The offset is 12 bits.
The instruction is 32 bits, while it's 16 bits in compressed instructions.
Pointer counter must be the multiple of 16(2 bytes).
Hence, the offset is multiplied by 2.
At the same time, it can represent larger range.

### Jump

All jump instructions writes `PC+4` to Rd.

J-Type Instruction.

    [J-Type]
    +-------------------------------------------------------------------------------------------------+
    | 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00 |
    +------------------------------------------------------------+--------------+---------------------+
    | imm_j(20 + 10:1 + 11 + 19:12)                              | rd           | opcode              |
    +------------------------------------------------------------+--------------+---------------------+

```asm
jal  rd, offset
```

Like branch instructions, the offset is multiplied by 2.

I-Type Instruction.

```asm
jalr rd, rs1, offset
```

The LSB is always set to 0 by `&`.

### Load immediate

U-Type Instructions.

    [U-Type]
    +-------------------------------------------------------------------------------------------------+
    | 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00 |
    +------------------------------------------------------------+--------------+---------------------+
    | imm_u(11:5)                                                | rd           | opcode              |
    +------------------------------------------------------------+--------------+---------------------+

```asm
lui rd, imm_u
auipc rd, imm_u
```

Both the instructions are right-shifted by 12 bits.

AUIPC combined with JAL can jump any relative address within the 32-bit range of the PC.

AUIPC combined with LW/SW can access any relative memory address within the 32-bit range of the PC.

### CSR

I-Type Instructions.

```asm
csrrw  rd, csr, rs1
csrrwi rd, csr, imm_z
csrrs  rd, csr, rs1
csrrsi rd, csr, imm_z
csrrc  rd, csr, rs1
csrrci rd, csr, imm_z
```

All the CSR instructions are first read the CSR value out, and then operate the CSR value with the source register.

`s` for set, the operated value is ORed with the CSR value.

`c` for clear, the operated value is inverted and ANDed with the CSR value.

### ECALL

ECALL is short for Environment Call.

I-Type Instruction.

```asm
ecall
```

But ECALL's high 25 bits are 0s.

When an exception is raised, ECALL calls the OS.

First, the value according to the CPU mode is written to the `mcause` register.

RSIC-V has 4 privilege levels which is encoded in the mcause register.

| Level | Encoding | Mode       |
| :-:   | :-:      | :-:        |
| 0     | 8        | User       |
| 1     | 9        | Supervisor |
| 2     | 10       | Hypervisor |
| 3     | 11       | Machine    |

Then, PC jumps to the trap_vector stored in the `mtvec` register(0x305).
The trap_vector describes the OS call when the exception is raised.

## Test

### RSIC-V tests

Generate the test elf and the dump files.

Change the start address in the linked data file `/usr/local/riscv/riscv-tests/env/p/link.ld`.

```
OUTPUT_ARCH( "riscv" )
ENTRY(_start)

SECTIONS
{
  . = 0x00000000;
  .text.init : { *(.text.init) }
  . = ALIGN(0x1000);
  .tohost : { *(.tohost) }
  . = ALIGN(0x1000);
  .text : { *(.text) }
  . = ALIGN(0x1000);
  .data : { *(.data) }
  .bss : { *(.bss) }
  _end = .;
}
```

```bash
cd /usr/local/riscv/riscv-tests
autoconf
./configure --prefix=/src/target
make
make install
```

Comment out the following code in `./target/share/riscv-tests/isa/.gitignore`.

```
rv*-*
```

### Compile

Convert the elf file to bin file.

```bash
mkdir /src/src/test/resources/riscv
cd /src/src/test/resources/riscv
riscv64-unknown-elf-objcopy -O binary /src/target/share/riscv-tests/isa/rv32ui-p-add rv32ui-p-add.bin
```

Convert the bin file to hex file.

```bash
od -An -tx1 -w1 -v rv32ui-p-add.bin >> rv32ui-p-add.hex
```

- -A, --address-radix=RADIX   output format for file offsets; RADIX is one of [doxn], for Decimal, Octal, Hex or None
- -t, --format=TYPE           select output format or formats
- -w[BYTES], --width[=BYTES]  output BYTES bytes per output line; 32 is implied when BYTES is not specified
- -v, --output-duplicates     do not use * to mark line suppression

```
TYPE is made up of one or more of these specifications:
  a          named character, ignoring high-order bit
  c          printable character or backslash escape
  d[SIZE]    signed decimal, SIZE bytes per integer
  f[SIZE]    floating point, SIZE bytes per float
  o[SIZE]    octal, SIZE bytes per integer
  u[SIZE]    unsigned decimal, SIZE bytes per integer
  x[SIZE]    hexadecimal, SIZE bytes per integer
```
