import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.lang.*;
import java.util.*;

public class TravellerAgent extends Agent {

	private double dist;
	private String from;
	private String to;
	private int seats;
	private int cnt = 0;
	private AID possiblePass;
	private AID possibleDriver;
	private AID choosenDriver;
	private double bestPrice;
	private HashSet<AID> drivers;
	private List<String> way = new ArrayList<>();
	private List<String> driversWay = new ArrayList<>();
	private double passPrice;
	private double driverPrice = Double.POSITIVE_INFINITY;
	private boolean aloneFlag = false;
	private double percent = 0.6;
	private DFAgentDescription agentDescription = new DFAgentDescription();
	private ServiceDescription serviceDescription = new ServiceDescription();
	private ServiceDescription categoryService = new ServiceDescription();
	private ServiceDescription possibleDriversService = new ServiceDescription();
	private Map<AID, String> categories = new HashMap< >();
	private AID dispetcher;

	private int getInt(AID agent) {
		return Integer.parseInt(agent.getLocalName().replaceAll("[\\D]", ""));
	}

	private double getDist2(List<String> arr){
		double sum = 0;
		for (int i = 0; i < arr.size() - 1; i++)
		    sum += Dispetcher.graphMatrix.getPath(arr.get(i), arr.get(i + 1)).getWeight();
		return sum;
	}

	private boolean canBeDriver() {
        return (percent > 0.2 && seats > 0);
    }

	private boolean canBePass(Agent agent) {
		boolean res = false;
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("can be driver");
		sd.setName("yes");
		template.addServices(sd);
		try {
			DFAgentDescription[] agents = DFService.search(agent, template);
            for (DFAgentDescription el : agents) {
                if (el.getName() != agent.getAID()) res = true;
            }
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		return res;
	}

	private void setCategory(Agent agent, AID aid, String type){
		DFAgentDescription ad = new DFAgentDescription();
		ad.setName(aid);
		ad.addServices(serviceDescription);
		ad.addServices(possibleDriversService);
		categoryService.setName(type);
		ad.addServices(categoryService);
		try {
			DFService.modify(agent,ad);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
	private String search (Agent agent, AID aid, String type){
		String c = "not found";
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("category");
		sd.setName(type);
		template.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(agent, template);
            for (DFAgentDescription el : result) {
                if (el.getName().equals(aid)) c = type;
            }
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		return c;

	}
	private String getCategory(Agent agent, AID aid) {

        String c = search(agent,aid,"not set");
		if (Objects.equals(c, "not found")) c = search(agent,aid,"driver");
		if (Objects.equals(c, "not found")) c = search(agent,aid,"tmp driver");
		if (Objects.equals(c, "not found")) c = search(agent,aid,"passenger");
		if (Objects.equals(c, "not found")) c = search(agent,aid,"alone");
		return c;
	}
	private void collectCategories(Agent agent) {
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("category");
		template.addServices(sd);
		try {
			DFAgentDescription[] agents = DFService.search(agent, template);
			for (DFAgentDescription a : agents) {
				if (categories.containsKey(a)) categories.replace(a.getName(),getCategory(agent,a.getName()));
				else categories.put(a.getName(),getCategory(agent,a.getName()));
			}
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

	}
	private void sendMsgToDispetcher(Agent agent, String info) {
		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
		msg.setContent(info);
		msg.setConversationId("Dispetcher");
		msg.addReceiver(dispetcher);
		agent.send(msg);
	}

	protected void setup() {
		Object[] args = getArguments();
		from = (String) args[0];
		to = (String) args[1];
		way.add(from);
		way.add(to);
		seats = Integer.parseInt((String)args[2]);
		dist = Dispetcher.graphMatrix.getPath(from, to).getWeight();
		System.out.println(getAID().getLocalName() + ": from " + from + " to " + to + ", " + seats + " free seats, dist = " + dist);

		agentDescription.setName(getAID());
		serviceDescription.setType("Tachki");
		serviceDescription.setName("TachkiServer");
		categoryService.setType("category");
		categoryService.setName("not set");
		possibleDriversService.setType("can be driver");
		possibleDriversService.setName("yes");
		agentDescription.addServices(categoryService);
		agentDescription.addServices(possibleDriversService);
		agentDescription.addServices(serviceDescription);

		try {
			DFService.register(this, agentDescription);
		}

		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Tachki");
		template.addServices(sd);
		try {
			DFAgentDescription[] agents = DFService.search(this, template);
			for (DFAgentDescription a : agents) {
				if (a.getName().getLocalName().contains("Di")) dispetcher = a.getName();
			}
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

		addBehaviour(new WakerBehaviour(this, 10000) {
			@Override
			protected void onWake() {
				addBehaviour(new LifeCycle(myAgent));
			}
		});
	}

	private class LifeCycle extends OneShotBehaviour {
		private Agent agent;
		LifeCycle(Agent agent) {
			this.agent = agent;
		}
		@Override
		public void action() {
			addBehaviour(new StartCycle(agent));
		}
	}
	// starts agent behaviour depending on all agents categories
	class StartCycle extends OneShotBehaviour {
		private Agent agent;

		StartCycle(Agent agent) {
			super(agent);
			this.agent = agent;
		}
		@Override
		public void action() {
			if (!aloneFlag && cnt > 0 && !Objects.equals(getCategory(agent, agent.getAID()), "driver")){
				setCategory(agent,agent.getAID(),"alone");
				System.out.println(agent.getLocalName() + " goes alone");
			}
			aloneFlag = false;
			collectCategories(agent);
			if (getCategory(agent,agent.getAID()).contains("tmp")) setCategory(agent,agent.getAID(),"not set");
			if (choosenDriver != null)
				sendMsgToDispetcher(agent, getCategory(agent,agent.getAID()) + "_" + getInt(choosenDriver));
			else
				sendMsgToDispetcher(agent, getCategory(agent,agent.getAID()) + "_ ");
			choosenDriver = null;
			possiblePass = null;
			possibleDriver = null;
			passPrice = Double.POSITIVE_INFINITY;
			bestPrice = Double.NEGATIVE_INFINITY;
			drivers = new HashSet<>();

			DFAgentDescription template = new DFAgentDescription();
			template.addServices(serviceDescription);
			try {
				DFAgentDescription[] agents = DFService.search(agent, template);
				for (DFAgentDescription a : agents) {
					if (!(a.getName().equals(agent.getAID())) && (Objects.equals(getCategory(agent, a.getName()), "not set") || Objects.equals(getCategory(agent, a.getName()), "driver")))
						drivers.add(a.getName());
				}
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}

			SequentialBehaviour sb = new SequentialBehaviour(agent);
			ParallelBehaviour pb = new ParallelBehaviour();

			if (Objects.equals(getCategory(agent, agent.getAID()), "not set") && canBePass(agent) && canBeDriver()) {
				sb.addSubBehaviour(new SendData(agent));
				pb.addSubBehaviour(new DriverBehaviour(agent));
				pb.addSubBehaviour(new PassengerBehaviour(agent));
				sb.addSubBehaviour(pb);
				addBehaviour(sb);
			}
			else if (Objects.equals(getCategory(agent, agent.getAID()), "not set") && canBePass(agent)) {
				sb.addSubBehaviour(new SendData(agent));
				pb.addSubBehaviour(new PassengerBehaviour(agent));
				sb.addSubBehaviour(pb);
				addBehaviour(sb);
			}

			else if (Objects.equals(getCategory(agent, agent.getAID()), "driver") && canBeDriver()) {
				sb.addSubBehaviour(new SendData(agent));
				pb.addSubBehaviour(new DriverBehaviour(agent));
				sb.addSubBehaviour(pb);

				addBehaviour(sb);
			}
			cnt++;
			collectCategories(agent);
			if (!categories.values().contains("not set"))
				sendMsgToDispetcher(agent, "allocated");
			if (!Objects.equals(getCategory(agent, agent.getAID()), "alone") && !Objects.equals(getCategory(agent, agent.getAID()), "passenger") && categories.values().contains("not set"))
				addBehaviour(new Restart(agent, 10000));
		}
	}
	// sending agent info to other agents
	private class SendData extends OneShotBehaviour{
		private Agent agent;

		SendData(Agent agent) {
			this.agent = agent;
		}
		@Override
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.CFP);
			msg.removeReceiver(getAID());
			msg.setContent(from + " " + to + " " + dist);
			msg.setConversationId("SendData");
			for (AID dr : drivers) {
				if (!Objects.equals(getCategory(agent, dr), "driver") || !Objects.equals(getCategory(agent, agent.getAID()), "driver"))
					msg.addReceiver(dr);
			}
			agent.send(msg);
		}
	}

	private class DriverBehaviour extends OneShotBehaviour {

		private Agent agent;

		DriverBehaviour(Agent agent) {
			this.agent = agent;
		}
		@Override
		public void action() {
			SequentialBehaviour sb = new SequentialBehaviour(agent);
			ParallelBehaviour pb = new ParallelBehaviour();
			for (AID dr : drivers) {
				SequentialBehaviour handleMsg = new SequentialBehaviour(agent);
				final ReceiverBehaviour.Handle handle = ReceiverBehaviour.newHandle();
				handleMsg.addSubBehaviour(new ReceiverBehaviour(agent, handle, 5000, MessageTemplate.and(MessageTemplate.MatchSender(dr), (MessageTemplate.MatchConversationId("SendData")))));
				handleMsg.addSubBehaviour(new GetPossiblePass(agent, handle));
				pb.addSubBehaviour(handleMsg);
			}
			sb.addSubBehaviour(pb);
			ParallelBehaviour pb2 = new ParallelBehaviour();
			for (AID dr : drivers) {
				SequentialBehaviour handleMsg2 = new SequentialBehaviour(agent);
				final ReceiverBehaviour.Handle handle2 = ReceiverBehaviour.newHandle();
				handleMsg2.addSubBehaviour(new ReceiverBehaviour(agent, handle2, 5000, MessageTemplate.and(MessageTemplate.MatchSender(dr), (MessageTemplate.MatchConversationId("AgreeForPropose")))));
				handleMsg2.addSubBehaviour(new GetBestPass(agent, handle2));
				pb.addSubBehaviour(handleMsg2);
			}
			sb.addSubBehaviour(pb2);
			sb.addSubBehaviour(new SendAgreeMsgToPass(agent));
			addBehaviour(sb);
		}
	}

	private class PassengerBehaviour extends OneShotBehaviour {

		private Agent agent;

		PassengerBehaviour(Agent agent) {
			this.agent = agent;
		}

		@Override
		public void action() {
			SequentialBehaviour sb = new SequentialBehaviour(agent);
			ParallelBehaviour pb = new ParallelBehaviour();
			for (AID dr : drivers) {
				SequentialBehaviour handleMsg = new SequentialBehaviour(agent);
				final ReceiverBehaviour.Handle handle = ReceiverBehaviour.newHandle();
				handleMsg.addSubBehaviour(new ReceiverBehaviour(agent, handle, 5000, MessageTemplate.and(MessageTemplate.MatchSender(dr), (MessageTemplate.MatchConversationId("Propose")))));
				handleMsg.addSubBehaviour(new GetPossibleDriver(agent, handle));
				pb.addSubBehaviour(handleMsg);
			}
			sb.addSubBehaviour(pb);
			sb.addSubBehaviour(new SendMsgToDriver(agent));
			final ReceiverBehaviour.Handle handle = ReceiverBehaviour.newHandle();
			sb.addSubBehaviour(new ReceiverBehaviour(agent, handle, 10000, (MessageTemplate.MatchConversationId("AgreeForAgree"))));
			sb.addSubBehaviour(new GetConfirmFromDriver(agent,handle));
			addBehaviour(sb);
		}
	}
	// driver counts suggestions from all passengers
	private class GetPossiblePass extends OneShotBehaviour {
		private ReceiverBehaviour.Handle handle;
		private Agent agent;
		private double utility;
		private double price;
		private double dist2;
		private List<String> way2;
		private double extra;

		// count system utility of driving with concrete passenger
		private void  CountUtility (Agent agent, String[] content, AID sender) {
			utility = getDist2(way) + Double.parseDouble(content[2]) - dist2;
			// decrease percent if during the last circle agent couldn't find any passengers
			price =  percent * (Double.parseDouble(content[2]) + extra - utility);
			ACLMessage msg = new ACLMessage(ACLMessage.CFP);
			msg.setConversationId("Propose");
			msg.addReceiver(sender);
			String wayString = "";
			for (String w: way2) wayString = wayString + w + " ";
			msg.setContent(price + " " + utility + "\n" + wayString);
			agent.send(msg);
			//System.out.println(agent.getLocalName() + " sends propose = " + (int)Math.round(price)  + " to " + sender.getLocalName());
		}
		// count distance, way and price for all possible passengers
		GetPossiblePass(Agent agent, ReceiverBehaviour.Handle handle) {

			this.agent = agent;
			this.handle = handle;
			this.way2 = new ArrayList<>();
			this.price = 0;
			this.utility = 0;
			this.dist2 = dist;
			this.extra = 0;
		}
		@Override
		public void action() {
			try {
				ACLMessage msg = handle.getMessage();
				if (msg.getContent() != null) {
					String[] content = msg.getContent().split(" ");
					if (way.size() == 2) {
						way2.add(from);
						way2.add(content[0]);
						way2.add(content[1]);
						way2.add(to);
						dist2 = getDist2(way2);
						extra = Dispetcher.graphMatrix.getPath(from, content[0]).getWeight() + Dispetcher.graphMatrix.getPath(content[1], to).getWeight();
						CountUtility(agent, content, msg.getSender());
					} else {
						if ((way.contains(content[0])) && (way.contains(content[1])) && (way.indexOf(content[0]) <= way.lastIndexOf(content[1]))) {
							way2.addAll(way);
							CountUtility(agent, content, msg.getSender());
						}
						else if (((way.contains(content[0])) && (!way.contains(content[1])) && !(Objects.equals(content[0], to))) || ((way.contains(content[0])) && (way.contains(content[1])) && (way.indexOf(content[0]) > way.lastIndexOf(content[1])))) {
							int i = way.lastIndexOf(content[0]);
							String tmp3 = "";
							for (String w: way) tmp3 = tmp3 + w + " ";
							way2.addAll(way.subList(0, i + 1));
							way2.add(content[0]);
							way2.addAll(way.subList(i + 1, way.size()));
							way2.add(content[1]);
							way2.add(way.get(way.size() - 1));
							dist2 = getDist2(way2);
							extra = Dispetcher.graphMatrix.getPath(way.get(way.size() - 2), content[1]).getWeight() + Dispetcher.graphMatrix.getPath(content[1], way.get(way.size() - 1)).getWeight();
							CountUtility(agent, content, msg.getSender());
						}
						else if (!(way.contains(content[0])) && (way.contains(content[1])) && !(Objects.equals(content[1], from))) {
							int i = way.indexOf(content[1]);
							way2.add(way.get(0));
							way2.add(content[0]);
							way2.addAll(way.subList(1, i + 1));
							way2.add(content[1]);
							way2.addAll(way.subList(i + 1, way.size()));
							dist2 = getDist2(way2);
							extra = Dispetcher.graphMatrix.getPath(way.get(0), content[0]).getWeight() + Dispetcher.graphMatrix.getPath(content[0], way.get(1)).getWeight();
							CountUtility(agent, content, msg.getSender());
						} else {
							way2.add(way.get(0));
							way2.add(content[0]);
							way2.addAll(way.subList(1, way.size() - 1));
							way2.add(content[1]);
							way2.add(way.get(way.size() - 1));
							dist2 = getDist2(way2);
							extra = Dispetcher.graphMatrix.getPath(way.get(0), content[0]).getWeight() + Dispetcher.graphMatrix.getPath(content[0], way.get(1)).getWeight() + Dispetcher.graphMatrix.getPath(way.get(way.size() - 2), content[1]).getWeight() + Dispetcher.graphMatrix.getPath(content[1], way.get(way.size() - 1)).getWeight();
							CountUtility(agent, content, msg.getSender());
						}
					}
				}
			}
			catch (ReceiverBehaviour.TimedOut ignored) {

			}
			catch (ReceiverBehaviour.NotYetReady notYetReady) {
				System.out.println(agent.getLocalName() + ": reply not yet ready");
			}
		}
	}

	// passenger chooses the offer with the best price
	private class GetPossibleDriver extends OneShotBehaviour {
		private ReceiverBehaviour.Handle handle;
		private Agent agent;
		private double price;
		private List<String> way2;
		private double dist2;

		GetPossibleDriver(Agent agent, ReceiverBehaviour.Handle handle) {
			this.agent = agent;
			this.handle = handle;
			this.price =  0;
			this.way2 = new ArrayList<>();
			this.dist2 = dist;
		}
		@Override
		public void action() {
			try {
				ACLMessage msg = handle.getMessage();
				if (Objects.equals(msg.getConversationId(), "Propose")) {
					double util = Double.parseDouble(msg.getContent().split("\n")[0].split(" ")[1]);
					way2 = Arrays.asList(msg.getContent().split("\n")[1].split(" "));
					int i = way2.indexOf(from);
					int j = way2.subList(i,way2.size()).indexOf(to);
					double tmpdist = getDist2(way2.subList(i, j + 1));
					driverPrice = Double.parseDouble(msg.getContent().split("\n")[0].split(" ")[0]);
					if (util >= 0)
						aloneFlag = true;
					if  (driverPrice <= passPrice && tmpdist < 1.5 * dist && util >= 0 && !(getCategory(agent,msg.getSender()).contains("passenger")))  {
						passPrice = driverPrice;
						possibleDriver = msg.getSender();
						driversWay.clear();
						for (String w: way2)
							driversWay.add(w);
					}
				}
			}
			catch (ReceiverBehaviour.TimedOut ignored) {

			}
			catch (ReceiverBehaviour.NotYetReady notYetReady) {
				System.out.println(agent.getLocalName() + ": reply not yet ready");
			}
		}
	}
	// sending message to driver with the best offer
	private class SendMsgToDriver extends OneShotBehaviour {
		private Agent agent;

		SendMsgToDriver(Agent agent) {

			this.agent = agent;
		}
		@Override
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.CFP);
			if (possibleDriver != null) {
				msg.addReceiver(possibleDriver);
				if (!getCategory(agent,possibleDriver).contains("dr")) setCategory(agent,possibleDriver,"tmp driver");
				String wayString = "";
				for (String w: driversWay)
					wayString = wayString + w + " ";
				msg.setContent(driverPrice + "\n" + wayString);
				msg.setConversationId("AgreeForPropose");
				agent.send(msg);
				System.out.println(agent.getAID().getLocalName() + " agrees for propose from " + possibleDriver.getLocalName());
			}
		}
	}
	// driver chooses one passenger with the maximum price
	private class GetBestPass extends OneShotBehaviour {
		private ReceiverBehaviour.Handle handle;
		private Agent agent;
		private double price;

		GetBestPass(Agent agent, ReceiverBehaviour.Handle handle) {
			this.agent = agent;
			this.handle = handle;
			this.price = 0;
		}
		@Override
		public void action() {
			try {
				ACLMessage msg = handle.getMessage();
				if (Objects.equals(msg.getConversationId(), "AgreeForPropose")) {
					price = Double.parseDouble(msg.getContent().split("\n")[0]);
					if (price >= bestPrice && !(getCategory(agent,msg.getSender()).contains("driver"))) {
						bestPrice = price;
						possiblePass = msg.getSender();
						way.clear();
						for (String s: msg.getContent().split("\n")[1].split(" "))
							way.add(s);
					}
				}
			}
			catch (ReceiverBehaviour.TimedOut ignored) {
			}
			catch (ReceiverBehaviour.NotYetReady notYetReady) {
				System.out.println(myAgent.getLocalName() + ": reply not yet ready");
			}
		}
	}

	private class SendAgreeMsgToPass extends OneShotBehaviour {
		private Agent agent;

		SendAgreeMsgToPass(Agent agent) {
			this.agent = agent;
		}
		@Override
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.AGREE);
			if (possiblePass != null) {
				setCategory(agent,agent.getAID(),"driver");
				setCategory(agent,possiblePass,"passenger");
				msg.setContent("passenger");
				msg.addReceiver(possiblePass);
				msg.setConversationId("AgreeForAgree");
				agent.send(msg);
				seats--;
				percent = percent - 0.1;
				if (!canBeDriver())
					possibleDriversService.setName("false");
				System.out.println(agent.getAID().getLocalName() + " agrees for agree from " + possiblePass.getLocalName());
			}
		}
	}
	// passenger gets agree from driver
	private class GetConfirmFromDriver extends OneShotBehaviour {
		private Agent agent;
		private ReceiverBehaviour.Handle handle;

		GetConfirmFromDriver(Agent agent, ReceiverBehaviour.Handle handle) {
			this.agent = agent;
			this.handle = handle;
		}
		public void action() {
			try {
				ACLMessage msg = handle.getMessage();
				if (Objects.equals(msg.getConversationId(), "AgreeForAgree")) {
						choosenDriver = msg.getSender();
						System.out.println(msg.getSender().getLocalName() + " is driver to " + agent.getLocalName());
				}
			}
			catch (ReceiverBehaviour.TimedOut ignored) {

			}
			catch (ReceiverBehaviour.NotYetReady notYetReady) {
				System.out.println(agent.getLocalName() + ": reply not yet ready");
			}
		}
	}

	private class Restart extends WakerBehaviour {
		Restart(Agent a, long timeout) {

			super(a, timeout);
		}
		@Override
		protected void onWake() {
			addBehaviour(new LifeCycle(myAgent));
		}
	}
}