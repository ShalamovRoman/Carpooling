import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;

public class SetUp extends Agent {

    protected void setup() {
        PlatformController controller = getContainerController();
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
        try {
            AgentController dispetcher = controller.createNewAgent("Dispetcher", Dispetcher.class.getName(), new String[AgentsInfo.length]);
            dispetcher.start();
        }
        catch (ControllerException ex) {
            ex.printStackTrace();
        }
        try
        {
            Thread.sleep(1000);
        }
        catch(InterruptedException ex) {
            ex.printStackTrace();
        }
        for (int i = 0; i < AgentsInfo.length; i++) {
            try {
                String[] info = (AgentsInfo[i]).split(" ");
                AgentController traveler = controller.createNewAgent("Traveller_" + i
                        , TravellerAgent.class.getName(), info);
                traveler.start();

            }
            catch (ControllerException ex) {
                ex.printStackTrace();
            }
        }
        doDelete();
    }
}
