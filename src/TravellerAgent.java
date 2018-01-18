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
	private double utility = 0;
	private AID possiblePass;
	private AID possibleDriver;
	private HashSet<AID> drivers = new HashSet<>();
	private List<String> way = new ArrayList<>();
	private List<String> way2 = new ArrayList<>();
	private double dist2;
	private double price;
	private double procent = 0.65;
	private final Lock mutex = new ReentrantLock(true);
	public static Map<String, String> infoToPass = new HashMap<String, String>();

	private void waitOthers() {
		try {
			SetUp.b.await();
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
			sb.addSubBehaviour(new SendData(agent));
			waitOthers();
			sb.addSubBehaviour(new DriverBehaviour(agent));
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
		String replyWith = String.valueOf(System.currentTimeMillis());
		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
			msg.removeReceiver(getAID());
			msg.setContent(from + " " + to + " " + dist);
			msg.setConversationId("SendData");
			msg.setReplyWith(replyWith);
			for (AID dr : drivers) {
			msg.addReceiver(dr);
			mutex.lock();
			System.out.println(agent.getAID().getLocalName() + " sends data to " + dr.getLocalName());
			mutex.unlock();
			}
			agent.send(msg);
	}}
	private class GetPossiblePass extends OneShotBehaviour {
		private ReceiverBehaviour.Handle handle;
		private Agent agent;
		private AID sender;
		private double utility=0;
		private double price=0;
		private double dist2=0;
		private List<String> way2 = new ArrayList<>();

		private void  CountUtility (Agent agent, String[] content, AID sender) {
			//if (this.dist2 < dist + Double.parseDouble(content[2])) {
			//		if (this.utility < dist + Double.parseDouble(content[2]) - this.dist2) {
					this.utility = dist + Double.parseDouble(content[2]) - this.dist2;
					int sum = 0;
					for (int i = this.way2.indexOf(content[0]); i < this.way2.indexOf(content[1]); i++) {
						sum += SetUp.graphMatrix.getPath(this.way2.get(i), this.way2.get(i + 1)).getWeight();
					}
					this.price = dist2 - this.utility;
					possiblePass = sender;

			//	}

			//}
			ACLMessage msg = new ACLMessage(ACLMessage.CFP);
			msg.setConversationId("Agree");
			msg.addReceiver(sender);
			msg.setContent(this.price + "");
			agent.send(msg);
			mutex.lock();
			System.out.println(agent.getLocalName() + " sends price = " + price + " to " + sender.getLocalName());
			mutex.unlock();

		}
		public GetPossiblePass( Agent agent, AID sender, ReceiverBehaviour.Handle handle) {

			this.agent = agent;
			this.handle = handle;
			this.sender = sender;
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
					if ((way.contains(content[0])) && (way.contains(content[1]))) {
						if (way.indexOf(content[0]) < way.indexOf(content[1])) {
							this.way2 = way;
							System.out.println(content + ".1 to " + agent.getAID().getLocalName() + " from " + msg.getSender().getLocalName());
							CountUtility(agent, content, msg.getSender());
						}
					} else if ((way.contains(content[0])) && (!way.contains(content[1]))) {
						if (way.size() == 2) {
							this.dist2 = SetUp.graphMatrix.getPath(content[0], from).getWeight() + SetUp.graphMatrix.getPath(to, content[1]).getWeight() + Double.parseDouble(content[2]);
							this.way2.add(from);
							this.way2.add(content[1]);
							this.way2.add(to);
							//System.out.println(content + ".2");
							System.out.println(content + ".2 to " + agent.getAID().getLocalName() + " from " + msg.getSender().getLocalName());

							CountUtility(agent, content, msg.getSender());
						} else {
							this.dist2 = dist + SetUp.graphMatrix.getPath(to, content[1]).getWeight();
							this.way2 = way.subList(0, way.size() - 2);
							this.way2.add(content[1]);
							this.way2.add(way.get(way.size() - 1));// +- пара индексов
							//System.out.println(content + ".3");
							System.out.println(content + ".3 to " + agent.getAID().getLocalName() + " from " + msg.getSender().getLocalName());

							CountUtility(agent, content, msg.getSender());
						}
					} else if (!(way.contains(content[0])) && (way.contains(content[1]))) {
						if (way.size() == 2) {
							this.dist2 = SetUp.graphMatrix.getPath(content[0], from).getWeight() + SetUp.graphMatrix.getPath(to, content[1]).getWeight() + Double.parseDouble(content[2]);
							this.way2.add(from);
							this.way2.add(content[0]);
							this.way2.add(to);
							//System.out.println(content + ".4");
							System.out.println(content + ".4 to " + agent.getAID().getLocalName() + " from " + msg.getSender().getLocalName());

							CountUtility(agent, content, msg.getSender());
						} else {
							double sum = 0;
							for (int i = 1; i < way.size() - 1; i++) {
								sum += SetUp.graphMatrix.getPath(way.get(i), way.get(i + 1)).getWeight();
							}
							this.dist2 = SetUp.graphMatrix.getPath(content[0], from).getWeight() + SetUp.graphMatrix.getPath(content[0], way.get(1)).getWeight() + sum;
							this.way2.add(way.get(0));
							this.way2.add(content[0]);
							this.way2.addAll(way.subList(1, way.size() - 1));
							//System.out.println(content + ".5");
							System.out.println(content + ".5 to " + agent.getAID().getLocalName() + " from " + msg.getSender().getLocalName());

							CountUtility(agent, content, msg.getSender());
						}
					} else {
						if (way.size() == 2) {
							this.dist2 = SetUp.graphMatrix.getPath(content[0], from).getWeight() + SetUp.graphMatrix.getPath(to, content[1]).getWeight() + Double.parseDouble(content[2]);
							this.way2.add(from);
							this.way2.add(content[0]);
							this.way2.add(content[1]);
							this.way2.add(to);
			//				System.out.println(dist2 + " .6 " + agent.getAID().getLocalName() + " from " + msg.getSender().getLocalName() );
							//System.out.println(content + ".6");

							System.out.println(content + ".6 to " + agent.getAID().getLocalName() + " from " + msg.getSender().getLocalName());

							CountUtility(agent, content, msg.getSender());
						} else {
							double sum = 0;
							for (int i = 1; i < way2.size() - 1; i++) {
								sum += SetUp.graphMatrix.getPath(way2.get(i), way2.get(i + 1)).getWeight();
							}

							this.dist2 = sum;//SetUp.graphMatrix.getPath(content[0], from).getWeight() + SetUp.graphMatrix.getPath(content[0], way.get(1)).getWeight() + sum + SetUp.graphMatrix.getPath(content[1], to).getWeight();
							this.way2.add(way.get(0));
							this.way2.add(content[0]);
							this.way2.addAll(way.subList(1, way.size() - 2));
							this.way2.add(content[1]);
							this.way2.add(way.get(way.size() - 1));
							//System.out.println(content + ".7");
							System.out.println(content + ".7 to " + agent.getAID().getLocalName() + " from " + msg.getSender().getLocalName());

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

	private class SendMsgToPass extends OneShotBehaviour {
		private Agent agent;

		public SendMsgToPass(Agent agent) {
			this.agent = agent;
		}

		@Override
		public void action() {
			//if (possiblePass != null) {
				ACLMessage msg = new ACLMessage(ACLMessage.CFP);
				msg.setConversationId("Agree");

				for (AID dr: drivers){
				msg.addReceiver(dr);
					msg.setContent(price + "");
				agent.send(msg);
				mutex.lock();
				System.out.println(agent.getLocalName() + " sends price = " + price + " to " + dr.getLocalName());
				mutex.unlock();}
			way = way2;
			dist = dist2;

		/*}
			else
			{
				mutex.lock();
				System.out.println(agent.getLocalName() + " goes alone");
				mutex.unlock();
			}*/
	}}


	private class GetPossibleDriver extends OneShotBehaviour {
			private ReceiverBehaviour.Handle handle;
			private Agent agent;

			public GetPossibleDriver(Agent agent, ReceiverBehaviour.Handle handle) {
				this.agent = agent;
				this.handle = handle;
			}

			@Override
			public void action() {

				try {
					ACLMessage msg = handle.getMessage();

					//System.out.println(msg);
					if (msg.getConversationId() == "Agree") {
						if (price < Double.parseDouble(msg.getContent())) {
							price = (Double.parseDouble(msg.getContent()));
							possibleDriver = (msg.getSender());

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
		private ReceiverBehaviour.Handle handle;
		private Agent agent;


		public SendMsgToDriver(Agent agent, ReceiverBehaviour.Handle handle) {
			this.agent = agent;
			this.handle = handle;
		}
		@Override
		public void action() {
			ACLMessage agreeMsg2 = new ACLMessage(ACLMessage.AGREE);
			if (possibleDriver != null) {
				agreeMsg2.addReceiver(possibleDriver);
				agreeMsg2.setConversationId("Confirm");
				agent.send(agreeMsg2);
				AID driver;
				if (price < utility) driver = agent.getAID();
				else driver = possibleDriver;
				System.out.println(agent.getLocalName() + ": goes with  " + possibleDriver.getLocalName()
						+ "; with economy " + utility + " driver is "+ driver.getLocalName());

				ACLMessage refuseMsg = new ACLMessage(ACLMessage.REFUSE);
				refuseMsg.setConversationId("Disagree");
				for (AID dr : drivers) {
					if (dr.getLocalName() != possibleDriver.getLocalName()) refuseMsg.addReceiver(dr);
					System.out.println(agent.getLocalName() + ": refuse agree from " + dr.getLocalName());
				}
				agent.send(refuseMsg);
				/*
				try {
					DFService.deregister(agent);
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
				*/
			}}}

	private class DriverBehaviour extends OneShotBehaviour {

				private Agent agent;

				public DriverBehaviour(Agent agent) {
					this.agent = agent;
				}

				@Override
				public void action() {
					SequentialBehaviour sb = new SequentialBehaviour(agent);

					ParallelBehaviour pb = new ParallelBehaviour();
					for (AID dr: drivers) {
						SequentialBehaviour handleMsg = new SequentialBehaviour(agent);
						final ReceiverBehaviour.Handle handle = ReceiverBehaviour.newHandle();
						handleMsg.addSubBehaviour(new ReceiverBehaviour(agent, handle, 5000, MessageTemplate.and(MessageTemplate.MatchSender(dr),(MessageTemplate.MatchConversationId("SendData")))));
						handleMsg.addSubBehaviour(new GetPossiblePass(agent, dr, handle));
						//handleMsg.addSubBehaviour(new SendMsgToPass(agent));
						pb.addSubBehaviour(handleMsg);
					}
					sb.addSubBehaviour(pb);
					//waitOthers();

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
			final ReceiverBehaviour.Handle handle = ReceiverBehaviour.newHandle();
			sb.addSubBehaviour(new ReceiverBehaviour(agent, handle, 5000, MessageTemplate.MatchConversationId("Agree")));
			sb.addSubBehaviour(new GetPossibleDriver(agent, handle));
			sb.addSubBehaviour(new SendMsgToDriver(agent, handle));
			addBehaviour(sb);

		}
	}



	}

