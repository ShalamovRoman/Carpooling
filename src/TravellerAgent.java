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
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TravellerAgent extends Agent {
	private double dist;
	private String from;
	private String to;
	private int seats;
	private int cnt = 0;
	private int maxcnt = 1;
	//private double utility = 0;
	private AID possiblePass;
	private AID possibleDriver;
	private double bestPrice;
	private HashSet<AID> drivers = new HashSet<>();
	private List<String> way = new ArrayList<>();
	//private List<String> way2 = new ArrayList<>();
	//private double dist2;
	private double passPrice = 1000000000;
	private double driverPrice = 0;
	private double procent = 1;
	private final Lock mutex = new ReentrantLock(true);
	private int susanna = 0;
	//public static Map<String, String> infoToPass = new HashMap<String, String>();

	private void waitOthers(int i) {
		try {
			SetUp.b.get(i).await();
		} catch (InterruptedException e) {
		} catch (BrokenBarrierException e) {
		}
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
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		addBehaviour(new WakerBehaviour(this, 10000) {
			@Override
			protected void onWake() {
				addBehaviour(new LifeCycle(myAgent));
			}
		});
	}

	private class LifeCycle extends SequentialBehaviour {
		public LifeCycle(Agent agent) {
			super(agent);
				addBehaviour(new StartCycle(agent, this));
		}
	}

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
			if (cnt < maxcnt) {
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription serviceDescription = new ServiceDescription();
			serviceDescription.setType("Tachki");
			template.addServices(serviceDescription);
			try {
				DFAgentDescription[] agents = DFService.search(agent, template);
				for (DFAgentDescription a : agents) {

					if (!(a.getName().equals(agent.getAID()))) drivers.add(a.getName());
				}
			} catch (FIPAException fe) {
				fe.printStackTrace();
			}
			SequentialBehaviour sb = new SequentialBehaviour(agent);
			ParallelBehaviour pb = new ParallelBehaviour();
			sb.addSubBehaviour(new SendData(agent));
			waitOthers(drivers.size());
			pb.addSubBehaviour(new DriverBehaviour(agent));
			pb.addSubBehaviour(new PassengerBehaviour(agent));
			sb.addSubBehaviour(pb);
			//waitOthers(drivers.size());
			//sb.addSubBehaviour(new SendConfirm(agent));
			addBehaviour(sb);
				cnt++;
			}
		}
	}

	private class SendData extends OneShotBehaviour{
		private Agent agent;


		public SendData( Agent agent) {

			this.agent = agent;
		}
		@Override
		public void action() {
		//String replyWith = String.valueOf(System.currentTimeMillis());
		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
			msg.removeReceiver(getAID());
			msg.setContent(from + " " + to + " " + dist);
			msg.setConversationId("SendData");
			//msg.setReplyWith(replyWith);
			for (AID dr : drivers) {
			msg.addReceiver(dr);
			mutex.lock();
			System.out.println(agent.getAID().getLocalName() + " sends data to " + dr.getLocalName());
			mutex.unlock();
			}
			agent.send(msg);
	}}
	/*private class SendConfirm extends OneShotBehaviour{
		private Agent agent;


		public SendConfirm( Agent agent) {

			this.agent = agent;
		}
		@Override
		public void action() {
			SequentialBehaviour sb = new SequentialBehaviour(agent);
				ParallelBehaviour pb = new ParallelBehaviour();
				for (AID dr : drivers) {
					SequentialBehaviour handleMsg = new SequentialBehaviour(agent);
					final ReceiverBehaviour.Handle handle = ReceiverBehaviour.newHandle();
					handleMsg.addSubBehaviour(new ReceiverBehaviour(agent, handle, 5000, MessageTemplate.and(MessageTemplate.MatchSender(dr), (MessageTemplate.MatchConversationId("AgreeForPropose")))));
					handleMsg.addSubBehaviour(new GetBestPass(agent, dr, handle));
					pb.addSubBehaviour(handleMsg);
				}
				sb.addSubBehaviour(pb);
				waitOthers(drivers.size());
				sb.addSubBehaviour(new SendMsgToPass(agent));
				addBehaviour(sb);
		}}
*/
	private double getDist2(List<String> arr){
		double sum = 0;
		for (int i = 0; i < arr.size() - 1; i++) {
			sum = sum + SetUp.graphMatrix.getPath(arr.get(i), arr.get(i+1)).getWeight();
		}
		return sum;
	}

	private class GetPossiblePass extends OneShotBehaviour {
		private ReceiverBehaviour.Handle handle;
		private Agent agent;
		private AID sender;
		private double utility = 0;
		private double price = 0;
		private double dist2 = 0;
		private List<String> way2 = new ArrayList<>();
		private double extra = 0;



		private void  CountUtility (Agent agent, String[] content, AID sender) {
			utility = getDist2(way) + Double.parseDouble(content[2]) - dist2;
			price = procent * (Double.parseDouble(content[2]) + extra - utility);
			ACLMessage msg = new ACLMessage(ACLMessage.CFP);
			msg.setConversationId("Propose");
			msg.addReceiver(sender);
			String tmp = "";
			for (String w: way2) tmp = tmp + w + " ";
			msg.setContent(price + " " + utility + "\n" + tmp);
			agent.send(msg);
			mutex.lock();
			System.out.println(agent.getLocalName() + " sends propose = " + price + " to " + sender.getLocalName());
			mutex.unlock();

		}
		public GetPossiblePass( Agent agent, AID sender, ReceiverBehaviour.Handle handle) {

			this.agent = agent;
			this.handle = handle;
			this.sender = sender;
			this.way2 = way2;
			this.price = price;
			this.utility = utility;
			this.dist2 = dist2;
			this.extra = extra;
		}
		@Override
		public void action() {
			try {
				ACLMessage msg = handle.getMessage();
				if  (msg.getContent() != null) {
					mutex.lock();
					System.out.println(agent.getAID().getLocalName() + " got msg from " + msg.getSender().getLocalName());
					mutex.unlock();
					String[] content = msg.getContent().split(" ");
					if ((way.contains(content[0])) && (way.contains(content[1])) && (way.indexOf(content[0]) <= way.lastIndexOf(content[1]))) {
							way2 = way;
							//System.out.println(content + ".1 to " + agent.getAID().getLocalName() + " from " + msg.getSender().getLocalName())
							CountUtility(agent, content, msg.getSender());
					} else if (((way.contains(content[0])) && (!way.contains(content[1])) && !(content[0] == to)) || ((way.contains(content[0])) && (way.contains(content[1])) && (way.indexOf(content[0]) > way.lastIndexOf(content[1])))) {
						if (way.size() == 2) {
							way2.add(from);
							way2.add(content[0]);
							way2.add(content[1]);
							way2.add(to);
							dist2 = getDist2(way2);
							extra = SetUp.graphMatrix.getPath(from, content[0]).getWeight() + SetUp.graphMatrix.getPath(content[1],to).getWeight();
							//System.out.println(content + ".2 to " + agent.getAID().getLocalName() + " from " + msg.getSender().getLocalName());
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
							//System.out.println(content + ".3 to " + agent.getAID().getLocalName() + " from " + msg.getSender().getLocalName());
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
							//System.out.println(content + ".4 to " + agent.getAID().getLocalName() + " from " + msg.getSender().getLocalName());
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
							//System.out.println(content + ".5 to " + agent.getAID().getLocalName() + " from " + msg.getSender().getLocalName());
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
							//System.out.println(content + ".6 to " + agent.getAID().getLocalName() + " from " + msg.getSender().getLocalName());
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
							//System.out.println(content + ".7 to " + agent.getAID().getLocalName() + " from " + msg.getSender().getLocalName());
							CountUtility(agent, content, msg.getSender());
						}
					}
				}

			} catch (ReceiverBehaviour.TimedOut timedOut) {
				//System.out.println(agent.getLocalName() + ": time out while receiving message");
			} catch (ReceiverBehaviour.NotYetReady notYetReady) {
				//System.out.println(agent.getLocalName() + ": message not yet ready");
				//notYetReady.printStackTrace();
			}

			}
	}

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
				this.dist2 = dist2;
			}

			@Override
			public void action() {

				try {
					ACLMessage msg = handle.getMessage();
					if (msg.getConversationId() == "Propose") {
					way2 = Arrays.asList(msg.getContent().split("\n")[1].split(" "));
					int i = way2.indexOf(from);
					int j = way2.subList(i,way2.size()).indexOf(to);
					dist2 = getDist2(way2.subList(i, j + 1));
					price = dist2 - dist + Double.parseDouble(msg.getContent().split("\n")[0].split(" ")[0]);
					if (price < passPrice) {
						passPrice = price;
						possibleDriver = msg.getSender();
						driverPrice = Double.parseDouble(msg.getContent().split("\n")[0].split(" ")[0]);
					}
					}
				} catch (ReceiverBehaviour.TimedOut timedOut) {
					//System.out.println(agent.getLocalName() + ": time out while receiving message");
				} catch (ReceiverBehaviour.NotYetReady notYetReady) {
					//System.out.println(agent.getLocalName() + ": message not yet ready");
					//notYetReady.printStackTrace();
				}}

			}
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
		} }

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
					if (price > bestPrice) {
						bestPrice = price;
						possiblePass = msg.getSender();
					}
				}
			} catch (ReceiverBehaviour.TimedOut timedOut) {
				//System.out.println(agent.getLocalName() + ": time out while receiving message");
			} catch (ReceiverBehaviour.NotYetReady notYetReady) {
				//System.out.println(agent.getLocalName() + ": message not yet ready");
				//notYetReady.printStackTrace();
			}}

	}

	private class SendMsgToPass extends OneShotBehaviour {
		private Agent agent;


		public SendMsgToPass(Agent agent) {
			this.agent = agent;
		}

		@Override
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.AGREE);
			if (possiblePass != null) {
				msg.addReceiver(possiblePass);
				msg.setConversationId("AgreeForAgree");
				agent.send(msg);
				System.out.println(agent.getAID().getLocalName() + " agrees for agree from " + possiblePass.getLocalName());
				for (AID dr: drivers) {
					if (dr != possiblePass){
						ACLMessage rfs = new ACLMessage(ACLMessage.REFUSE);
						rfs.addReceiver(dr);
						rfs.setConversationId("RefuseForAgree");
						agent.send(rfs);
					}
				}
			}
		} }

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
				//waitOthers(drivers.size());
				ParallelBehaviour pb2 = new ParallelBehaviour();
				for (AID dr : drivers) {
					SequentialBehaviour handleMsg2 = new SequentialBehaviour(agent);////все переименовать и это будет другой класс
					final ReceiverBehaviour.Handle handle2 = ReceiverBehaviour.newHandle();
					handleMsg2.addSubBehaviour(new ReceiverBehaviour(agent, handle2, 5000, MessageTemplate.and(MessageTemplate.MatchSender(dr), (MessageTemplate.MatchConversationId("AgreeForPropose")))));
					handleMsg2.addSubBehaviour(new GetBestPass(agent, dr, handle2));
					pb.addSubBehaviour(handleMsg2);
				}
				sb.addSubBehaviour(pb2);
				waitOthers(drivers.size());
				sb.addSubBehaviour(new SendMsgToPass(agent));
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
				waitOthers(drivers.size());
				sb.addSubBehaviour(new SendMsgToDriver(agent));
				addBehaviour(sb);

			}
		}





	}

