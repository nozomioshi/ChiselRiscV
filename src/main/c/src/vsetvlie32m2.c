#include <stdio.h>

int main() {
    unsigned int size = 10;
    unsigned int vl;

    while(size > 0) {
        asm volatile(
            "vsetvli %0, %1, e32, m2" // Assembly code
            : "=r" (vl)  // Output operands
            : "r" (size) // Input operands
        );
        size -= vl;
    }
    asm volatile("unimp");
    return 0;
}
