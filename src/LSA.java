public class LSA {
    private int routerID=-1;
    private NeighborInfo[] neighbors=null;
    private int mailID=-1;

    public LSA(int routerID,NeighborInfo[] neighbors,int mailID) {
        this.routerID=routerID;
        this.neighbors=neighbors;
        this.mailID=mailID;
    }

    public int getSenderID() {
        return routerID;
    }
    public int getMailID() {
        return mailID;
    }

    public NeighborInfo[] getNeighbors() {
        return neighbors;
    }

}

