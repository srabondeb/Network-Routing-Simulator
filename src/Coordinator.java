import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Coordinator {

    private int PrintID = 0;
    private int Turn = 0;
    private int UPRound = 0;
    private boolean notConverged = true;
    private int receiver = 0;
    private int round = 0;
    public final Object printLock = new Object();

    private int finalround = 0;
    private PrintWriter writer;

    public List<String> updateRecords = Collections.synchronizedList(new ArrayList<>());

    public Coordinator() {
        try {
            writer = new PrintWriter(new FileWriter("OUTPUT.txt", true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void writeInRecordBook(String record) {
        updateRecords.add(record);

    }

    public synchronized void DisplayRecordBook() {
        round++;
        System.out.println("\nRound " + round + ": " + findNodeName(Turn));
        writer.println("\nRound " + round + ": " + findNodeName(Turn));
        writer.flush();

        for (String line : updateRecords) {
            System.out.println(line);
            writer.println(line);
            writer.flush();
        }
        updateRecords.clear();
    }

    public synchronized void UpdateReceiver() {
        receiver++;
        if (receiver == 5) {
            notifyAll();
        }
    }

    public synchronized void receiverWait() {
        while (receiver < 5) {
            try {
                wait();
            } catch (Exception e) {
            }
        }

    }

    public int getRound() {
        return finalround;
    }

    public synchronized void UPRoundIncrement() {
        UPRound++;
    }

    public synchronized void resetUPRound() {
        UPRound = 0;
    }

    public synchronized boolean keepRunning() {
        return notConverged;
    }

    public synchronized void stopThreads() {
        notConverged = false;
        notifyAll();
    }

    public void threadWait(int routerID) {
        String message = null;
        synchronized (this) {
            while (routerID != Turn && notConverged) {
                try {
                    wait();

                } catch (Exception e) {
                }
            }
            // message = "\nRound " + round + ": " + findNodeName(Turn);
            // round++;
        }
        // System.out.println(message);
    }

    public synchronized int nextThread() {
        Turn++;
        if (Turn == 5) {
            Turn = 0;

            if (UPRound == 0) {
                notConverged = false;
            } else {
                UPRound = 0;
            }

        }
        notifyAll();
        return Turn;
    }

    public synchronized void printWait(int routerID) {
        while (routerID != PrintID) {
            try {
                wait();
            } catch (Exception e) {
            }
        }
    }

    public synchronized void donePrinting() {
        PrintID++;
        notifyAll();

        if (PrintID == 5) {
            System.out.println("\nTotal Round : " + round);
            writer.println("\nTotal Round : " + round);
            writer.flush();
            finalround = round;
        }
    }

    public synchronized void printRoutingTable(int routerID, int[][] Matrix, NeighborInfo[] myNeighbors) {
        String nodeName = findNodeName(routerID);
        System.out.println("Node " + nodeName + " Routing Table:");
        writer.println("Node " + nodeName + " Routing Table:\n");
        writer.flush();

        System.out.println();

        writer.println("Destination | NextHop | Cost\n");
        writer.flush();
        System.out.println("Destination | NextHop | Cost");

        for (int i = 0; i < 5; i++) {

            nodeName = findNodeName(i);
            System.out.print(nodeName + "           | ");
            writer.print(nodeName + "           | ");
            writer.flush();

            int bestHop = 0;
            int bestCost = 999;

            if (routerID == i) {
                bestCost = Matrix[i][i];
                ;
                bestHop = i;
            } else {
                for (int j = 0; j < myNeighbors.length; j++) {
                    int n = myNeighbors[j].getNeighborID();
                    if (Matrix[i][n] < bestCost) {
                        bestCost = Matrix[i][n];
                        bestHop = n;
                    }

                }

            }

            System.out.print(findNodeName(bestHop) + "       | ");
            writer.print(findNodeName(bestHop) + "       | ");
            writer.flush();

            System.out.println(bestCost);
            writer.println(bestCost);
            writer.flush();

        }

        System.out.println();
        writer.println();
        writer.flush();
    }

    public synchronized void printRoutingTable(int routerID, int[] bestCost, int[] nextHop) {
        String nodeName = findNodeName(routerID);
        System.out.println("Node " + nodeName + " Routing Table:");

        writer.println("Node " + nodeName + " Routing Table:");
        writer.flush();

        System.out.println();

        writer.println();
        writer.flush();

        System.out.println("Destination | NextHop | Cost");

        writer.println("Destination | NextHop | Cost");
        writer.flush();

        for (int i = 0; i < 5; i++) {

            if (nextHop[i] == -1) {
                System.out.println(findNodeName(i) + "           | -       | " + bestCost[i]);

                writer.println(findNodeName(i) + "           | -       | " + bestCost[i]);
                writer.flush();

            } else {
                System.out.println(findNodeName(i) + "           | " + nextHop[i] + "       | " + bestCost[i]);
                writer.println(findNodeName(i) + "           | " + nextHop[i] + "       | " + bestCost[i]);
                writer.flush();
            }
        }

        System.out.println();
        writer.println();
        writer.flush();
    }

    public String findNodeName(int id) {
        String nodeName = "";
        if (id == 0) {
            nodeName = "A";
        }
        if (id == 1) {
            nodeName = "B";
        }
        if (id == 2) {
            nodeName = "C";
        }
        if (id == 3) {
            nodeName = "D";
        }
        if (id == 4) {
            nodeName = "E";
        }

        return nodeName;

    }
}

