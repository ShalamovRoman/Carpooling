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

public class TravellerAgent extends Agent {
	private double dist;
	private String from;
	private String to;
	private int seats;
	private int cnt = 0;
	private int maxcnt = 1;
	private double utility = 0;
	private double utility2 = 0;
	private AID possiblePass;
	private AID possibleDriver;
	private HashSet<AID> drivers = new HashSet<>();
	private List<String> way = new ArrayList<>();
	private List<String> way2 = new ArrayList<>();
	private double dist2;
	private double price;
	private double procent = 0.65;
	private HashSet<AID> driversToWait = new HashSet<>();


	protected void setup() {

		Object[] args = getArguments();
		from = (String) args[0];
		to = (String) args[1];
		way.add(from);
		way.add(to);
		seats = Integer.parseInt((String)args[2]);
		dist = SetUp.graphMatrix.getPath(from, to).getWeight();
		System.out.println(getAID().getLocalName() + ": from " + from + " to " + to + ", " + seats + " free seats, dist = " + dist);

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

	private class StartCycle extends OneShotBehaviour {
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
			driversToWait = drivers;
			ParallelBehaviour pb = new ParallelBehaviour();
			pb.addSubBehaviour(new SendData(agent));
			pb.addSubBehaviour(new ProcessMessage(agent));
			addBehaviour(pb);
				cnt++;
			}
		}
	}
	private class ProcessMessage extends SequentialBehaviour {
		public ProcessMessage(Agent agent) {
			ParallelBehaviour pb = new ParallelBehaviour(agent, ParallelBehaviour.WHEN_ALL);
			pb.addSubBehaviour(new DriverBehaviour(agent));
			pb.addSubBehaviour(new PassengerBehaviour(agent));
			addBehaviour(pb);
		}
	}
	private class SendData extends OneShotBehaviour{
		private Agent agent;


		public SendData( Agent agent) {

			this.agent = agent;
		}
		@Override
		public void action() {
	//	String replyWith = String.valueOf(System.currentTimeMillis());
		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
			msg.removeReceiver(getAID());
			msg.setContent(from + " " + to + " " + dist);
			msg.setConversationId("SendContent");
		//	msg.setReplyWith(replyWith);
			for (AID dr : drivers) {
			msg.addReceiver(dr);
			System.out.println(agent.getAID().getLocalName() + " sends " + msg.getContent() + " to " + dr.getLocalName());
				agent.send(msg);
			}

	}}
	private class GetPossiblePass extends CyclicBehaviour {
		private ReceiverBehaviour.Handle handle;
		private Agent agent;


		public GetPossiblePass( Agent agent, ReceiverBehaviour.Handle handle) {

			this.agent = agent;
			this.handle = handle;
		}
		@Override
		public void action() {

			if (driversToWait.size() > 0) {
				 try {
					ACLMessage msg = handle.getMessage();
					if (msg.getConversationId() == "SendContent" && msg.getContent() != null && driversToWait.contains(msg.getSender())) {
						System.out.println(agent.getAID().getLocalName() + " got " + msg.getContent() + " from " + msg.getSender().getLocalName());
						driversToWait.remove(msg.getSender());
						String[] content = msg.getContent().split(" ");
						if ((way.contains(content[0])) && (way.contains(content[1]))) {
							if (way.indexOf(content[0]) < way.indexOf(content[1])) {
								way2 = way;
								CountUtility(agent, content, msg.getSender());
							}
						} else if ((way.contains(content[0])) && (!way.contains(content[1]))) {
							if (way.size() == 2) {
								dist2 = SetUp.graphMatrix.getPath(content[0], from).getWeight() + SetUp.graphMatrix.getPath(to, content[1]).getWeight() + Double.parseDouble(content[2]);
								way2.add(from);
								way2.add(content[1]);
								way2.add(to);
								CountUtility(agent, content, msg.getSender());
							} else {
								dist2 = dist + SetUp.graphMatrix.getPath(to, content[1]).getWeight();
								way2 = way.subList(0, way.size() - 2);
								way2.add(content[1]);
								way2.add(way.get(way.size() - 1));// +- пара индексов
								CountUtility(agent, content, msg.getSender());
							}
						} else if (!(way.contains(content[0])) && (way.contains(content[1]))) {
							if (way.size() == 2) {
								dist2 = SetUp.graphMatrix.getPath(content[0], from).getWeight() + SetUp.graphMatrix.getPath(to, content[1]).getWeight() + Double.parseDouble(content[2]);
								way2.add(from);
								way2.add(content[0]);
								way2.add(to);
								CountUtility(agent, content, msg.getSender());
							} else {
								double sum = 0;
								for (int i = 1; i < way.size() - 1; i++) {
									sum += SetUp.graphMatrix.getPath(way.get(i), way.get(i + 1)).getWeight();
								}
								dist2 = SetUp.graphMatrix.getPath(content[0], from).getWeight() + SetUp.graphMatrix.getPath(content[0], way.get(1)).getWeight() + sum;
								way2.add(way.get(0));
								way2.add(content[0]);
								way2.addAll(way.subList(1, way.size() - 1));
								CountUtility(agent, content, msg.getSender());
							}
						} else {
							if (way.size() == 2) {
								dist2 = SetUp.graphMatrix.getPath(content[0], from).getWeight() + SetUp.graphMatrix.getPath(to, content[1]).getWeight() + Double.parseDouble(content[2]);
								way2.add(from);
								way2.add(content[0]);
								way2.add(content[1]);
								way2.add(to);
								CountUtility(agent, content, msg.getSender());
							} else {
								double sum = 0;
								for (int i = 1; i < way2.size() - 1; i++) {
									sum += SetUp.graphMatrix.getPath(way2.get(i), way2.get(i + 1)).getWeight();
								}

								dist2 = sum;//SetUp.graphMatrix.getPath(content[0], from).getWeight() + SetUp.graphMatrix.getPath(content[0], way.get(1)).getWeight() + sum + SetUp.graphMatrix.getPath(content[1], to).getWeight();
								way2.add(way.get(0));
								way2.add(content[0]);
								way2.addAll(way.subList(1, way.size() - 2));
								way2.add(content[1]);
								way2.add(way.get(way.size() - 1));
								CountUtility(agent, content, msg.getSender());
							}
						}
					}
/*					double commonDist = SetUp.graphMatrix.getPath(from, content[0]).getWeight() + SetUp.graphMatrix.getPath(to, content[1]).getWeight() + Double.parseDouble(content[2]);
					if (commonDist < dist + Double.parseDouble((content[2]))) {
						if (utility < dist + Double.parseDouble(content[2]) - commonDist) {
							utility = (dist + Double.parseDouble(content[2]) - commonDist);
							possiblePass = (msg.getSender());
						}
					} else {
						ACLMessage refuseMsg =  new ACLMessage(ACLMessage.REFUSE);
						refuseMsg.setConversationId("Disagree");
						refuseMsg.addReceiver(msg.getSender());
						agent.send(refuseMsg);
						System.out.println(agent.getLocalName() + ": refuse agree from " + msg.getSender().getLocalName());
					}*/

				} catch (ReceiverBehaviour.TimedOut timedOut) {
					//System.out.println(agent.getLocalName() + ": time out while receiving message");
				} catch (ReceiverBehaviour.NotYetReady notYetReady) {
					//System.out.println(agent.getLocalName() + ": message not yet ready");
					//notYetReady.printStackTrace();
				}
				//count--;
			}

		}
	}
	private class SendMsgToPass extends OneShotBehaviour {
		private ReceiverBehaviour.Handle handle;
		private Agent agent;

		public SendMsgToPass(Agent agent, ReceiverBehaviour.Handle handle) {
			this.agent = agent;
			this.handle = handle;
		}

		@Override
		public void action() {
			if (possiblePass != null) {
			ACLMessage agreeMsg = new ACLMessage(ACLMessage.AGREE);
			agreeMsg.addReceiver(possiblePass);
			agreeMsg.setContent(utility + "");
			agreeMsg.setConversationId("Agree");
			//System.out.println((agreeMsg));
			agent.send(agreeMsg);
			System.out.println(agent.getLocalName() + ": wants to go with " + possiblePass.getLocalName());
			way = way2;
			dist = dist2;

		}
			else System.out.println(agent.getLocalName() + " goes alone");
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

				/*CLMessage refuseMsg = new ACLMessage(ACLMessage.REFUSE);
				refuseMsg.setConversationId("Disagree");
				for (AID dr : drivers) {
					if (dr.getLocalName() != possibleDriver.getLocalName()) refuseMsg.addReceiver(dr);
					System.out.println(agent.getLocalName() + ": refuse agree from " + dr.getLocalName());
				}
				agent.send(refuseMsg);*/
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
					final ReceiverBehaviour.Handle handle = ReceiverBehaviour.newHandle();
					sb.addSubBehaviour(new ReceiverBehaviour(agent, handle, 5000, MessageTemplate.MatchConversationId("SendContent")));
					sb.addSubBehaviour(new GetPossiblePass(agent, handle));
					sb.addSubBehaviour(new SendMsgToPass(agent, handle));
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

	private void  CountUtility (Agent agent, String[] content, AID sender) {
		if (dist2 < dist + Double.parseDouble(content[2])) {
					if (utility < dist + Double.parseDouble(content[2]) - dist2) {
						utility = dist + Double.parseDouble(content[2]) - dist2;
						int sum = 0;
						for (int i = way2.indexOf(content[0]); i < way2.indexOf(content[1]) - 1; i++) {
							sum += SetUp.graphMatrix.getPath(way.get(i), way.get(i + 1)).getWeight();
						}
						price = procent * (sum - utility);
						possiblePass = sender;
					}//utility = 0
				}
				if (possiblePass != null) {
					ACLMessage msg = new ACLMessage(ACLMessage.CFP);
					msg.setConversationId("SendPrice");
					msg.setContent(price + "");
					msg.addReceiver(possiblePass);
					agent.send(msg);
				}
			}

	}

