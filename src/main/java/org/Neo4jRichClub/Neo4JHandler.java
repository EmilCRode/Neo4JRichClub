package org.Neo4jRichClub;

import com.opencsv.CSVWriter;
import org.neo4j.ogm.config.*;
import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Neo4JHandler {
    private Session session;
    private long sumWeights1;
    private long sumWeights2;
    private int maxWeight1;
    private int minWeight1;

    public Neo4JHandler() {
        ConfigurationSource props = new FileConfigurationSource("neo4j.properties");
        Configuration configuration = new Configuration.Builder(props).build();
        SessionFactory sessionFactory = new SessionFactory(configuration, "org.ScientificWorksRelationshipGraph");
        this.session = sessionFactory.openSession();

    }
    public void writeInDegreeDistributionToCSV(String directorypath) throws IOException{
        List<NetworkEdge> networkEdgesAuthorCites = new ArrayList<>();
        Iterable<Map<String, Object>> resultsAuthorcites = session.query("MATCH((a1:Author)-[r:AUTHORCITES]->(a2:Author)) RETURN a1,a1.in_degree,a1.out_degree, a2, a2.in_degree, a2.out_degree", new HashMap<>()).queryResults();
        for(Map<String, Object> result: resultsAuthorcites){
            networkEdgesAuthorCites.add(new NetworkEdge(new NetworkNode((Node) result.get("a1"), ((Long) result.get("a1.in_degree")).intValue(), ((Long) result.get("a1.out_degree")).intValue() ), new NetworkNode((Node) result.get("a2"), ((Long) result.get("a2.in_degree")).intValue(), ((Long) result.get("a2.out_degree")).intValue())));
        }
        Map<Long, Integer> nodeInDegreeMap = new HashMap<>();
        int maxInDegree = 0;
        List<String[]> csvLines = new ArrayList<>();
        for(NetworkEdge edge : networkEdgesAuthorCites){
            nodeInDegreeMap.put(edge.getStartNode().getId(), edge.getStartNode().getInDegree());
            maxInDegree = Math.max(edge.getStartNode().getInDegree(), maxInDegree);
            nodeInDegreeMap.put(edge.getEndNode().getId(), edge.getEndNode().getInDegree());
            maxInDegree = Math.max(edge.getEndNode().getInDegree(), maxInDegree);
        }
        int[] degreeDistribution = new int[maxInDegree +1];
        for(int inDegree : nodeInDegreeMap.values()){
            degreeDistribution[inDegree] ++;
        }
        for(int i = 0; i < degreeDistribution.length; i++){
            csvLines.add(new String[]{Integer.toString(i), Integer.toString(degreeDistribution[i])});
        }
        String[] header = new String[]{"indegree", "nrofnodes"};
        String filepath = directorypath + "degreedistribution_citations.csv";
        writeDataToCsv(filepath, header,csvLines);
    }
    public void printDirectedRCCoefficients(){
        List<NetworkEdge> networkEdgesAuthorCites = queryForAuthorCites();
        int maxDegree = 0;
        Map<Long,Integer> nodes =new HashMap<>();
        for(NetworkEdge edge : networkEdgesAuthorCites){
            nodes.put(edge.getStartNode().getId(), edge.getStartNode().getInDegree());
            nodes.put(edge.getEndNode().getId(), edge.getEndNode().getInDegree());
            maxDegree = Math.max(edge.getStartNode().getInDegree(), maxDegree);
            maxDegree = Math.max((edge.getEndNode().getInDegree()), maxDegree);
        }
        for(int k = 0; k < maxDegree; k++){
            double richClubCoefficient = calculateInDegreeRichClubCoefficient(k,nodes, networkEdgesAuthorCites);
            double randomRCC = calculateDirectedRandomRichClubCoefficient(k,nodes, networkEdgesAuthorCites);
            System.out.println("InDegree: "+ k + " RC-Coeefficient: " + richClubCoefficient / randomRCC);
        }
    }
    public void writeDirectedRCCoefficientsToCSV(String directorypath) throws IOException{
        List<NetworkEdge> networkEdgesAuthorCites = queryForAuthorCites();
        int maxDegree = 0;
        Map<Long,Integer> nodes =new HashMap<>();
        List<String[]> lines = new ArrayList<>();
        for(NetworkEdge edge : networkEdgesAuthorCites){
            nodes.put(edge.getStartNode().getId(), edge.getStartNode().getInDegree());
            nodes.put(edge.getEndNode().getId(), edge.getEndNode().getInDegree());
            maxDegree = Math.max(edge.getStartNode().getInDegree(), maxDegree);
            maxDegree = Math.max((edge.getEndNode().getInDegree()), maxDegree);
        }
        for(int k = 0; k < maxDegree; k++){
            double richClubCoefficient = calculateInDegreeRichClubCoefficient(k,nodes,networkEdgesAuthorCites);
            double randomRCC = calculateDirectedRandomRichClubCoefficient(k,nodes, networkEdgesAuthorCites);
            lines.add(new String[]{Integer.toString(k), Double.toString(richClubCoefficient), Double.toString(randomRCC), Double.toString(richClubCoefficient/randomRCC) });
        }
        String filepath = directorypath + "richclub_citations.csv";
        writeDataToCsv(filepath, new String[]{"indegree", "richclub", "randomrichclub", "normalizedrichclub"}, lines);
    }
    private double calculateInDegreeRichClubCoefficient(int kInDegree, Map<Long,Integer> nodes, List<NetworkEdge> networkEdgesAuthorCites){
        int nrOfEdges = 0;
        int nrOfNodes = 0;
        for(NetworkEdge edge: networkEdgesAuthorCites){
            nrOfEdges += (edge.getStartNode().getInDegree() < kInDegree || edge.getEndNode().getInDegree() < kInDegree) ? 0 : 1;
        }
        for(Long id : nodes.keySet()){
            if(nodes.get(id) > kInDegree){ nrOfNodes++; }
        }
        return (double) nrOfEdges/(nrOfNodes*(nrOfNodes-1));
    }
    private double calculateDirectedRandomRichClubCoefficient(int kDegree, Map<Long, Integer> nodes, List<NetworkEdge> networkEdgesAuthorCites){
        int nrOfNodes = 0;
        int nrOfInLinks = 0;
        int nrOfOutLinks = 0;
        for(Long id : nodes.keySet()){
            if(nodes.get(id) > kDegree){ nrOfNodes++; }
        }
        for(NetworkEdge edge: networkEdgesAuthorCites){
            if(edge.getStartNode().getOutDegree() > kDegree){
                nrOfOutLinks ++;
            }
            if(edge.getEndNode().getInDegree() > kDegree){
                nrOfInLinks ++;
            }
        }
        long denominator = (long) networkEdgesAuthorCites.size()* nrOfNodes * (nrOfNodes - 1);
        if(denominator == 0){ return Double.NaN;}
        return (double)(nrOfInLinks * nrOfOutLinks) / denominator;
    }
    public void writeAuthorcitesRCCToCSV(String directorypath)throws IOException{
        List<String[]> csvLines = new ArrayList<>();
        List<NetworkEdge> edgesAuthorCoCitation = queryForAuthorCoCitation();
        int maxDegree = 0;
        Map<Long,Integer> nodes =new HashMap<>();
        for(NetworkEdge edge : edgesAuthorCoCitation){
            nodes.put(edge.getStartNode().getId(), edge.getStartNode().getDegree());
            nodes.put(edge.getEndNode().getId(), edge.getEndNode().getDegree());
            maxDegree = Math.max(edge.getStartNode().getDegree(), maxDegree);
            maxDegree = Math.max((edge.getEndNode().getDegree()), maxDegree);
        }
        List<NetworkEdge> edgesNullModell = generateNullModelNetworkCM(edgesAuthorCoCitation,10*edgesAuthorCoCitation.size());
        for(int k = 0; k < maxDegree; k++){
            double richClubCoefficient = calculateUndirectedRCC(k,nodes,edgesAuthorCoCitation);
            double randomRCC = calculateUndirectedRCC(k,nodes, edgesNullModell);
            csvLines.add(new String[]{Integer.toString(k), Double.toString(richClubCoefficient), Double.toString(randomRCC), Double.toString(richClubCoefficient/randomRCC) });
        }
        String filepath = directorypath + "richclub_authorcocitation.csv";
        String[] header = new String[]{"degree", "richclub", "randomrichclub", "normalizedrichclub"};
        writeDataToCsv(filepath, header, csvLines);
    }
    public void writeAuthorBibCouplingRCCToCSV(String directorypath)throws IOException{
        List<String[]> csvLines = new ArrayList<>();
        List<NetworkEdge> edgesAuthorBibCoupling = queryForAuthorBibCoupling();
        int maxDegree = 0;
        Map<Long,Integer> nodes =new HashMap<>();
        for(NetworkEdge edge : edgesAuthorBibCoupling){
            nodes.put(edge.getStartNode().getId(), edge.getStartNode().getDegree());
            nodes.put(edge.getEndNode().getId(), edge.getEndNode().getDegree());
            maxDegree = Math.max(edge.getStartNode().getDegree(), maxDegree);
            maxDegree = Math.max((edge.getEndNode().getDegree()), maxDegree);
        }
        List<NetworkEdge> edgesNullModell = generateNullModelNetworkCM(edgesAuthorBibCoupling,10*edgesAuthorBibCoupling.size());
        for(int k = 0; k < maxDegree; k++){
            double richClubCoefficient = calculateUndirectedRCC(k,nodes,edgesAuthorBibCoupling);
            double randomRCC = calculateUndirectedRCC(k,nodes, edgesNullModell);
            csvLines.add(new String[]{Integer.toString(k), Double.toString(richClubCoefficient), Double.toString(randomRCC), Double.toString(richClubCoefficient/randomRCC) });
        }
        String filepath = directorypath + "richclub_authorbibcoupling.csv";
        String[] header = new String[]{"degree", "richclub", "randomrichclub", "normalizedrichclub"};
        writeDataToCsv(filepath, header, csvLines);
    }
    private double calculateUndirectedRCC(int kDegree, Map<Long,Integer> nodes, List<NetworkEdge> networkEdgesAuthorCoCites){
        int nrOfEdges = 0;
        int nrOfNodes = 0;
        for(NetworkEdge edge: networkEdgesAuthorCoCites){
            nrOfEdges += (edge.getStartNode().getDegree() < kDegree || edge.getEndNode().getDegree() < kDegree) ? 0 : 1;
        }
        for(int degree : nodes.values()){
            if(degree > kDegree){ nrOfNodes++; }
        }
        return (double) nrOfEdges*2/(nrOfNodes*(nrOfNodes-1));
    }
    private List<NetworkEdge> generateNullModelNetworkCM(List<NetworkEdge> inputEdges,int iterations){
        List<NetworkEdge> edges= new ArrayList<>();
        for(NetworkEdge inputEdge : inputEdges){
            edges.add(inputEdge.clone());
        }
        edges = cannistraciMuscoloni(edges);
        int count = 0;
        while(count < iterations){
            NetworkEdge edgeAB = pickByProbability1(edges);
            NetworkEdge edgeCD = pickByProbability2(edges);
            NetworkNode nodeA = edgeAB.getStartNode();
            NetworkNode nodeB = edgeAB.getEndNode();
            NetworkNode nodeC = edgeCD.getStartNode();
            NetworkNode nodeD = edgeCD.getEndNode();
            if(nodeA.getId() != nodeB.getId() && nodeB.getId() != nodeC.getId() && nodeC.getId() != nodeD.getId()){
                if(Math.random() >= 0.5 && !edgeExists(edges, nodeA, nodeD) && !edgeExists(edges, nodeB, nodeC)){
                    edgeAB.setEndNode(nodeD);

                    edgeCD.setStartNode(nodeB);
                    edgeCD.setEndNode(nodeC);
                    count ++;
                } else if(Math.random() < 0.5 && !edgeExists(edges, nodeA, nodeC) && !edgeExists(edges, nodeB, nodeD)) {
                    edgeAB.setEndNode(nodeC);

                    edgeCD.setStartNode(nodeB);
                    edgeCD.setEndNode(nodeD);
                    count ++;
                }
                edgeAB.calcWeight1();
                edgeAB.calcWeight2(maxWeight1, minWeight1);
                edgeAB.calcProbabilities(sumWeights1, sumWeights2);

                edgeCD.calcWeight1();
                edgeCD.calcWeight2(maxWeight1, minWeight1);
                edgeCD.calcProbabilities(sumWeights1, sumWeights2);
            }
        }
        return edges;
    }
    private List<NetworkEdge> cannistraciMuscoloni(List<NetworkEdge> edges){
        sumWeights1 = 0;
        sumWeights2 = 0;
        maxWeight1 = 0;
        minWeight1 = 0;
        for(NetworkEdge edge: edges) {
            int weight1 = edge.calcWeight1();
            maxWeight1 = Math.max(weight1, maxWeight1);
            minWeight1 = Math.min(weight1, minWeight1);
            sumWeights1 += weight1;
        }
        for(NetworkEdge edge : edges) {
            int weight2 = edge.calcWeight2(maxWeight1, minWeight1);
            sumWeights2 += weight2;
        }
        for(NetworkEdge edge: edges){
            edge.calcProbabilities(sumWeights1, sumWeights2);
        }
        return edges;
    }
    private NetworkEdge pickByProbability1(List<NetworkEdge> edges){
        double sumOfProbalities = 0.0;
        for(NetworkEdge edge : edges)
        {
            sumOfProbalities += edge.getP1();
        }
        double k = Math.random();
        double cumulativeProb = 0.0;
        for(NetworkEdge edge : edges){
            cumulativeProb += edge.getP1()/sumOfProbalities;
            if(k <= cumulativeProb){ return edge; }
        }
        return null;
    }
    private NetworkEdge pickByProbability2(List<NetworkEdge> edges){
        double sumOfProbalities = 0.0;
        for(NetworkEdge edge : edges)
        {
            sumOfProbalities += edge.getP2();
        }
        double k = Math.random();
        double cumulativeProb = 0.0;
        for(NetworkEdge edge : edges){
            cumulativeProb += edge.getP2()/sumOfProbalities;
            if(k <= cumulativeProb){ return edge; }
        }
        return null;
    }
    private boolean edgeExists(List<NetworkEdge> edges, NetworkNode startNode, NetworkNode endNode){
        for(NetworkEdge edge: edges){
            if(edge.getStartNode().equals(startNode) && edge.getEndNode().equals(endNode)){
                return true;
            }
        }
        return false;
    }
    private static void writeDataToCsv(String filePath,String[] header, List<String[]> lines) throws IOException {
        // first create file object for file placed at location
        // specified by filepath
        File file = new File(filePath);
        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(file);

            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputfile);

            // adding header to csv
            writer.writeNext(header);

            // add data to csv
            for(String[] line : lines){
                writer.writeNext(line);
            }

            // closing writer connection
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    private List<NetworkEdge> queryForAuthorCites(){
        List<NetworkEdge> networkEdgesAuthorCites = new ArrayList<>();
        Iterable<Map<String, Object>> resultsAuthorcites = session.query("MATCH((a1:Author)-[r:AUTHORCITES]->(a2:Author)) RETURN a1,a1.in_degree,a1.out_degree, a2, a2.in_degree, a2.out_degree", new HashMap<>()).queryResults();
        for(Map<String, Object> result: resultsAuthorcites){
            networkEdgesAuthorCites.add(new NetworkEdge(new NetworkNode((Node) result.get("a1"), ((Long) result.get("a1.in_degree")).intValue(), ((Long) result.get("a1.out_degree")).intValue() ), new NetworkNode((Node) result.get("a2"), ((Long) result.get("a2.in_degree")).intValue(), ((Long) result.get("a2.out_degree")).intValue())));
        }
        return networkEdgesAuthorCites;
    }
    private List<NetworkEdge> queryForAuthorCoCitation(){
        List<NetworkEdge> networkEdgesAuthorCoCitation = new ArrayList<>();
        // WARNING: This uses the deprecated function id()
        Iterable<Map<String, Object>> resultsAuthorCocitation = session.query("match (a1)-[r:AUTHORCOCITATION]-(a2) where id(a1) < id(a2) return a1, a1.degreeCoCitation, a2, a2.degreeCoCitation", new HashMap<>()).queryResults();
        for(Map<String, Object> result: resultsAuthorCocitation){
            networkEdgesAuthorCoCitation.add(new NetworkEdge(new NetworkNode((Node) result.get("a1"), ((Long) result.get("a1.degreeCoCitation")).intValue() ), new NetworkNode((Node) result.get("a2"), ((Long) result.get("a2.degreeCoCitation")).intValue())));
        }
        return networkEdgesAuthorCoCitation;
    }
    private List<NetworkEdge> queryForAuthorBibCoupling(){
        List<NetworkEdge> networkEdgesAuthorCoCitation = new ArrayList<>();
        // WARNING: This uses the deprecated function id()
        Iterable<Map<String, Object>> resultsAuthorCocitation = session.query("match (a1)-[r:BIBCOUPLINGAUTHOR]-(a2) where id(a1) < id(a2) return a1, a1.degreeBibCoupling, a2, a2.degreeBibCoupling", new HashMap<>()).queryResults();
        for(Map<String, Object> result: resultsAuthorCocitation){
            networkEdgesAuthorCoCitation.add(new NetworkEdge(new NetworkNode((Node) result.get("a1"), ((Long) result.get("a1.degreeBibCoupling")).intValue() ), new NetworkNode((Node) result.get("a2"), ((Long) result.get("a2.degreeBibCoupling")).intValue())));
        }
        return networkEdgesAuthorCoCitation;
    }
}