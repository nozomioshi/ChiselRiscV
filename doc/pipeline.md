# [pipeline](../src/main/scala/pipeline)

## Fetch

The PC register is the only register in the fetch stage.

The ALU output, jump flag and target comes from Execute stage while the `ECALL` jump immediately in the fetch stage.

```scala
val ifRegPc = RegInit(StartAddr)

idRegPc   := ifRegPc
idRegInst := inst
```

## Decode

```scala
val idRegPc   = RegInit(0.U(WordLen.W))
val idRegInst = RegInit(0.U(WordLen.W))

idRegPc   := ifRegPc
idRegInst := inst
```

## Excute

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

## Memory Access

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

## Write Back

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
