import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import java.util.*;
import java.util.concurrent.CyclicBarrier;

import jade.core.*;

public class SetUp extends Agent {
    private static SimpleWeightedGraph<String, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    //	public static FloydWarshallShortestPaths graphMatrix;
    public static DijkstraShortestPath graphMatrix;
    //public static Map<AID, String> categories = new HashMap<>();

    public static List<CyclicBarrier> b = new ArrayList<>();
    public static Map<Integer, String> categories = new HashMap<Integer, String>();
    public static int forPrint;
    public static Map<Integer, String> choosenDrivers = new HashMap<Integer, String>();
    public static  Double[] procents;
    protected void setup() {
        forPrint = 0;
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        graph.addVertex("E");
        graph.addVertex("F");
        graph.addVertex("G");
        graph.setEdgeWeight(graph.addEdge("A", "B"), 600);
        graph.setEdgeWeight(graph.addEdge("B", "E"), 600);
        graph.setEdgeWeight(graph.addEdge("E", "G"), 200);
        graph.setEdgeWeight(graph.addEdge("G", "D"), 700);
        graph.setEdgeWeight(graph.addEdge("F", "G"), 200);
        graph.setEdgeWeight(graph.addEdge("C", "F"), 300);
        graph.setEdgeWeight(graph.addEdge("A", "C"), 400);
        graph.setEdgeWeight(graph.addEdge("A", "D"), 300);
        graph.setEdgeWeight(graph.addEdge("C", "D"), 100);
        graph.setEdgeWeight(graph.addEdge("F", "E"), 100);
        PlatformController controller = getContainerController();
        //graphMatrix = new FloydWarshallShortestPaths(graph);
        graphMatrix = new DijkstraShortestPath(graph);
        String line = "A F 4\n" +
                "D E 4\n" +
                "A E 4\n" +
                "B G 4\n" +
                "E D 4\n" +
                "A G 4\n";
        String[] data = line.split("\n");
        for (int i = 1; i <= data.length; i++) {
            b.add(new CyclicBarrier(i));
        }
        procents= new Double[data.length];
        Arrays.fill(procents, 0.6);
        for (int i = 0; i < data.length; i++) {
            try {
                String[] info = data[i].split(" ");
                AgentController traveler = controller.createNewAgent("Traveller_" + i
                        , TravellerAgent.class.getName(), info);
                traveler.start();
                categories.put(i, "not set");

            }
            catch (ControllerException ex) {
                System.err.println("Exception while adding traveller agent Traveller_" + i);
                ex.printStackTrace();
            }
        }


    }
}
