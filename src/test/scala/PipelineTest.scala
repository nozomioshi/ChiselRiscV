package pipeline
package tests

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class HazardTest extends AnyFlatSpec with ChiselScalatestTester {
    val configBrHazard = () => {
        val cpu = new Top
        cpu.mem.loadMemoryFromHexFile("src/test/resources/hex/brhazard.hex")
        cpu
    }
    val configWbHazard = () => {
        val cpu = new Top
        cpu.mem.loadMemoryFromHexFile("src/test/resources/hex/wbhazard.hex")
        cpu
    }
    val configExHazard = () => {
        val cpu = new Top
        cpu.mem.loadMemoryFromHexFile("src/test/resources/hex/exhazard.hex")
        cpu
    }
    behavior of "cpu"
        it should "work through Branch Hazard" in {
            test(configBrHazard()) { dut =>
                while(!dut.exit.peek().litToBoolean) {
                    dut.clock.step()
                }
                dut.clock.step()    
            }
        }
    behavior of "cpu"
        it should "work through Data Hazard: Wb" in {
            test(configWbHazard()) { dut =>
                while(!dut.exit.peek().litToBoolean) {
                    dut.clock.step()
                }
                dut.clock.step()    
            }
        }
    behavior of "cpu"
        it should "work through Data Hazard: Ex" in {
            test(configExHazard()) { dut =>
                while(!dut.exit.peek().litToBoolean) {
                    dut.clock.step()
                }
                dut.clock.step()    
            }
        }            
}
