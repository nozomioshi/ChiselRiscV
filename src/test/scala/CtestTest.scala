package ctest
package tests

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class HexTest extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "cpu"
        it should "work through hex" in {
            val config = () => {
                val cpu = new Top
                cpu.mem.loadMemoryFromHexFile("src/test/resources/hex/ctest.hex")
                cpu
            }
            test(config()) { dut =>
                while(!dut.exit.peek().litToBoolean) {
                    dut.clock.step()
                }
                dut.clock.step()
            }
        }
}
