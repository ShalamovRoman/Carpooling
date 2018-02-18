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
	private int maxcnt = 3;
	private AID possiblePass;
	private AID possibleDriver;
	private double bestPrice;
	private HashSet<AID> drivers = new HashSet<>();
	private List<String> way = new ArrayList<>();
	private double passPrice = 1000000000;
	private double driverPrice = 0;
	private double procent = 0.5;
	private final Lock mutex = new ReentrantLock(true);
	private String susanna = "not set";
	private Map<AID, String> utilities= new HashMap<>();
	private String passSusanna = "not set";


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
			System.out.println("cycle started");
			if (cnt < maxcnt) {
				if (cnt == 0) {
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
				}
				System.out.println(susanna);
				SequentialBehaviour sb = new SequentialBehaviour(agent);
				ParallelBehaviour pb = new ParallelBehaviour();
				sb.addSubBehaviour(new SendData(agent));
				waitOthers(drivers.size());

				System.out.println(susanna);

				if (susanna == "not set") {
					pb.addSubBehaviour(new DriverBehaviour(agent));
					pb.addSubBehaviour(new PassengerBehaviour(agent));
					sb.addSubBehaviour(pb);
					//waitOthers(drivers.size());
					//sb.addSubBehaviour(new SendConfirm(agent));
					addBehaviour(sb);
				}
				else if (susanna == "driver" && seats > 0) {
					pb.addSubBehaviour(new DriverBehaviour(agent));
					sb.addSubBehaviour(pb);
					addBehaviour(sb);
				}

/*			pb.addSubBehaviour(new DriverBehaviour(agent));
			pb.addSubBehaviour(new PassengerBehaviour(agent));
			sb.addSubBehaviour(pb);
			//waitOthers(drivers.size());
			//sb.addSubBehaviour(new SendConfirm(agent));
			addBehaviour(sb); */
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
			utilities.put(sender, utility + "");
			System.out.println("receiver  " + sender.getLocalName() + " util " + utility + " sender " + agent.getLocalName());
			price = procent * (Double.parseDouble(content[2]) + extra) - utility;

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
					System.out.println(msg.getContent() + "aaaaaaa");
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
					String tmp = utilities.get(msg.getSender());
					double util = Double.parseDouble(msg.getContent().split("\n")[0].split(" ")[1]);
					utilities.replace(msg.getSender(), tmp + " " + util);
					way2 = Arrays.asList(msg.getContent().split("\n")[1].split(" "));
					int i = way2.indexOf(from);
					int j = way2.subList(i,way2.size()).indexOf(to);
					dist2 = getDist2(way2.subList(i, j + 1));
					price = (1 - procent) * (dist - dist2 + Double.parseDouble(msg.getContent().split("\n")[0].split(" ")[0])) + util; //тут чет изменила
					//price = (dist - dist2) + (1 - procent) * Double.parseDouble(msg.getContent().split("\n")[0].split(" ")[0]) + util;
					if  (price < passPrice && util > 0) {
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
				msg.setContent(driverPrice + " " + susanna);
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
					price = Double.parseDouble(msg.getContent().split(" ")[0]);
					if (price > bestPrice) {
						bestPrice = price;
						possiblePass = msg.getSender();
						passSusanna = msg.getContent().split(" ")[1];
					}
				}
			} catch (ReceiverBehaviour.TimedOut timedOut) {
				//System.out.println(agent.getLocalName() + ": time out while receiving message");
			} catch (ReceiverBehaviour.NotYetReady notYetReady) {
				//System.out.println(agent.getLocalName() + ": message not yet ready");
				//notYetReady.printStackTrace();
			}}

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
				if (passSusanna == "driver") {
					susanna = "passenger";
					possibleDriver = msg.getSender();
					msg.setContent("driver");
				}
				else {
					//else {
					//System.out.println(Double.parseDouble(utilities.get(possiblePass).split(" ")[0]));
					//System.out.println(Double.parseDouble(utilities.get(possiblePass).split(" ")[1]));
					if (Double.parseDouble(utilities.get(possiblePass).split(" ")[0]) == Double.parseDouble(utilities.get(possiblePass).split(" ")[1])) {
						System.out.println("hui");
						System.out.println(Double.parseDouble(utilities.get(possiblePass).split(" ")[0]));
						System.out.println(Double.parseDouble(utilities.get(possiblePass).split(" ")[1]));
					}
					else {
						if (Double.parseDouble(utilities.get(possiblePass).split(" ")[0]) > Double.parseDouble(utilities.get(possiblePass).split(" ")[1])) {


							susanna = "driver";
							msg.setContent("passenger");
						} else {
							susanna = "passenger";
							msg.setContent("driver");
							System.out.println((Double.parseDouble(utilities.get(possiblePass).split(" ")[0]) + " " + Double.parseDouble(utilities.get(possiblePass).split(" ")[1])));
						}
					}
				}
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

						//Check4refuses
					}
/*					else {
					    addBehaviour(new ChangePassengerCategory(possiblePass,agent));
                    } */
				}
               /*
                susanna = "driver";
                seats--;
                System.out.println(agent.getLocalName() + " category changed to " + susanna);
                addBehaviour(new WakerBehaviour(agent, 5000) {
                    @Override
                    protected void onWake() {
                        System.out.println("restarting lifecycle of " + agent.getLocalName());
                        addBehaviour(new LifeCycle(myAgent));
                    }
                });*/
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
			sb.addSubBehaviour(new SendAgreeMsgToPass(agent));
			waitOthers(drivers.size());
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
			waitOthers(drivers.size());
			final ReceiverBehaviour.Handle handle = ReceiverBehaviour.newHandle();
			sb.addSubBehaviour(new ReceiverBehaviour(agent, handle, 5000, (MessageTemplate.MatchConversationId("AgreeForAgree"))));
			sb.addSubBehaviour(new GetConfirmFromDriver(agent,handle));
			addBehaviour(sb);

		}
	}

	private class ChangeDriverCategory extends OneShotBehaviour {
		private Agent agent;


		public ChangeDriverCategory(Agent agent) {
			this.agent = agent;
		}

		@Override
		public void action() {
			susanna = "driver";
			seats--;
			System.out.println(agent.getLocalName() + " category changed to " + susanna);
			addBehaviour(new WakerBehaviour(agent, 5000) {
				@Override
				protected void onWake() {
					System.out.println("restarting lifecycle of " + agent.getLocalName());
					addBehaviour(new LifeCycle(myAgent));
				}
			});
		} }

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

					if (msg.getContent().contains("d")) {System.out.println(agent.getLocalName() + " is driver to " + msg.getSender().getLocalName());}
					else {System.out.println(msg.getSender().getLocalName() + " is driver to " + agent.getLocalName());}
				}

			} catch (ReceiverBehaviour.TimedOut timedOut) {
				//System.out.println(agent.getLocalName() + ": time out while receiving message");
			} catch (ReceiverBehaviour.NotYetReady notYetReady) {
				//System.out.println(agent.getLocalName() + ": message not yet ready");
				//notYetReady.printStackTrace();
			}
		}
	}
}