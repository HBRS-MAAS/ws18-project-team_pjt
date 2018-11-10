package org.team_pjt.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.*;
import org.team_pjt.objects.Clock;
import org.team_pjt.objects.Location;
import org.team_pjt.behaviours.*;

import java.util.*;

public class OrderAgent extends Agent /*implements  Comparable<Order>*/ {
    private String customer_id;
    private int type;
    private String customer_name;
    private Location location;
    private AID[] bakery_agents;
    private Clock system_clock;
    private String order_id;
    private Clock order_date;
    private Clock delivery_date;
    private Hashtable<String, Float> products;

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

        system_clock = new Clock(0, 0);

        addBehaviour(new receiveClock());
        addBehaviour(new receiveKillMessage());

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("bakery");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    bakery_agents = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        bakery_agents[i] = result[i].getName();
                    }
                }
                catch (FIPAException fe) {
                    System.out.println("Error searching bakery Agents");
                    fe.printStackTrace();
                }
                if(order_date.compareTo(system_clock) == 0) {
                    myAgent.addBehaviour(new getBakery());
                }
            }
        });

    }

    private class getBakery extends Behaviour {
        private int step = 0;
        private MessageTemplate mt;
        private AID bestBakery;
        private float bestPrice;
        private int repliesCnt = 0;

        @Override
        public void action() {
            switch(step) {
                case 0:
                    ACLMessage cfp = new ACLMessage((ACLMessage.CFP));
                    for (int i = 0; i < bakery_agents.length; ++i) {
                        cfp.addReceiver(bakery_agents[i]);
                    }

                    JSONObject simpleOrder = new JSONObject();
                    simpleOrder.put("order_id", order_id);
                    JSONObject d_date = new JSONObject();
                    d_date.put("day", delivery_date.getDay());
                    d_date.put("hour", delivery_date.getHour());
                    simpleOrder.put("delivery_date", d_date);

                    JSONObject json_products = new JSONObject();
                    Iterator<String> product_keys_iterator = products.keySet().iterator();
                    for(int i = 0; i < products.size(); ++i) {
                        String product_key = product_keys_iterator.next();
                        json_products.put(product_key, products.get(product_key));
                    }

                    simpleOrder.put("products", json_products);

                    cfp.setContent(simpleOrder.toString());
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
                        if (repliesCnt >= bakery_agents.length) {
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

    private boolean readArgs(Object[] args) {
        if (args != null && args.length > 0) {
            JSONObject customer = new JSONObject(((String)args[1]).replaceAll("###", ","));
            customer_id = customer.getString("guid");
            type = customer.getInt("type");
            customer_name = customer.getString("name");
            location = new Location(customer.getJSONObject("location").getFloat("x"), customer.getJSONObject("location").getFloat("y"));

            readJSONOrder(((String)args[0]).replaceAll("###", ","));

            return true;
        }
        return false;
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

                system_clock.setDay(day);
                system_clock.setHour(hour);
            }
            else {
                block();
            }
        }
    }

    public void readJSONOrder(String jsonOrderString) {
        JSONObject jsonOrder = new JSONObject(jsonOrderString);
        this.order_id = jsonOrder.getString("guid");
        this.customer_id = jsonOrder.getString("customer_id");

        int day =(jsonOrder.getJSONObject("order_date").getInt("day"));
        int hour = (jsonOrder.getJSONObject("order_date").getInt("hour"));

        this.order_date = new Clock(day, hour);

        day = (jsonOrder.getJSONObject("delivery_date").getInt("day"));
        hour = (jsonOrder.getJSONObject("delivery_date").getInt("hour"));

        this.delivery_date = new Clock(day, hour);

        this.products = new Hashtable<>();
        JSONObject jsonProducts = jsonOrder.getJSONObject("products");
        String[] listProducts = JSONObject.getNames(jsonProducts);
        for (int i = 0; i < listProducts.length; ++i) {
            this.products.put(listProducts[i], new Float(jsonProducts.getFloat(listProducts[i])));
        }
    }
}
