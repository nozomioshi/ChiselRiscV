#include <stdio.h>

int main() {
    unsigned int size = 5;
    unsigned int vl;

    while(size > 0) {
        asm volatile(
            "vsetvli %0, %1, e64, m1" // Assembly code
            : "=r" (vl)  // Output operands
            : "r" (size) // Input operands
        );
        size -= vl;
    }
    asm volatile("unimp");
    return 0;
}