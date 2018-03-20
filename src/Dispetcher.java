import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.*;
import java.util.concurrent.CyclicBarrier;


public class Dispetcher extends Agent {
    private Map<Integer, String[]> agentsInfo = new HashMap<>();
    private String[] info = new String[2];
    private boolean allocated = true;
    private String print = "\r\n";
    private static SimpleWeightedGraph<String, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    public static DijkstraShortestPath graphMatrix;
    public static List<CyclicBarrier> b = new ArrayList<>();

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

    private int getInt(AID agent) {
        return Integer.parseInt(agent.getLocalName().replaceAll("[\\D]", ""));
    }

    protected void setup() {
        System.out.println("Dobry vecher, ya dispetcher!");
        Object[] args = getArguments();
        info[0] = "not set";
        info[1] = "";
        SetGraph();
        graphMatrix = new DijkstraShortestPath<>(graph);
        for (int i = 0; i < args.length; i++)
            agentsInfo.put(i, info);
        DFAgentDescription ad = new DFAgentDescription();
        ad.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Tachki");
        sd.setName("TachkiServer");
        ad.addServices(sd);
        try {
            DFService.register(this, ad);
        }

        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        addBehaviour(new WakerBehaviour(this, 10000) {
            @Override
            protected void onWake() {
                addBehaviour(new Cycle(myAgent));
            }
        });
    }

    private class Cycle extends CyclicBehaviour {
        private Agent agent;
        public Cycle(Agent agent) {
            this.agent = agent;
        }
        @Override
        public void action() {
            SequentialBehaviour handleMsg = new SequentialBehaviour(agent);
            final ReceiverBehaviour.Handle handle = ReceiverBehaviour.newHandle();
            handleMsg.addSubBehaviour(new ReceiverBehaviour(agent, handle, 1000));
            handleMsg.addSubBehaviour(new GetMessages(handle));
            addBehaviour(handleMsg);
        }
    }
    private class GetMessages extends OneShotBehaviour {
        private ReceiverBehaviour.Handle handle;
        public GetMessages(ReceiverBehaviour.Handle handle) {
            this.handle = handle;
        }
        @Override
        public void action() {
            try {
                ACLMessage msg = handle.getMessage();
                if (msg.getContent().contains("all")) {
                    try {
                        Thread.sleep(5800);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }

                    for (Map.Entry<Integer, String[]> entry : agentsInfo.entrySet()) {
                        if (entry.getValue()[0].contains("p"))
                            print += "Traveller_" + entry.getKey() + " " + entry.getValue()[0] + " (driver is " + "Traveller_" + entry.getValue()[1] + ")\r\n";
                        else
                            print += "Traveller_" + entry.getKey() + " " + entry.getValue()[0] + "\r\n";
                    }

                        if (allocated) {
                            allocated = false;
                            System.out.println(print);
                            System.out.println("Agents allocated");
                        }


                    }

                else agentsInfo.replace(getInt(msg.getSender()),msg.getContent().split("_"));
            }

            catch (Exception ignored) {
            }
        }
    }
}
