
JAVA NETWORK ROUTING SIMULATOR (DVR vs LSR)

Project Info
- Author: Srabon Debnath & Alex Soto-Yapura
- Course: CS455 (Computer Networks)
- Assignment: Routing Simulator (DVR vs LSR)
- Date: Nov 2025 – Dec 2025
- Language: Java
- Tested With: JDK 25 (IntelliJ IDEA)

1) Project Overview
This project is a Java-based network routing simulator that compares:
- Distance Vector Routing (DVR) / Bellman-Ford style updates
- Link State Routing (LSR) using LSA flooding + Dijkstra shortest path

The simulator runs multiple rounds of message exchange between routers and prints:
- Per-round updates (what changed and why)
- The evolving DV/LSA matrices
- Final routing tables (Destination, Next Hop, Cost)
- A convergence-round comparison between DVR and LSR

2) Key Ideas / Rules
- Routers are labeled: A, B, C, D, E (5 nodes total in the provided graphs).
- Edge weights are positive integers.
- `999` is used as INF (no known path / unreachable at the moment).
- “Round” means one step in the simulator where a node sends/receives and may update its table.

3) Input Files
The simulator reads graph input files such as:
- Graph1.txt
- Graph2.txt

(These files define the network topology and link costs.)

4) How to Run
Option A (IntelliJ):
- Open the project in IntelliJ
- Run the main class: `DVRnLSRMain`

Option B (Command Line idea):
- Compile the project
- Run the main class: `DVRnLSRMain`

Note: The program prints detailed logs to the console.

5) Output
For each graph, the program prints:
- DVR rounds + final DVR routing tables
- LSR rounds + final LSR routing tables
- DVR vs LSR convergence rounds comparison

6) Sample Results (from my runs)
Graph2.txt
- DVR converged in 24 rounds
- LSR converged in 14 rounds
Result: LSR converged faster than DVR on Graph2.

Graph1.txt
- DVR converged in 14 rounds
- LSR converged in 14 rounds
Result: DVR and LSR converged in the same number of rounds on Graph1.

7) Notes / Assumptions
- This project focuses on correctness + step-by-step convergence visibility (verbose logging).
- INF is represented by 999 for readability in printed matrices.

8) (Optional) JavaDoc
Important classes/methods include JavaDoc comments explaining:
- How DV tables are updated (DVR)
- How LSAs are flooded and stored (LSR)
- How Dijkstra is run to compute shortest paths

Author: Srabon Debnath
