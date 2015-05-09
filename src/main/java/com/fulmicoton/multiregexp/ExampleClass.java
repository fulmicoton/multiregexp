package com.fulmicoton.multiregexp;

import org.objectweb.asm.Opcodes;


public class ExampleClass implements CodePointGetter {


    public final int getCodePoint(char a) {
        if (a < 3) {
            return 0;
        }
        else if (a > 10) {
            return 1;
        }
        return 2;
    }

}
