package org.distributed.matrix;


import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;
import java.util.Optional;

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

    private int calculate(FUNCTION_NAMES funcName) {
        try (Arena arena = Arena.ofConfined()) {
            // Obtain an instance of the native linker
            SymbolLookup lookup = SymbolLookup.libraryLookup(Path.of(currentDirectory + LIBRARY_REL_PATH), arena);

            Optional<MemorySegment> constructorAddr = lookup.find(CONSTRUCTOR_NAME);
            Optional<MemorySegment> funcAddr = lookup.find(funcName.getNativeFuncName());

            // Create a description of the C function
            FunctionDescriptor newIntCalculator = FunctionDescriptor.of(ValueLayout.ADDRESS);
            FunctionDescriptor funcDesc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS);

            MemorySegment constructor = constructorAddr.orElseThrow();
            this.linker.downcallHandle(constructor, newIntCalculator);

            return invokeHandle(funcAddr.orElseThrow(), constructor, funcDesc);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private int invokeHandle(MemorySegment funcAddr, MemorySegment constructorInstance, FunctionDescriptor desc) throws Throwable {
        MethodHandle functionHandle = this.linker.downcallHandle(funcAddr, desc);
        return (int) functionHandle.invokeExact(constructorInstance);
    }

    @Override
    public int multiply() {
        return calculate(FUNCTION_NAMES.MULTIPLY);
    }

    @Override
    public void close() {
        // no op
    }
}
