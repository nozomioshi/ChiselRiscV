#include <stdio.h>

int main() {
    unsigned int size = 10;
    unsigned int x[] = {
        0x11111111,
        0x22222222,
        0x33333333,
        0x44444444,
        0x55555555,
        0x66666666,
        0x77777777,
        0x88888888,
        0x99999999,
        0xaaaaaaaa
    };
    unsigned int *xp = x;

    unsigned int y[] = {
        0xbbbbbbbb,
        0xcccccccc,
        0xdddddddd,
        0xeeeeeeee,
        0xffffffff,
        0x11111111,
        0x22222222,
        0x33333333,
        0x44444444,
        0x55555555
    };
    unsigned int *yp = y;

    unsigned int z[size];
    unsigned int *zp = z;

    unsigned int vl;

    while(size > 0) {
        asm volatile(
            "vsetvli %0, %1, e32, m2"
            : "=r" (vl)
            : "r" (size)
        );
        size -= vl;

        asm volatile(
            "vle32.v v1, (%0)"
            :
            : "r" (xp)
        );
        xp += vl;

        asm volatile(
            "vle32.v v3, (%0)"
            :
            : "r" (yp)
        );
        yp += vl;

        asm volatile(
            "vadd.vv v5, v3, v1"
        );

        asm volatile(
            "vse32.v v5, (%0)"
            :
            : "r" (zp)
        );
        asm volatile( // Verif
            "vle32.v v7, (%0)"
            :
            : "r" (zp)
        );
    }

    asm volatile("unimp");
    return 0;
}
