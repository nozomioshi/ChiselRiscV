package riscvtests

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
    val immS = Cat(inst(31, 25), inst(11, 7))
    val immSsext = Cat(Fill(20, immS(11)), immS)

    val controlSignals = ListLookup(inst, List(AluX, Op1Rs1, Op2Rs2),
        Array(
            Lw   -> List(AluAdd,  Op1Rs1, Op2Imi, MenX, RenS, WbMem),
            Sw   -> List(AluAdd,  Op1Rs1, Op2Ims, MenS, RenX, WbX),
            Add  -> List(AluAdd,  Op1Rs1, Op2Rs2, MenX, RenS, WbAlu),
            Addi -> List(AluAdd,  Op1Rs1, Op2Imi, MenX, RenS, WbAlu),
            Sub  -> List(AluSub,  Op1Rs1, Op2Rs2, MenX, RenS, WbAlu),
            And  -> List(AluAnd,  Op1Rs1, Op2Rs2, MenX, RenS, WbAlu),
            Or   -> List(AluOr,   Op1Rs1, Op2Rs2, MenX, RenS, WbAlu),
            Xor  -> List(AluXor,  Op1Rs1, Op2Rs2, MenX, RenS, WbAlu),
            Andi -> List(AluAnd,  Op1Rs1, Op2Imi, MenX, RenS, WbAlu),
            Ori  -> List(AluOr,   Op1Rs1, Op2Imi, MenX, RenS, WbAlu),
            Xori -> List(AluXor,  Op1Rs1, Op2Imi, MenX, RenS, WbAlu),
            Sll  -> List(AluSll,  Op1Rs1, Op2Rs2, MenX, RenS, WbAlu),
            Srl  -> List(AluSrl,  Op1Rs1, Op2Rs2, MenX, RenS, WbAlu),
            Sra  -> List(AluSra,  Op1Rs1, Op2Rs2, MenX, RenS, WbAlu),
            Slli -> List(AluSll,  Op1Rs1, Op2Imi, MenX, RenS, WbAlu),
            Srli -> List(AluSrl,  Op1Rs1, Op2Imi, MenX, RenS, WbAlu),
            Srai -> List(AluSra,  Op1Rs1, Op2Imi, MenX, RenS, WbAlu),
            Slt  -> List(AluSlt,  Op1Rs1, Op2Rs2, MenX, RenS, WbAlu),
            Sltu -> List(AluSltu, Op1Rs1, Op2Rs2, MenX, RenS, WbAlu),
            Slti -> List(AluSlt,  Op1Rs1, Op2Imi, MenX, RenS, WbAlu),
            Sltiu-> List(AluSltu, Op1Rs1, Op2Imi, MenX, RenS, WbAlu),
        )
    )

    val exeFun :: op1Sel :: op2Sel :: memWen :: regFileWen :: wbSel :: Nil = controlSignals

    val op1Data = MuxCase(0.U, Seq(
        (op1Sel === Op1Rs1) -> rs1Data
    ))
    val op2Data = MuxCase(0.U, Seq(
        (op2Sel === Op2Rs2) -> rs2Data,
        (op2Sel === Op2Imi) -> immIsext,
        (op2Sel === Op2Ims) -> immSsext
    ))

    // Execute
    val aluOut = MuxCase(0.U, Seq(
        (exeFun === AluAdd) -> (op1Data + op2Data),
        (exeFun === AluSub) -> (op1Data - op2Data),
        (exeFun === AluAnd) -> (op1Data & op2Data),
        (exeFun === AluOr)  -> (op1Data | op2Data),
        (exeFun === AluXor) -> (op1Data ^ op2Data),
        (exeFun === AluSll) -> (op1Data << op2Data(4, 0))(31, 0),
        (exeFun === AluSrl) -> (op1Data >> op2Data(4, 0)),
        (exeFun === AluSra) -> (op1Data.asSInt >> op2Data(4, 0)).asUInt,
        (exeFun === AluSlt) -> (op1Data.asSInt < op2Data.asSInt).asUInt,
        (exeFun === AluSltu)-> (op1Data < op2Data).asUInt
    ))

    // Memory access
    dmem.addr := aluOut
    dmem.wEn := memWen
    dmem.wData := rs2Data

    // Write back
    val wbData = MuxCase(aluOut, Seq(
        (wbSel === WbMem) -> dmem.data
    ))
    when(regFileWen === RenS) {
        regFile(wbAddr) := wbData
    }

    // Debugging
    exit := (inst === 0x00602823.U)

    printf(cf"pcReg      : 0x${Hexadecimal(pcReg)}\n")
    printf(cf"inst       : 0x${Hexadecimal(inst)}\n")
    printf(cf"rs1Addr    : ${Decimal(rs1Addr)}\n")
    printf(cf"rs2Addr    : ${Decimal(rs2Addr)}\n")
    printf(cf"wbAddr     : ${Decimal(wbAddr)}\n")
    printf(cf"rs1Data    : 0x${Hexadecimal(rs1Data)}\n")
    printf(cf"rs2Data    : 0x${Hexadecimal(rs2Data)}\n")
    printf(cf"wbData     : 0x${Hexadecimal(wbData)}\n")
    printf(cf"dmem.addr  : ${Decimal(dmem.addr)}\n")
    printf(cf"dmem.wEn   : ${dmem.wEn}\n")
    printf(cf"dmem.wData : 0x${Hexadecimal(dmem.wData)}\n")
    printf("-----------------\n")
}