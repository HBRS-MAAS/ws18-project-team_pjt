package org.team_pjt.agents;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
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

import java.util.*;
// ToDo OrderProcessing in OrderProcessingAgent umbenennen
public class OrderProcessing extends BaseAgent {
    private String sBakeryId;
    private Location lLocation;
    private HashMap<String, Product> hmProducts; // = Available Products
    private AID aidScheduler;
    private AID[] allAgents;
    private int endDays;
    private boolean order_received;

    protected void setup(){
        super.setup();
        Object[] oArguments = getArguments();
        if (!readArgs(oArguments)) {
            System.out.println("No parameter given for OrderProcessing " + getName());
        }
        this.register("OrderProcessing", this.sBakeryId);
        findScheduler();
        order_received = false;
        addBehaviour(new OfferRequestServerNew());
        addBehaviour(new TimeManager());
        System.out.println("OrderProcessing " + getName() + " ready");
    }

    private class distributeFullOrder extends OneShotBehaviour {
        Order order;

        public distributeFullOrder(Order order) {
            super();
            this.order = order;
        }

        @Override
        public void action() {
            findAllAgents();
            ACLMessage propagate_accepted_order = new ACLMessage(ACLMessage.INFORM);
            propagate_accepted_order.setContent(order.toJSONString());
            for(AID agent : allAgents) {
                propagate_accepted_order.addReceiver(agent);
            }
            sendMessage(propagate_accepted_order);
        }
    }

    private class TimeManager extends Behaviour {
        private boolean isDone = false;
        @Override
        public void action() {
            if(!getAllowAction()) {
                return;
            }
            if(!order_received) {
                finished();
//                System.out.println(myAgent.getName() + " called finished");
                isDone = true;
                if (getCurrentDay() >= endDays) {
                    deRegister();
                    addBehaviour(new shutdown());
                }
            }
        }

        @Override
        public boolean done() {
            if(isDone) {
                addBehaviour(new TimeManager());
            }
            return isDone;
        }
    }

    private class OfferRequestServerNew extends Behaviour {
        private boolean bFeasibleOrder;
        private int step = 0;
        private Order order;
        private ACLMessage cfpMessage;

        @Override
        public void action() {
            switch (step) {
                case 0:
                    MessageTemplate cfpMT = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                    cfpMessage = myAgent.receive(cfpMT);
                    if (cfpMessage != null) {
                        order_received = true;
                        System.out.println("cfp received");
                        order = new Order(cfpMessage.getContent());
                        myAgent.addBehaviour(new distributeFullOrder(order));
                        List<String> order_av_products = new LinkedList<>(order.getProducts().keySet());
                        bFeasibleOrder = checkForAvailableProducts(order_av_products);
                        System.out.println("checked available products");

                        if (!bFeasibleOrder) {
                            sendNotFeasibleMessage(cfpMessage, "No needed Product available!");
                            System.out.println("no product available");
                            step = 3;
                            return;
                        }

                        ACLMessage schedulerRequest = new ACLMessage(ACLMessage.REQUEST);
                        Hashtable<String, Integer> order_products = order.getProducts();
                        Iterator<String> product_iterator = order_products.keySet().iterator();
                        while (product_iterator.hasNext()) {
                            String product_name = product_iterator.next();
                            if (!order_av_products.contains(product_name)) {
                                product_iterator.remove();
                            }
                        }

                        order.setProducts(order_products);
                        schedulerRequest.setConversationId(order.getGuid());
                        schedulerRequest.setContent(order.toJSONString());
                        schedulerRequest.addReceiver(aidScheduler);
                        sendMessage(schedulerRequest);
                        System.out.println("asked scheduler for feasibility");
                        step++;
                    }
                    else {
                        block();
                    }
                    break;
                case 1:
                    MessageTemplate schedulerReply = MessageTemplate.and(MessageTemplate.MatchConversationId(order.getGuid()),
                            MessageTemplate.MatchSender(aidScheduler));
                    ACLMessage schedulerMessage = myAgent.receive(schedulerReply);
                    if (schedulerMessage != null) {
                        System.out.println("schedule reply received!");
                        if (schedulerMessage.getPerformative() == ACLMessage.CONFIRM) {
                            ACLMessage proposeMsg = cfpMessage.createReply();
                            proposeMsg.setPerformative(ACLMessage.PROPOSE);

                            JSONObject proposeObject = new JSONObject();
                            JSONObject products = new JSONObject();
                            proposeObject.put("guid", order.getGuid());
                            for (String product_name : order.getProducts().keySet()) {
                                double priceAllProductsOfType = hmProducts.get(product_name).getSalesPrice() * order.getProducts().get(product_name);
                                products.put(product_name, priceAllProductsOfType);
                            }
                            proposeObject.put("products", products);
                            proposeMsg.setContent(proposeObject.toString());
                            proposeMsg.setConversationId(order.getGuid());
                            sendMessage(proposeMsg);
                            System.out.println("proposed available products");
                            step++;
                        } else if (schedulerMessage.getPerformative() == ACLMessage.DISCONFIRM) {
                            bFeasibleOrder = false;
                        }
                        if (!bFeasibleOrder) {
                            sendNotFeasibleMessage(cfpMessage, "Not able to schedule Order!");
                            step = 3;
                        }
                    } else {
                        block();
                    }
                    break;
                case 2:
                    distributeScheduledOrder();
                    break;
            }
        }

        @Override
        public boolean done() {
            boolean isDone = step >= 3;
            if(isDone) {
                myAgent.addBehaviour(new OfferRequestServerNew());
                order_received = false;
            }
            return isDone;
        }

        private void sendNotFeasibleMessage(ACLMessage msg, String content) {
            ACLMessage clientReply = msg.createReply();
            clientReply.setPerformative(ACLMessage.REFUSE);
            clientReply.setContent(content);
            sendMessage(clientReply);
            System.out.println("not feasible message sent");
        }

        private void distributeScheduledOrder() {
            System.out.println("waiting for accepted proposal");
            MessageTemplate acceptedProposalMT = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
                    MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL));
            ACLMessage accepted_proposal = receive(acceptedProposalMT);
            if(accepted_proposal != null) {
                if(accepted_proposal.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                    step++;
                    return;
                }
                System.out.println("accept proposal received");
                findAllAgents();
                ACLMessage propagate_accepted_order = new ACLMessage(ACLMessage.PROPAGATE);
                propagate_accepted_order.setContent(accepted_proposal.getContent());
                propagate_accepted_order.addReceiver(aidScheduler);
                sendMessage(propagate_accepted_order);
                System.out.println("Order Processing Propagated all scheduled Orders");
                step++;
            }
            else {
                block();
            }
        }
    }

    private void findScheduler() {
        DFAgentDescription[] dfSchedulerAgentResult = new DFAgentDescription[0];
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setName("scheduler-"+sBakeryId.split("-")[1]);
        template.addServices(sd);
        while (dfSchedulerAgentResult.length == 0) {
            try {
                dfSchedulerAgentResult = DFService.search(this, template);
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }
        aidScheduler = dfSchedulerAgentResult[0].getName();
        System.out.println("Scheduler found! - " + aidScheduler);
    }

    private void findAllAgents() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
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
    }

    private boolean checkForAvailableProducts(List<String> neededProducts) {
        boolean bFeasibleOrder = false;
        List<String> notAvailableProducts = new LinkedList<>();
        for (String product_name : neededProducts) {
            if(!hmProducts.containsKey(product_name)) {
                notAvailableProducts.add(product_name);
            }
        }
        if(neededProducts.size() != notAvailableProducts.size()) {
            bFeasibleOrder = true;
        }
        neededProducts.removeAll(notAvailableProducts);
        return bFeasibleOrder;
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