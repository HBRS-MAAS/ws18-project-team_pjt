package org.team_pjt.agents;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
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
import org.team_pjt.objects.Product;
import org.team_pjt.objects.Location;
//import org.team_pjt.objects.Product;

import java.util.*;
// ToDo OrderProcessing in OrderProcessingAgent umbenennen
public class OrderProcessing extends BaseAgent {
    public static final String DURATION = "duration";
    private String sBakeryId;
    private Location lLocation;
    private HashMap<String, Product> hmProducts; // = Available Products
    private StringBuffer sbResponseArray;
    private boolean bFeasibleOrder;
    private AID aidScheduler;

    protected void setup(){
        super.setup();
        DFAgentDescription[] dfSchedulerAgentResult = new DFAgentDescription[0];
        Object[] oArguments = getArguments();
        if (!readArgs(oArguments)) {
            System.out.println("No parameter given for OrderProcessing " + getName());
        }
//        registerAgent();
        super.register("OrderProcessing", this.sBakeryId);
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
//        sd.setType("scheduler"+);
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
        addBehaviour(new shutdown());
        System.out.println("OrderProcessing " + getName() + " ready");

    }

    private void checkOrderForFeasibility(AID aidSender, JSONArray jsaCustomerArray) {
        bFeasibleOrder = false;
        Iterator<Object> iCustomerArray = jsaCustomerArray.iterator();
        while(iCustomerArray.hasNext()){
            sbResponseArray = new StringBuffer();
            sbResponseArray.append("{");
            JSONObject jsoObject = (JSONObject) iCustomerArray.next();
            JSONArray jsaOrders = jsoObject.getJSONArray("orders");
            Iterator<Object> iJsaIterator = jsaOrders.iterator();
            while(iJsaIterator.hasNext()){
                JSONObject oNextProduct = (JSONObject) iJsaIterator.next();
                JSONObject jsoProducts = oNextProduct.getJSONObject("products");
                Iterator<String> iKeys = jsoProducts.keys();
                // Jan provisorisch
                int i = 0;
                while(iKeys.hasNext()){
                    String sNextProduct = iKeys.next();
                    if(hmProducts.get(sNextProduct)!= null){
                        if(!bFeasibleOrder){
                            JSONObject jsodeliveryDate = oNextProduct.getJSONObject("deliveryDate");
                            sbResponseArray.append("deliveryDate:");
                            sbResponseArray.append(jsodeliveryDate.toString());
                            sbResponseArray.append(",");
                            sbResponseArray.append("products: {");
                        }
                        if(i != 0){
                            sbResponseArray.append(",");
                        }
                        bFeasibleOrder = true;
                        sbResponseArray.append(sNextProduct + ":" +  jsoProducts.getInt(sNextProduct));
                        i++;
                    }
                }
            }
            sbResponseArray.append("}}");
            sbResponseArray.toString();
            ACLMessage almCustomerResponseMessage = null;
            if (bFeasibleOrder) {
                almCustomerResponseMessage = new ACLMessage(ACLMessage.PROPOSE);
                almCustomerResponseMessage.addReceiver(aidScheduler);
                almCustomerResponseMessage.setContent(sbResponseArray.toString());
            } else {
                almCustomerResponseMessage = new ACLMessage(ACLMessage.REFUSE);
                almCustomerResponseMessage.setContent("No matching products");
            }
            // ToDo Antwort vom Scheduler einbauen
            almCustomerResponseMessage.addReceiver(aidSender);
            super.send(almCustomerResponseMessage);
            if(bFeasibleOrder){
                addOrderToQueue();
                distributeOrder();
            }
            bFeasibleOrder = false;
        }
    }

    private void addOrderToQueue() {

    }

    private void distributeOrder() {

    }

//    private void registerAgent() {
//        DFAgentDescription dfd = new DFAgentDescription();
//        dfd.setName(getAID());
//        ServiceDescription sd = new ServiceDescription();
//        sd.setType("schedulerbakery");
//        sd.setName(this.sBakeryId);
//        dfd.addServices(sd);
//        try {
//            DFService.register(this, dfd);
//        } catch (FIPAException e) {
//            e.printStackTrace();
//        }
//    }

    private String prepareArguments(Object[] oArguments) {
        String[] stringArray = Arrays.copyOf(oArguments, oArguments.length, String[].class);
        StringBuilder sbBuilder = new StringBuilder();
        for(int i = 0; i< stringArray.length;i++){
            sbBuilder.append(stringArray[i]);
            if(i < stringArray.length - 1){sbBuilder.append(",");}
        }
        String sArguments = sbBuilder.toString();
        return sArguments;
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
              }
              JSONObject jsoLocation = bakery.getJSONObject("location");
              lLocation = new Location(jsoLocation.getDouble("y"), jsoLocation.getDouble("x"));
              return true;
          }
          else {
              return false;
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
     */