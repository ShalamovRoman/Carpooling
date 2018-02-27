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
	private int countOfDrivers;
	private int countOfNotSet;
	private List<String> way = new ArrayList<>();
	private double passPrice;
	private double driverPrice = Double.POSITIVE_INFINITY;
	private final Lock mutex = new ReentrantLock(true);
	private Map<AID, String> utilities = new HashMap<>();
	private int alreadyPassFlag;
	private int alreadyDriverFlag;
	private boolean aloneFlag = false;

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
		if (SetUp.percents[getInt(agent)] > 0.2)
			return true;
		else
			return false;
	}

	private boolean canBePass(AID agent) {
		boolean res = false;
		for (int i = 0; i < SetUp.percents.length; i++) {
			if (i != getInt(agent) && SetUp.percents[i] > 0.2)
				res = true;
		}
		return res;
	}

	private void waitOthers(int i) {
		try {
			SetUp.b.get(i).await();
		}
		catch (InterruptedException e) {}
		catch (BrokenBarrierException e) {}
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

		DFAgentDescription agentDescription = new DFAgentDescription();
		agentDescription.setName(getAID());
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType("Tachki");
		serviceDescription.setName("TachkiServer");
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
			if (!aloneFlag && cnt > 0 && SetUp.categories.get(getInt(agent.getAID())) != "driver")
				SetUp.categories.replace(getInt(agent.getAID()), "alone");
			aloneFlag = false;
			for (Map.Entry<Integer, String> entry : SetUp.categories.entrySet())
				if (entry.getValue().contains("tmp")) SetUp.categories.replace(entry.getKey(), "not set");

			possiblePass = null;
			possibleDriver = null;
			passPrice = Double.POSITIVE_INFINITY;
			bestPrice = Double.NEGATIVE_INFINITY;

			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription serviceDescription = new ServiceDescription();
			serviceDescription.setType("Tachki");
			template.addServices(serviceDescription);

			drivers = new HashSet<>();
			try {
				DFAgentDescription[] agents = DFService.search(agent, template);
				for (DFAgentDescription a : agents) {
					if (!(a.getName().equals(agent.getAID())) && (SetUp.categories.get(getInt(a.getName())) != "passenger") && (SetUp.categories.get(getInt(a.getName())) != "alone"))
						drivers.add(a.getName());
					if (!(a.getName().equals(agent.getAID())) && cnt == 0)
						utilities.put(a.getName(), " ");
				}
			}
			catch (FIPAException fe) {}

			countOfDrivers = 0;
			countOfNotSet = 0;
			alreadyPassFlag = 0;
			alreadyDriverFlag = 0;

			for (int i = 0; i < drivers.size(); i++) {
				if (!(agent.getName().equals(agent.getAID())) && (SetUp.categories.get(getInt(agent.getAID())) == "driver"))
					countOfDrivers++;
				else if (!(agent.getName().equals(agent.getAID())) && (SetUp.categories.get(getInt(agent.getAID())) == "not set"))
					countOfNotSet++;
			}

			SequentialBehaviour sb = new SequentialBehaviour(agent);
			ParallelBehaviour pb = new ParallelBehaviour();
			waitOthers(drivers.size());

			String print = "";
			for (Map.Entry<Integer, String> entry : SetUp.categories.entrySet()) {
				if (entry.getValue() == "passenger")
					print = print + entry.getKey() + " " + entry.getValue() + " (driver is " + SetUp.chooseDrivers.get(entry.getKey()) + ")\r\n";
				else
					print = print + entry.getKey() + " " + entry.getValue() + "\r\n";
				if (entry.getValue() == "not set")
					alreadyPassFlag += 1;
			}
			if (SetUp.forPrint == cnt) {
				SetUp.forPrint += 1;
				mutex.lock();
				System.out.println(print);
				mutex.unlock();
			}

			waitOthers(drivers.size());
			if (SetUp.categories.get(getInt(agent.getAID())) == "not set" && alreadyPassFlag == 1 && canBePass(agent.getAID())) {
				sb.addSubBehaviour(new SendData(agent));
				pb.addSubBehaviour(new PassengerBehaviour(agent));
				sb.addSubBehaviour(pb);
				addBehaviour(sb);
			}
			else if (SetUp.categories.get(getInt(agent.getAID())) == "not set" && canBePass(agent.getAID()) && canBeDriver(agent.getAID())) {
				sb.addSubBehaviour(new SendData(agent));
				pb.addSubBehaviour(new DriverBehaviour(agent));
				pb.addSubBehaviour(new PassengerBehaviour(agent));
				sb.addSubBehaviour(pb);
				addBehaviour(sb);
			}
			else if (SetUp.categories.get(getInt(agent.getAID())) == "driver" && seats > 0 && canBeDriver(agent.getAID())) {
				sb.addSubBehaviour(new SendData(agent));
				pb.addSubBehaviour(new DriverBehaviour(agent));
				sb.addSubBehaviour(pb);
				alreadyDriverFlag = 1;
				addBehaviour(sb);
			}
			cnt++;
			waitOthers(drivers.size());
			if (SetUp.categories.get(getInt(agent.getAID())) != "alone" && SetUp.categories.get(getInt(agent.getAID())) != "passenger" && SetUp.categories.values().contains("not set"))
				addBehaviour(new Restart(agent, 10000));
			else if (SetUp.categories.get(getInt(agent.getAID())) == "alone") {
				System.out.println(agent.getLocalName() + " goes alone");
			}
			else if (SetUp.forPrint > 0) {
				if (!(SetUp.categories.containsValue("not set"))) {
					print = "";
					for (Map.Entry<Integer, String> entry : SetUp.categories.entrySet()) {
						if (entry.getValue() == "passenger")
							print = print + entry.getKey() + " " + entry.getValue() + " (driver is " + SetUp.chooseDrivers.get(entry.getKey()) + ")\r\n";
						else
							print = print + entry.getKey() + " " + entry.getValue() + "\r\n";
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
				if (SetUp.categories.get(getInt(dr)) != "driver" || SetUp.categories.get(getInt(agent.getAID())) != "driver")
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
			if (cnt == 0)
				waitOthers(drivers.size());
			else
				waitOthers(countOfDrivers);
			sb.addSubBehaviour(new SendAgreeMsgToPass(agent));
			if (cnt == 0)
				waitOthers(drivers.size());
			else
				waitOthers(countOfDrivers);
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
			if (cnt == 0)
				waitOthers(drivers.size());
			else
				waitOthers(countOfNotSet);
			sb.addSubBehaviour(new SendMsgToDriver(agent));
			if (cnt == 0)
				waitOthers(drivers.size());
			else
				waitOthers(countOfNotSet);
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
			StringBuilder tmp = new StringBuilder(utilities.get(sender));
			utilities.replace(sender, " " + utility + tmp);
			//процент уменьшается, если на предыдущем кругу агент не нашел себе попутчиков
			price =  SetUp.percents[getInt(agent.getAID())] * (Double.parseDouble(content[2]) + extra - utility);
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
					String tmp = utilities.get(msg.getSender());
					double util = Double.parseDouble(msg.getContent().split("\n")[0].split(" ")[1]);
					utilities.replace(msg.getSender(), tmp + util + " ");
					way2 = Arrays.asList(msg.getContent().split("\n")[1].split(" "));
					int i = way2.indexOf(from);
					int j = way2.subList(i,way2.size()).indexOf(to);
					dist2 = getDist2(way2.subList(i, j + 1));
					driverPrice = Double.parseDouble(msg.getContent().split("\n")[0].split(" ")[0]);
					if (util > 0)
						aloneFlag = true;
					if  (driverPrice <= passPrice && util > 0 && !(SetUp.categories.get(getInt(msg.getSender())).contains("passenger")))  {
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
				SetUp.categories.replace(getInt(possibleDriver),"tmpdriver");
				msg.setContent(driverPrice + "");
				msg.setConversationId("AgreeForPropose");
				agent.send(msg);
				System.out.println(agent.getAID().getLocalName() + " agrees for propose from " + possibleDriver.getLocalName());
				for (AID dr: drivers) {
					if (dr != possibleDriver){
						ACLMessage rfs = new ACLMessage(ACLMessage.REFUSE);
						rfs.addReceiver(dr);
						rfs.setConversationId("RefuseForPropose");
						agent.send(rfs);
					}
				}
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
					if (price >= bestPrice && !(SetUp.categories.get(getInt(msg.getSender())).contains("driver"))) {
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
				if (alreadyDriverFlag != 1 && alreadyPassFlag != 1) {
					String[] arr = utilities.get(possiblePass).trim().split(" ");
					if (Double.parseDouble(arr[0]) == Double.parseDouble(arr[arr.length-1])) {
						SetUp.categories.replace(getInt(agent.getAID()),"driver");
						SetUp.categories.replace(getInt(possiblePass),"passenger");
						msg.setContent("passenger");
					}
					else {
						if (Double.parseDouble(arr[0]) > Double.parseDouble(arr[arr.length-1])) {
							SetUp.categories.replace(getInt(agent.getAID()),"driver");
							SetUp.categories.replace(getInt(possiblePass),"passenger");
							msg.setContent("passenger");
						}
						else {
							SetUp.categories.replace(getInt(agent.getAID()),"passenger");
							SetUp.categories.replace(getInt(possiblePass),"driver");
							msg.setContent("driver");
						}
					}
				}
				else {
					SetUp.categories.replace(getInt(agent.getAID()),"driver");
					SetUp.categories.replace(getInt(possiblePass),"passenger");
					msg.setContent("passenger");
				}

				msg.addReceiver(possiblePass);
				msg.setConversationId("AgreeForAgree");
				agent.send(msg);
				mutex.lock();
				System.out.println(agent.getAID().getLocalName() + " agrees for agree from " + possiblePass.getLocalName());
				mutex.unlock();
				for (AID dr: drivers) {
					if (dr != possiblePass){
						ACLMessage rfs = new ACLMessage(ACLMessage.REFUSE);
						rfs.addReceiver(dr);
						rfs.setConversationId("RefuseForAgree");
						agent.send(rfs);
					}
				}
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
					if (msg.getContent().contains("d")) {
						SetUp.chooseDrivers.put(getInt(msg.getSender()),agent.getLocalName());
						SetUp.percents[getInt(agent.getAID())] = SetUp.percents[getInt(agent.getAID())] - 0.05;
						mutex.lock();
						System.out.println(agent.getLocalName() + " is driver to " + msg.getSender().getLocalName());
						mutex.unlock();
						seats--;
					}
					else {
						SetUp.chooseDrivers.put(getInt(agent.getAID()),msg.getSender().getLocalName());
						SetUp.percents[getInt(msg.getSender())] = SetUp.percents[getInt(msg.getSender())] - 0.05;
						mutex.lock();
						System.out.println(msg.getSender().getLocalName() + " is driver to " + agent.getLocalName());
						mutex.unlock();
					}
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
			System.out.println(myAgent.getLocalName() + ": restart life cycle");
			addBehaviour(new LifeCycle(myAgent));
		}
	}
}