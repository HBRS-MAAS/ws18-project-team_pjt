package org.team_pjt.agents;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.team_pjt.Objects.Clock;
import org.team_pjt.Objects.Order;

import java.util.Collections;
import java.util.LinkedList;


@SuppressWarnings("serial")
public class CustomerAgent extends Agent {
	private String customerID;
	private AID[] bakeryAgents;
	private LinkedList<Order> orders;
	private LinkedList<Order> sendOrders;
	private Clock clock;

	protected void setup() {
		//TODO: different Customer Types
		Object[] args = getArguments();

		if(!readArgs(args)) {
			System.out.println("No parameter given " + getName());
			return;
		}
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			//e.printStackTrace();
		}

		clock = new Clock(0, 0);

		addBehaviour(new receiveClock());
		addBehaviour(new receiveKillMessgae());

		sendOrders = new LinkedList<>();

        addBehaviour(new CyclicBehaviour(this) {
            @Override
			public void action() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("bakery");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    bakeryAgents = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        bakeryAgents[i] = result[i].getName();
                    }
                }
                catch (FIPAException fe) {
                    System.out.println("Error searching bakery Agents");
                    fe.printStackTrace();
                }

                Collections.sort(orders);
				LinkedList<Order> ordersToDelete = new LinkedList<>();
				for (Order order : orders) {
					if(order.getOrderDate().compareTo(clock) == 0) {
						myAgent.addBehaviour(new sendOrder(order));
						sendOrders.add(order);
						ordersToDelete.add(order);
					}
				}
				for (Order order : ordersToDelete) {
					orders.remove(order);
				}
            }
        });

	}
	protected void takeDown() {
		System.out.println(getAID().getLocalName() + ": Terminating.");
	}

	private boolean readArgs(Object[] args) {
		if (args != null && args.length > 0) {
			JSONObject customer = new JSONObject(((String)args[0]).replaceAll("###", ","));
			customerID = customer.getString("guid");
			JSONArray jsonOrders = (new JSONObject(((String)args[1]).replaceAll("###", ","))).getJSONArray("orders");
			int numOrders = jsonOrders.length();
			this.orders = new LinkedList<>();
			for (int i = 0; i < numOrders; ++i) {
				this.orders.add(new Order(jsonOrders.getJSONObject(i).toString()));
//				System.out.println(this.orders.getLast().getOrderDate().toString());
			}
			return true;
		}
		return false;
	}

    // Taken from http://www.rickyvanrijn.nl/2017/08/29/how-to-shutdown-jade-agent-platform-programmatically/
	private class shutdown extends OneShotBehaviour{
		public void action() {
			ACLMessage shutdownMessage = new ACLMessage(ACLMessage.REQUEST);
			Codec codec = new SLCodec();
			myAgent.getContentManager().registerLanguage(codec);
			myAgent.getContentManager().registerOntology(JADEManagementOntology.getInstance());
			shutdownMessage.addReceiver(myAgent.getAMS());
			shutdownMessage.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
			shutdownMessage.setOntology(JADEManagementOntology.getInstance().getName());
			try {
			    myAgent.getContentManager().fillContent(shutdownMessage,new Action(myAgent.getAID(), new ShutdownPlatform()));
			    myAgent.send(shutdownMessage);
			}
			catch (Exception e) {
			    //LOGGER.error(e);
			}

		}
	}

	private class receiveClock extends CyclicBehaviour {

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE),
													 MessageTemplate.MatchConversationId("clock-update"));
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				String content = msg.getContent();
				int day = Integer.parseInt(content.substring(1, 4));
				int hour = Integer.parseInt(content.substring(5, 7));

				clock.setDay(day);
				clock.setHour(hour);
				System.out.println("clock received: " + clock.toString());
			}
			else {
				block();
			}
		}
	}

	private class sendOrder extends Behaviour {
        private int step = 0;
        private MessageTemplate mt;
        private Order order;
        private AID bestBakery;
        private float bestPrice;
		private int repliesCnt = 0;

        public sendOrder(Order order) {
        	super();
        	this.order = order;
		}

		@Override
		public void action() {
			switch(step) {
                case 0:
                    ACLMessage cfp = new ACLMessage((ACLMessage.CFP));
                    for (int i = 0; i < bakeryAgents.length; ++i) {
                        cfp.addReceiver(bakeryAgents[i]);
                    }
                    cfp.setContent(order.toJSONString());
                    cfp.setConversationId("bakery-order");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis());
                    myAgent.send(cfp);
                    System.out.println(getAID() + " send Call For Proposal");
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("bakery-order"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
				case 1:
					ACLMessage reply = myAgent.receive(mt);
					if (reply != null) {
						System.out.println("CFP answer received");
						if (reply.getPerformative() == ACLMessage.PROPOSE) {
							float price = Float.parseFloat(reply.getContent());
							if (bestBakery == null || price < bestPrice) {
								bestPrice = price;
								bestBakery = reply.getSender();
							}
						}
						repliesCnt++;
						if (repliesCnt >= bakeryAgents.length) {
							if (bestBakery == null) {
								System.out.println("No bakery for processing order found");
							}
							step = 2;
						}
					}
					else {
						block();
					}
            }
		}

		@Override
		public boolean done() {
			return step == 2;
		}
	}

	private class receiveProducts extends OneShotBehaviour {

		@Override
		public void action() {
			//TODO
		}
	}

	private class receiveKillMessgae extends CyclicBehaviour {

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE),
					MessageTemplate.MatchConversationId("kill"));
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				System.out.println("killing: " + myAgent.getAID());
				myAgent.addBehaviour(new shutdown());
				myAgent.doDelete();
			}
			else {
				block();
			}
		}
	}
}
