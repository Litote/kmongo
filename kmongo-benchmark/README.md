To run the benchmark

```bash
mvn clean package
java -jar jackson/target/benchmarks.jar -wi 1 -i 1 -f 1
java -jar native/target/benchmarks.jar -wi 1 -i 1 -f 1
```
