package lw

import chisel3._
import chisel3.util._

import common.Constants._
import common.Instructions._

class Core extends Module {
    val imem = IO(Flipped(new ImemPortIo))
    val dmem = IO(Flipped(new DmemPortIo))
    val exit = IO(Output(Bool()))

    // Registers
    val regFile = Mem(32, UInt(WordLen.W))
    
    // Fetch
    val pcReg = RegInit(StartAddr)
    pcReg := pcReg + 4.U
    imem.addr := pcReg
    val inst = imem.inst

    // Decode
    val rs1Addr = inst(19, 15)
    val rs2Addr = inst(24, 20)
    val wbAddr  = inst(11, 7)
    val rs1Data = Mux(rs1Addr =/= 0.U, regFile(rs1Addr), 0.U)
    val rs2Data = Mux(rs2Addr =/= 0.U, regFile(rs2Addr), 0.U)

    val immI = inst(31, 20)
    val immIsext = Cat(Fill(20, immI(11)), immI)

    // Execute
    val aluOut = MuxCase(0.U, Seq(
        (inst === Lw) -> (rs1Data + immIsext)
    ))

    // Memory access
    dmem.addr := aluOut

    // Write back
    val wbData = dmem.data
    when (inst === Lw) {
        regFile(wbAddr) := wbData
    }

    // Debugging
    exit := (inst === 0x14131211.U)

    printf(cf"pcReg     : 0x${Hexadecimal(pcReg)}\n")
    printf(cf"inst      : 0x${Hexadecimal(inst)}\n")
    printf(cf"rs1Addr   : ${Decimal(rs1Addr)}\n")
    printf(cf"rs2Addr   : ${Decimal(rs2Addr)}\n")
    printf(cf"wbAddr    : ${Decimal(wbAddr)}\n")
    printf(cf"rs1Data   : 0x${Hexadecimal(rs1Data)}\n")
    printf(cf"rs2Data   : 0x${Hexadecimal(rs2Data)}\n")
    printf(cf"dmem.addr : ${Decimal(dmem.addr)}\n")
    printf(cf"wbData    : 0x${Hexadecimal(wbData)}\n")
    printf("-----------------\n")
}
