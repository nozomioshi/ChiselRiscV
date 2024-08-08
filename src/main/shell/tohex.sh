#!/bin/bash

FILES=/src/target/share/riscv-tests/isa/rv32*i-p-*
SAVE_DIR=/src/src/test/resources/riscv

for f in $FILES
do
    FILE_NAMES="${f##*/}"
    if [[ ! $f =~ "dump" ]]
    then
        riscv64-unknown-elf-objcopy -O binary $f $SAVE_DIR/$FILE_NAMES.bin
        od -An -tx1 -w1 -v $SAVE_DIR/$FILE_NAMES.bin > $SAVE_DIR/$FILE_NAMES.hex
        rm -f $SAVE_DIR/$FILE_NAMES.bin
    fi
done
