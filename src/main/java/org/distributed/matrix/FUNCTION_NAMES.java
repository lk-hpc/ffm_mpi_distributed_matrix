package org.distributed.matrix;

public enum FUNCTION_NAMES {
    MULTIPLY("intMatrixCalculatorMultiply");

    private final String nativeFuncName;

    FUNCTION_NAMES(String nativeFuncName) {
        this.nativeFuncName = nativeFuncName;
    }

    public String getNativeFuncName() {
        return this.nativeFuncName;
    }
}