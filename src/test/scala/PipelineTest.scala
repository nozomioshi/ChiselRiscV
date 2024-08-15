package pipeline
package tests

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class HazardTest extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "cpu"
        it should "work through hex" in {
            val configBrHazard = () => {
                val cpu = new Top
                cpu.mem.loadMemoryFromHexFile("src/test/resources/hex/brhazard.hex")
                cpu
            }
            test(configBrHazard()) { dut =>
                while(!dut.exit.peek().litToBoolean) {
                    dut.clock.step()
                }
                dut.clock.step()    
            }
        }
}
