import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import java.util.*;
import jade.core.*;

public class SetUp extends Agent {
	private static SimpleWeightedGraph<String, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
	public static FloydWarshallShortestPaths graphMatrix;
	public static Map<String, String> categories = new HashMap<String, String>();
	protected void setup() {
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
		graphMatrix = new FloydWarshallShortestPaths(graph);
		String line = "A F 4\n" +
				"D E 4\n" +
				"A E 4\n" +
				"B G 4\n" +
				"E D 4\n" +
				"A G 4\n";
		String[] data = line.split("\n");
		for (int i = 0; i < data.length; i++) {
			try {
				String[] info = data[i].split(" ");
				AgentController traveler = controller.createNewAgent("Traveller_" + i
						, TravellerAgent.class.getName(), info);
				traveler.start();
				categories.put("Traveller" + i, "traveller");

			}
			catch (ControllerException ex) {
				System.err.println("Exception while adding traveller agent Traveller_" + i);
				ex.printStackTrace();
			}
		}


	}
}
