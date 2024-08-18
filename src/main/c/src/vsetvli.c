#include <stdio.h>

int main() {
    unsigned int size = 5;
    unsigned int v1;

    while(size > 0) {
        asm volatile(
            "vsetvli %0, %1, e32, m1" // Assembly code
            : "=r" (v1)  // Output operands
            : "r" (size) // Input operands
        );
        size -= v1;
    }
    asm volatile("unimp");
    return 0;
}
