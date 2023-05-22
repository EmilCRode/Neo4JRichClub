package org.Neo4jRichClub;

public class Main {
    public static void main(String[] args) throws Exception {
        Neo4JHandler neo4JHandler = new Neo4JHandler();
        neo4JHandler.writeDirectedRCCoefficientsToCSV(args[0]);
        neo4JHandler.writeInDegreeDistributionToCSV(args[0]);
        neo4JHandler.writeAuthorcitesRCCToCSV(args[0]);
        neo4JHandler.writeAuthorBibCouplingRCCToCSV(args[0]);
    }
}
