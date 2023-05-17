package org.Neo4jRichClub;
public class NetworkEdge {
    private NetworkNode startNode;
    private NetworkNode endNode;
    private int weight1;
    private int weight2;
    private double p1;
    private double p2;
    public NetworkEdge(NetworkNode startNode, NetworkNode endNode){
        this.startNode = startNode;
        this.endNode = endNode;
    }
    public NetworkNode getStartNode() {
        return startNode;
    }

    public void setStartNode(NetworkNode startNode) {
        this.startNode = startNode;
    }

    public NetworkNode getEndNode() {
        return endNode;
    }

    public void setEndNode(NetworkNode endNode) {
        this.endNode = endNode;
    }

    public int getWeight1() {
        return weight1;
    }

    public void setWeight1(int weight) {
        this.weight1 = weight;
    }

    public int getWeight2() {
        return weight2;
    }

    public void setWeight2(int weight2) {
        this.weight2 = weight2;
    }

    public double getP1() {
        return p1;
    }

    public void setP1(double p1) {
        this.p1 = p1;
    }

    public double getP2() {
        return p2;
    }

    public void setP2(double p2) {
        this.p2 = p2;
    }
}
