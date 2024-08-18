%: ./src/%.c
	riscv64-unknown-elf-gcc $< -O2 -march=rv32iv -mabi=ilp32 -c -o ./build/$@.o
	riscv64-unknown-elf-ld ./build/$@.o -b elf32-littleriscv -T ./scripts/link.ld -o ./build/$@
	riscv64-unknown-elf-objcopy ./build/$@ -O binary ./bin/$@.bin
	od ./bin/$@.bin -An -tx1 -w1 -v > ../../test/resources/hex/$@.hex
	riscv64-unknown-elf-objdump ./build/$@ -b elf32-littleriscv -D > ./dump/$@.dump