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
import org.json.JSONArray;
import org.json.JSONObject;
import org.team_pjt.behaviours.receiveKillMessage;
import org.team_pjt.objects.Location;
import org.team_pjt.objects.Product;

import java.util.*;

public class OrderProcessing extends Agent {
    private Hashtable<String, Float> available_products;
    private Location location;
    private String bakery_guid;
    private String bakery_name;

    private Hashtable<String, List<Product>> assigned_orders;
    private List<AID> accepted_order_agents;

    protected void setup() {
        Object[] args = getArguments();

        if(!readArgs(args)) {
            System.out.println("No parameter given " + getName());
            return;
        }
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
            JSONObject bakery = new JSONObject(((String)args[1]).replaceAll("###", ","));
            JSONArray products = bakery.getJSONArray("products");
            Iterator<Object> product_iterator = products.iterator();

            bakery_guid = bakery.getString("guid");
            bakery_name = bakery.getString("name");
            location = new Location(bakery.getJSONObject("location").getFloat("x"), bakery.getJSONObject("location").getFloat("y"));

            while(product_iterator.hasNext()) {
                JSONObject product = (JSONObject)product_iterator.next();
                available_products.put(product.getString("guid"), product.getFloat("sales_price"));
            }

            return true;
        }
        return false;
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

                JSONObject json_needed_products = simple_order.getJSONObject("products");
                Hashtable<String, Integer> needed_products = new Hashtable<>();
                Iterator<String> needed_product_iterator = json_needed_products.keySet().iterator();
                while (needed_product_iterator.hasNext()) {
                    String product = needed_product_iterator.next();
                    needed_products.put(product, json_needed_products.getInt(product));
                }

                float price = 0f;
                if (available_products.keySet().containsAll(needed_products.keySet())) {
                    for (String pr: needed_products.keySet()) {
                        price += available_products.get(pr);
                    }
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price));
                }
                else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                    System.out.println("Some products not available");
                }
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
