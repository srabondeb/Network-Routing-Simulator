
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;

public class LSRNode extends Thread {

    private int routerID;
    private int port;
    private Coordinator track;
    private ServerSocket serverSocket;
    private NeighborInfo[] myNeighbors;
    private int seq;
    private static final Object lock = new Object();

    private boolean notInitialed;

    private static LSA[] LSDB = new LSA[5];
    LinkedList<LSAWrapper> mailbox = new LinkedList<>();

    int[][] matrix = new int[5][5];
    int[][] oldMatrix = new int[5][5];

    LinkedList<String> PathChanges = new LinkedList<>();

    public LSRNode(int routerID, NeighborInfo[] myNeighbors, Coordinator track) {
        this.routerID = routerID;
        this.myNeighbors = myNeighbors;
        this.track = track;
        this.port = 30000 + routerID;
        this.seq = 0;
        this.notInitialed = true;
        int assign = 0;
        for (int i = 0; i < 5; i++) {
            LSDB[i] = null;
            for (int j = 0; j < 5; j++) {

                if (i == j) {
                    assign = 0;
                } else {
                    assign = 999;
                }

                matrix[i][j] = assign;
                oldMatrix[i][j] = assign;
            }

        }

    }

    public boolean DijkstraUpdate() {
        boolean updated = false;
        PathChanges.clear();

        for (int i = 0; i < 5; i++) {
            oldMatrix[i] = Arrays.copyOf(matrix[i], 5);
        }

        // initialization
        if (notInitialed) {
            LSDB[routerID] = new LSA(routerID, myNeighbors, seq++);
            updated = true;
            notInitialed = false;
            floodLSA(LSDB[routerID], -1);
        }

        synchronized (mailbox) {
            while (!mailbox.isEmpty()) {

                LSAWrapper wrap = mailbox.removeFirst();
                LSA mail = wrap.getLSAmessage();
                int immediateSender = wrap.getImidiateSender();

                int originialSenderID = mail.getSenderID();
                int mailID = mail.getMailID();

                track.writeInRecordBook(
                        "Node " + NodeName(routerID) + " received LSA from " + NodeName(originialSenderID));
                track.writeInRecordBook("Updating LSR table at node " + NodeName(routerID));

                if (LSDB[originialSenderID] == null || LSDB[originialSenderID].getMailID() < mailID) {
                    LSDB[originialSenderID] = mail;
                    floodLSA(mail, immediateSender);
                    updated = true;
                }
            }
        }
        if (updated) {
            updateMatrix();
            Dijkstra();
            track.UPRoundIncrement();
        }
        return updated;

    }

    int[] nextHop = new int[5];
    int[] bestCost = new int[5];

    public void Dijkstra() {
        boolean[] visited = new boolean[5];
        for (int i = 0; i < 5; i++) {
            bestCost[i] = 999;
            visited[i] = false;
            nextHop[i] = -1;
        }

        visited[routerID] = true;
        bestCost[routerID] = 0;
        nextHop[routerID] = routerID;

        for (NeighborInfo n : myNeighbors) {
            int id = n.getNeighborID();
            int cost = n.getCost();
            bestCost[id] = cost;
            nextHop[id] = id;
        }

        while (true) {

            int a = -1;
            int minimum = 999;

            for (int k = 0; k < 5; k++) {
                if (!visited[k] && bestCost[k] < minimum) {
                    minimum = bestCost[k];
                    a = k;
                }

            }
            if (a == -1) {
                break;
            }

            visited[a] = true;

            for (int b = 0; b < 5; b++) {
                if (visited[b] || matrix[a][b] == 999) {
                    continue;
                }

                int newCost = bestCost[a] + matrix[a][b];
                if (newCost < bestCost[b]) {
                    int oldCost = bestCost[b];
                    bestCost[b] = newCost;

                    if (a == routerID) {
                        nextHop[b] = b;
                    } else {
                        nextHop[b] = nextHop[a];
                    }

                    String entry = NodeName(b) + ": " + oldCost + "-->" + newCost + " via " + NodeName(nextHop[b]);
                    PathChanges.add(entry);

                }

            }

        }

    }

    public void updateMatrix() {

        int assign = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {

                if (i == j) {
                    assign = 0;
                } else {
                    assign = 999;
                }
                matrix[i][j] = assign;
            }

        }

        for (int i = 0; i < 5; i++) {

            if (LSDB[i] != null) {
                LSA mail = LSDB[i];

                NeighborInfo[] neighbor = mail.getNeighbors();

                for (NeighborInfo n : neighbor) {
                    int id = n.getNeighborID();
                    int cost = n.getCost();
                    matrix[i][id] = cost;
                    matrix[id][i] = cost;
                }

            }

        }

    }

    public void send(int neighborID, LSAWrapper wrap) {
        int neighborPort = 30000 + neighborID;
        try {
            Socket socket = new Socket("localhost", neighborPort);
            OutputStream outStream = socket.getOutputStream();
            ObjectOutputStream oOutStream = new ObjectOutputStream(outStream);
            oOutStream.writeObject(wrap);
            oOutStream.flush();

            oOutStream.close();
            socket.close();
        } catch (Exception e) {
        }
    }

    public void floodLSA(LSA lsaMessage, int senderID) {

        for (NeighborInfo n : myNeighbors) {
            int neighborID = n.getNeighborID();

            if (neighborID == senderID)
                continue;

            track.writeInRecordBook("Sending LSA to node " + NodeName(neighborID));

            LSAWrapper wrap = new LSAWrapper(lsaMessage, routerID);
            send(neighborID, wrap);

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

                LSAWrapper wrap = (LSAWrapper) oInStream.readObject();

                LSA lsaMessage = wrap.getLSAmessage();
                int immediateSender = wrap.getImidiateSender();

                int ID = lsaMessage.getSenderID();
                track.writeInRecordBook("Node " + NodeName(routerID) + " received LSA from " + NodeName(ID));

                synchronized (mailbox) {
                    mailbox.add(wrap);
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

    @Override
    public void run() {
        Thread receiverThread = new Thread(() -> {
            Receive();
        });

        receiverThread.start();
        track.receiverWait();

        while (track.keepRunning()) {
            track.threadWait(routerID);
            boolean updated = DijkstraUpdate();

            track.writeInRecordBook("\n");
            track.writeInRecordBook("Current LSA matrix");

            printDVMatrix(matrix);
            track.writeInRecordBook("\nLast LSA matrix");

            printDVMatrix(oldMatrix);

            if (!updated) {
                track.writeInRecordBook("Updated from last LSA matrix or the same? the same\n\n");

            } else {
                track.writeInRecordBook("Updated from last LSA matrix or the same? updated\n\n");

            }
            if (!PathChanges.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                builder.append("Updated entries at node ").append(NodeName(routerID)).append(":\n");

                for (String updates : PathChanges) {

                    builder.append(updates).append("\n\n");
                }
                track.writeInRecordBook(builder.toString());
            }

            synchronized (track.printLock) {
                track.DisplayRecordBook();
            }

            track.nextThread();

        }
        closeSocket();
        updateMatrix();
        Dijkstra();
        track.printWait(routerID);
        track.printRoutingTable(routerID, bestCost, nextHop);
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

