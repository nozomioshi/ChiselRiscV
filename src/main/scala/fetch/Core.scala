package fetch

import chisel3._

import common.Constants._

class Core extends Module {
    val imem = IO(Flipped(new ImemPortIo))
    val exit = IO(Output(Bool()))

    val regFile = Mem(32, UInt(WordLen.W))
    
    // Fetch
    val pcReg = RegInit(StartAddr)
    pcReg := pcReg + 4.U
    imem.addr := pcReg
    val inst = imem.inst

    exit := (inst === 0x34333231.U)

    // Debugging
    printf(cf"pcReg : 0x${Hexadecimal(pcReg)}\n")
    printf(cf"inst  : 0x${Hexadecimal(inst)}\n")
    printf("-----------------\n")
}
