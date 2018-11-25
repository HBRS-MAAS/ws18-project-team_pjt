package org.team_pjt.agents;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.team_pjt.behaviours.shutdown;
import org.team_pjt.objects.Location;
import org.team_pjt.objects.Order;
import org.team_pjt.objects.Product;

import javax.swing.*;
import java.util.*;

public class SchedulerAgent extends BaseAgent {
    private String sBakeryId;
    private Location lLocation;
    private HashMap<String, Float> hmPrepTables;
    private HashMap<String, Float> hmKneadingMachine;
    private HashMap<String, Product> hmProducts; // = Available Products
    private HashMap<Integer, Order> scheduledOrders;
    private AID order_processing;
    private int endDays;
    boolean newOrderScheduled = true;

    protected void setup(){
        super.setup();
        Object[] oArguments = getArguments();
        if (!readArgs(oArguments)) {
            System.out.println("No parameter given for OrderProcessing " + getName());
        }
        this.register("scheduler", getName().split("@")[0]);
        findOrderProcessing();
        scheduledOrders = new HashMap<>();

        addBehaviour(new isNewOrderChecker());

        System.out.println("SchedulerAgent is ready");
    }

    private void findOrderProcessing() {
        DFAgentDescription[] dfSchedulerAgentResult = new DFAgentDescription[0];
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setName("bakery-"+sBakeryId.split("-")[1]);
        template.addServices(sd);
        while (dfSchedulerAgentResult.length == 0) {
            try {
                dfSchedulerAgentResult = DFService.search(this, template);
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }
        order_processing = dfSchedulerAgentResult[0].getName();
        System.out.println("OrderProcessing found! - " + order_processing);
    }

    private class isNewOrderChecker extends Behaviour {
        boolean isDone = false;
        @Override
        public void action() {
            if(!getAllowAction()) {
                return;
            }
            MessageTemplate mtNewOrder = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchSender(order_processing));
            ACLMessage newOrder = myAgent.receive(mtNewOrder);
            if(newOrder != null) {
                if(newOrder.getContent().toUpperCase().equals("NO NEW ORDER")) {
//                    System.out.println(myAgent.getName() + " called finished()");
                    finished();
                }
                else {
                    myAgent.addBehaviour(new receiveOrder());
                }
                myAgent.addBehaviour(new isNewOrderChecker());
                isDone = true;
            }
            else {
                block();
            }
        }

        @Override
        public boolean done() {
            return isDone;
        }
    }

    private class receiveOrder extends Behaviour {
        boolean isDone = false;
        @Override
        public void action() {
            if(!getAllowAction()) {
                return;
            }
            if(getCurrentDay() >= endDays) {
                addBehaviour(new shutdown());
            }
            ACLMessage schedule_request = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
            if(schedule_request != null) {
                newOrderScheduled = false;
                System.out.println("schedule request received!");
                String sContent = schedule_request.getContent();
                JSONObject jsoProducts = new JSONObject(sContent);
                int delivery_day = jsoProducts.getJSONObject("deliveryDate").getInt("day");
                ACLMessage schedule_reply = schedule_request.createReply();
                System.out.println(schedule_reply.getAllReceiver().next());
                if(scheduledOrders.containsKey(delivery_day)) {
                    schedule_reply.setPerformative(ACLMessage.DISCONFIRM);
                    schedule_reply.setContent("Scheduling impossible!");
                    sendMessage(schedule_reply);
                }
                else {
                    schedule_reply.setPerformative(ACLMessage.CONFIRM);
                    schedule_reply.setContent("Scheduling possible!");
                    sendMessage(schedule_reply);
                    System.out.println("schedule reply sent!");
                }
                newOrderScheduled = true;
                isDone = true;
            }
            else {
                block();
            }
            if(newOrderScheduled) {
//                System.out.println(myAgent.getName() + " called finished()");
                finished();
            }
        }

        @Override
        public boolean done() {
            return isDone;
        }
    }

    private class getAcceptedProposal extends CyclicBehaviour {

        @Override
        public void action() {
            if(!getAllowAction()) {
                return;
            }
            MessageTemplate accepted_proposalMT = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE),
                    MessageTemplate.MatchSender(order_processing));
            ACLMessage accepted_proposal = receive(accepted_proposalMT);
            if(accepted_proposal != null) {
                Order order = new Order(accepted_proposal.getContent());
                scheduledOrders.put(order.getDeliveryDay(), order);
                scheduledOrders = sortOrders(scheduledOrders);
                System.out.println("Order added");
            }
            else {
                block();
            }
        }
    }

    private class QueueRequestServer extends CyclicBehaviour {
        // TODO
        @Override
        public void action() {

        }
    }

    public static HashMap<Integer, Order> sortOrders(HashMap<Integer, Order> hm) {
        List<Map.Entry<Integer, Order>> orders = new LinkedList<Map.Entry<Integer, Order>>(hm.entrySet());

        Collections.sort(orders, new Comparator<Map.Entry<Integer, Order> >() {
            @Override
            public int compare(Map.Entry<Integer, Order> o1, Map.Entry<Integer, Order> o2) {
                if(o1.getKey() < o2.getKey()) {
                    return -1;
                }
                if(o1.getKey() > o2.getKey()) {
                    return 1;
                }
                return 0;
            }
        });

        // put data from sorted list to hashmap
        HashMap<Integer, Order> temp = new LinkedHashMap<>();
        for (Map.Entry<Integer, Order> aa : orders) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    private boolean readArgs(Object[] oArgs){
        if(oArgs != null && oArgs.length > 0){
            hmProducts = new HashMap<>();
            JSONObject bakery = new JSONObject(((String)oArgs[0]).replaceAll("###", ","));
            JSONArray products = bakery.getJSONArray("products");
            Iterator<Object> product_iterator = products.iterator();

            sBakeryId = bakery.getString("guid");

            while(product_iterator.hasNext()) {
                JSONObject jsoProduct = (JSONObject) product_iterator.next();
                Product product = new Product(jsoProduct.toString());
                hmProducts.put(product.getGuid(), product);
            }
            JSONObject jsoLocation = bakery.getJSONObject("location");
            lLocation = new Location(jsoLocation.getDouble("y"), jsoLocation.getDouble("x"));

            JSONObject meta_data = new JSONObject(((String)oArgs[1]).replaceAll("###", ","));
            this.endDays = meta_data.getInt("durationInDays");

            return true;
        }
        else {
            return false;
        }
    }
}
