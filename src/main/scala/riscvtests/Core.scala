package riscvtests

import chisel3._
import chisel3.util._

import common.Constants._
import common.Instructions._

class Core extends Module {
    val imem = IO(Flipped(new ImemPortIo))
    val dmem = IO(Flipped(new DmemPortIo))
    val gp   = IO(Output(UInt(WordLen.W)))
    val exit = IO(Output(Bool()))

    // Registers
    val regFile = Mem(32, UInt(WordLen.W))
    val csrRegFile = Mem(4096, UInt(WordLen.W))
    
    // Fetch
    val pcReg = RegInit(StartAddr)
    val pcPlus4 = pcReg + 4.U

    val inst = imem.inst

    val brFlag = Wire(Bool())
    val brTarget = Wire(UInt(WordLen.W))

    val jmpFlag   = inst === Jal || inst === Jalr
    val eCallFlag = inst === Ecall

    val aluOut = Wire(UInt(WordLen.W))
    pcReg := MuxCase(pcPlus4, Seq(
        brFlag    -> brTarget,
        jmpFlag   -> aluOut,
        eCallFlag -> csrRegFile(0x305.U) // Trap vector
    ))
    imem.addr := pcReg

    // Decode
    val rs1Addr = inst(19, 15)
    val rs2Addr = inst(24, 20)
    val wbAddr  = inst(11, 7)
    val rs1Data = Mux(rs1Addr =/= 0.U, regFile(rs1Addr), 0.U)
    val rs2Data = Mux(rs2Addr =/= 0.U, regFile(rs2Addr), 0.U)

    val immI        = inst(31, 20)
    val immIsext    = Cat(Fill(20, immI(11)), immI)
    val immS        = Cat(inst(31, 25), inst(11, 7))
    val immSsext    = Cat(Fill(20, immS(11)), immS)
    val immB        = Cat(inst(31), inst(7), inst(30, 25), inst(11, 8))
    val immBsext    = Cat(Fill(19, immB(11)), immB, 0.U(1.W))
    val immJ        = Cat(inst(31), inst(19, 12), inst(20), inst(30, 21))
    val immJsext    = Cat(Fill(11, immJ(19)), immJ, 0.U(1.W))
    val immU        = inst(31, 12)
    val immUshifted = Cat(immU, 0.U(12.W))

    val controlSignals = ListLookup(inst, List(AluX, Op1Rs1, Op2Rs2, MenX, RenS, WbX, CsrX),
        Array(
            Lw     -> List(AluAdd,   Op1Rs1, Op2Imi, MenX, RenS, WbMem, CsrX),
            Sw     -> List(AluAdd,   Op1Rs1, Op2Ims, MenS, RenX, WbX,   CsrX),
            Add    -> List(AluAdd,   Op1Rs1, Op2Rs2, MenX, RenS, WbAlu, CsrX),
            Addi   -> List(AluAdd,   Op1Rs1, Op2Imi, MenX, RenS, WbAlu, CsrX),
            Sub    -> List(AluSub,   Op1Rs1, Op2Rs2, MenX, RenS, WbAlu, CsrX),
            And    -> List(AluAnd,   Op1Rs1, Op2Rs2, MenX, RenS, WbAlu, CsrX),
            Or     -> List(AluOr,    Op1Rs1, Op2Rs2, MenX, RenS, WbAlu, CsrX),
            Xor    -> List(AluXor,   Op1Rs1, Op2Rs2, MenX, RenS, WbAlu, CsrX),
            Andi   -> List(AluAnd,   Op1Rs1, Op2Imi, MenX, RenS, WbAlu, CsrX),
            Ori    -> List(AluOr,    Op1Rs1, Op2Imi, MenX, RenS, WbAlu, CsrX),
            Xori   -> List(AluXor,   Op1Rs1, Op2Imi, MenX, RenS, WbAlu, CsrX),
            Sll    -> List(AluSll,   Op1Rs1, Op2Rs2, MenX, RenS, WbAlu, CsrX),
            Srl    -> List(AluSrl,   Op1Rs1, Op2Rs2, MenX, RenS, WbAlu, CsrX),
            Sra    -> List(AluSra,   Op1Rs1, Op2Rs2, MenX, RenS, WbAlu, CsrX),
            Slli   -> List(AluSll,   Op1Rs1, Op2Imi, MenX, RenS, WbAlu, CsrX),
            Srli   -> List(AluSrl,   Op1Rs1, Op2Imi, MenX, RenS, WbAlu, CsrX),
            Srai   -> List(AluSra,   Op1Rs1, Op2Imi, MenX, RenS, WbAlu, CsrX),
            Slt    -> List(AluSlt,   Op1Rs1, Op2Rs2, MenX, RenS, WbAlu, CsrX),
            Sltu   -> List(AluSltu,  Op1Rs1, Op2Rs2, MenX, RenS, WbAlu, CsrX),
            Slti   -> List(AluSlt,   Op1Rs1, Op2Imi, MenX, RenS, WbAlu, CsrX),
            Sltiu  -> List(AluSltu,  Op1Rs1, Op2Imi, MenX, RenS, WbAlu, CsrX),
            Beq    -> List(BrBeq,    Op1Rs1, Op2Rs2, MenX, RenX, WbX,   CsrX),
            Bne    -> List(BrBne,    Op1Rs1, Op2Rs2, MenX, RenX, WbX,   CsrX),
            Blt    -> List(BrBlt,    Op1Rs1, Op2Rs2, MenX, RenX, WbX,   CsrX),
            Bge    -> List(BrBge,    Op1Rs1, Op2Rs2, MenX, RenX, WbX,   CsrX),
            Bltu   -> List(BrBltu,   Op1Rs1, Op2Rs2, MenX, RenX, WbX,   CsrX),
            Bgeu   -> List(BrBgeu,   Op1Rs1, Op2Rs2, MenX, RenX, WbX,   CsrX),
            Jal    -> List(AluAdd,   Op1Pc,  Op2Imj, MenX, RenS, WbPc,  CsrX),
            Jalr   -> List(AluJalr,  Op1Rs1, Op2Imi, MenX, RenS, WbPc,  CsrX),
            Lui    -> List(AluAdd,   Op1X,   Op2Imu, MenX, RenS, WbAlu, CsrX),
            AuiPc  -> List(AluAdd,   Op1Pc,  Op2Imu, MenX, RenS, WbAlu, CsrX),
            CsrRw  -> List(AluCopy1, Op1Rs1, Op2X,   MenX, RenS, WbCsr, CsrW),
            CsrRs  -> List(AluCopy1, Op1Rs1, Op2X,   MenX, RenS, WbCsr, CsrS),
            CsrRc  -> List(AluCopy1, Op1Rs1, Op2X,   MenX, RenS, WbCsr, CsrC),
            CsrRwi -> List(AluCopy1, Op1Imz, Op2X,   MenX, RenS, WbCsr, CsrW),
            CsrRsi -> List(AluCopy1, Op1Imz, Op2X,   MenX, RenS, WbCsr, CsrS),
            CsrRci -> List(AluCopy1, Op1Imz, Op2X,   MenX, RenS, WbCsr, CsrC),
            Ecall  -> List(AluX,     Op1X,   Op2X,   MenX, RenX, WbX,   CsrE)
        )
    )

    val exeFun :: op1Sel :: op2Sel :: memWen :: regFileWen :: wbSel :: csrCmd :: Nil = controlSignals

    val op1Data = MuxCase(0.U, Seq(
        (op1Sel === Op1Rs1) -> rs1Data,
        (op1Sel === Op1Pc)  -> pcReg
    ))
    val op2Data = MuxCase(0.U, Seq(
        (op2Sel === Op2Rs2) -> rs2Data,
        (op2Sel === Op2Imi) -> immIsext,
        (op2Sel === Op2Ims) -> immSsext,
        (op2Sel === Op2Imj) -> immJsext,
        (op2Sel === Op2Imu) -> immUshifted
    ))

    // Execute
    aluOut := MuxCase(0.U, Seq(
        (exeFun === AluAdd)   -> (op1Data + op2Data),
        (exeFun === AluSub)   -> (op1Data - op2Data),
        (exeFun === AluAnd)   -> (op1Data & op2Data),
        (exeFun === AluOr)    -> (op1Data | op2Data),
        (exeFun === AluXor)   -> (op1Data ^ op2Data),
        (exeFun === AluSll)   -> (op1Data << op2Data(4, 0))(31, 0),
        (exeFun === AluSrl)   -> (op1Data >> op2Data(4, 0)),
        (exeFun === AluSra)   -> (op1Data.asSInt >> op2Data(4, 0)).asUInt,
        (exeFun === AluSlt)   -> (op1Data.asSInt < op2Data.asSInt).asUInt,
        (exeFun === AluSltu)  -> (op1Data < op2Data).asUInt,
        (exeFun === AluJalr)  -> ((op1Data + op2Data) & ~1.U(WordLen.W)),
        (exeFun === AluCopy1) -> op1Data
    ))

    brFlag := MuxCase(false.B, Seq(
        (exeFun === BrBeq)  -> (op1Data === op2Data),
        (exeFun === BrBne)  -> (op1Data =/= op2Data),
        (exeFun === BrBlt)  -> (op1Data.asSInt < op2Data.asSInt),
        (exeFun === BrBge)  -> !(op1Data.asSInt < op2Data.asSInt),
        (exeFun === BrBltu) -> (op1Data < op2Data),
        (exeFun === BrBgeu) -> !(op1Data < op2Data)
    ))

    brTarget := pcReg + immBsext

    // Memory access
    dmem.addr  := aluOut
    dmem.wEn   := memWen
    dmem.wData := rs2Data

    val csrAddr = Mux(csrCmd === CsrE, 0x342.U, inst(31, 20)) // mcause: 0x342
    val csrRdata = csrRegFile(csrAddr)
    val csrWdata = MuxCase(0.U, Seq(
        (csrCmd === CsrW) -> op1Data,
        (csrCmd === CsrS) -> (csrRdata | op1Data),
        (csrCmd === CsrC) -> (csrRdata & ~op1Data),
        (csrCmd === CsrE) -> 11.U // Machine ECALL
    ))
    when(csrCmd > 0.U) {
        csrRegFile(csrAddr) := csrWdata
    }

    // Write back
    val wbData = MuxCase(aluOut, Seq(
        (wbSel === WbMem) -> dmem.data,
        (wbSel === WbPc)  -> pcPlus4,
        (wbSel === WbCsr) -> csrRdata
    ))
    when(regFileWen === RenS) {
        regFile(wbAddr) := wbData
    }

    // Debugging
    gp   := regFile(3)
    exit := (pcReg === 0x44.U)

    printf(cf"pcReg      : 0x${Hexadecimal(pcReg)}\n")
    printf(cf"inst       : 0x${Hexadecimal(inst)}\n")
    printf(cf"gp         : ${regFile(3)}\n")
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
