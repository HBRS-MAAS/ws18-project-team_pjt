package org.team_pjt.agents;

import jade.core.AID;
import jade.core.behaviours.*;
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

import java.util.*;

public class ClientDummy extends BaseAgent {
    private AID[] aidSchedulerAgents;
    private List<Order> ordersToSent;
    private List<Order> ordersSent;
    private List<Order> ordersReceived;
    private String guid;
    private String name;
    private int type;
    private Location location;
    private int endDays;
    private boolean placingOrder = false;

    protected void setup(){
        super.setup();
        Object[] oArguments = getArguments();
        if(!readArgs(oArguments)){
            System.out.println("No parameters given for ClientDummy " + getName());
        }
        register("customer", guid);
        ordersSent = new LinkedList<>();
        addBehaviour(new OrderTimeChecker());
    }

    private boolean readArgs(Object[] oArguments) {
        JSONObject joClient;
        if(oArguments != null && oArguments.length > 0){
            String client_string = ((String)oArguments[0]).replaceAll("###", ",");
            joClient = new JSONObject(client_string);
            this.guid = joClient.getString("guid");
            this.name = joClient.getString("name");
            this.type = joClient.getInt("type");
            this.location = new Location(joClient.getJSONObject("location").getDouble("y"),joClient.getJSONObject("location").getDouble("x"));

            ordersToSent = new LinkedList<>();

            Iterator<Object> order_iterator = joClient.getJSONArray("orders").iterator();
            while(order_iterator.hasNext()) {
                JSONObject joOrder = (JSONObject)order_iterator.next();
                ordersToSent.add(new Order(joOrder.toString()));
            }

            Collections.sort(ordersToSent, new Comparator<Order>() {
                @Override
                public int compare(Order o1, Order o2) {
                    if(o1.getOrderDay() < o2.getOrderDay()) {
                        return -1;
                    }
                    else if(o1.getOrderDay() == o2.getOrderDay() && o1.getOrderHour() < o2.getOrderHour()) {
                        return -1;
                    }
                    else if(o1.getOrderDay() == o2.getOrderDay() && o1.getOrderHour() == o2.getOrderHour()) {
                        return 0;
                    }
                    return 1;
                }
            });

            JSONObject meta_data = new JSONObject(((String)oArguments[1]).replaceAll("###", ","));
            this.endDays = meta_data.getInt("durationInDays");

            return true;
        }
        return false;
    }

    private void sendNoNewOrderMessage() {
        AID[] orderProcessingAgents = findOrderProcessingAgents();
        ACLMessage cfp = new ACLMessage(ACLMessage.INFORM);
        cfp.setConversationId("syncing");
        cfp.setContent("no new Order");
        for(AID agent : orderProcessingAgents) {
            cfp.addReceiver(agent);
        }
        sendMessage(cfp);
//        System.out.println(this.getName() + " called finished()");
        finished();
        addBehaviour(new OrderTimeChecker());
    }

    private class OrderTimeChecker extends Behaviour {
        boolean oneMessageSend = false;
        @Override
        public void action() {
            if(!getAllowAction()) {
                return;
            }
            if(getCurrentDay() >= endDays) {
                deRegister();
                addBehaviour(new shutdown());
            }
            if(!ordersToSent.isEmpty() && ordersToSent.get(0).getOrderDay() == getCurrentDay() && ordersToSent.get(0).getOrderHour() == getCurrentHour()) {
                placingOrder = true;
                myAgent.addBehaviour(new RequestPerformer(ordersToSent.get(0)));
                ordersSent.add(ordersToSent.remove(0));
                oneMessageSend = true;
            }
            else {
                sendNoNewOrderMessage();
                oneMessageSend = true;
            }
        }

        @Override
        public boolean done() {
            return oneMessageSend;
        }
    }

    private class RequestPerformer extends Behaviour {
        private AID[] orderProcessingAgents;
        private Order order;
        private Hashtable<String, Hashtable<String, Double>> proposedPrices;
        private int step = 0;

        public RequestPerformer(Order order) {
            super();
            proposedPrices = new Hashtable<>();
            this.order = order;
        }

        @Override
        public void action() {
            if(!getAllowAction()) {
                return;
            }

            switch (step) {
                case 0:
                    orderProcessingAgents = findOrderProcessingAgents();
                    sendCallForProposal();
                    step++;
                    break;
                case 1:
                    receiveProposals();
                    break;
//                case 2:
//                    sendAcceptedProposals();
//                    break;
            }
        }

        @Override
        public boolean done() {
            boolean isDone = step >= 2;
            if(isDone) {
                placingOrder = false;
//                System.out.println(myAgent.getName() + " called finished()");
                finished();
                myAgent.addBehaviour(new OrderTimeChecker());
            }

            return isDone;
        }

        private void sendCallForProposal() {
            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            cfp.setConversationId(order.getGuid());
            cfp.setContent(order.toJSONString());
            for(AID agent : orderProcessingAgents) {
                cfp.addReceiver(agent);
            }
            sendMessage(cfp);
            System.out.println("day: " + getCurrentDay() + " hour: " + getCurrentHour() + " cfp send " + order.getGuid());
        }

        private void receiveProposals() {
            int proposalCounter = 0;
            MessageTemplate proposalTemplate = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
                    MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
            while(proposalCounter < orderProcessingAgents.length) {
                ACLMessage proposal = myAgent.receive(proposalTemplate);
                if (proposal != null) {
                    proposalCounter++;
                    if(proposal.getPerformative() == ACLMessage.REFUSE) {
                        System.out.println("Bakery " + proposal.getSender() + " refused call for proposal with message:");
                        System.out.println(proposal.getContent());
                    }
                    else {
                        System.out.println("proposal received!");
                        Hashtable<String, Double> available_products = new Hashtable<>();
                        JSONObject order = new JSONObject(proposal.getContent());
                        for(String product_name : order.getJSONObject("products").keySet()) {
                            available_products.put(product_name, order.getJSONObject("products").getDouble(product_name));
                        }
                        proposedPrices.put(proposal.getSender().getName(), available_products);
                        ACLMessage accept_proposal = proposal.createReply();
                        accept_proposal.setContent(proposal.getContent());
                        accept_proposal.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        sendMessage(accept_proposal);
                        System.out.println("accept proposal send");
                    }
                    step++;
                }
                else {
                    block();
                }
            }
        }

//        private void sendAcceptedProposals() {
//            Hashtable<String, List<String>> accepted_proposals = new Hashtable<>();
//            for(String bakery : proposedPrices.keySet()) {
//                accepted_proposals.put(bakery, new LinkedList<>());
//            }
//
//            Hashtable<String, Integer> products = order.getProducts();
//            int num_products = products.size();
//
//            for(int i = 0; i < num_products; ++i) {
//                double lowest_price = -1;
//                String bakery = null;
//                String product = new LinkedList<String>(products.keySet()).get(i);
//                for(String bak : proposedPrices.keySet()) {
//                    if(proposedPrices.get(bak).containsKey(product)) {
//                        if(bakery == null || lowest_price > proposedPrices.get(bak).get(product)) {
//                            bakery = bak;
//                            lowest_price = proposedPrices.get(bak).get(product);
//                        }
//                    }
//                }
//                if(bakery != null) {
//                    accepted_proposals.get(bakery).add(product);
//                }
//            }
//
//            Iterator<String> bakeryIterator = accepted_proposals.keySet().iterator();
//            for(int i = 0; i < accepted_proposals.size(); ++i) {
//                String bakery = bakeryIterator.next();
//                if(!accepted_proposals.get(bakery).isEmpty()) {
//                    JSONObject jsonOrder = new JSONObject(order.toJSONString());
//                    JSONObject jsonProducts = jsonOrder.getJSONObject("products");
//                    Set<String> keys = jsonProducts.keySet();
//                    List<String> accepted_products = accepted_proposals.get(bakery);
//                    for(String pr : keys) {
//                        if(!accepted_products.contains(pr)) {
//                            jsonProducts.remove(pr);
//                        }
//                    }
//                    jsonOrder.put("products", jsonProducts);
//
//                    ACLMessage accept_proposal = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
//                    accept_proposal.setConversationId(order.getGuid());
//                    accept_proposal.setContent(jsonOrder.toString());
//                    accept_proposal.addReceiver(new AID(bakery));
//                    sendMessage(accept_proposal);
//                }
//                else {
//                    ACLMessage refuse_proposal = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
//                    refuse_proposal.setConversationId(order.getGuid());
//                    refuse_proposal.addReceiver(new AID(bakery));
//                    sendMessage(refuse_proposal);
//                }
//            }
//            step++;
//        }
    }

    private AID[] findOrderProcessingAgents() {
        AID[] orderProcessingAgents = new AID[0];
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("OrderProcessing");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            orderProcessingAgents = new AID[result.length];
            for (int i = 0; i < result.length; ++i) {
                orderProcessingAgents[i] = result[i].getName();
            }
//            System.out.println(orderProcessingAgents.length + " OrderProcessingAgents found!");
        }
        catch (FIPAException fe) {
            System.out.println("Error searching OrderProcessingAgents");
            fe.printStackTrace();
        }
        return orderProcessingAgents;
    }
}
