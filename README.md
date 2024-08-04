## DISTRIBUTED MATRIX MULTIPLICATION

## Build

### Pre-requisites 
- cmake >= 3.28.1
- maven >= 3.9.6

```bash
mvn clean install
```

## Run

```bash
java --enable-preview --enable-native-access=ALL-UNNAMED -cp target/matrix-1.0-SNAPSHOT.jar org.distributed.matrix.RunMatrixCalculator
```
