package riscvtests

import chisel3._

class Top extends Module {
    val exit = IO(Output(Bool()))

    val core = Module(new Core)
    val mem  = Module(new Memory)
    mem.imem <> core.imem
    mem.dmem <> core.dmem
    
    exit := core.exit
}
