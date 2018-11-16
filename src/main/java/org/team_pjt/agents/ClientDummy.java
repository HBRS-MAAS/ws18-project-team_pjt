package org.team_pjt.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
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

import java.util.Iterator;

public class ClientDummy extends Agent {
    private AID[] aidSchedulerAgents;
    JSONArray jsaClientInfo;
    protected void setup(){
        Object[] oArguments = getArguments();
        if(!readArgs(oArguments)){
            System.out.println("No parameters given for ClientDummy " + getName());
        }
        registerAgent();
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("schedulerbakery");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    aidSchedulerAgents = new AID[result.length];
                    for (int i = 0; i < result.length; ++i){
                        aidSchedulerAgents[i] = result[i].getName();
                    }
                } catch (FIPAException e) {
                    e.printStackTrace();
                }
                ACLMessage aclMessage = new ACLMessage(ACLMessage.QUERY_IF);
                for(int i = 0; i < aidSchedulerAgents.length; i++){
                    aclMessage.addReceiver(aidSchedulerAgents[i]);
                }
                aclMessage.setContent(jsaClientInfo.toString());
                myAgent.send(aclMessage);
            }
        });
        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                ACLMessage aclReceiveOffer = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL));
                if(aclReceiveOffer != null && aclReceiveOffer.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
                    String sContent = aclReceiveOffer.getContent();
                    JSONArray jsonArrayResponse = new JSONArray(sContent);
                    Iterator<Object> iJsonArrayResponse = jsonArrayResponse.iterator();
                    while(iJsonArrayResponse.hasNext()){
                        JSONObject jsonObject = (JSONObject) iJsonArrayResponse.next();
                        if(jsonObject.isEmpty()){
                            System.out.println("No demanded products are available");
                        }
                    }
                }
            }
        });
        addBehaviour(new receiveKillMessage());
        addBehaviour(new shutdown());

    }

    private void registerAgent() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("dummyCustomer");
        sd.setName(getName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private boolean readArgs(Object[] oArguments) {
        jsaClientInfo = null;
        if(oArguments != null && oArguments.length > 0){
            jsaClientInfo = new JSONArray(oArguments[0].toString().replaceAll("###",","));
        }
        return false;
    }
}
