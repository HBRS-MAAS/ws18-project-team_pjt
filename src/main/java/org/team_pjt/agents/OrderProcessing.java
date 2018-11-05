package org.team_pjt.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
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
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("bakery");
        sd.setName("test"); //TODO
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
        addBehaviour(new receiveKillMessage());

        addBehaviour(new receiveOrders());
    }

    private boolean readArgs(Object[] args) {
        if (args != null && args.length > 0) {

        }
        return true;
    }

    private class receiveOrders extends CyclicBehaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CFP),
                                                     MessageTemplate.MatchConversationId("bakery-order"));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                Order order = new Order(msg.getContent());
                System.out.println("CFP received");

                ACLMessage reply = msg.createReply();

                Set<String> neededProducts = order.getProducts().keySet();
//                Set<String> availableProducts = bakery.getAvailableProducts().keySet(); TODO
                float price = 23.56f;
//                if (availableProducts.containsAll(neededProducts)) {
//                    for (String pr: neededProducts) {
//                        price += bakery.getAvailableProducts().get(pr).getSalesPrice();
//                    }
//                    reply.setPerformative(ACLMessage.PROPOSE);
//                    reply.setContent(String.valueOf(price));
//                }
//                else {
//                    reply.setPerformative(ACLMessage.REFUSE);
//                    reply.setContent("not-available");
//                    System.out.println("Some products not available");
//                }
                reply.setPerformative(ACLMessage.PROPOSE);
                reply.setContent(String.valueOf(price));
                myAgent.send(reply);
                System.out.println("PROPOSE send!");
            }
            else {
                block();
            }
        }
    }

    private class receiveKillMessage extends CyclicBehaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE),
                    MessageTemplate.MatchConversationId("kill"));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                System.out.println("killing: " + myAgent.getAID());
                try {
                    DFService.deregister(myAgent);
                }
                catch (FIPAException fe) {
                    fe.printStackTrace();
                }
                myAgent.doDelete();
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
