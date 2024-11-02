# Simple Lottery System

This project is to showcase a simple application that reads big data from a file.

## Use Case
The use case is based on the Hungarian lottery system where one can pick 5 numbers. Those numbers then will be matched with the organizers match. The winner is the one who can match 2, 3, 4, 5 number.

For example, the organizer picks: {1, 2, 3, 4 ,5}.
- Player A picks: {1, 10, 2, 50, 90}. Player A will win 2 numbers because he can match 1 and 2.
- Player B picks: {1, 2, 3, 4, 5}. Player B will win 5 numbers because he can match all numbers.
- Player C picks: {1, 2, 3, 4, 6}. Player C will win 4 numbers because he can match 1, 2, 3, 4.

## Challenges
You are given a file that contains 10M rows of data. Each row contains 5 numbers, which represents one player's choice. Build an application that can read the file and then match the numbers with the organizer's numbers in a very low latency, possibly under ~100ms.

## Dependencies
1. Maven
2. Java 21 (preferably GraalVM)
3. Docker (optional)

## Approaches
There are 2 approaches on this project:
1. Using basic/standard Java library. [link](src/main/java/dev/andreasarf/lottery/basic/README.md)
2. Using experimental Java library, ie Memory Segment & Arena. [link](src/main/java/dev/andreasarf/lottery/experimental/README.md)

## How to run
### Using Local Machine
1. Install Maven and Java 21 (preferably GraalVM)
2. Run the following command to build the project:
    ```bash
    make build-mvn
    ```
3. After that can run following command to run the app:
   - Basic approach
     ```bash
     make exec-basic
     ```
   - Experimental approach
     ```bash
     make exec-exp
     ```
### Using Docker
1. Install Docker and Make
2. Run the following command to build the project:
    ```bash
    make build-mvn-docker
    ```
3. After that can run following command to run the app:
   - Basic approach
     ```bash
     make exec-basic-docker
     ```
   - Experimental approach
     ```bash
      make exec-exp-docker
     ```

### Note
1. Number of workers can be adjusted in the code file: `numOfWorkers`. By default, it's set to number of processor.
2. The set of numbers can be adjusted in the code file: `set`.
3. The file path can be adjusted in the code file: `FILE`.
   - it's not provided in this project. can be created by your own.
   - the format
     ```
     16 51 74 35 75
     17 11 60 63 49
     80 17 51 75 37
     19 11 82 44 26
     52 10 32 31 27
     48 69 6 57 65
     8 74 59 81 71
     ```

---
&copy; 2024 Andreas Arifin
