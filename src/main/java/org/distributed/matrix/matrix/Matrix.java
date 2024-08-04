package org.distributed.matrix.matrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.file.Files;
import java.nio.file.Path;

public class Matrix {

    private final long[][] array;

    private final MemorySegment memorySegment;

    public Matrix(long[][] array, Arena arena) {
        this.array = array;
        this.memorySegment = createMemorySegment(arena);
    }

    public Matrix(Path filePath, Arena arena) {
        this.array = extractCsvToLongArray(filePath);
        this.memorySegment = createMemorySegment(arena);
    }

    public long getAddress() {
        return memorySegment.address();
    }

    public long getByteSize() {
        return memorySegment.byteSize();
    }

    public int getNumberOfRows() {
        return array.length;
    }

    public int getNumberOfColumns() {
        return array[0].length;
    }

    public MemorySegment getMemorySegment() {
        return memorySegment;
    }

    public static MemorySegment createResultMemorySegment(int numberOfRows, int numberOfColumns, Arena arena) {
        long sizeInBytes = numberOfRows * numberOfColumns * ValueLayout.JAVA_LONG.byteSize() + 2 * ValueLayout.JAVA_INT.byteSize();
        MemorySegment segment = arena.allocate(sizeInBytes);

        // Write number of rows and columns in the beginning of the bytearray.
        segment.set(ValueLayout.JAVA_INT, 0, numberOfRows);
        segment.set(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT.byteSize(), numberOfColumns);

        return segment;
    }

    private MemorySegment createMemorySegment(Arena arena) {
        // Calculate total size: (number of rows * size of int) + (number of columns * size of int) + (rows * columns * size of long)
        int rows = array.length;
        int columns = array[0].length;
        long sizeInBytes = rows * columns * ValueLayout.JAVA_LONG.byteSize() + 2 * ValueLayout.JAVA_INT.byteSize();

        // Allocate a memory segment
        MemorySegment segment = arena.allocate(sizeInBytes);

        // Write number of rows and columns in the beginning of the bytearray.
        segment.set(ValueLayout.JAVA_INT, 0, rows);
        segment.set(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT.byteSize(), columns);

        // Write the long[][] array into the memory segment
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                // Calculate the offset for the current element
                long offset = 2 * ValueLayout.JAVA_INT.byteSize() + ((long) i * columns + j) * ValueLayout.JAVA_LONG.byteSize();
                // Set the value at the calculated offset
                segment.set(ValueLayout.JAVA_LONG, offset, array[i][j]);
            }
        }

        return segment;
    }

    private static long[][] extractCsvToLongArray(Path filePath) throws NumberFormatException {
        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            // Read the first line to determine the dimensions of the array
            String line = br.readLine();
            if (line == null || line.isEmpty()) {
                throw new IOException("CSV file is empty or invalid.");
            }

            String[] firstRow = line.split(",");
            int columns = firstRow.length;

            // Validate the first row and convert to long
            long[] tempRow = new long[columns];
            for (int i = 0; i < columns; i++) {
                tempRow[i] = Long.parseLong(firstRow[i].trim());
            }

            // Calculate number of rows
            int rows = 1;
            while ((line = br.readLine()) != null) {
                rows++;
            }

            // Initialize the array
            long[][] data = new long[rows][columns];
            data[0] = tempRow;

            // Rewind the reader to the beginning of the file
            br.close();
            try (BufferedReader br2 = Files.newBufferedReader(filePath)) {
                // Skip the first line as we have already read it
                br2.readLine();
                int rowIndex = 1;
                while ((line = br2.readLine()) != null) {
                    String[] values = line.split(",");
                    if (values.length != columns) {
                        throw new IOException("CSV file is malformed: inconsistent number of columns.");
                    }
                    for (int i = 0; i < columns; i++) {
                        data[rowIndex][i] = Long.parseLong(values[i].trim());
                    }
                    rowIndex++;
                }
            }
            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
