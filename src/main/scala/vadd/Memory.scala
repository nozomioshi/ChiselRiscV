package vadd

import chisel3._
import chisel3.util.Cat
import chisel3.util.experimental.loadMemoryFromFileInline

import common.Constants.{WordLen, StartAddr, Vlen}

class ImemPortIo extends Bundle {
    val addr = Input(UInt(WordLen.W))
    val inst = Output(UInt(WordLen.W))
}

class DmemPortIo extends Bundle {
    val addr   = Input(UInt(WordLen.W))
    val wEn    = Input(Bool())
    val wData  = Input(UInt(WordLen.W))
    val data   = Output(UInt(WordLen.W))
    val vData = Output(UInt((Vlen*8).W))
}

class Memory extends Module {
    val imem = IO(new ImemPortIo)
    val dmem = IO(new DmemPortIo)

    val mem = Mem(math.pow(2, 14).toInt, UInt(8.W))
    
    def loadMemoryFromHexFile(filename: String): Unit = loadMemoryFromFileInline(mem, filename)
    def readData(len: Int) = Cat(Seq.tabulate(len/8)(n => mem(dmem.addr + n.U)).reverse)

    imem.inst := Cat(
        mem(imem.addr + 3.U),
        mem(imem.addr + 2.U),
        mem(imem.addr + 1.U),
        mem(imem.addr)
    )
    dmem.data := readData(WordLen)
    dmem.vData := readData(Vlen*8)

    when(dmem.wEn) {
        mem(dmem.addr + 3.U) := dmem.wData(31, 24)
        mem(dmem.addr + 2.U) := dmem.wData(23, 16)
        mem(dmem.addr + 1.U) := dmem.wData(15, 8)
        mem(dmem.addr)       := dmem.wData(8, 0)
    }
}
