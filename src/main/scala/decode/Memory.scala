package decode

import chisel3._
import chisel3.util.Cat
import chisel3.util.experimental.loadMemoryFromFileInline

import common.Constants.{WordLen, StartAddr}

class ImemPortIo extends Bundle {
    val addr = Input(UInt(WordLen.W))
    val inst = Output(UInt(WordLen.W))
}

class Memory extends Module {
    val imem = IO(new ImemPortIo)

    val mem = Mem(math.pow(2, 14).toInt, UInt(8.W))
    
    def loadMemoryFromHexFile(filename: String): Unit = loadMemoryFromFileInline(mem, filename)

    imem.inst := Cat(
        mem(imem.addr + 3.U),
        mem(imem.addr + 2.U),
        mem(imem.addr + 1.U),
        mem(imem.addr)
    )
}
