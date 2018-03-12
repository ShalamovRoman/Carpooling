import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.*;


public class Manager extends Agent {
    private Map<Integer, String[]> agentsInfo = new HashMap<>();
    private String[] info = new String[2];
    private boolean allocated = true;
    private String print = "\r\n";

    private int getInt(AID agent) {
        return Integer.parseInt(agent.getLocalName().replaceAll("[\\D]", ""));
    }
    protected void setup() {
        System.out.println("Manager agent is created");
        Object[] args = getArguments();
        info[0] = "not set";
        info[1] = "";
        for (int i = 0; i < args.length; i++) agentsInfo.put(i, info);
        DFAgentDescription ad = new DFAgentDescription();
        ad.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Tachki");
        sd.setName("TachkiServer");
        ad.addServices(sd);

        try {
            DFService.register(this, ad);
        }

        catch (FIPAException fe) {}
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
            handleMsg.addSubBehaviour(new GetMessages(agent,handle));
            addBehaviour(handleMsg);
        }
    }
    private class GetMessages extends OneShotBehaviour {
        private Agent agent;
        private ReceiverBehaviour.Handle handle;
        public GetMessages(Agent agent, ReceiverBehaviour.Handle handle) {
            this.agent = agent;
            this.handle = handle;
        }
        @Override
        public void action() {
            try {
                ACLMessage msg = handle.getMessage();
                if (msg.getContent().contains("yo")) {
                    if (allocated){
                        allocated = false;
                    for (Map.Entry<Integer, String[]> entry : agentsInfo.entrySet()) {
                        if (entry.getValue()[0].contains("p"))
                            print = print + "Traveller_" + entry.getKey()+ " " + entry.getValue()[0] + " (driver is " + "Traveller_" + entry.getValue()[1] + ")\r\n";
                        else
                            print = print + "Traveller_" + entry.getKey() + " " + entry.getValue()[0] + "\r\n";
                    }
                    System.out.println(print);
                    }
                }
                else agentsInfo.replace(getInt(msg.getSender()),msg.getContent().split("_"));
            }
            catch (Exception e) {}
        }
    }
}
