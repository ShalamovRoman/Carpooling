import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import java.util.*;
import java.util.concurrent.CyclicBarrier;

public class SetUp extends Agent {

    private static SimpleWeightedGraph<String, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    public static DijkstraShortestPath graphMatrix;
    public static List<CyclicBarrier> b = new ArrayList<>();
   // public static Map<Integer, String> categories = new HashMap< >();
    public static int forPrint;
    public static Map<Integer, String> chooseDrivers = new HashMap<>();
    public static  Double[] percents;

    private void SetGraph() {
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
    }
    protected void setup() {

        forPrint = 0;
        PlatformController controller = getContainerController();

        SetGraph();
        graphMatrix = new DijkstraShortestPath<>(graph);

        String InputLine = "A F 4\n" +
                "D E 4\n" +
                "A E 4\n" +
                "B G 4\n" +
                "E D 4\n" +
                "A G 4\n" +
                "A F 4\n" +
                "G B 4\n" +
                "C D 4\n" +
                "A B 4\n" +
                "A F 4\n";

        String[] AgentsInfo = InputLine.split("\n");
        for (int i = 1; i <= AgentsInfo.length; i++) {
            b.add(new CyclicBarrier(i));
        }
        percents = new Double[AgentsInfo.length];
        Arrays.fill(percents, 0.6);
        for (int i = 0; i < AgentsInfo.length; i++) {
            try {
                String[] info = (AgentsInfo[i]).split(" ");
                AgentController traveler = controller.createNewAgent("Traveller_" + i
                        , TravellerAgent.class.getName(), info);;
                traveler.start();

            }
            catch (ControllerException ex) {
                System.err.println("Exception while adding traveller agent Traveller_" + i);
                ex.printStackTrace();
            }
        }


    }
}
