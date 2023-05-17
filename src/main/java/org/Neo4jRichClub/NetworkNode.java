package org.Neo4jRichClub;

import org.neo4j.ogm.model.Node;

public class NetworkNode {
    private final Node ogmNode;
    private final long id;
    private final int degree;
    public NetworkNode(Node node, int degree){
        this.ogmNode = node;
        this.id = node.getId();
        this.degree = degree;
    }
    public Object getOgmNode() {
        return ogmNode;
    }
    public long getId() {
        return id;
    }
    public int getDegree() {
        return degree;
    }
}
