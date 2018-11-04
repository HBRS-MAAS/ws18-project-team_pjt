package org.team_pjt.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.team_pjt.Objects.Bakery;
import org.team_pjt.Objects.Order;

import java.util.LinkedList;
import java.util.Set;

public class OrderProcessing extends Agent {
    private Bakery bakery;
    private LinkedList<Order> orders;

    protected void setup() {
        addBehaviour(new receiveOrders());
    }
    protected void takeDown() {
        System.out.println(getAID().getLocalName() + ": Terminating.");
    }

    private boolean readArgs(Object[] args) {
        if (args != null && args.length > 0) {

        }
        return true;
    }

    private class receiveOrders extends CyclicBehaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
                                                     MessageTemplate.MatchConversationId("bakery-order"));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                Order order = new Order(msg.getContent());

                ACLMessage reply = msg.createReply();

                Set<String> neededProducts = order.getProducts().keySet();
                Set<String> availableProducts = bakery.getAvailableProducts().keySet();
                float price = 0;
                if (availableProducts.containsAll(neededProducts)) {
                    for (String pr: neededProducts) {
                        price += bakery.getAvailableProducts().get(pr).getSalesPrice();
                    }
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price));
                }
                else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                    System.out.println("Some products not available");
                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }
}

    // Taken from http://www.rickyvanrijn.nl/2017/08/29/how-to-shutdown-jade-agent-platform-programmatically/
//    private class shutdown extends OneShotBehaviour {
//        public void action() {
//            ACLMessage shutdownMessage = new ACLMessage(ACLMessage.REQUEST);
//            Codec codec = new SLCodec();
//            myAgent.getContentManager().registerLanguage(codec);
//            myAgent.getContentManager().registerOntology(JADEManagementOntology.getInstance());
//            shutdownMessage.addReceiver(myAgent.getAMS());
//            shutdownMessage.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
//            shutdownMessage.setOntology(JADEManagementOntology.getInstance().getName());
//            try {
//                myAgent.getContentManager().fillContent(shutdownMessage,new Action(myAgent.getAID(), new ShutdownPlatform()));
//                myAgent.send(shutdownMessage);
//            }
//            catch (Exception e) {
//                //LOGGER.error(e);
//            }
//
//        }
//    }
//}
