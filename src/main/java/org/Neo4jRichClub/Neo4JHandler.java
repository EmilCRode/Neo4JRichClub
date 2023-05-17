package org.Neo4jRichClub;

import org.neo4j.ogm.config.*;
import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.util.*;

public class Neo4JHandler {
    private final Session session;
    private List<NetworkEdge> networkEdgesCM;
    private List<NetworkEdge> networkEdgesRaw;
    public Neo4JHandler() {
        ConfigurationSource props = new FileConfigurationSource("neo4j.properties");
        Configuration configuration = new Configuration.Builder(props).build();
        SessionFactory sessionFactory = new SessionFactory(configuration, "org.ScientificWorksRelationshipGraph");
        session = sessionFactory.openSession();
        networkEdgesRaw = new ArrayList<>();
        Iterable<Map<String, Object>> results = session.query("MATCH((a1:Author)-[r:AUTHORCITES]->(a2:Author)) RETURN a1,a1.in_degree, a2, a2.in_degree", new HashMap<>()).queryResults();
        for(Map<String, Object> result: results){
            networkEdgesRaw.add(new NetworkEdge(new NetworkNode((Node) result.get("a1"), ((Long) result.get("a1.in_degree")).intValue() ), new NetworkNode((Node) result.get("a2"), ((Long) result.get("a2.in_degree")).intValue() )));
        }
        networkEdgesCM = cannistraciMuscoloni(networkEdgesRaw);
    }
    public void printDirectedRCCoefficients(){
        int maxDegree = 0;
        Map<Long,Integer> nodes =new HashMap<>();
        for(NetworkEdge edge : networkEdgesRaw){
            nodes.put(edge.getStartNode().getId(), edge.getStartNode().getDegree());
            nodes.put(edge.getEndNode().getId(), edge.getEndNode().getDegree());
            maxDegree = Math.max(edge.getStartNode().getDegree(), maxDegree);
            maxDegree = Math.max((edge.getEndNode().getDegree()), maxDegree);
        }
        for(int k = 0; k < maxDegree; k++){
            System.out.println("InDegree: "+ k + " RC-Coeefficient: " + calculateInDegreeRichClubCoefficient(k,nodes));
        }
    }
    public double calculateInDegreeRichClubCoefficient(int kDegree, Map<Long,Integer> nodes){
        int nrOfEdges = 0;
        int nrOfNodes = 0;
        for(NetworkEdge edge: networkEdgesRaw){
            nrOfEdges += (edge.getStartNode().getDegree() < kDegree || edge.getEndNode().getDegree() < kDegree) ? 0 : 1;
        }
        for(Long id : nodes.keySet()){
            if(nodes.get(id) > kDegree){ nrOfNodes++; }
        }
        return (double) nrOfEdges/(nrOfNodes*(nrOfNodes-1));
    }
    public void generateNullModelNetwork(int iterations){
        int count = 0;
        while(count < iterations){
            NetworkEdge edgeAB = pickByProbability1(networkEdgesCM);
            NetworkEdge edgeCD = pickByProbability2(networkEdgesCM);
            NetworkNode nodeA = edgeAB.getStartNode();
            NetworkNode nodeB = edgeAB.getEndNode();
            NetworkNode nodeC = edgeCD.getStartNode();
            NetworkNode nodeD = edgeCD.getEndNode();
            if(nodeA.getId() != nodeB.getId() && nodeB.getId() != nodeC.getId() && nodeC.getId() != nodeD.getId()){
                if(Math.random() >= 0.5 && !edgeExists(networkEdgesCM, nodeA, nodeD) && !edgeExists(networkEdgesCM, nodeB, nodeC)){
                    edgeAB.setEndNode(nodeD);
                    edgeCD.setStartNode(nodeB);
                    edgeCD.setEndNode(nodeC);
                    count ++;
                } else if(Math.random() < 0.5 && !edgeExists(networkEdgesCM, nodeA, nodeC) && !edgeExists(networkEdgesCM, nodeB, nodeD)) {
                    edgeAB.setEndNode(nodeC);
                    edgeCD.setStartNode(nodeB);
                    edgeCD.setEndNode(nodeD);
                    count ++;
                }
            }
        }
    }
    public NetworkEdge pickByProbability1(List<NetworkEdge> edges){
        // Assuming sum(probabilities) = 1
        double k = Math.random();
        double cumulativeProb = 0.0;
        for(NetworkEdge edge : edges){
            cumulativeProb += edge.getP1();
            if(k <= cumulativeProb){ return edge; }
        }
        return null;
    }
    public NetworkEdge pickByProbability2(List<NetworkEdge> edges){
        // Assuming sum(probabilities) = 1
        double k = Math.random();
        double cumulativeProb = 0.0;
        for(NetworkEdge edge : edges){
            cumulativeProb += edge.getP2();
            if(k <= cumulativeProb){ return edge; }
        }
        return null;
    }
    public List<NetworkEdge> cannistraciMuscoloni(List<NetworkEdge> edges){
        int maxWeight1 = 1;
        int minWeight2 = 1;
        int sumWeight1 = 0;
        int sumWeight2 = 0;
        for(NetworkEdge edge: edges) {
            int weight1 = edge.getStartNode().getDegree() * edge.getEndNode().getDegree();
            maxWeight1 = Math.max(weight1, maxWeight1);
            minWeight2 = Math.min(weight1, minWeight2);
            edge.setWeight1(weight1);
            sumWeight1 += weight1;
        }
        for(NetworkEdge edge : edges) {
            int weight2 = Math.absExact(edge.getWeight1() - maxWeight1 - minWeight2); //check if abs or abs exact
            edge.setWeight2(weight2);
            sumWeight2 += weight2;
        }
        for(NetworkEdge edge: edges){
            edge.setP1((double)(edge.getWeight1()/(double) sumWeight1));
            edge.setP2((double)(edge.getWeight2()/(double) sumWeight2));
        }
        return edges;
    }
    public boolean edgeExists(List<NetworkEdge> edges, NetworkNode startNode, NetworkNode endNode){
        for(NetworkEdge edge: edges){
            if(edge.getStartNode().equals(startNode) && edge.getEndNode().equals(endNode)){
                return true;
            }
        }
        return false;
    }
}