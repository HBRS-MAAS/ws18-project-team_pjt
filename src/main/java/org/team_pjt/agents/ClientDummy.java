package org.team_pjt.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ClientDummy extends BaseAgent {
    private AID[] aidSchedulerAgents;
    private List<Order> orders;
    private String guid;
    private String name;
    private int type;
    private Location location;

    protected void setup(){
        Object[] oArguments = getArguments();
        if(!readArgs(oArguments)){
            System.out.println("No parameters given for ClientDummy " + getName());
        }
        register("customer", guid);
        addBehaviour(new shutdown());
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

            orders = new LinkedList<>();

            Iterator<Object> order_iterator = joClient.getJSONArray("orders").iterator();
            while(order_iterator.hasNext()) {
                JSONObject joOrder = (JSONObject)order_iterator.next();
                orders.add(new Order(joOrder.toString()));
            }

            return true;
        }
        return false;
    }
}
