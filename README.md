# ChiselRiscV

This is a 32-bit RISC-V CPU implemented according to the book "[CPU Design with RISC-V and Chisel - First step to custom CPU implementation with open-source ISA.](https://github.com/chadyuu/riscv-chisel-book)"

## Environment

- WLS2
- Ubuntu: 22.04LTS
- sbt: 1.10.0
- scala: 2.13.12
  - chisel: 6.5.0
  - chiseltest: 6.0.0

### RISC-V GNU toolchain

Dockerfile build:

```bash
sudo service docker start
docker build . -t riscv/cpu
docker run -it -v ./:/src riscv/cpu
```

After the build, you can run the container with the following command:

```bash
docker container start container_name/id -i
```

where `container_name/id` can be found by running `docker ps -a`.

## Feature

- [fetch](doc/fetch.md)
- [decode](doc/decode.md)
- [lw](doc/lw.md)
- [sw](doc/sw.md)
- [riscvtests](doc/riscvtests.md)
- [ctest](doc/ctest.md)
- [pipeline](doc/pipeline.md)
- [vsetvli](doc/vsetvli.md)

## Related Efforts

- [studying-riscv-chisel-book](https://github.com/ritalin/studying-riscv-chisel-book) - The reference of solutions to some problems encountered during the implementation of the book.
