package pipeline
package tests

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import java.nio.file.{Files, Paths}
import java.io.{File, PrintStream}

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

class RiscvTest extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "cpu"
        Files.newDirectoryStream(Paths.get("src/test/resources/riscv"), filter => {
            val f = filter.toString()
            f.endsWith(".hex") &&
            (f.contains("ui-p") || f.contains("mi-p-csr") || f.contains("mi-p-scall")) &&
            !(f.contains("sh") || f.contains("lhu") || f.contains("fence_i") || f.contains("lb") || f.contains("sb") || f.contains("lh") || f.contains("lbu"))
        }).forEach { f =>
            val testName = f.getFileName.toString.stripSuffix(".hex")
            it should s"work through $testName" in {
                Console.withOut(new PrintStream(new File(s"results/${testName}.txt"))) {
                    val config = () => {
                        val cpu = new Top
                        cpu.mem.loadMemoryFromHexFile(f.toString)
                        cpu
                    }

                    test(config()) { dut =>
                        while(!dut.exit.peek().litToBoolean) {
                            dut.clock.step()
                        }
                        dut.clock.step()
                        dut.gp.expect(1.U)
                    }
                }
            }
        }
}
