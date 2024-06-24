class IntMatrixCalculator {

    public:
        IntMatrixCalculator() {}
        int multiply();
};

extern "C" {

IntMatrixCalculator* newIntMatrixCalculator();

void destroyIntMatrixCalculator(IntMatrixCalculator* handle);

int intMatrixCalculatorMultiply(IntMatrixCalculator* ptr);
}
