package org.team_pjt.agents;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CustomerAgent extends BaseAgent {
    private JSONArray dataArray = new JSONArray();
    public String globalOrder = "";
    private JSONArray orders = new JSONArray();
    //private JSONObject location = new JSONObject();
    private Object location = null;

    private String customerName = "";
    private String customerID = "";

    private int[] latestOrder = new int[2];

    private AID [] sellerAgents;

    private int sum_sent; //number of sent orders
    private int sum_total; //number o total orders
    private boolean process_done; //wait until communication with order processing finished

    protected void setup() {
        super.setup();

        //Wait until order procesing agent set up
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        customerID = getAID().getLocalName();

        System.out.println(customerID + " is ready.");

        getSellers();

        //System.out.println(customerName + " will send order to " + sellerAgents.length + " sellers");

        retrieve("src/main/resources/config/small/clients.json");
        sum_total = getOrder(customerID);
        latestOrder = whenLatestOrder();

        register("customer", customerID);

        //addBehaviour(new isNewOrderChecker());
        addBehaviour(new GetCurrentOrder());
    }

    protected void takeDown() {
        deRegister();
        System.out.println(customerID + " sent " + sum_sent + " order");
        System.out.println(customerID + ": Terminating.");
    }

    protected void getSellers() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("OrderProcessing");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(CustomerAgent.this, template);
            sellerAgents = new AID[result.length];
            for (int i = 0; i < result.length; ++i) {
                sellerAgents[i] = result[i].getName();
            }
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private class GetCurrentOrder extends Behaviour {
        private boolean isDone = false;
        private boolean passTime = false;

        public String getGlobalOrder(){return globalOrder;}

        @Override
        public void action() {
            if(!getAllowAction()) {
                return;
            }

            int hour = getCurrentHour();
            int day = getCurrentDay();

            //System.out.println("current hour: " + getCurrentHour());
            //System.out.println("current day: " + getCurrentDay());

            if (day > latestOrder[0] && hour > latestOrder[1]) {
                System.out.println("It passed time");
                passTime = true;
            } else {
                //System.out.println("continue ...");
                passTime = false;

                //Get Order at Specified Time
                ArrayList<JSONObject> orderList = getCurrentOrder(hour, day);
                JSONObject order = new JSONObject();

//                System.out.println("orderlist size: " + orderList.size());

                while (orderList.size() > 0) {
                    order = orderList.remove(0);
                    System.out.println(order);
                    System.out.println("XXXXXXXXXXXXXXXXXXXX");
                    // Visualisation2 vistest = new Visualisation2();
                    // vistest.display("Hallo");
                    // Via class variable    globalOrder = order.getJSONObject("products").toString();
                    //CallGUI.setOrder();
                    CustomerAgent.this.addBehaviour(new CallForProposal(order));
                    sum_sent++;
                    process_done = false;
                }

                myAgent.addBehaviour(new GetCurrentOrder()); //don't call when all order are ordered?
            }

            //Inform the order processing the customer doesn't want to buy anything at the time
//            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
//            for (int i = 0; i < sellerAgents.length; ++i) {
//                msg.addReceiver(sellerAgents[i]);
//            }
//            msg.setContent("We don't want to buy anything now!");
//            sendMessage(msg);

            //System.out.println("call finish");
            finished();
            isDone = true;
        }

        @Override
        public boolean done() {
            //System.out.println(sum_sent);
            //System.out.println(sum_total);

            if (process_done && (sum_sent >= sum_total || passTime == true)) {
                addBehaviour(new shutdown());
            }

            return isDone;
        }

        private class shutdown extends OneShotBehaviour{
            public void action() {
                ACLMessage shutdownMessage = new ACLMessage(ACLMessage.REQUEST);
                Codec codec = new SLCodec();
                myAgent.getContentManager().registerLanguage(codec);
                myAgent.getContentManager().registerOntology(JADEManagementOntology.getInstance());
                shutdownMessage.addReceiver(myAgent.getAMS());
                shutdownMessage.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                shutdownMessage.setOntology(JADEManagementOntology.getInstance().getName());
                try {
                    myAgent.getContentManager().fillContent(shutdownMessage,new Action(myAgent.getAID(), new ShutdownPlatform()));
                    myAgent.send(shutdownMessage);
                } catch (Exception e) {
                    //LOGGER.error(e);
                }
            }
        }
    }

    private class CallForProposal extends OneShotBehaviour {
        private MessageTemplate mt;
        private JSONObject myOrder = new JSONObject();

        CallForProposal(JSONObject order) {
            myOrder = order;
        }

        @Override
        public void action() {
            // Send the order (message) to all sellers
            ACLMessage msg = new ACLMessage(ACLMessage.CFP);

            for (int i = 0; i < sellerAgents.length; ++i) {
                msg.addReceiver(sellerAgents[i]);
            }

            //System.out.println("myOrder: " + myOrder.toString());

            myOrder = includeLocation(myOrder);

            String orderID = "";
            try {
                orderID = myOrder.getString("guid");
                msg.setConversationId(orderID);
                msg.setLanguage("JSON");
                msg.setContent(myOrder.toString());
                msg.addReplyTo(getAID());
                msg.setReplyWith("order-"+System.currentTimeMillis()); // Unique value
                sendMessage(msg);
                System.out.println(customerID + " send order: " + msg.getContent().toString());

                // Prepare the template to get proposals
                mt = MessageTemplate.and(MessageTemplate.MatchConversationId(orderID),
                        MessageTemplate.MatchInReplyTo(msg.getReplyWith()));

                CustomerAgent.this.addBehaviour(new ReceiveProposal(mt, myOrder));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            finished();
        }
    }

    // Receive Proposals from Bakeries: Bakery name that sells the order and the price
    private class ReceiveProposal extends Behaviour {
        private JSONObject incomingProposal = new JSONObject();
        private JSONObject myOrder = new JSONObject();
        private List<AID> proposalSender = new ArrayList();

        private MessageTemplate myTemplate;
        private boolean isDone = false;

        private String orderID = "";

        private int receivedReply = 0;

        ReceiveProposal(MessageTemplate mt, JSONObject order) {
            //System.out.println("Received Proposal");
            myTemplate = mt;
            myOrder = order;
        }

        @Override
        public void action() {
            ACLMessage message = myAgent.receive(myTemplate);

            if (message != null) {
                //Purchase order reply received
                //System.out.println("Received Message: " + message.getContent());
                String bakeryName = message.getSender().getLocalName();
                JSONObject products = new JSONObject();

                if (message.getPerformative() == ACLMessage.PROPOSE) {
                    if (message.getLanguage().equals("JSON")) {
                        try {
                            JSONObject proposal = new JSONObject(message.getContent());
                            products = proposal.getJSONObject("products");

                            proposalSender.add(message.getSender());

                            incomingProposal.put(bakeryName, products);

                            receivedReply++;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (message.getPerformative() == ACLMessage.REFUSE) {
                    receivedReply++;
                }

                System.out.println("Received reply = " + receivedReply);

                if (receivedReply == sellerAgents.length) {
                    //System.out.println(receivedReply);
                    System.out.println("incomingProposal " + incomingProposal);

                    isDone = true;
                    finished();

                    if (!incomingProposal.isEmpty()) {
                        CustomerAgent.this.addBehaviour(new SendConfirmation(incomingProposal, myOrder, proposalSender));
                    } else {
                        System.out.println("No bakery accept my order.. >_<");
                    }
                }
            } else {
                block();
            }
        }

        @Override
        public boolean done() {
            return isDone;
        }

    }

    private class SendConfirmation extends OneShotBehaviour {
        private JSONObject proposal = new JSONObject();
        private JSONObject selected = new JSONObject();
        private JSONObject myOrder = new JSONObject();
        private JSONObject reOrder = new JSONObject();
        private List<AID> sellers = new ArrayList();

        SendConfirmation(JSONObject incomingProposal, JSONObject order, List<AID> proposalSender) {
            proposal = incomingProposal;
            myOrder = order;
            sellers = proposalSender;
        }

        @Override
        public void action() {
            try {
                selected = findTheCheapest(proposal, myOrder);

                //Send the confirmation
                for (int i = 0; i < sellers.size(); ++i) {
                    String id = sellers.get(i).getLocalName();

                    if (selected.has(id)) {
                        JSONObject products = selected.getJSONObject(id);

                        reOrder = myOrder;

                        reOrder.put("products", products);

                        JSONObject newProductList = new JSONObject();

                        ACLMessage confirm = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                        confirm.addReceiver(sellers.get(i));
                        confirm.setLanguage("JSON");
                        confirm.setContent(reOrder.toString());
                        send(confirm);

                        System.out.println(customerID + " accept " + id + ": " + confirm.getContent());
                    } else {
                        ACLMessage confirm = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                        confirm.addReceiver(sellers.get(i));
                        confirm.setContent("Your bakery is too expensive.. :(");
                        send(confirm);

                        System.out.println(customerID + " reject " + id + ": " + confirm.getContent());
                    }
                }

                finished();
                process_done = true;
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    //FUNCTIONS TO MANAGE JSON OBJECTS
    //Retrieve client data from config file
    private void retrieve(String fileName) {
        File file = new File(fileName);
        String filePath = file.getAbsolutePath();
        String fileContent = "";

        try {
            fileContent = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            dataArray = new JSONArray(fileContent);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Get list of order
    private int getOrder(String id) {
        String customerID = "";

        //Take Orders from Customer (based on the name)
        try {
            for (int i = 0; i < dataArray.length(); i++) {
                customerID = dataArray.getJSONObject(i).getString("guid");

                if (customerID.equals(id)) {
                    orders = dataArray.getJSONObject(i).getJSONArray("orders");
                    location = dataArray.getJSONObject(i).get("location");

                    //Should the length reduced by one?
                    System.out.println(customerID + " has " + (orders.length() - 1) + " order");

                    return orders.length() - 1;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return 0;
    }

    //Get the latest order date, so that later it will be used to terminated after it pass the time
    private int[] whenLatestOrder() {
        JSONObject order_time = new JSONObject();
        ArrayList<Date> date = new ArrayList<>();
        int[] lastDate = new int[2];

        try {
//            System.out.println("get time from orders");
//            System.out.println(orders.length());
            for (int i = 0; i < orders.length(); i++) {
                order_time = orders.getJSONObject(i).getJSONObject("order_date");

                int day = order_time.getInt("day");
                int hour = order_time.getInt("hour");

                date.add(new Date(hour, day));
            }

        } catch (JSONException e) {
            System.out.println("fail to get time from orders");
            e.printStackTrace();
        }

        Comparator<Date> comparator = Comparator.comparingInt(Date::getDay).thenComparingInt(Date::getHour);

        // Sort the stream:
        Stream<Date> DateStream = date.stream().sorted(comparator);

        // Make sure that the output is as expected:
        List<Date> sortedDate = DateStream.collect(Collectors.toList());

	    /*for (int i = 0; i < sortedDate.size(); i++) {
	    	System.out.println(sortedDate.get(i).getDay() + " ~~ " + sortedDate.get(i).getHour());
	    }*/

        //System.out.println(sortedDate.size());

        lastDate[0] = sortedDate.get(sortedDate.size() - 1).getDay();
        lastDate[1] = sortedDate.get(sortedDate.size() - 1).getHour();

        return lastDate;
    }

    public static class Date {
        public int hour;
        public int day;

        public Date(int hour, int day)
        {
            this.hour = hour;
            this.day = day;
        }

        public int getHour() {
            return this.hour;
        }

        public int getDay() {
            return this.day;
        }
    }

    private JSONObject findTheCheapest(JSONObject proposal, JSONObject myOrder) {
        JSONObject confirmation = new JSONObject();
        JSONObject proposedPrice = new JSONObject();

        JSONObject orderedProduct = new JSONObject();
        orderedProduct = myOrder.getJSONObject("products");

        try {
            Iterator<?> iter = orderedProduct.keys();
            while(iter.hasNext()) {
                String type = (String)iter.next();

                JSONObject selectedProduct = new JSONObject();
                Double min_price = Double.MAX_VALUE;
                String chosenBakery = "";

                for (AID seller : sellerAgents) {
                    String id = seller.getLocalName();

                    if (proposal.has(id)) {
                        proposedPrice = proposal.getJSONObject(id);

                        if (min_price > proposedPrice.getDouble(type) && proposedPrice.getDouble(type) != 0) {
                            chosenBakery = id;
                            min_price = proposedPrice.getDouble(type);
                        }
                    }
                }

                if (chosenBakery != "") {
                    if (confirmation.has(chosenBakery)) {
                        selectedProduct = confirmation.getJSONObject(chosenBakery);
                        //type = type + ", " + confirmation.getString(chosenBakery);
                    }

                    int amount = myOrder.getJSONObject("products").getInt(type);
                    selectedProduct.put(type, amount);

                    //System.out.println("Selected Product: " + selectedProduct.toString());

                    confirmation.put(chosenBakery, selectedProduct);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return confirmation;
    }

    private ArrayList<JSONObject> getCurrentOrder(int currentHour, int currentDay) {
        JSONObject order_time = new JSONObject();
        ArrayList<JSONObject> orderList = new ArrayList<JSONObject>();

        //Check Date
        try {
            int n = 0;
            for (int i = 0; i < orders.length(); i++) {
                order_time = orders.getJSONObject(i).getJSONObject("order_date");

                int hour = order_time.getInt("hour");
                int day = order_time.getInt("day");

                if ((hour == currentHour) && (day == currentDay) && getCurrentMinute() == 0) {
                    orderList.add(orders.getJSONObject(i));
                    n++;
                }
            }

            if (n > 0) {
                //System.out.println(orderList.toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return orderList;
    }

    private JSONObject includeLocation(JSONObject order) {
        try {
            order.put("location", location);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return order;
    }
}