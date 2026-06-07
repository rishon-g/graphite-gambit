# Graphite Gambit 
- Avoid ***erasers***! Watch out for ***white-out*** and ***ink spills***!
- Don't get caught by ***pencil sharpeners***!
- Collect ***graphite***!
- Collect ***plot points*** to create a connect-the-dots shape!
- Navigate to the ***end cell*** after all ***plot points*** are collected to **WIN**!
## Instructions
### Build
1. Clone the repository with:
```bash
git clone https://github.com/rishon-g/graphite-gambit.git
```
2. To package, run:
```bash
mvn clean package
```
3. Run the game using the launcher with
Windows:
```bash
java -jar target/graphite-gambit-1.0.jar
```
MacOS:
```bash
java -XstartOnFirstThread -jar target/graphite-gambit-1.0.jar
```

### Testing
To run all tests, use:
```bash
mvn test
```
To run a test on a specific class, use:
```bash
mvn test -Dtest=className
```
