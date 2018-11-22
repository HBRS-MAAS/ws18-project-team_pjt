package org.team_pjt.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.team_pjt.behaviours.receiveKillMessage;
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

    private class OrderTimeChecker extends CyclicBehaviour {

        @Override
        public void action() {
            if(getCurrentDay() >= endDays) {
                addBehaviour(new shutdown());
            }
            if(!ordersToSent.isEmpty() && ordersToSent.get(0).getOrderDay() == getCurrentDay() && ordersToSent.get(0).getOrderHour() == getCurrentHour()) {
                placingOrder = true;
                myAgent.addBehaviour(new RequestPerformer(ordersToSent.get(0)));
                ordersSent.add(ordersToSent.remove(0));
            }
            if(!placingOrder) {
                finished();
            }
        }
    }

    private class RequestPerformer extends Behaviour {
        private AID[] orderProcessingAgents;
        private Order order;
        private Hashtable<String, Hashtable<String, Double>> proposedPrices;
        private ACLMessage lastSendMessage;
        private ACLMessage lastReceivedMessage;
        private int step = 0;

        public RequestPerformer(Order order) {
            super();
            proposedPrices = new Hashtable<>();
            this.order = order;
        }

        @Override
        public void action() {
            switch (step) {
                case 0:
                    findOrderProcessingAgents();
                    sendCallForProposal();
                    step++;
                    break;
                case 1:
                    receiveProposals();
                    step++;
                    break;
            }
            placingOrder = false;
        }

        @Override
        public boolean done() {
            return step >= 2;
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
            ACLMessage proposal = myAgent.receive(proposalTemplate);
            while(proposalCounter != orderProcessingAgents.length) {
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
                    }
                } else {
                    block();
                }
            }
        }

        private void findOrderProcessingAgents() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("OrderProcessing");
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                orderProcessingAgents = new AID[result.length];
                for (int i = 0; i < result.length; ++i) {
                    orderProcessingAgents[i] = result[i].getName();
                }
            }
            catch (FIPAException fe) {
                System.out.println("Error searching OrderProcessingAgents");
                fe.printStackTrace();
            }
            System.out.println(orderProcessingAgents.length + " OrderProcessingAgents found!");
        }
    }
}
