package common

import chisel3._

object Constants {
    val WordLen    = 32
    val StartAddr  = 0.U(WordLen.W)
    val Bubble     = 0x00000013.U(WordLen.W)  // [ADDI x0,x0,0] = Bubble
    val Unimp      = "x_c0001073".U(WordLen.W) // [CSRRW x0, cycle, x0]
    val AddrLen    = 5 // rs1,rs2,wb
    val CsrAddrLen = 12
    val Vlen       = 128
    val LmulLen    = 2
    val SewLen     = 11
    val VlAddr     = 0xC20
    val VtypeAddr  = 0xC21

    val ExeFunLen  = 5
    val AluX       = 0.U(ExeFunLen.W)
    val AluAdd     = 1.U(ExeFunLen.W)
    val AluSub     = 2.U(ExeFunLen.W)
    val AluAnd     = 3.U(ExeFunLen.W)
    val AluOr      = 4.U(ExeFunLen.W)
    val AluXor     = 5.U(ExeFunLen.W)
    val AluSll     = 6.U(ExeFunLen.W)
    val AluSrl     = 7.U(ExeFunLen.W)
    val AluSra     = 8.U(ExeFunLen.W)
    val AluSlt     = 9.U(ExeFunLen.W)
    val AluSltu    = 10.U(ExeFunLen.W)
    val BrBeq      = 11.U(ExeFunLen.W)
    val BrBne      = 12.U(ExeFunLen.W)
    val BrBlt      = 13.U(ExeFunLen.W)
    val BrBge      = 14.U(ExeFunLen.W)
    val BrBltu     = 15.U(ExeFunLen.W)
    val BrBgeu     = 16.U(ExeFunLen.W)
    val AluJalr    = 17.U(ExeFunLen.W)
    val AluCopy1   = 18.U(ExeFunLen.W)
    val AluVaddvv  = 19.U(ExeFunLen.W)
    val Vset       = 20.U(ExeFunLen.W)
    val AluPcnt    = 21.U(ExeFunLen.W)

    val Op1Len     = 2
    val Op1Rs1     = 0.U(Op1Len.W)
    val Op1Pc      = 1.U(Op1Len.W)
    val Op1X       = 2.U(Op1Len.W)
    val Op1Imz     = 3.U(Op1Len.W)

    val Op2Len     = 3
    val Op2X       = 0.U(Op2Len.W)
    val Op2Rs2     = 1.U(Op2Len.W)
    val Op2Imi     = 2.U(Op2Len.W)
    val Op2Ims     = 3.U(Op2Len.W)
    val Op2Imj     = 4.U(Op2Len.W)
    val Op2Imu     = 5.U(Op2Len.W)

    val MenLen     = 2
    val MenX       = 0.U(MenLen.W)
    val MenS       = 1.U(MenLen.W) // Scalar
    val MenV       = 2.U(MenLen.W) // Vector

    val RenLen     = 2
    val RenX       = 0.U(RenLen.W)
    val RenS       = 1.U(RenLen.W) // Scalar
    val RenV       = 2.U(RenLen.W) // Vector

    val WbSelLen   = 3
    val WbX        = 0.U(WbSelLen.W)
    val WbAlu      = 0.U(WbSelLen.W)
    val WbMem      = 1.U(WbSelLen.W)
    val WbPc       = 2.U(WbSelLen.W)
    val WbCsr      = 3.U(WbSelLen.W)
    val WbMemV     = 4.U(WbSelLen.W)
    val WbAluV     = 5.U(WbSelLen.W)
    val WbVl       = 6.U(WbSelLen.W)

    val MwLen      = 3
    val MwX        = 0.U(MwLen.W)
    val MwW        = 1.U(MwLen.W)
    val MwH        = 2.U(MwLen.W)
    val MwB        = 3.U(MwLen.W)
    val MwHu       = 4.U(MwLen.W)
    val MwBu       = 5.U(MwLen.W)

    val CsrLen     = 3
    val CsrX       = 0.U(CsrLen.W)
    val CsrW       = 1.U(CsrLen.W)
    val CsrS       = 2.U(CsrLen.W)
    val CsrC       = 3.U(CsrLen.W)
    val CsrE       = 4.U(CsrLen.W)
    val CsrV       = 5.U(CsrLen.W)
}
