import jade.core.Agent;
import jade.core.AID;
import jade.core.AgentDescriptor;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.lang.*;
import java.util.HashSet;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TravellerAgent extends Agent {

	private double dist;
	private String from;
	private String to;
	private int seats;
	private int cnt = 0;
	private AID possiblePass;
	private AID possibleDriver;
	private double bestPrice;
	private HashSet<AID> drivers;
	private List<String> way = new ArrayList<>();
	private double passPrice;
	private double driverPrice = Double.POSITIVE_INFINITY;
	private final Lock mutex = new ReentrantLock(true);
	private boolean aloneFlag = false;
	private double percent = 0.6;
	private DFAgentDescription agentDescription = new DFAgentDescription();
	private ServiceDescription serviceDescription = new ServiceDescription();
	private ServiceDescription categoryService = new ServiceDescription();
	private ServiceDescription possibleDriversService = new ServiceDescription();
	private Map<AID, String> categories = new HashMap< >();

	private int getInt(AID agent) {
		return Integer.parseInt(agent.getLocalName().replaceAll("[\\D]", ""));
	}

	private double getDist2(List<String> arr){
		double sum = 0;
		for (int i = 0; i < arr.size() - 1; i++) {
			sum = sum + SetUp.graphMatrix.getPath(arr.get(i), arr.get(i+1)).getWeight();
		}
		return sum;
	}

	private boolean canBeDriver(AID agent) {
		if (percent > 0.2 && seats > 0)
			return true;
		else
			return false;
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
			for (int i = 0; i < agents.length; i++) {
				if (agents[i].getName() != agent.getAID()) res = true;
			}
		}
		catch (FIPAException fe) {}

		return res;

	}

	private void waitOthers(int i) {
		try {
			SetUp.b.get(i).await();
		}
		catch (InterruptedException e) {}
		catch (BrokenBarrierException e) {}
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
		} catch (FIPAException e) {
			e.printStackTrace();
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
			for (int i = 0; i < result.length; i++) {
				if (result[i].getName().equals(aid)) c = type;
			}
		}
		catch (FIPAException fe) {}

		return c;

	}
	private String getCategory(Agent agent, AID aid) {

		String c = "";
		c = search(agent,aid,"not set");
		if (c=="not found") c = search(agent,aid,"driver");
		if (c=="not found") c = search(agent,aid,"tmpdriver");
		if (c=="not found") c = search(agent,aid,"passenger");
		if (c=="not found") c = search(agent,aid,"tmppassenger");
		if (c=="not found") c = search(agent,aid,"alone");
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
		catch (FIPAException fe) {}

	}

	protected void setup() {
		Object[] args = getArguments();
		from = (String) args[0];
		to = (String) args[1];
		way.add(from);
		way.add(to);
		seats = Integer.parseInt((String)args[2]);
		mutex.lock();
		dist = SetUp.graphMatrix.getPath(from, to).getWeight();
		System.out.println(getAID().getLocalName() + ": from " + from + " to " + to + ", " + seats + " free seats, dist = " + dist);
		mutex.unlock();


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

		catch (FIPAException fe) {}

		addBehaviour(new WakerBehaviour(this, 10000) {
			@Override
			protected void onWake() {
				addBehaviour(new LifeCycle(myAgent));
			}
		});
	}

	private class LifeCycle extends OneShotBehaviour {
		private Agent agent;
		public LifeCycle(Agent agent) {
			this.agent = agent;
		}
		@Override
		public void action() {
			addBehaviour(new StartCycle(agent, this));
		}
	}
	// запускает определенное поведение агента в зависимости от его категории и категорий других агентов
	public class StartCycle extends OneShotBehaviour {
		private Agent agent;
		private LifeCycle cycle;

		public StartCycle(Agent agent, LifeCycle cycle) {
			super(agent);
			this.agent = agent;
			this.cycle = cycle;
		}
		@Override
		public void action() {
			if (!aloneFlag && cnt > 0 && getCategory(agent, agent.getAID()) != "driver"){
				setCategory(agent,agent.getAID(),"alone");
			}
			aloneFlag = false;
			collectCategories(agent);
			for (Map.Entry<AID, String> entry : categories.entrySet())
				if (entry.getValue().contains("tmp")) setCategory(agent,entry.getKey(), "not set");

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
					if (!(a.getName().equals(agent.getAID())) && getCategory(agent,a.getName()) != "passenger" && getCategory(agent,a.getName()) != "alone")
						drivers.add(a.getName());
				}
			}
			catch (FIPAException fe) {}


			SequentialBehaviour sb = new SequentialBehaviour(agent);
			ParallelBehaviour pb = new ParallelBehaviour();
			//waitOthers(drivers.size());

			String print = "\r\n";
			collectCategories(agent);
			for (Map.Entry<AID, String> entry : categories.entrySet()) {
				if (entry.getValue() == "passenger")
					print = print + entry.getKey().getLocalName() + " " + entry.getValue() + " (driver is " + SetUp.chooseDrivers.get(getInt(entry.getKey())) + ")\r\n";
				else
					print = print + entry.getKey().getLocalName() + " " + entry.getValue() + "\r\n";
			}
			if (SetUp.forPrint == cnt) {
				SetUp.forPrint += 1;
				mutex.lock();
				System.out.println(print);
				mutex.unlock();
			}
			if (getCategory(agent,agent.getAID()) == "not set" && canBePass(agent) && canBeDriver(agent.getAID())) {
				sb.addSubBehaviour(new SendData(agent));
				pb.addSubBehaviour(new DriverBehaviour(agent));
				pb.addSubBehaviour(new PassengerBehaviour(agent));
				sb.addSubBehaviour(pb);
				addBehaviour(sb);
			}
			else if (getCategory(agent,agent.getAID()) == "not set" && canBePass(agent)) {
				sb.addSubBehaviour(new SendData(agent));
				pb.addSubBehaviour(new PassengerBehaviour(agent));
				sb.addSubBehaviour(pb);
				addBehaviour(sb);
			}

			else if (getCategory(agent,agent.getAID()) == "driver" && canBeDriver(agent.getAID())) {
				sb.addSubBehaviour(new SendData(agent));
				pb.addSubBehaviour(new DriverBehaviour(agent));
				sb.addSubBehaviour(pb);

				addBehaviour(sb);
			}
			cnt++;
			waitOthers(drivers.size());
			collectCategories(agent);
			if (getCategory(agent,agent.getAID()) != "alone" && getCategory(agent,agent.getAID()) != "passenger" && categories.values().contains("not set"))
				addBehaviour(new Restart(agent, 10000));
			else if (getCategory(agent,agent.getAID()) == "alone") {
				System.out.println(agent.getLocalName() + " goes alone");
			}
			else if (SetUp.forPrint > 0) {
				if (!(categories.containsValue("not set"))) {
					print = "\r\n";
					for (Map.Entry<AID, String> entry : categories.entrySet()) {
						if (entry.getValue() == "passenger")
							print = print + entry.getKey().getLocalName() + " " + entry.getValue() + " (driver is " + SetUp.chooseDrivers.get(getInt(entry.getKey()))  + ")\r\n";
						else
							print = print + entry.getKey().getLocalName() + " " + entry.getValue() + "\r\n";
						SetUp.forPrint = -1;
					}
					mutex.lock();
					System.out.println(print);
					System.out.println("Agents allocated");
					mutex.unlock();
				}
			}
		}
	}
	//посылка каждому агенту информации о себе: from, to и кратчайшее расстояние
	private class SendData extends OneShotBehaviour{
		private Agent agent;

		public SendData( Agent agent) {
			this.agent = agent;
		}
		@Override
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.CFP);
			msg.removeReceiver(getAID());
			msg.setContent(from + " " + to + " " + dist);
			msg.setConversationId("SendData");
			for (AID dr : drivers) {
				if (getCategory(agent,dr) != "driver" || getCategory(agent,agent.getAID()) != "driver")
					msg.addReceiver(dr);
			}
			agent.send(msg);
		}
	}

	private class DriverBehaviour extends OneShotBehaviour {

		private Agent agent;

		public DriverBehaviour(Agent agent) {
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
				handleMsg.addSubBehaviour(new GetPossiblePass(agent, dr, handle));
				pb.addSubBehaviour(handleMsg);
			}
			sb.addSubBehaviour(pb);
			ParallelBehaviour pb2 = new ParallelBehaviour();
			for (AID dr : drivers) {
				SequentialBehaviour handleMsg2 = new SequentialBehaviour(agent);
				final ReceiverBehaviour.Handle handle2 = ReceiverBehaviour.newHandle();
				handleMsg2.addSubBehaviour(new ReceiverBehaviour(agent, handle2, 5000, MessageTemplate.and(MessageTemplate.MatchSender(dr), (MessageTemplate.MatchConversationId("AgreeForPropose")))));
				handleMsg2.addSubBehaviour(new GetBestPass(agent, dr, handle2));
				pb.addSubBehaviour(handleMsg2);
			}
			sb.addSubBehaviour(pb2);

			if (cnt == 0){}
				//waitOthers(drivers.size());
			else{}
				//waitOthers(countOfDrivers);
			sb.addSubBehaviour(new SendAgreeMsgToPass(agent));
			if (cnt == 0){}
				//waitOthers(drivers.size());
			else{}
				//waitOthers(countOfDrivers);
			addBehaviour(sb);
		}
	}

	private class PassengerBehaviour extends OneShotBehaviour {

		private Agent agent;

		public PassengerBehaviour(Agent agent) {
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
				handleMsg.addSubBehaviour(new GetPossibleDriver(agent, dr, handle));
				pb.addSubBehaviour(handleMsg);
			}
			sb.addSubBehaviour(pb);
			if (cnt == 0){}
				//waitOthers(drivers.size());
			else{}
				//waitOthers(countOfNotSet);
			sb.addSubBehaviour(new SendMsgToDriver(agent));
			if (cnt == 0){}
				//waitOthers(drivers.size());
			else{}
				//waitOthers(countOfNotSet);
			final ReceiverBehaviour.Handle handle = ReceiverBehaviour.newHandle();
			sb.addSubBehaviour(new ReceiverBehaviour(agent, handle, 10000, (MessageTemplate.MatchConversationId("AgreeForAgree"))));
			sb.addSubBehaviour(new GetConfirmFromDriver(agent,handle));
			addBehaviour(sb);
		}
	}
	//как водитель, агент считает все предложения(цены) для всех пассажиров
	private class GetPossiblePass extends OneShotBehaviour {
		private ReceiverBehaviour.Handle handle;
		private Agent agent;
		private AID sender;
		private double utility = 0;
		private double price = 0;
		private double dist2 = 0;
		private List<String> way2 = new ArrayList<>();
		private double extra = 0;

		//вычисление выгоды для системы от совместной поездки с определенным пассажиром
		private void  CountUtility (Agent agent, String[] content, AID sender) {
			utility = getDist2(way) + Double.parseDouble(content[2]) - dist2;
			//процент уменьшается, если на предыдущем кругу агент не нашел себе попутчиков
			price =  percent * (Double.parseDouble(content[2]) + extra - utility);
			ACLMessage msg = new ACLMessage(ACLMessage.CFP);
			msg.setConversationId("Propose");
			msg.addReceiver(sender);
			String tmp2 = "";
			for (String w: way2) tmp2 = tmp2 + w + " ";
			msg.setContent(price + " " + utility + "\n" + tmp2);
			agent.send(msg);
			mutex.lock();
			System.out.println(agent.getLocalName() + " sends propose = " + (int)Math.round(price) + " to " + sender.getLocalName());
			mutex.unlock();

		}
		//пересчет расстояния, пути и цены для всех возможных пассажиров
		public GetPossiblePass( Agent agent, AID sender, ReceiverBehaviour.Handle handle) {

			this.agent = agent;
			this.handle = handle;
			this.sender = sender;
			this.way2 = way2;
			this.price = price;
			this.utility = utility;
			this.dist2 = dist;
			this.extra = extra;
		}
		@Override
		public void action() {
			try {
				ACLMessage msg = handle.getMessage();
				if  (msg.getContent() != null) {
					String[] content = msg.getContent().split(" ");
					if ((way.contains(content[0])) && (way.contains(content[1])) && (way.indexOf(content[0]) <= way.lastIndexOf(content[1]))) {
						way2 = way;
						CountUtility(agent, content, msg.getSender());
					} else if (((way.contains(content[0])) && (!way.contains(content[1])) && !(content[0] == to)) || ((way.contains(content[0])) && (way.contains(content[1])) && (way.indexOf(content[0]) > way.lastIndexOf(content[1])))) {
						if (way.size() == 2) {
							way2.add(from);
							way2.add(content[0]);
							way2.add(content[1]);
							way2.add(to);
							dist2 = getDist2(way2);
							extra = SetUp.graphMatrix.getPath(from, content[0]).getWeight() + SetUp.graphMatrix.getPath(content[1],to).getWeight();
							CountUtility(agent, content, msg.getSender());
						} else {
							int i = way.lastIndexOf(content[0]);
							way2 = way.subList(0, i + 1);
							way2.add(content[0]);
							way2.addAll(way.subList(i + 1, way.size() - 1));
							way2.add(content[1]);
							way2.add(way.get(way.size() - 1));
							dist2 = getDist2(way2);
							extra = SetUp.graphMatrix.getPath(way.get(way.size() - 2), content[1]).getWeight() + SetUp.graphMatrix.getPath(content[1],way.get(way.size() - 1)).getWeight();
							CountUtility(agent, content, msg.getSender());
						}
					} else if (!(way.contains(content[0])) && (way.contains(content[1])) && !(content[1] == from)) {
						if (way.size() == 2) {
							way2.add(from);
							way2.add(content[0]);
							way2.add(content[1]);
							way2.add(to);
							dist2 = getDist2(way2);
							extra = SetUp.graphMatrix.getPath(from, content[0]).getWeight() + SetUp.graphMatrix.getPath(content[1],to).getWeight();
							int a = 0;
							CountUtility(agent, content, msg.getSender());
						} else {
							int i = way.indexOf(content[1]);
							way2.add(way.get(0));
							way2.add(content[0]);
							way2.addAll(way.subList(1, i + 1));
							way2.add(content[1]);
							way.addAll(way.subList(i + 1, way.size()));
							dist2 = getDist2(way2);
							extra = SetUp.graphMatrix.getPath(way.get(0), content[0]).getWeight() + SetUp.graphMatrix.getPath(content[0],way.get(1)).getWeight();
							CountUtility(agent, content, msg.getSender());
						}
					} else {
						if (way.size() == 2) {
							way2.add(from);
							way2.add(content[0]);
							way2.add(content[1]);
							way2.add(to);
							dist2 = getDist2(way2);
							extra = SetUp.graphMatrix.getPath(from, content[0]).getWeight() + SetUp.graphMatrix.getPath(content[1],to).getWeight();
							double b = 0;
							CountUtility(agent, content, msg.getSender());
						} else {
							way2.add(way.get(0));
							way2.add(content[0]);
							way2.addAll(way.subList(1, way.size() - 1));
							way2.add(content[1]);
							way2.add(way.get(way.size() - 1));
							dist2 = getDist2(way2);
							extra = SetUp.graphMatrix.getPath(way.get(0), content[0]).getWeight() + SetUp.graphMatrix.getPath(content[0],way.get(1)).getWeight() + SetUp.graphMatrix.getPath(way.get(way.size() - 2), content[1]).getWeight() + SetUp.graphMatrix.getPath(content[1],way.get(way.size() - 1)).getWeight();
							CountUtility(agent, content, msg.getSender());
						}

					}
				}
			}
			catch (ReceiverBehaviour.TimedOut timedOut) {}
			catch (ReceiverBehaviour.NotYetReady notYetReady) {}
		}
	}
	//пассажир из всех предложений от водителей выбирает с минимальной ценой
	private class GetPossibleDriver extends OneShotBehaviour {
		private ReceiverBehaviour.Handle handle;
		private Agent agent;
		private AID sender;
		private double price = 0;
		private List<String> way2 = new ArrayList<>();
		private double dist2 = 0;

		public GetPossibleDriver(Agent agent, AID sender, ReceiverBehaviour.Handle handle) {
			this.agent = agent;
			this.handle = handle;
			this.sender = sender;
			this.price = price;
			this.way2 = way2;
			this.dist2 = dist;
		}
		@Override
		public void action() {
			try {
				ACLMessage msg = handle.getMessage();
				if (msg.getConversationId() == "Propose") {
					double util = Double.parseDouble(msg.getContent().split("\n")[0].split(" ")[1]);
					way2 = Arrays.asList(msg.getContent().split("\n")[1].split(" "));
					int i = way2.indexOf(from);
					int j = way2.subList(i,way2.size()).indexOf(to);
					dist2 = getDist2(way2.subList(i, j + 1));
					driverPrice = Double.parseDouble(msg.getContent().split("\n")[0].split(" ")[0]);
					if (util > 0)
						aloneFlag = true;
					if  (driverPrice <= passPrice && util > 0 && !(getCategory(agent,msg.getSender()).contains("passenger")))  {
						passPrice = driverPrice;
						possibleDriver = msg.getSender();
					}
				}
			}
			catch (ReceiverBehaviour.TimedOut timedOut) {}
			catch (ReceiverBehaviour.NotYetReady notYetReady) {}
		}
	}
	//отправка сообщения водителю с лучшим предложением
	private class SendMsgToDriver extends OneShotBehaviour {
		private Agent agent;

		public SendMsgToDriver(Agent agent) {

			this.agent = agent;
		}
		@Override
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.CFP);
			if (possibleDriver != null) {
				msg.addReceiver(possibleDriver);
				setCategory(agent,possibleDriver,"tmpdriver");
				msg.setContent(driverPrice + "");
				msg.setConversationId("AgreeForPropose");
				agent.send(msg);
				System.out.println(agent.getAID().getLocalName() + " agrees for propose from " + possibleDriver.getLocalName());
			}
		}
	}
	//водитель из согласившихся пассажиров выбирает одного с максимальной ценой
	private class GetBestPass extends OneShotBehaviour {
		private ReceiverBehaviour.Handle handle;
		private Agent agent;
		private AID sender;
		private double price = 0;

		public GetBestPass (Agent agent, AID sender, ReceiverBehaviour.Handle handle) {
			this.agent = agent;
			this.handle = handle;
			this.sender = sender;
			this.price = price;
		}
		@Override
		public void action() {
			try {
				ACLMessage msg = handle.getMessage();
				if (msg.getConversationId() == "AgreeForPropose") {
					price = Double.parseDouble(msg.getContent());
					if (price >= bestPrice && !(getCategory(agent,msg.getSender()).contains("driver"))) {
						bestPrice = price;
						possiblePass = msg.getSender();
					}
				}
			}
			catch (ReceiverBehaviour.TimedOut timedOut) {}
			catch (ReceiverBehaviour.NotYetReady notYetReady) {}
		}
	}

	private class SendAgreeMsgToPass extends OneShotBehaviour {
		private Agent agent;


		public SendAgreeMsgToPass(Agent agent) {
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
				if (!canBeDriver(agent.getAID())) possibleDriversService.setName("false");
				mutex.lock();
				System.out.println(agent.getAID().getLocalName() + " agrees for agree from " + possiblePass.getLocalName());
				mutex.unlock();
			}
		}
	}
	//получение пассажиром согласия или отказа от водителя
		//окончательное определение ролей в зависимости от выгоды для системы
	private class GetConfirmFromDriver extends OneShotBehaviour {
		private Agent agent;
		private ReceiverBehaviour.Handle handle;

		public GetConfirmFromDriver(Agent agent, ReceiverBehaviour.Handle handle) {
			this.agent = agent;
			this.handle = handle;
		}
		public void action() {
			try {
				ACLMessage msg = handle.getMessage();
				if (msg.getConversationId() == "AgreeForAgree") {
						SetUp.chooseDrivers.put(getInt(agent.getAID()),msg.getSender().getLocalName());
						mutex.lock();
						System.out.println(msg.getSender().getLocalName() + " is driver to " + agent.getLocalName());
						mutex.unlock();
				}
			}
			catch (ReceiverBehaviour.TimedOut timedOut) {}
			catch (ReceiverBehaviour.NotYetReady notYetReady) {}
		}
	}

	private class Restart extends WakerBehaviour {
		public Restart(Agent a, long timeout) {

			super(a, timeout);
		}
		@Override
		protected void onWake() {
			addBehaviour(new LifeCycle(myAgent));
		}
	}
}