package org.Neo4jRichClub;

import org.neo4j.ogm.model.Node;

public class NetworkNode {
    private final long id;
    private final int inDegree;
    private final int outDegree;
    private final int degree;
    public NetworkNode(Node node, int inDegree, int outDegree){
        this.id = node.getId();
        this.inDegree = inDegree;
        this.outDegree = outDegree;
        this.degree = -1;
    }
    public NetworkNode(Node node, int degree){
        this.id = node.getId();
        this.degree = degree;
        this.inDegree = -1;
        this.outDegree = -1;
    }
    public long getId() {
        return this.id;
    }
    public int getInDegree() {
        return this.inDegree;
    }
    public int getOutDegree(){ return this.outDegree; }
    public int getDegree(){ return this.degree; }
}
