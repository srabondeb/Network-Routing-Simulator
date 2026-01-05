
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class DVRnLSRMain {
    public static void main(String[] args) throws FileNotFoundException {
        clearOutputFile();
        network_init("Graph2.txt");
        network_init("Graph1.txt");
    }

    public static void network_init(String fileName) {

        PrintWriter writerTWo = null;
        try {
            writerTWo = new PrintWriter(new FileWriter("OUTPUT.txt", true));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int[][] matrix = null;
        File file = new File(fileName);
        Scanner scaned = null;

        if (!file.exists()) {
            System.out.println("The network.txt file does not exist please include the file before running");
            writerTWo.println("The network.txt file does not exist please include the file before running");
            writerTWo.flush();
            return;
        }

        try {
            scaned = new Scanner(file);
            matrix = new int[5][5];
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {

                    matrix[i][j] = scaned.nextInt();
                }
            }
            scaned.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        int A = 0, B = 1, C = 2, D = 3, E = 4;

        Coordinator DVRtrack = new Coordinator();
        Coordinator LSRtrack = new Coordinator();

        int totalDVRround = 0;

        System.out.println("\n:::::  Running DSR  for "+fileName+":::::");
        writerTWo.println("\n\n\n:::::  Running DSR  for "+fileName+":::::");
        writerTWo.flush();
        DVRNode NodeA = new DVRNode(A, getNeighbors(matrix, A), DVRtrack);
        DVRNode NodeB = new DVRNode(B, getNeighbors(matrix, B), DVRtrack);
        DVRNode NodeC = new DVRNode(C, getNeighbors(matrix, C), DVRtrack);
        DVRNode NodeD = new DVRNode(D, getNeighbors(matrix, D), DVRtrack);
        DVRNode NodeE = new DVRNode(E, getNeighbors(matrix, E), DVRtrack);

        NodeA.start();
        NodeB.start();
        NodeC.start();
        NodeD.start();
        NodeE.start();

        try {
            NodeA.join();
            NodeB.join();
            NodeC.join();
            NodeD.join();
            NodeE.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        totalDVRround = DVRtrack.getRound();

        System.out.println("\n\n\n:::::  Running LSR for "+fileName+"  :::::");
        writerTWo.println("\n\n\n:::::  Running LSR  for "+fileName+":::::");
        writerTWo.flush();

        LSRNode LNodeA = new LSRNode(A, getNeighbors(matrix, A), LSRtrack);
        LSRNode LNodeB = new LSRNode(B, getNeighbors(matrix, B), LSRtrack);
        LSRNode LNodeC = new LSRNode(C, getNeighbors(matrix, C), LSRtrack);
        LSRNode LNodeD = new LSRNode(D, getNeighbors(matrix, D), LSRtrack);
        LSRNode LNodeE = new LSRNode(E, getNeighbors(matrix, E), LSRtrack);

        LNodeA.start();
        LNodeB.start();
        LNodeC.start();
        LNodeD.start();
        LNodeE.start();

        try {
            LNodeA.join();
            LNodeB.join();
            LNodeC.join();
            LNodeD.join();
            LNodeE.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int totalLSRround = LSRtrack.getRound();

        System.out.println();
        writerTWo.println();
        writerTWo.flush();

        System.out.println("DVR vs LSR round Comparison for : "+ fileName);
        writerTWo.println("DVR vs LSR round Comparison for : " +fileName);
        writerTWo.flush();

        System.out.println();
        writerTWo.println();
        writerTWo.flush();

        System.out.println("DVR for "+fileName +" : "+ totalDVRround + " rounds");
        writerTWo.println("DVR for "+fileName +" : "+ totalDVRround + " rounds");
        writerTWo.flush();

        System.out.println("LSR for "+fileName +" : "+ totalLSRround + " rounds");
        writerTWo.println("LSR for "+fileName +" : "+ totalLSRround + " rounds");
        writerTWo.flush();

        System.out.print("\nResult: ");
        writerTWo.println("\nResult: ");
        writerTWo.flush();

        if (totalLSRround < totalDVRround) {
            System.out.println("LSR faster convergence and efficency  Then DVR");
            writerTWo.println("LSR faster convergence and efficency  Then DVR");
            writerTWo.flush();

        } else {
            if(totalLSRround > totalDVRround) {
                System.out.println("DVR has faster convergence and efficency Then LSR");
                writerTWo.println("DVR has faster convergence and efficency Then LSR");
                writerTWo.flush();
            }else {
                System.out.println("DVR and LSR both has same efficiency");
                writerTWo.println("DVR has faster convergence and efficency Then LSR");
                writerTWo.flush();
            }
        }
        writerTWo.close();

    }

    public static void clearOutputFile() {
        PrintWriter writer;
        try {
            writer = new PrintWriter(new FileWriter("OUTPUT.txt", false));
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static NeighborInfo[] getNeighbors(int[][] matrix, int routerID) {
        int size = 0;
        for (int i = 0; i < 5; i++) {
            if (matrix[routerID][i] != 0) {
                size++;
            }
        }
        NeighborInfo[] myNeighbors = new NeighborInfo[size];
        NeighborInfo temp = null;
        int j = 0;
        for (int k = 0; k < 5; k++) {
            if (matrix[routerID][k] != 0) {
                temp = new NeighborInfo(k, matrix[routerID][k]);
                myNeighbors[j] = temp;
                j++;
            }
        }
        return myNeighbors;
    }

}


