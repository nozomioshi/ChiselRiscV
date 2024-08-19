package vse
package tests

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class HexTest extends AnyFlatSpec with ChiselScalatestTester {
    val e32m1Config = () => {
        val cpu = new Top
        cpu.mem.loadMemoryFromHexFile("src/test/resources/hex/vse32m1.hex")
        cpu
    }
    val e64m1Config = () => {
        val cpu = new Top
        cpu.mem.loadMemoryFromHexFile("src/test/resources/hex/vse64m1.hex")
        cpu
    }
    val e32m2Config = () => {
        val cpu = new Top
        cpu.mem.loadMemoryFromHexFile("src/test/resources/hex/vse32m2.hex")
        cpu
    }
    behavior of "cpu"
        it should "work through e32/m1" in {
            test(e32m1Config()) { dut =>
                while(!dut.exit.peek().litToBoolean) {
                    dut.clock.step()
                }
                dut.clock.step()
            }
        }
    behavior of "cpu"
        it should "work through e64/m1" in {
            test(e64m1Config()) { dut =>
                while(!dut.exit.peek().litToBoolean) {
                    dut.clock.step()
                }
                dut.clock.step()
            }
        }
    behavior of "cpu"
        it should "work through e32/m2" in {
            test(e32m2Config()) { dut =>
                while(!dut.exit.peek().litToBoolean) {
                    dut.clock.step()
                }
                dut.clock.step()
            }
        }
}
