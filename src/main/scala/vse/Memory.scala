package vse

import chisel3._
import chisel3.util.{Cat, switch, is}
import chisel3.util.experimental.loadMemoryFromFileInline

import common.Constants.{WordLen, StartAddr, Vlen}
import common.Constants.{MenS, MenV}

class ImemPortIo extends Bundle {
    val addr = Input(UInt(WordLen.W))
    val inst = Output(UInt(WordLen.W))
}

class DmemPortIo extends Bundle {
    val addr    = Input(UInt(WordLen.W))
    val wEn     = Input(UInt(WordLen.W))
    val wData   = Input(UInt(WordLen.W))
    val vWdata  = Input(UInt((Vlen*8).W))
    val dataLen = Input(UInt(WordLen.W)) 
    val data    = Output(UInt(WordLen.W))
    val vRdata  = Output(UInt((Vlen*8).W))
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
    dmem.vRdata := readData(Vlen*8)

    switch(dmem.wEn) {
        is (MenS) {
            mem(dmem.addr + 3.U) := dmem.wData(31, 24)
            mem(dmem.addr + 2.U) := dmem.wData(23, 16)
            mem(dmem.addr + 1.U) := dmem.wData(15, 8)
            mem(dmem.addr)       := dmem.wData(8, 0)
        }
        is (MenV) {
            val dataLenByte = dmem.dataLen / 8.U
            for(i <- 0 to Vlen-1) {
                when(i.U < dataLenByte) {
                    mem(dmem.addr + i.U) := dmem.vWdata(8*(i+1)-1, 8*i)
                }
            }
        }
    }
}
