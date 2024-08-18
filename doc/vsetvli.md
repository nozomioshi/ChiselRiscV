# [vsetvli](../src/main/scala/vsetvli)

In RISC-V Vector Extension, the Vector Length(VL) is calculated by hardware based on the instruction and the vector configuration.
The Selected Element Width(SEW) is the only software defined parameter among the VL, VLEN and SEW in RISC-V Vector Extension.

| ISA  | VLEN             | SEW              | VL                  |
| :-:  | :-:              | :-:              | :-:                 |
| RVV  | Hardware fixed   | Software defined | Hardware calculated |
| SIMD | Software defined | Software defined | Software defined    |

Vector CSR.

| Address | Name  | Description                   |
| :-:     | :-:   | :-:                           |
| 0xC20   | VL    | vector length                 |
| 0xC21   | VTYPE | vector data type: include SEW |

VSETVLI is the instruction to set the CSR settings before the process of vector instructions.

I-Type Instruction.

```asm
vsetvli rd, rs1, vtypei
```

`rs1` stores the Application Vector Length(AVL) and the VL is written back to `rd`.

VTYPE information.

| Bit      | Description                                                         |
| :-:      | :-:                                                                 |
| XLEN-1   | vill: If the setting value is not appropriate, it will be set to 1. |
| XLEN-2:8 | Reserved                                                            |
| 7        | vma: vector mask agnostic                                           |
| 6        | vta: vector tail agnostic                                           |
| 5        | vlmul[2]                                                            |
| 4:2      | vsew[2:0]                                                           |
| 1:0      | vlmul[1:0]                                                          |

VTYPE is a sign extended `vtypei`.
In this implementation, `vill`, `vma` and `vta` are ignored.

According to `VLEN`, `LMUL` and `SEW`, the maximum value `VLMAX` of `VL` which means the maximum number of elements can be processed in one calculation is calculated by hardware.

$$
VLMAX = (VLEN \times LMUL) / SEW.
$$

`LMUL` determines the number ratio of the elements in the vector register, which is 1, 2, 4, 8, 1/2, 1/4, 1/8.

## Test

Set the RISC-V architecture from `rv32i` to `rv32iv`.

```makefile
riscv64-unknown-elf-gcc $< -O2 -march=rv32i -mabi=ilp32 -c -o ./build/$@.o
```

```makefile
riscv64-unknown-elf-gcc $< -O2 -march=rv32iv -mabi=ilp32 -c -o ./build/$@.o
```

```c
asm volatile(
    "vsetvli %0, %1, e32, m1" // Assembly code
    : "=r" (vl)  // Output operands
    : "r" (size) // Input operands
);
```


