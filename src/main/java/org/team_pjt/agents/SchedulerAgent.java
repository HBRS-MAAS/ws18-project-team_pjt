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
import javafx.util.Pair;
import netscape.javascript.JSObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.team_pjt.behaviours.shutdown;
import org.team_pjt.Objects.Order;
import org.team_pjt.Objects.Product;
import org.team_pjt.utils.Logger;

import java.util.*;

public class SchedulerAgent extends BaseAgent {
    private String sBakeryId;
    private int timeStepSize;
    //    private Location lLocation;
    private HashMap<String, HashMap<Integer, List<Pair<String, Integer>>>> prepTables;
    private HashMap<String, HashMap<Integer, List<Pair<String, Integer>>>> kneadingMachines;
    private HashMap<String, Product> hmProducts; // = Available Products
    //    private HashMap<Integer, Order> scheduledOrders;
    private HashMap<String, Order> orderedOrders;
    private AID order_processing;
    private int endDays;
    private boolean order_received = false;
    private Logger logger;

    protected void setup(){
        super.setup();
        Object[] oArguments = getArguments();
        if (!readArgs(oArguments)) {
            System.out.println(getName() + ": No parameter given for OrderProcessing " + getName());
        }
        logger = new Logger(getName(), "debug");
        this.register("scheduler", getName().split("@")[0]);
        findOrderProcessing();
        addBehaviour(new isNewOrderCheckerNew());
        addBehaviour(new TimeManager());
        addBehaviour(new QueueRequestServer());
        addBehaviour(new ScheduledOrderRequestServer());

//        System.out.println("SchedulerAgent is ready");
    }

    private boolean checkDoughPrepStage(Order order, HashMap<String, List<Pair<String, Integer>>> tempPrepTables, HashMap<String, List<Pair<String, Integer>>> tempKneadingMachines) {
        Set<String> orderProducts = order.getProducts().keySet();
//        HashMap<String, List<Pair<String, Integer>>> tempPrepTables = new HashMap<>();
//        HashMap<String, List<Pair<String, Integer>>> tempKneadingMachines = new HashMap<>();

        Set<String> kneadingMachineIDs = kneadingMachines.keySet();
        Set<String> prepTableIDs = prepTables.keySet();

        int deliveryDay = order.getDeliveryDay();
        int deliveryHour = order.getDeliveryHour();

        int deadline = Math.min(deliveryHour*60, 720);

        for(String km : kneadingMachineIDs) {
            if(kneadingMachines.get(km).containsKey(deliveryDay)) {
                tempKneadingMachines.put(km, new LinkedList<>(kneadingMachines.get(km).get(deliveryDay)));
            }
            else {
                tempKneadingMachines.put(km, new LinkedList<>());
            }
        }

        for(String pt :prepTableIDs) {
            if(prepTables.get(pt).containsKey(deliveryDay)) {
                tempPrepTables.put(pt, new LinkedList<>(prepTables.get(pt).get(deliveryDay)));
            }
            else {
                tempPrepTables.put(pt, new LinkedList<>());
            }
        }

        boolean doughPrepPossible = true;
        for(String pName : orderProducts) {
            Product p = hmProducts.get(pName);
            Iterator<Object> step_iterator = p.getSteps().iterator();
            boolean proofing = false;
            int endingTime = 0;
            while(step_iterator.hasNext()) {
                JSONObject step = (JSONObject) step_iterator.next();
                switch (step.getString("action")) {
                    case "kneading":
                        int kneadingTime = step.getInt("duration");
                        String chosenKM = checkKneadingMachineTimes(kneadingTime, pName, tempKneadingMachines, deadline);
                        if(chosenKM == null) {
//                            System.out.println(getName() + ": no kneadingMachineFound!");
                            return false;
                        }
                        List<Pair<String, Integer>> dayPlan = tempKneadingMachines.get(chosenKM);
                        boolean alreadyIn = false;
                        for(Pair<String, Integer> i : dayPlan) {
                            if(i.getKey().equals(pName)) {
                                alreadyIn = true;
                                endingTime = i.getValue();
                            }
                        }
                        if(!alreadyIn && !dayPlan.isEmpty()) {
                            int latestTime = tempKneadingMachines.get(chosenKM).get(tempKneadingMachines.get(chosenKM).size()-1).getValue();
                            tempKneadingMachines.get(chosenKM).add(new Pair(pName, latestTime + kneadingTime));
                            endingTime += latestTime + kneadingTime;
                        }
                        else if(!alreadyIn && dayPlan.isEmpty()) {
                            tempKneadingMachines.get(chosenKM).add(new Pair(pName, kneadingTime));
                            endingTime += kneadingTime;
                        }

                        break;
                    case "item preparation":
                        int itemPrepTime = step.getInt("duration") * order.getProducts().get(pName);

                        String chosenPT = checkItemPrepTables(itemPrepTime, endingTime, tempPrepTables, deadline);
                        if(chosenPT == null) {
//                            System.out.println(getName() + ": endingTime: " + (endingTime + itemPrepTime) + " itemPrepTable: " + itemPrepTime);
//                            System.out.println(getName() + ": not possible to choose prepTable");
//                            System.out.println(getName() + ": " + pName);
                            return false;
                        }
                        tempPrepTables.get(chosenPT).add(new Pair(pName, endingTime + itemPrepTime));
                        endingTime += itemPrepTime;

                        break;
                    case "proofing":
                        endingTime += step.getInt("duration");
//                        System.out.println(getName() + ": endingTime: " + endingTime);
                        if(endingTime <= deadline) {
                            proofing = true;
                        }
                        break;
                    default:
                        endingTime += step.getInt("duration");
                        break;
                }
                if(proofing) {
                    break;
                }
            }
            doughPrepPossible = doughPrepPossible && proofing;
        }

        return doughPrepPossible;
    }

    private String checkItemPrepTables(int itemPrepTime, int startingTime, HashMap<String, List<Pair<String, Integer>>> tempPrepTables, int deadline) {
        List<Pair<String, Integer>> tempPrepTable = null;
        Set<String> prepTableIDs = tempPrepTables.keySet();

        Iterator<String> ptIterator = prepTableIDs.iterator();
        String earliestPrepTable = ptIterator.next();
        String actualPt = earliestPrepTable;
        while (ptIterator.hasNext()) {
            List<Pair<String, Integer>> dailyPlan = tempPrepTables.get(actualPt);
            if (dailyPlan.isEmpty()) {
                earliestPrepTable = actualPt;
                tempPrepTable = tempPrepTables.get(earliestPrepTable);
                break;
            }
            List<Pair<String, Integer>> dailyPlanEarliest = tempPrepTables.get(earliestPrepTable);
            if(dailyPlan.get(dailyPlan.size()-1).getValue() <
                    dailyPlanEarliest.get(dailyPlanEarliest.size()-1).getValue()) {
                earliestPrepTable = actualPt;
            }
            actualPt = ptIterator.next();
        }
        if(tempPrepTable == null) {
            tempPrepTable = tempPrepTables.get(earliestPrepTable);
        }
        if(!tempPrepTable.isEmpty() && (tempPrepTable.get(tempPrepTable.size() - 1).getValue() > startingTime)) {
            startingTime = tempPrepTable.get(tempPrepTable.size() - 1).getValue();
        }
        if((startingTime + itemPrepTime) < deadline) {
            return earliestPrepTable;
        }
        return null;
    }

    private String checkKneadingMachineTimes(int kneadingTime, String productName, HashMap<String, List<Pair<String, Integer>>> tempKneadingMachines, int deadline) {
        List<Pair<String, Integer>> tempKneadingMachine = null;
        Set<String> kneadingMachineIDs = tempKneadingMachines.keySet();
        String alreadyKneadedIn = checkIfTypeAlreadyKneaded(productName, tempKneadingMachines);
        if(alreadyKneadedIn != null) {
            return alreadyKneadedIn;
        }

        Iterator<String> kmIterator = kneadingMachineIDs.iterator();
        String earliestKneadingMachine = kmIterator.next();
        String actualKm = earliestKneadingMachine;
        while (kmIterator.hasNext()) {
            List<Pair<String, Integer>> dailyPlan = tempKneadingMachines.get(actualKm);
            if (dailyPlan.isEmpty()) {
                earliestKneadingMachine = actualKm;
                tempKneadingMachine = tempKneadingMachines.get(earliestKneadingMachine);
                break;
            }
            List<Pair<String, Integer>> dailyPlanEarliest = tempKneadingMachines.get(earliestKneadingMachine);
            if(dailyPlan.get(dailyPlan.size()-1).getValue() <
                    dailyPlanEarliest.get(dailyPlanEarliest.size()-1).getValue()) {
                earliestKneadingMachine = actualKm;
            }
            actualKm = kmIterator.next();
        }
        if(tempKneadingMachine == null) {
            tempKneadingMachine = tempKneadingMachines.get(earliestKneadingMachine);
        }
        if(tempKneadingMachine.isEmpty() || (tempKneadingMachine.get(tempKneadingMachine.size() - 1).getValue() + kneadingTime) < deadline) {
            return earliestKneadingMachine;
        }
        return null;
    }

    private String checkIfTypeAlreadyKneaded(String productName, HashMap<String, List<Pair<String, Integer>>> tempKneadingMachines) {
        Iterator<String> kmIt = tempKneadingMachines.keySet().iterator();
        while(kmIt.hasNext()) {
            String km = kmIt.next();
            for(Pair<String, Integer> p : tempKneadingMachines.get(km)) {
                if(p.getKey().equals(productName)) {
                    return km;
                }
            }
        }
        return null;
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
//        System.out.println("OrderProcessing found! - " + order_processing);
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
                logger.log(new Logger.LogMessage("OrderQueue: " + orderedOrders.size(), "no"));
                if (getCurrentDay() >= endDays && orderedOrders.size() == 0) { //TODO: When no orders to do end agent!
                    deRegister();
                    addBehaviour(new shutdown());
                }
            }
        }

        private void clearQueue() {
            for (String orderID : orderedOrders.keySet()) {
                Order o = orderedOrders.get(orderID);
                if(o.getDeliveryDay() <= getCurrentDay() && o.getDeliveryHour() < getCurrentHour()) {
                    orderedOrders.remove(orderID);
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

    private class isNewOrderCheckerNew extends Behaviour {
        boolean isDone = false;
        @Override
        public void action() {
            MessageTemplate mtNewOrder = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchSender(order_processing));
            ACLMessage newOrder = myAgent.receive(mtNewOrder);
            if(newOrder != null) {
                myAgent.addBehaviour(new receiveOrderNew());
                myAgent.addBehaviour(new isNewOrderCheckerNew());
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

    private class receiveOrderNew extends Behaviour {
        private boolean isDone = false;
        private int step = 0;

        @Override
        public void action() {
            if(getCurrentDay() >= endDays) {
                addBehaviour(new shutdown());
            }
            switch (step) {
                case 0:
                    ACLMessage schedule_request = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
                    if (schedule_request != null) {
                        logger.log(new Logger.LogMessage("schedule request received!", "release"));
                        String sContent = schedule_request.getContent();
                        ACLMessage schedule_reply = schedule_request.createReply();
                        HashMap<String, List<Pair<String, Integer>>> tempPrepTables = new HashMap<>();
                        HashMap<String, List<Pair<String, Integer>>> tempKneadingMachines = new HashMap<>();
                        boolean doughPrepPossible = checkDoughPrepStage(new Order(sContent), tempPrepTables, tempKneadingMachines);
                        logger.log(new Logger.LogMessage("doughPrepPossible: " + doughPrepPossible, "release"));
                        if (!doughPrepPossible) {
                            schedule_reply.setPerformative(ACLMessage.DISCONFIRM);
                            schedule_reply.setContent("Scheduling impossible!");
                            sendMessage(schedule_reply);
                            step = 2;
                        } else {
                            schedule_reply.setPerformative(ACLMessage.CONFIRM);
                            schedule_reply.setContent("Scheduling possible!");
                            sendMessage(schedule_reply);
                            logger.log(new Logger.LogMessage("schedule reply sent!", "release"));
//                            System.out.println(myAgent.getName() + ": schedule reply sent!");
                            step++;
                        }
                    } else {
                        block();
                    }
                    break;
                case 1:
                    MessageTemplate accepted_proposalMT = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE),
                            MessageTemplate.MatchSender(order_processing));
                    MessageTemplate rejectProposalMT = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL),
                            MessageTemplate.MatchConversationId("proposal-rejected"));
                    ACLMessage accepted_proposal = receive(MessageTemplate.or(accepted_proposalMT, rejectProposalMT));
                    if(accepted_proposal != null) {
                        if(accepted_proposal.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                            logger.log(new Logger.LogMessage(accepted_proposal.getContent(), "release"));
//                            System.out.println(myAgent.getName() + ": " + accepted_proposal.getContent());
                            ++step;
                            return;
                        }

                        Order order = new Order(accepted_proposal.getContent());

                        int deliveryDay = order.getDeliveryDay();

                        HashMap<String, List<Pair<String, Integer>>> tempPrepTables = new HashMap<>();
                        HashMap<String, List<Pair<String, Integer>>> tempKneadingMachines = new HashMap<>();
                        checkDoughPrepStage(order, tempPrepTables, tempKneadingMachines);

                        for(String key : prepTables.keySet()) {
                            if(tempPrepTables.get(key) != null) {
                                prepTables.get(key).put(deliveryDay, tempPrepTables.get(key));
                            }
                            if(tempKneadingMachines.get(key) != null) {
                                kneadingMachines.get(key).put(deliveryDay, tempKneadingMachines.get(key));
                            }
                        }
                        orderedOrders.put(order.getGuid(), order);
                        orderedOrders = sortOrders(orderedOrders);
                        logger.log(new Logger.LogMessage("Order added", "release"));
                        logger.log(new Logger.LogMessage("accept proposal received", "release"));
                        AID[] allAgents = findAllAgents();
                        ACLMessage propagate_accepted_order = new ACLMessage(ACLMessage.PROPAGATE);

                        List<Order> orders = new LinkedList<>(orderedOrders.values());
                        JSONArray sortedOrders = new JSONArray();

                        for(Order o : orders) {
                            sortedOrders.put(new JSONObject(o.toJSONString()));
                        }

                        propagate_accepted_order.setContent(sortedOrders.toString());
                        for(AID agent : allAgents) {
                            propagate_accepted_order.addReceiver(agent);
                        }
                        sendMessage(propagate_accepted_order);
                        logger.log(new Logger.LogMessage("Scheduler Agent Propagated all scheduled Orders", "release"));
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
        @Override
        public void action() {
            MessageTemplate mtQueueRequest = MessageTemplate.and(MessageTemplate.MatchConversationId("queue request"),
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
            ACLMessage queue_request = myAgent.receive(mtQueueRequest);
            if(queue_request != null) {
                String order_id = queue_request.getContent();
                int pos = 0;

                Iterator<String> orderID_iterator = orderedOrders.keySet().iterator();
                boolean found = false;
                while(orderID_iterator.hasNext()) {
                    String order = orderID_iterator.next();
                    if(order.equals(order_id)) {
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

    private class ScheduledOrderRequestServer extends CyclicBehaviour {
        /*
                Behaviour for getting orders for visualization
         */
        @Override
        public void action() {
            MessageTemplate allOrderRequestMT = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    MessageTemplate.MatchConversationId("allOrders"));
            ACLMessage allOrderRequest = myAgent.receive(allOrderRequestMT);
            if(allOrderRequest != null) {
                ACLMessage reply = allOrderRequest.createReply();
                if(orderedOrders.isEmpty()) {
                    reply.setContent("No scheduled Order");
                }
                else {
                    JSONArray orders = new JSONArray();
                    Iterator<String> keys = orderedOrders.keySet().iterator();
                    while(keys.hasNext()) {
                        String key = keys.next();
                        Order order = orderedOrders.get(key);
                        orders.put(new JSONObject(order.toJSONString()));
                    }
                    reply.setContent(orders.toString());
                }
                sendMessage(reply);
            }
            else {
                block();
            }
        }
    }

    public static HashMap<String, Order> sortOrders(HashMap<String, Order> hm) {
        List<Map.Entry<String, Order>> orders = new LinkedList<Map.Entry<String, Order>>(hm.entrySet());

        Collections.sort(orders, new Comparator<Map.Entry<String, Order> >() {
            @Override
            public int compare(Map.Entry<String, Order> o1, Map.Entry<String, Order> o2) {
                if(o1.getValue().getDeliveryDay() < o2.getValue().getDeliveryDay()) {
                    return -1;
                }
                if(o1.getValue().getDeliveryDay() > o2.getValue().getDeliveryDay()) {
                    return 1;
                }
                if(o1.getValue().getDeliveryDay() == o2.getValue().getDeliveryDay() && o1.getValue().getOrderDay() < o2.getValue().getOrderDay()) {
                    return -1;
                }
                if(o1.getValue().getDeliveryDay() == o2.getValue().getDeliveryDay() && o1.getValue().getOrderDay() > o2.getValue().getOrderDay()) {
                    return 1;
                }
                return 0;
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Order> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Order> aa : orders) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    private boolean readArgs(Object[] oArgs){
        if(oArgs != null && oArgs.length > 0){
//            scheduledOrders = new HashMap<>();
            orderedOrders = new HashMap<>();
            kneadingMachines = new HashMap<>();
            prepTables = new HashMap<>();
            hmProducts = new HashMap<>();

            JSONObject bakery = new JSONObject(((String)oArgs[0]).replaceAll("###", ","));

            sBakeryId = bakery.getString("guid");

            JSONArray products = bakery.getJSONArray("products");
            Iterator<Object> product_iterator = products.iterator();
            while(product_iterator.hasNext()) {
                JSONObject jsoProduct = (JSONObject) product_iterator.next();
                Product product = new Product(jsoProduct.toString());
                hmProducts.put(product.getGuid(), product);
            }

            JSONArray JSONdoughPrepTables = bakery.getJSONObject("equipment").getJSONArray("doughPrepTables");

            Iterator<Object> doughPrepTable_iterator = JSONdoughPrepTables.iterator();
            while(doughPrepTable_iterator.hasNext()) {
                JSONObject table = (JSONObject) doughPrepTable_iterator.next();
                prepTables.put(table.getString("guid"), new HashMap<>());
            }

            JSONArray JSONKneadingMachines = bakery.getJSONObject("equipment").getJSONArray("kneadingMachines");

            Iterator<Object> kneadingMachine_iterator = JSONKneadingMachines.iterator();
            while(kneadingMachine_iterator.hasNext()) {
                JSONObject kneadingMachine = (JSONObject) kneadingMachine_iterator.next();
                kneadingMachines.put(kneadingMachine.getString("guid"), new HashMap<>());
            }
//            JSONObject jsoLocation = bakery.getJSONObject("location");
//            lLocation = new Location(jsoLocation.getDouble("y"), jsoLocation.getDouble("x"));

            JSONObject meta_data = new JSONObject(((String)oArgs[1]).replaceAll("###", ","));
            this.endDays = meta_data.getInt("durationInDays");

            JSONObject timeStep = meta_data.getJSONObject("timeStep");
            timeStepSize = (24 * timeStep.getInt("day") + timeStep.getInt("hour")) * 60
                    + timeStep.getInt("minute");
            return true;
        }
        else {
            return false;
        }
    }
}