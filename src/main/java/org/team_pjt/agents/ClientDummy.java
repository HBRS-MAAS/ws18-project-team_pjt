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
    JSONArray jsaDoughPrepArray;

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
            jsaDoughPrepArray = new JSONArray();
            while(order_iterator.hasNext()) {
                JSONObject joOrder = (JSONObject)order_iterator.next();
                jsaDoughPrepArray.put(joOrder);
                ordersToSent.add(new Order(joOrder.toString()));
            }
//            addBehaviour(new sendOneOrderToDoughPrep());
//            ordersToSent.to
//            new JSONArray(ordersToSent.toString());
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
    // provisorisch

    private class sendOneOrderToDoughPrep extends OneShotBehaviour{
        @Override
        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Dough-manager");
            template.addServices(sd);
            DFAgentDescription[] dfSearch = null;
            try {
                dfSearch = DFService.search(myAgent, template);
            } catch (FIPAException e) {
                e.printStackTrace();
            }
            ACLMessage aclMessage = new ACLMessage(ACLMessage.PROPOSE);
            aclMessage.addReceiver(dfSearch[0].getName());
            aclMessage.setContent(jsaDoughPrepArray.toString());
            sendMessage(aclMessage);
        }
    }
    // provisorisch
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
                        JSONObject json_order = new JSONObject(proposal.getContent());
                        for(String product_name : json_order.getJSONObject("products").keySet()) {
                            available_products.put(product_name, json_order.getJSONObject("products").getDouble(product_name));
                        }
                        proposedPrices.put(proposal.getSender().getName(), available_products);
                        ACLMessage accept_proposal = proposal.createReply();

                        JSONObject jsonOrder = new JSONObject(order.toJSONString());
                        JSONObject newOrder = new JSONObject(jsonOrder.toString());
                        JSONObject jsonProducts = jsonOrder.getJSONObject("products");
                        JSONObject jsonProductsNew = newOrder.getJSONObject("products");
                        Set<String> allOrderProductNames = jsonProducts.keySet();
                        Set<String> allProposedProductNames = json_order.getJSONObject("products").keySet();

                        for(String product_name : allOrderProductNames) {
                            if(!allProposedProductNames.contains(product_name)) {
                                jsonProductsNew.remove(product_name);
                            }
                        }
                        newOrder.put("products", jsonProductsNew);

                        accept_proposal.setContent(newOrder.toString());
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
