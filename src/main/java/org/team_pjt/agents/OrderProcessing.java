package org.team_pjt.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.JSONObject;
import org.team_pjt.behaviours.receiveKillMessage;
import org.team_pjt.objects.Location;
import org.team_pjt.objects.Product;

import java.util.*;

public class OrderProcessing extends Agent {
    private Hashtable<String, Float> available_products;
    private List<AID> ovens;
    private List<AID> trucks;
    private Location location;
    private String bakery_guid;
    private String bakery_name;

    private Hashtable<String, List<Product>> assigned_orders;
    private List<AID> accepted_order_agents;

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
                ACLMessage reply = msg.createReply();
                JSONObject simple_order = new JSONObject(msg.getContent());
                System.out.println("CFP received");

                Set<String> needed_products = simple_order.getJSONObject("products").keySet();


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
}
