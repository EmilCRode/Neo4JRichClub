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
    public void calcProbabilities(long sumOfWeights1, long sumOfWeights2){
        this.p1 = weight1/(double)sumOfWeights1;
        this.p2 = weight2/(double)sumOfWeights2;
    }
    public int calcWeight1(){
        this.weight1 = this.startNode.getDegree() * this.endNode.getDegree();
        return this.weight1;
    }
    public int calcWeight2(int maxWeight1, int minWeight1){
        this.weight2 = Math.absExact(this.weight1 - maxWeight1 - minWeight1);
        return this.weight2;
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

    public double getP1() {
        return p1;
    }

    public double getP2() {
        return p2;
    }

    public int getWeight1() {
        return weight1;
    }

    public void setWeight1(int weight1) {
        this.weight1 = weight1;
    }

    public int getWeight2() {
        return weight2;
    }

    public void setWeight2(int weight2) {
        this.weight2 = weight2;
    }

    public void setP1(double p1) {
        this.p1 = p1;
    }

    public void setP2(double p2) {
        this.p2 = p2;
    }

    public NetworkEdge clone(){
        NetworkEdge clone = new NetworkEdge(this.startNode, this.endNode);
        clone.setWeight1(this.weight1);
        clone.setWeight2(this.weight2);
        clone.setP1(this.p1);
        clone.setP2(this.p2);
        return clone;
    }
}
