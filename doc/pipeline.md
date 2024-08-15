# [pipeline](../src/main/scala/pipeline)

## Implement

### Fetch

The PC register is the only register in the fetch stage.

The ALU output, jump flag and target comes from Execute stage while the `ECALL` jump immediately in the fetch stage.

```scala
val ifRegPc = RegInit(StartAddr)

idRegPc   := ifRegPc
idRegInst := inst
```

### Decode

```scala
val idRegPc   = RegInit(0.U(WordLen.W))
val idRegInst = RegInit(0.U(WordLen.W))

idRegPc   := ifRegPc
idRegInst := inst
```

### Excute

```scala
val exeRegPc       = RegInit(0.U(WordLen.W))
val exeRegInst     = RegInit(0.U(WordLen.W))
val exeRegWbAddr   = RegInit(0.U(WordLen.W))
val exeRegRs2Data  = RegInit(0.U(WordLen.W))
val exeRegExeFun   = RegInit(0.U(WordLen.W))
val exeRegMemWen   = RegInit(0.U(WordLen.W))
val exeRegRfWen    = RegInit(0.U(WordLen.W))
val exeRegWbSel    = RegInit(0.U(WordLen.W))
val exeRegCsrCmd   = RegInit(0.U(WordLen.W))
val exeRegimmBsext = RegInit(0.U(WordLen.W))
val exeRegOp1Data  = RegInit(0.U(WordLen.W))
val exeRegOp2Data  = RegInit(0.U(WordLen.W))

exeRegPc       := idRegPc
exeRegInst     := idRegInst
exeRegWbAddr   := wbAddr
exeRegRs2Data  := rs2Data
exeRegimmBsext := immBsext
exeRegExeFun   := exeFun
exeRegMemWen   := memWen
exeRegRfWen    := regFileWen
exeRegWbSel    := wbSel
exeRegCsrCmd   := csrCmd
exeRegOp1Data  := op1Data
exeRegOp2Data  := op2Data
```

### Memory Access

```scala
val memRegPc      = RegInit(0.U(WordLen.W))
val memRegInst    = RegInit(0.U(WordLen.W))
val memRegWbAddr  = RegInit(0.U(WordLen.W))
val memRegRs2Data = RegInit(0.U(WordLen.W))
val memRegMemWen  = RegInit(0.U(WordLen.W))
val memRegRfWen   = RegInit(0.U(WordLen.W))
val memRegWbSel   = RegInit(0.U(WordLen.W))
val memRegCsrCmd  = RegInit(0.U(WordLen.W))
val memRegOp1Data = RegInit(0.U(WordLen.W))
val memRegAluOut  = RegInit(0.U(WordLen.W))

memRegPc      := memRegPc
memRegInst    := exeRegInst
memRegWbAddr  := exeRegWbAddr
memRegRs2Data := exeRegRs2Data
memRegMemWen  := exeRegMemWen
memRegRfWen   := exeRegRfWen
memRegWbSel   := exeRegWbSel
memRegCsrCmd  := exeRegCsrCmd
memRegOp1Data := exeRegOp1Data
memRegAluOut  := exeAluOut
```

### Write Back

```scala
val wbRegPc       = RegInit(0.U(WordLen.W))
val wbRegWbAddr   = RegInit(0.U(WordLen.W))
val wbRegRfWen    = RegInit(0.U(WordLen.W))
val wbRegWbSel    = RegInit(0.U(WordLen.W))
val wbRegAluOut   = RegInit(0.U(WordLen.W))
val wbRegdmemData = RegInit(0.U(WordLen.W))
val wbRegCsrRdata = RegInit(0.U(WordLen.W))

wbRegPc       := memRegPc
wbRegWbAddr   := memRegWbAddr
wbRegRfWen    := memRegRfWen
wbRegWbSel    := memRegWbSel
wbRegAluOut   := memRegAluOut
wbRegdmemData := dmem.data
wbRegCsrRdata := csrRdata
```

## Perf

The `wbData` can be calculated in the Memory Access stage.
As a result, the number of Write Back stage pipeline registers can be reduced.
The Write Back process can be separated from the calculation of data.


```scala
// Registers
val wbRegWbAddr   = RegInit(0.U(WordLen.W))
val wbRegRfWen    = RegInit(0.U(WordLen.W))
val wbRegWbData   = RegInit(0.U(WordLen.W))

// Execute
val wbData = MuxCase(wbRegAluOut, Seq(
    (wbRegWbSel === WbMem) -> dmem.data,
    (wbRegWbSel === WbPc)  -> (wbRegPc+4.U),
    (wbRegWbSel === WbCsr) -> csrRdata
))

// Write back
wbRegWbAddr   := memRegWbAddr
wbRegRfWen    := memRegRfWen
wbRegWbData   := wbData
```

The caculation of `csrAddr` can be considered as a decode.
Thus, it can be moved to the Decode stage.

```scala
val csrAddr = Mux(csrCmd === CsrE, 0x342.U, idRegInst(31, 20)) // mcause: 0x342
```

Therefore, `exeRegInst` and `memRegInst` can be removed.
But `exeRegCsrAddr` and `memRegCsrAddr` are added.
It is supposed to have a little bit of acceleration.

However, the calculation of `csrWdata` is dependent on the `csrRdata`, so it need to be calculated in the Memory Access stage.

```scala
val csrWdata = MuxCase(0.U, Seq(
    (memRegCsrCmd === CsrW) -> memRegOp1Data,
    (memRegCsrCmd === CsrS) -> (csrRdata | memRegOp1Data),
    (memRegCsrCmd === CsrC) -> (csrRdata & ~memRegOp1Data),
    (memRegCsrCmd === CsrE) -> 11.U // Machine ECALL
))
```

## Hazard

### Branch Hazard

The simplest way to solve the branch hazard is to stall the pipeline.
When the Execute stage is executing a branch or jump instruction, it will stall the pipeline for two cycles in a 5-stage pipeline.

`nop` instruction actually means `addi x0, x0, 0`.

To achieve these stallings, two MUX are added.

The first one is at the input of `idRegInst`.
The other one is at its output.

```scala
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
// ...
)

val csrAddr = Mux(csrCmd === CsrE, 0x342.U, idInst(31, 20)) // mcause: 0x342
```

### Data Hazard

There are two kinds of data hazards.

The first one is that the needed data has not been written back to the register file but already been calculated.
The other one is that the needed data hasn't been calculated yet.
The first kind of data hazard can be solved by forwarding.
The second kind of data hazard can be solved by stalling.
