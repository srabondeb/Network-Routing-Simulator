public class NeighborInfo {
    private int neighborID=-1;
    private int cost=-1;

    public NeighborInfo(int neighborID,int cost){
        this.neighborID=neighborID;
        this.cost=cost;
    }
    public int getNeighborID(){
        return neighborID;
    }
    public int getCost(){
        return cost;
    }
}

