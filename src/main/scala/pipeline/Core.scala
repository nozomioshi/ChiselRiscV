package pipeline

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
    val regFile    = Mem(32, UInt(WordLen.W))
    val csrRegFile = Mem(4096, UInt(WordLen.W))

    val idRegPc   = RegInit(0.U(WordLen.W))
    val idRegInst = RegInit(0.U(WordLen.W))

    val exeRegPc       = RegInit(0.U(WordLen.W))
    val exeRegWbAddr   = RegInit(0.U(WordLen.W))
    val exeRegRs2Data  = RegInit(0.U(WordLen.W))
    val exeRegCsrAddr  = RegInit(0.U(WordLen.W))
    val exeRegExeFun   = RegInit(0.U(WordLen.W))
    val exeRegMemWen   = RegInit(0.U(WordLen.W))
    val exeRegRfWen    = RegInit(0.U(WordLen.W))
    val exeRegWbSel    = RegInit(0.U(WordLen.W))
    val exeRegCsrCmd   = RegInit(0.U(WordLen.W))
    val exeRegimmBsext = RegInit(0.U(WordLen.W))
    val exeRegOp1Data  = RegInit(0.U(WordLen.W))
    val exeRegOp2Data  = RegInit(0.U(WordLen.W))

    val memRegPc      = RegInit(0.U(WordLen.W))
    val memRegWbAddr  = RegInit(0.U(WordLen.W))
    val memRegRs2Data = RegInit(0.U(WordLen.W))
    val memRegCsrAddr = RegInit(0.U(WordLen.W))
    val memRegMemWen  = RegInit(0.U(WordLen.W))
    val memRegRfWen   = RegInit(0.U(WordLen.W))
    val memRegWbSel   = RegInit(0.U(WordLen.W))
    val memRegCsrCmd  = RegInit(0.U(WordLen.W))
    val memRegOp1Data = RegInit(0.U(WordLen.W))
    val memRegAluOut  = RegInit(0.U(WordLen.W))

    val wbRegWbAddr   = RegInit(0.U(WordLen.W))
    val wbRegRfWen    = RegInit(0.U(WordLen.W))
    val wbRegWbData   = RegInit(0.U(WordLen.W))
    
    // Fetch
    val ifRegPc = RegInit(StartAddr)

    val inst = imem.inst

    val exeBrFlag   = Wire(Bool())
    val exeBrTarget = Wire(UInt(WordLen.W))

    val exeJmpFlag = Wire(Bool())
    val eCallFlag  = inst === Ecall

    val exeAluOut = Wire(UInt(WordLen.W))

    ifRegPc := MuxCase(ifRegPc+4.U, Seq(
        exeBrFlag -> exeBrTarget,
        exeJmpFlag   -> exeAluOut,
        eCallFlag -> csrRegFile(0x305.U) // Trap vector
    ))
    imem.addr := ifRegPc

    // Decode
    idRegPc   := ifRegPc
    idRegInst := Mux((exeBrFlag||exeJmpFlag), Bubble, inst)

    val idInst = Mux((exeBrFlag||exeJmpFlag), Bubble, idRegInst)

    val rs1Addr = idInst(19, 15)
    val rs2Addr = idInst(24, 20)
    val wbAddr  = idInst(11, 7)
    val rs1Data = Mux(rs1Addr =/= 0.U, regFile(rs1Addr), 0.U)
    val rs2Data = Mux(rs2Addr =/= 0.U, regFile(rs2Addr), 0.U)

    val immI        = idInst(31, 20)
    val immIsext    = Cat(Fill(20, immI(11)), immI)
    val immS        = Cat(idInst(31, 25), idInst(11, 7))
    val immSsext    = Cat(Fill(20, immS(11)), immS)
    val immB        = Cat(idInst(31), idInst(7), idInst(30, 25), idInst(11, 8))
    val immBsext    = Cat(Fill(19, immB(11)), immB, 0.U(1.W))
    val immJ        = Cat(idInst(31), idInst(19, 12), idInst(20), idInst(30, 21))
    val immJsext    = Cat(Fill(11, immJ(19)), immJ, 0.U(1.W))
    val immU        = idInst(31, 12)
    val immUshifted = Cat(immU, 0.U(12.W))
    val immZ        = idInst(19, 15)
    val immZext     = Cat(Fill(27, 0.U), immZ)

    val controlSignals = ListLookup(idInst, List(AluX, Op1Rs1, Op2Rs2, MenX, RenS, WbX, CsrX),
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
        (op1Sel === Op1Pc)  -> idRegPc,
        (op1Sel === Op1Imz) -> immZext
    ))
    val op2Data = MuxCase(0.U, Seq(
        (op2Sel === Op2Rs2) -> rs2Data,
        (op2Sel === Op2Imi) -> immIsext,
        (op2Sel === Op2Ims) -> immSsext,
        (op2Sel === Op2Imj) -> immJsext,
        (op2Sel === Op2Imu) -> immUshifted
    ))

    val csrAddr = Mux(csrCmd === CsrE, 0x342.U, idInst(31, 20)) // mcause: 0x342

    // Execute
    exeRegPc       := idRegPc
    exeRegWbAddr   := wbAddr
    exeRegRs2Data  := rs2Data
    exeRegCsrAddr  := csrAddr
    exeRegimmBsext := immBsext
    exeRegExeFun   := exeFun
    exeRegMemWen   := memWen
    exeRegRfWen    := regFileWen
    exeRegWbSel    := wbSel
    exeRegCsrCmd   := csrCmd
    exeRegOp1Data  := op1Data
    exeRegOp2Data  := op2Data

    exeAluOut := MuxCase(0.U, Seq(
        (exeRegExeFun === AluAdd)   -> (exeRegOp1Data + exeRegOp2Data),
        (exeRegExeFun === AluSub)   -> (exeRegOp1Data - exeRegOp2Data),
        (exeRegExeFun === AluAnd)   -> (exeRegOp1Data & exeRegOp2Data),
        (exeRegExeFun === AluOr)    -> (exeRegOp1Data | exeRegOp2Data),
        (exeRegExeFun === AluXor)   -> (exeRegOp1Data ^ exeRegOp2Data),
        (exeRegExeFun === AluSll)   -> (exeRegOp1Data << exeRegOp2Data(4, 0))(31, 0),
        (exeRegExeFun === AluSrl)   -> (exeRegOp1Data >> exeRegOp2Data(4, 0)),
        (exeRegExeFun === AluSra)   -> (exeRegOp1Data.asSInt >> exeRegOp2Data(4, 0)).asUInt,
        (exeRegExeFun === AluSlt)   -> (exeRegOp1Data.asSInt < exeRegOp2Data.asSInt).asUInt,
        (exeRegExeFun === AluSltu)  -> (exeRegOp1Data < exeRegOp2Data).asUInt,
        (exeRegExeFun === AluJalr)  -> ((exeRegOp1Data + exeRegOp2Data) & ~1.U(WordLen.W)),
        (exeRegExeFun === AluCopy1) -> exeRegOp1Data
    ))

    exeBrFlag := MuxCase(false.B, Seq(
        (exeRegExeFun === BrBeq)  -> (exeRegOp1Data === exeRegOp2Data),
        (exeRegExeFun === BrBne)  -> (exeRegOp1Data =/= exeRegOp2Data),
        (exeRegExeFun === BrBlt)  -> (exeRegOp1Data.asSInt < exeRegOp2Data.asSInt),
        (exeRegExeFun === BrBge)  -> !(exeRegOp1Data.asSInt < exeRegOp2Data.asSInt),
        (exeRegExeFun === BrBltu) -> (exeRegOp1Data < exeRegOp2Data),
        (exeRegExeFun === BrBgeu) -> !(exeRegOp1Data < exeRegOp2Data)
    ))

    exeBrTarget := exeRegPc + exeRegimmBsext
    exeJmpFlag  := exeRegWbSel === WbPc

    // Memory access
    memRegPc      := exeRegPc
    memRegWbAddr  := exeRegWbAddr
    memRegRs2Data := exeRegRs2Data
    memRegCsrAddr := exeRegCsrAddr
    memRegMemWen  := exeRegMemWen
    memRegRfWen   := exeRegRfWen
    memRegWbSel   := exeRegWbSel
    memRegCsrCmd  := exeRegCsrCmd
    memRegOp1Data := exeRegOp1Data
    memRegAluOut  := exeAluOut

    dmem.addr  := memRegAluOut
    dmem.wEn   := memRegMemWen
    dmem.wData := memRegRs2Data
    
    val csrRdata = csrRegFile(memRegCsrAddr)
    val csrWdata = MuxCase(0.U, Seq(
        (memRegCsrCmd === CsrW) -> memRegOp1Data,
        (memRegCsrCmd === CsrS) -> (csrRdata | memRegOp1Data),
        (memRegCsrCmd === CsrC) -> (csrRdata & ~memRegOp1Data),
        (memRegCsrCmd === CsrE) -> 11.U // Machine ECALL
    ))
    when(memRegCsrCmd > 0.U) {
        csrRegFile(csrAddr) := csrWdata
    }

    val wbData = MuxCase(memRegAluOut, Seq(
        (memRegWbSel === WbMem) -> dmem.data,
        (memRegWbSel === WbPc)  -> (memRegPc+4.U),
        (memRegWbSel === WbCsr) -> csrRdata
    ))

    // Write back
    wbRegWbAddr := memRegWbAddr
    wbRegRfWen  := memRegRfWen
    wbRegWbData := wbData
    
    when(wbRegRfWen === RenS) {
        regFile(wbRegWbAddr) := wbRegWbData
    }

    // Debugging
    gp   := regFile(3)
    exit := (idRegInst === Unimp)

    printf(cf"ifRegPc       : 0x${Hexadecimal(ifRegPc)}\n")
    printf(cf"idRegPc       : 0x${Hexadecimal(idRegPc)}\n")
    printf(cf"idReginst     : 0x${Hexadecimal(idRegInst)}\n")
    printf(cf"exeRegPc      : 0x${Hexadecimal(exeRegPc)}\n")
    printf(cf"exeRegOp1Data : 0x${Hexadecimal(exeRegOp1Data)}\n")
    printf(cf"exeRegOp2Data : 0x${Hexadecimal(exeRegOp2Data)}\n")
    printf(cf"exeAluOut     : 0x${Hexadecimal(exeAluOut)}\n")
    printf(cf"memRegPc      : 0x${Hexadecimal(memRegPc)}\n")
    printf(cf"wbData        : 0x${Hexadecimal(wbData)}\n")
    printf("-----------------\n")
}
