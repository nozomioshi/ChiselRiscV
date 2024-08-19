#include <stdio.h>

int main() {
    unsigned int size = 5;
    unsigned int x[] = {
        0x11111111,
        0x22222222,
        0x33333333,
        0x44444444,
        0x55555555
    };
    unsigned int *xp = x;
    unsigned int vl;
    
    while(size > 0) {
        asm volatile(
            "vsetvli %0, %1, e32, m1"
            : "=r" (vl)
            : "r"  (size)
        );
        size -= vl;

        asm volatile(
            "vle32.v v1, (%0)"
            :
            : "r" (xp)
        );
        xp += vl;
    }
    asm volatile("unimp");
    return 0;
}
