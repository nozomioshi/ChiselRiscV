package vsetvli

import chisel3._

import common.Constants.WordLen

class Top extends Module {
    val gp   = IO(Output(UInt(WordLen.W)))
    val exit = IO(Output(Bool()))

    val core = Module(new Core)
    val mem  = Module(new Memory)
    mem.imem <> core.imem
    mem.dmem <> core.dmem
    
    gp   := core.gp
    exit := core.exit
}
