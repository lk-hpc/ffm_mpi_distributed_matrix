#include "MatrixCalculator.h"
#include <mpi.h>
#include <iostream>
#include <vector>

void multiplyMatrices(const std::vector<std::vector<int> > &A, const std::vector<std::vector<int> > &B,
                      std::vector<std::vector<int> > &C, int startRow, int endRow, int BR, int CC) {
    for (int i = startRow; i < endRow; ++i) {
        for (int j = 0; j < CC; ++j) {
            C[i][j] = 0;
            for (int k = 0; k < BR; ++k) {
                C[i][j] += A[i][k] * B[k][j];
            }
        }
    }
}

int IntMatrixCalculator::multiply() {
    MPI_Init(NULL, NULL);

        int rank;
        MPI_Comm_rank(MPI_COMM_WORLD, &rank);
        int world_size;
        MPI_Comm_size(MPI_COMM_WORLD, &world_size);

        int AR = 1; // Number of rows in matrix A
        int AC = 2; // Number of columns in matrix A

        int BR = AC; // Number of rows in matrix B (= Number of columns in matrix A)
        int BC = 3; // Number of columns in matrix B

        int CR = AR; // Number of rows in matrix C (= Number of rows in matrix A)
        int CC = BC; // Number of columns in matrix C (= Number of columns in matrix B)

        std::vector<std::vector<int> > A(AR, std::vector<int>(AC));
        std::vector<std::vector<int> > B(BR, std::vector<int>(BC));
        std::vector<std::vector<int> > C(CR, std::vector<int>(CC));

        // Create matrices A and B with some values only in root rank (rank == 0)
        if (rank == 0) {
            for (int i = 0; i < AR; ++i) {
                for (int j = 0; j < AC; ++j) {
                    A[i][j] = (i + 1) * (j + 1);
                }
            }

            for (int i = 0; i < BR; ++i) {
                for (int j = 0; j < BC; ++j) {
                    B[i][j] = (i + 1) * (j + 1);
                }
            }
        }

        // Broadcast A and B to all ranks (from root rank)
        // ToDo: Send only relavent rows to each rank for calculations (Only Matrix A. B has to be there anyways)
        for (int i = 0; i < AR; ++i) {
            MPI_Bcast(A[i].data(), AC, MPI_INT, 0, MPI_COMM_WORLD);
        }
        for (int i = 0; i < BR; ++i) {
            MPI_Bcast(B[i].data(), BC, MPI_INT, 0, MPI_COMM_WORLD);
        }

        // Print A
        if (rank == 0) {
            std::cout << "Matrix A:\n";
            for (int i = 0; i < AR; ++i) {
                for (int j = 0; j < AC; ++j) {
                    std::cout << A[i][j] << " ";
                }
                std::cout << "\n";
            }
        }

        // Print B
        if (rank == 0) {
            std::cout << "Matrix B:\n";
            for (int i = 0; i < BR; ++i) {
                for (int j = 0; j < BC; ++j) {
                    std::cout << B[i][j] << " ";
                }
                std::cout << "\n";
            }
        }

        // Calculate StartRow and EndRow for each rank
        int rowsPerProcess = AR / world_size;
        int startRow = rank * rowsPerProcess;
        int endRow = (rank == world_size - 1) ? AR : startRow + rowsPerProcess;

        // Do Calculation in each rank
        multiplyMatrices(A, B, C, startRow, endRow, BR, CC);

        // Collect Results in root rank
        if (rank == 0) {
            for (int i = 1; i < world_size; ++i) {
                int start = i * rowsPerProcess;
                int end = (i == world_size - 1) ? CR : start + rowsPerProcess;
                for (int j = start; j < end; ++j) {
                    MPI_Recv(C[j].data(), CC, MPI_INT, i, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
                }
            }
        } else {
            for (int i = startRow; i < endRow; ++i) {
                MPI_Send(C[i].data(), CC, MPI_INT, 0, 0, MPI_COMM_WORLD);
            }
        }

        // Print Results
        if (rank == 0) {
            std::cout << "Result Matrix C:\n";
            for (int i = 0; i < CR; ++i) {
                for (int j = 0; j < CC; ++j) {
                    std::cout << C[i][j] << " ";
                }
                std::cout << "\n";
            }
        }

        MPI_Finalize();

        return 0;
}

IntMatrixCalculator* newIntMatrixCalculator() {
    return new IntMatrixCalculator();
}

void destroyIntMatrixCalculator(IntMatrixCalculator* handle) {
    delete handle;
}

int intMatrixCalculatorMultiply(IntMatrixCalculator* ptr) {
    return ptr->multiply();
}