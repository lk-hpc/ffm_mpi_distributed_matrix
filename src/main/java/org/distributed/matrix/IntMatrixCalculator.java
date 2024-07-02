package org.distributed.matrix;

import org.distributed.matrix.matrix.Matrix;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class IntMatrixCalculator implements IIntMatrixCalculator {

    private static final String LIBRARY_REL_PATH = "/src/main/cpp/build/libSimpleLib.so";
    private static final String PROPERTY_KEY = "user.dir";
    public static final String CONSTRUCTOR_NAME = "newIntMatrixCalculator";
    private final Linker linker;
    private final String currentDirectory;

    public IntMatrixCalculator() {
        this.linker = Linker.nativeLinker();
        this.currentDirectory = System.getProperty(PROPERTY_KEY);
    }

    public int calculate() {
        try (Arena arena = Arena.ofConfined()) {

            Matrix matrixA = new Matrix(Path.of("src/main/java/resources/matrixA.csv"), arena);
            Matrix matrixB = new Matrix(Path.of("src/main/java/resources/matrixB.csv"), arena);

            MemorySegment resultMemorySegment = Matrix.createResultMemorySegment(matrixA.getNumberOfRows(), matrixB.getNumberOfColumns(), arena);

            // ToDo : To Delete. This is just for Testing
            readFromMemorySegment(matrixA.getMemorySegment());

            // Obtain an instance of the native linker
            SymbolLookup lookup = SymbolLookup.libraryLookup(Path.of(currentDirectory + LIBRARY_REL_PATH), arena);

            MemorySegment constructorMemorySegment = lookup.find(CONSTRUCTOR_NAME).orElseThrow();
            MemorySegment functionMemorySegment = lookup.find(FUNCTION_NAMES.MULTIPLY.getNativeFuncName()).orElseThrow();

            // Create a description of the C function
            FunctionDescriptor newIntCalculator = FunctionDescriptor.of(ValueLayout.ADDRESS);
            FunctionDescriptor funcDesc = FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_LONG,
                    ValueLayout.JAVA_LONG,
                    ValueLayout.JAVA_LONG,
                    ValueLayout.JAVA_LONG,
                    ValueLayout.JAVA_LONG);

            this.linker.downcallHandle(constructorMemorySegment, newIntCalculator);
            MethodHandle functionHandle = this.linker.downcallHandle(functionMemorySegment, funcDesc);
            return (int) functionHandle.invokeExact(
                    constructorMemorySegment,
                    matrixA.getAddress(),
                    matrixA.getByteSize(),
                    matrixB.getAddress(),
                    matrixB.getByteSize(),
                    resultMemorySegment.address());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    // ToDo : To Delete. This is just for Testing
    private static void readFromMemorySegment(MemorySegment memorySegment) {
        // Reading back the values from the memory segment (for demonstration)
        int numberOfRows = memorySegment.get(ValueLayout.JAVA_INT, 0);
        int numberOfColumns = memorySegment.get(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT.byteSize());

        System.out.println("Number of Rows (" + numberOfRows + ")");
        System.out.println("Number of Columns (" + numberOfColumns + "): ");

        for (int i = 0; i < numberOfRows; i++) {
            for (int j = 0; j < numberOfColumns; j++) {
                long offset = 2 * ValueLayout.JAVA_INT.byteSize() + ((long) i * numberOfColumns + j) * ValueLayout.JAVA_LONG.byteSize();
                long value = memorySegment.get(ValueLayout.JAVA_LONG, offset);
                System.out.println("Value at (" + i + ", " + j + "): " + value);
            }
        }
    }

    @Override
    public int multiply() {
        return calculate();
    }

    @Override
    public void close() {
        // no op
    }
}
