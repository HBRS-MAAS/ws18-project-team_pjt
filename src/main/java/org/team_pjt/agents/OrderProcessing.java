package org.team_pjt.agents;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
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
import org.team_pjt.objects.Order;
import org.team_pjt.objects.Product;
import org.team_pjt.objects.Location;

import java.util.*;
// ToDo OrderProcessing in OrderProcessingAgent umbenennen
public class OrderProcessing extends BaseAgent {
    private String sBakeryId;
    private Location lLocation;
    private HashMap<String, Product> hmProducts; // = Available Products
    private AID aidScheduler;
    private AID[] allAgents;
    private int endDays;

    protected void setup(){
        super.setup();
        Object[] oArguments = getArguments();
        if (!readArgs(oArguments)) {
            System.out.println("No parameter given for OrderProcessing " + getName());
        }
        this.register("OrderProcessing", this.sBakeryId);
        findScheduler();
        addBehaviour(new OfferRequestServer());
        System.out.println("OrderProcessing " + getName() + " ready");

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

    private void addOrderToQueue() {

    }

    private void distributeOrder() {

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

    private class OfferRequestServer extends CyclicBehaviour {
        private boolean bFeasibleOrder;

        private void sendNotFeasibleMessage(ACLMessage msg, String content) {
            ACLMessage clientReply = msg.createReply();
            clientReply.setPerformative(ACLMessage.REFUSE);
            clientReply.setContent(content);
            myAgent.send(clientReply);
        }

        @Override
        public void action() {
            if(getCurrentDay() >= endDays) {
                System.out.println("system shutdown!");
                addBehaviour(new shutdown());
            }
            MessageTemplate cfpMT = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage cfpMsg = myAgent.receive(cfpMT);
            if(cfpMsg != null) {
                Order order = new Order(cfpMsg.getContent());
                List<String> order_av_products = new LinkedList<>(order.getProducts().keySet());
                bFeasibleOrder = checkForAvailableProducts(order_av_products);

                if(!bFeasibleOrder) {
                    sendNotFeasibleMessage(cfpMsg, "No needed Product available!");
                    return;
                }

                ACLMessage schedulerRequest = new ACLMessage(ACLMessage.REQUEST);
                Hashtable<String, Integer> order_products = order.getProducts();

                for(String product_name : order_products.keySet()) {
                    if(!order_av_products.contains(product_name)) {
                        order_products.remove(product_name);
                    }
                }

                order.setProducts(order_products);
                schedulerRequest.setConversationId(order.getGuid());
                schedulerRequest.setContent(order.toJSONString());
                myAgent.send(schedulerRequest);

                MessageTemplate schedulerReply = MessageTemplate.and(MessageTemplate.MatchConversationId(order.getGuid()),
                        MessageTemplate.MatchSender(aidScheduler));
                ACLMessage schedulerMessage = myAgent.receive(schedulerReply);
                if(schedulerMessage != null) {
                    if(schedulerMessage.getPerformative() == ACLMessage.CONFIRM) {
                        ACLMessage proposeMsg = cfpMsg.createReply();
                        proposeMsg.setPerformative(ACLMessage.PROPOSE);

                        JSONObject proposeObject = new JSONObject();
                        JSONObject products = new JSONObject();
                        proposeObject.put("guid", order.getGuid());
                        for(String product_name : order.getProducts().keySet()) {
                            double priceAllProductsOfType = hmProducts.get(product_name).getSalesPrice() * order.getProducts().get(product_name);
                            products.put(product_name, priceAllProductsOfType);
                        }
                        proposeObject.put("products", products);
                        proposeMsg.setContent(proposeObject.toString());
                        proposeMsg.setConversationId(order.getGuid());
                        myAgent.send(proposeMsg);

                        MessageTemplate proposalReplyMT = MessageTemplate.and(MessageTemplate.MatchConversationId(order.getGuid()),
                                MessageTemplate.MatchSender(cfpMsg.getSender()));
                        ACLMessage proposalReply = myAgent.receive(proposalReplyMT);
                        if(proposalReply != null) {
                            if(proposalReply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {

                                // send to all Agents, send to schedule to addToQueue
                            }
                            else if (proposalReply.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                                return;
                            }
                        }
                        else {
                            block();
                        }
                    }
                    else if(schedulerMessage.getPerformative() == ACLMessage.DISCONFIRM) {
                        bFeasibleOrder = false;
                    }
                    if(!bFeasibleOrder) {
                        sendNotFeasibleMessage(cfpMsg, "Not able to schedule Order!");
                        return;
                    }
                }
                else {
                    block();
                }
            }
            else {
                block();
            }
        }
    }
}

    /*
    Needed Messages
        * Receive Order from client:
            * Type: CFP
            * Sender: Client
            * Receiver: All OrderProcessingAgents
            * Content: JSONObject as String -> Order
        * Proposal to Client:
            * Type: PROPOSAL
            * Sender: OrderProcessingAgent
            * Receiver: client who send order
            * Content: JSONObject as String -> List of available products with prices (amount of product times sales_price for product)
        * Refusal to Client:
            * Type: REFUSE
            * Sender: OrderProcessingAgent
            * Receiver: client who send order
            * Content: String -> reason for refusal (no needed product available or not enough time to produce order)
        * Receive accepted proposal:
            * Type: ACCEPT_PROPOSAL
            * Sender: Client
            * Receiver: chosen OrderProcessingAgent
            * Content: JSONObject as String -> List of Products which should be produced by chosen bakery
        * Receive Reject proposal:
            * Type: REJECT_PROPOSAL
            * Sender: Client
            * Receiver: not chosen OrderProcessingAgent
            * Content: String -> "rejected"
        * Confirmation of order:
            * Type: CONFIRM
            * Sender: OrderProcessingAgent
            * Receiver: client who send order
            * Content: String -> "order confirmed"
        * Check Scheduler:
            * Type: REQUEST
            * Sender: OrderProcessingAgent
            * Receiver: SchedulerAgent
            * Content: JSONObject -> order with only available products
        * Confirm Schedule:
            * Type: CONFIRM
            * Sender: SchedulerAgent
            * Receiver: OrderProcessingAgent
            * Content: String -> Scheduling possible
        * Disconfirm Schedule:
            * Type: DISCONFIRM
            * Sender: SchedulerAgent
            * Receiver: OrderProcessingAgent
            * Content: String -> Scheduling impossible
        * Propagate accepted Orders:
            * Type: PROPAGATE
            * Sender: SchedulerAgent
            * Receiver: allAgents
            * Content: JSONArray -> sorted List of all received Orders
     */