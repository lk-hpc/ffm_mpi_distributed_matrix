class IntMatrixCalculator {

    public:
        IntMatrixCalculator() {}
        int multiply(long addressMatrixA, long byteSizeMatrixA, long addressMatrixB, long byteSizeMatrixB, long addressMatrixResult);
};

extern "C" {

IntMatrixCalculator* newIntMatrixCalculator();

void destroyIntMatrixCalculator(IntMatrixCalculator* handle);

int intMatrixCalculatorMultiply(IntMatrixCalculator* ptr, long addressMatrixA, long byteSizeMatrixA, long addressMatrixB, long byteSizeMatrixB, long addressMatrixResult);
}
