package org.Neo4jRichClub;

public class Main {
    public static void main(String args[]){
        Neo4JHandler neo4JHandler = new Neo4JHandler();
        neo4JHandler.printDirectedRCCoefficients();
        //neo4JHandler.generateNullModelNetwork(1000);
    }
}
