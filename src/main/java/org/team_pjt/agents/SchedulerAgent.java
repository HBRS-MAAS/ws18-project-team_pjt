package org.team_pjt.agents;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.team_pjt.behaviours.shutdown;
import org.team_pjt.objects.Order;
import org.team_pjt.objects.Product;

import java.util.*;

public class SchedulerAgent extends BaseAgent {
    private String sBakeryId;
//    private Location lLocation;
    private HashMap<String, Float> hmPrepTables;
    private HashMap<String, Float> hmKneadingMachine;
    private HashMap<String, Product> hmProducts; // = Available Products
    private HashMap<Integer, Order> scheduledOrders;
    private AID order_processing;
    private int endDays;

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
        addBehaviour(new QueueRequestServer());

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
                    //System.out.println(myAgent.getName() + " called finished()");
                    while(myAgent.receive() != null){}
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
        private boolean isDone = false;
        private int step = 0;
        @Override
        public void action() {
            if(!getAllowAction()) {
                return;
            }
            if(getCurrentDay() >= endDays) {
                addBehaviour(new shutdown());
            }
            switch (step) {
                case 0:
                    ACLMessage schedule_request = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
                    if (schedule_request != null) {
                        System.out.println("schedule request received!");
                        String sContent = schedule_request.getContent();
                        JSONObject jsoProducts = new JSONObject(sContent);
                        int delivery_day = jsoProducts.getJSONObject("deliveryDate").getInt("day");
                        ACLMessage schedule_reply = schedule_request.createReply();
                        System.out.println(schedule_reply.getAllReceiver().next());
                        if (scheduledOrders.containsKey(delivery_day)) {
                            schedule_reply.setPerformative(ACLMessage.DISCONFIRM);
                            schedule_reply.setContent("Scheduling impossible!");
                            sendMessage(schedule_reply);
                        } else {
                            schedule_reply.setPerformative(ACLMessage.CONFIRM);
                            schedule_reply.setContent("Scheduling possible!");
                            sendMessage(schedule_reply);
                            System.out.println("schedule reply sent!");
                        }
                        step++;
                    } else {
                        block();
                    }
                case 1:
                    MessageTemplate accepted_proposalMT = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE),
                            MessageTemplate.MatchSender(order_processing));
                    ACLMessage accepted_proposal = receive(accepted_proposalMT);
                    if(accepted_proposal != null) {
                        Order order = new Order(accepted_proposal.getContent());
                        scheduledOrders.put(order.getDeliveryDay(), order);
                        scheduledOrders = sortOrders(scheduledOrders);
                        System.out.println("Order added");
                        System.out.println("accept proposal received");
                        AID[] allAgents = findAllAgents();
                        ACLMessage propagate_accepted_order = new ACLMessage(ACLMessage.PROPAGATE);

                        List<Order> orders = new LinkedList<>(scheduledOrders.values());
                        JSONArray sortedOrders = new JSONArray();

                        for(Order o : orders) {
                            sortedOrders.put(new JSONObject(o.toJSONString()));
                        }

                        propagate_accepted_order.setContent(sortedOrders.toString());
                        for(AID agent : allAgents) {
                            propagate_accepted_order.addReceiver(agent);
                        }
//                        try {
//                            System.out.println("Now SchedulerAgent " + getName() + " sleeps");
//                            Thread.sleep(10000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                        sendMessage(propagate_accepted_order);
                        System.out.println("Scheduler Agent Propagated all scheduled Orders");
                        step++;
                    }
                    else {
                        block();
                    }
            }
        }

        @Override
        public boolean done() {
            isDone = step >= 2;
            if(isDone) {
                finished();
            }
            return isDone;
        }

        private AID[] findAllAgents() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            template.addServices(sd);
            AID[] allAgents;
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                allAgents = new AID[result.length];
                int counter = 0;
                for(DFAgentDescription ad : result) {
                    allAgents[counter] = ad.getName();
                    counter++;
                }
            }
            catch (FIPAException fe) {
                fe.printStackTrace();
                allAgents = new AID[0];
            }
            return allAgents;
        }

    }

    private class QueueRequestServer extends CyclicBehaviour {
        // TODO
        @Override
        public void action() {
            MessageTemplate mtQueueRequest = MessageTemplate.and(MessageTemplate.MatchConversationId("queue request"),
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
            ACLMessage queue_request = myAgent.receive(mtQueueRequest);
            if(queue_request != null) {
                String order_id = queue_request.getContent();
                int pos = 0;

                Iterator<Integer> order_date_iterator = scheduledOrders.keySet().iterator();
                boolean found = false;
                while(order_date_iterator.hasNext()) {
                    int day = order_date_iterator.next();
                    if(scheduledOrders.get(day).getGuid().equals(order_id)) {
                        found = true;
                        break;
                    }
                    pos++;
                }
                ACLMessage reply = queue_request.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                if(found) {
                    reply.setContent(Integer.toString(pos));
                }
                else {
                    reply.setContent(Integer.toString(-1));
                }
                sendMessage(reply);
            }
            else {
                block();
            }
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
//            JSONObject jsoLocation = bakery.getJSONObject("location");
//            lLocation = new Location(jsoLocation.getDouble("y"), jsoLocation.getDouble("x"));

            JSONObject meta_data = new JSONObject(((String)oArgs[1]).replaceAll("###", ","));
            this.endDays = meta_data.getInt("durationInDays");

            return true;
        }
        else {
            return false;
        }
    }
}
