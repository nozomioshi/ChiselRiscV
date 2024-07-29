# [fetch](../src/main/scala/fetch)

Memory consists of UInt(8.W), representing a byte.
RISC-V uses little-endian, which means the least significant byte is stored at the lowest address.

8 bit fetch.hex

| Address | Data |
| :-----: | :--: |
| 0       | 11   |
| 1       | 12   |
| 2       | 13   |
| 3       | 14   |
| 4       | 21   |
| ...     | ...  |
| 11      | 34   |

8 bit fetch.hex after being organized into 32 bit.

| Address | Data     |
| :-----: | :------: |
| 0       | 14131211 |
| 4       | 24232221 |
| 8       | 34333221 |

## Test

`org.scalatest.FlatSpec` is obsolete. $\rightarrow$ `org.scalatest.flatspec.AnyFlatSpec`

`loadMemoryFromFile` under `chiseltest 6.0.0` tests can't load the memory from a file.

> [WARNING] Unsupported annotation: LoadMemoryAnnotation \
> Please report this issue at https://github.com/ucb-bar/chiseltest/issues \
> \- should work through hex *** FAILED *** \
>  chiseltest.TimeoutException: timeout on Top.clock: IO[Clock] at 1000 idle cycles You can extend the timeout by calling .setTimeout(\<n\>) on your clock (setting it to 0 means 'no timeout'). \
>  at chiseltest.internal.AccessCheck.simulationStep(AccessCheck.scala:174) \
>  at chiseltest.internal.SimController.\$anonfun\$scheduler\$1(SimController.scala:59) \
>  at chiseltest.internal.Scheduler.doStep(Scheduler.scala:377) \
>  at chiseltest.internal.Scheduler.stepThread(Scheduler.scala:329) \
>  at chiseltest.internal.SimController.step(SimController.scala:103) \
>  at chiseltest.package\$testableClock.step(package.scala:400) \
>  at fetch.tests.HexTest.\$anonfun\$new\$3(FetchTest.scala:14) \
>  at fetch.tests.HexTest.\$anonfun\$new\$3\$adapted(FetchTest.scala:12) \
>  at chiseltest.internal.SimController.run(SimController.scala:124) \
>  at chiseltest.internal.Context\$.\$anonfun\$runTest\$2(Context.scala:30)
>  ...

Use `loadMemoryFromFileInline` instead.

By the way, `dut.clock.setTimeout()` can be used to change the timeout of 1000 cycles. Setting it to zero should disable the timeout altogether.
