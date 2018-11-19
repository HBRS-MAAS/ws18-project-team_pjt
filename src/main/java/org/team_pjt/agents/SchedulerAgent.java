package org.team_pjt.agents;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.team_pjt.behaviours.shutdown;
import org.team_pjt.objects.Order;
import org.team_pjt.objects.Product;

import java.util.HashMap;
import java.util.Iterator;

public class SchedulerAgent extends BaseAgent {
    private HashMap<String, Float> hmPrepTables;
    private HashMap<String, Float> hmKneadingMachine;
    private HashMap<String, Product> hmProducts; // = Available Products
    private HashMap<Integer, Order> scheduledOrders;
    private int endDays;

    protected void setup(){
        super.setup();
        this.register("scheduler", getName().split("@")[0]);
        System.out.println("SchedulerAgent is ready");
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                if(getCurrentDay() >= endDays) {
                    System.out.println("system shutdown!");
                    addBehaviour(new shutdown());
                }
//                System.out.println(getCurrentDay() + " - " + getCurrentHour());
//                System.out.println(getAllowAction());
                ACLMessage aclmProducts = (ACLMessage) myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
                if(aclmProducts != null &&(aclmProducts.getPerformative() == ACLMessage.PROPOSE)){
                    String sContent = aclmProducts.getContent();
                    JSONObject jsoProducts = new JSONObject(sContent);
                    JSONObject jsoDeliveryDate = jsoProducts.getJSONObject("deliveryDate");
//                    Iterator<String> iJsaProductIterator = jsoProducts.keys();
//                    while(iJsaProductIterator.hasNext()){
////                        JSONObject jsoProduct = (JSONObject) iJsaProductIterator.next();
////                        JSONObject jsoProducts = jsoProduct.getJSONObject("products");
////                        Iterator<String> iProductKeys = jsoProducts.keys();
////                        while(iProductKeys.hasNext()){
////
//                        }
//                    }
                }
            }
        });
    }
}
