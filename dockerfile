# base image
FROM ubuntu:18.04

# environment variables
ENV RISC=/usr/local/riscv
ENV PATH=${RISC}/bin:$PATH
ENV MAKEFLAGS=-j4

# work directory
WORKDIR ${RISC}

# base tools
RUN apt update && \
    apt install -y autoconf automake autotools-dev curl libmpc-dev libmpfr-dev libgmp-dev gawk build-essential bison flex texinfo gperf libtool patchutils bc zlib1g-dev libexpat-dev pkg-config git libusb-1.0-0-dev device-tree-compiler default-jdk gnupg vim

# riscv-gnu-toolchain
RUN git clone -b rvv-0.9.x-for-book --single-branch https://github.com/chadyuu/riscv-gnu-toolchain.git
RUN cd riscv-gnu-toolchain && git submodule update --init --recursive riscv-binutils
RUN cd riscv-gnu-toolchain && git submodule update --init --recursive riscv-gcc
RUN cd riscv-gnu-toolchain && git submodule update --init --recursive riscv-newlib
RUN cd riscv-gnu-toolchain && git submodule update --init --recursive riscv-gdb
RUN cd riscv-gnu-toolchain && mkdir build && cd build && ../configure --prefix=${RISC} --enable-multilib && make

# riscv-tests
RUN git clone -b master --single-branch https://github.com/riscv/riscv-tests && \
	cd riscv-tests && git checkout c4217d88bce9f805a81f42e86ff56ed363931d69 && \
	git submodule update --init --recursive
