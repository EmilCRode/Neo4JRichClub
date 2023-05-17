package org.Neo4jRichClub;

import org.neo4j.ogm.model.Node;

public class NetworkNode {
    private final Node ogmNode;
    private final long id;
    private final int inDegree;
    private final int outDegree;
    public NetworkNode(Node node, int inDegree, int outDegree){
        this.ogmNode = node;
        this.id = node.getId();
        this.inDegree = inDegree;
        this.outDegree = outDegree;
    }
    public Object getOgmNode() {
        return ogmNode;
    }
    public long getId() {
        return id;
    }
    public int getInDegree() {
        return inDegree;
    }
    public int getOutDegree(){ return outDegree; }
}
