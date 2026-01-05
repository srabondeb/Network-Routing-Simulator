
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;

public class DVRNode extends Thread {

    private int routerID;
    private int port;
    private Coordinator track;
    private ServerSocket serverSocket;
    private NeighborInfo[] myNeighbors;
    boolean InitialedDV = false;
    int[] DVector = new int[5];
    int[] oldDV = new int[5];
    int[][] Matrix = new int[5][5];
    int[][] oldMatrix = new int[5][5];
    LinkedList<int[]> mailbox = new LinkedList<>();
    LinkedList<String> dvChanges = new LinkedList<>();

    public DVRNode(int routerID, NeighborInfo[] myNeighbors, Coordinator track) {
        this.routerID = routerID;
        this.myNeighbors = myNeighbors;
        this.track = track;
        this.port = 20000 + routerID;

        for (int i = 0; i < 5; i++) {
            DVector[i] = 999;
            oldDV[i] = 999;
        }
        DVector[routerID] = 0;

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                Matrix[i][j] = 999;
                oldMatrix[i][j] = 999;
            }
        }
        Matrix[routerID][routerID] = 0;

    }

    public void send(int neighborID, int[] message) {
        int neighborPort = 20000 + neighborID;
        try {
            Socket socket = new Socket("localhost", neighborPort);
            OutputStream outStream = socket.getOutputStream();
            ObjectOutputStream oOutStream = new ObjectOutputStream(outStream);
            oOutStream.writeObject(message);
            oOutStream.flush();

            oOutStream.close();
            socket.close();
        } catch (Exception e) {
        }
    }

    public void sendDV() {
        int[] Mmessage = new int[6];
        Mmessage[0] = routerID;
        for (int i = 0; i < 5; i++) {
            Mmessage[i + 1] = DVector[i];
        }
        for (NeighborInfo info : myNeighbors) {
            int neighborID = info.getNeighborID();

            track.writeInRecordBook("Sending DV to node " + NodeName(neighborID));

            send(neighborID, Mmessage.clone());
        }
    }

    public void Receive() {
        try {
            serverSocket = new ServerSocket(port);
            track.UpdateReceiver();
            while (track.keepRunning() && !serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                InputStream inStream = socket.getInputStream();
                ObjectInputStream oInStream = new ObjectInputStream(inStream);
                int[] message = (int[]) oInStream.readObject();

                track.writeInRecordBook("Node " + NodeName(routerID) + " received DV from " + NodeName(message[0]));

                synchronized (mailbox) {
                    mailbox.add(message);
                }
                oInStream.close();
                socket.close();
            }
            serverSocket.close();
        } catch (Exception e) {
        }
    }

    public void closeSocket() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception e) {
        }
    }

    public boolean bellmanFordUpdate() {
        boolean updated = false;
        dvChanges.clear();
        oldDV = Arrays.copyOf(DVector, 5);
        for (int i = 0; i < 5; i++) {
            oldMatrix[i] = Arrays.copyOf(Matrix[i], 5);
        }
        if (!InitialedDV) {
            InitialedDV = true;
            for (NeighborInfo info : myNeighbors) {
                int n = info.getNeighborID();
                int cost = info.getCost();
                if (cost < oldDV[n]) {
                    String entry = NodeName(n) + ": " + oldDV[n] + "-->" + cost + " via " + NodeName(n);
                    dvChanges.add(entry);
                    DVector[n] = cost;
                    Matrix[n][n] = cost;
                    updated = true;
                }
            }
        }
        synchronized (mailbox) {
            while (!mailbox.isEmpty()) {
                int[] message = mailbox.removeFirst();
                int senderID = message[0];

                track.writeInRecordBook("Node " + NodeName(routerID) + " received DV from " + NodeName(senderID));
                track.writeInRecordBook(" Updating DV table at node " + NodeName(routerID));
                for (int i = 1; i < 6; i++) {
                    int dest = i - 1;
                    if (message[i] == 999 || DVector[senderID] == 999) {
                        continue;
                    }
                    if (dest == routerID) {
                        continue;
                    }
                    int newCost = DVector[senderID] + message[i];
                    int oldCost = Matrix[dest][senderID];
                    if (newCost < oldCost) {
                        Matrix[dest][senderID] = newCost;
                        if (newCost < DVector[dest]) {
                            int oldDVector = DVector[dest];
                            DVector[dest] = newCost;
                            String entry = NodeName(dest) + ": " + oldDVector + "-->" + newCost + " via "
                                    + NodeName(senderID);
                            dvChanges.add(entry);
                            updated = true;
                        }
                    }
                }
            }
        }
        if (updated) {
            track.UPRoundIncrement();
        }
        return updated;
    }

    @Override
    public void run() {
        Thread receiverThread = new Thread(() -> {
            Receive();
        });

        receiverThread.start();
        track.receiverWait();

        while (track.keepRunning()) {

            track.threadWait(routerID);
            boolean updated = bellmanFordUpdate();

            track.writeInRecordBook("\n");

            track.writeInRecordBook("Current DV matrix");

            printDVMatrix(Matrix);
            track.writeInRecordBook("\nLast DV matrix");

            printDVMatrix(oldMatrix);

            if (!updated) {
                track.writeInRecordBook("Updated from last DV matrix or the same? the same\n\n");

            } else {
                track.writeInRecordBook("Updated from last DV matrix or the same? updated\n\n");

            }
            if (!dvChanges.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                builder.append("Updated entries at node ").append(NodeName(routerID)).append(":\n");

                for (String updates : dvChanges) {

                    builder.append(updates).append("\n\n");
                }
                track.writeInRecordBook(builder.toString());
            }

            synchronized (track.printLock) {
                track.DisplayRecordBook();
            }
            if (updated) {
                sendDV();
            }

            track.nextThread();
            ;
        }
        closeSocket();
        track.printWait(routerID);
        track.printRoutingTable(routerID, Matrix, myNeighbors);
        track.donePrinting();
    }

    public String NodeName(int id) {
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

    public void printDVMatrix(int[][] Mat) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {

                builder.append(Mat[i][j] + "\t");
            }

            builder.append("\n");
        }

        builder.append("\n");

        track.writeInRecordBook(builder.toString());
    }

}
