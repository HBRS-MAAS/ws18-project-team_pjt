package org.team_pjt.agents;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.team_pjt.behaviours.receiveKillMessage;
import org.team_pjt.behaviours.shutdown;

import java.util.Iterator;

public class OvenAgent extends Agent {
    private String sOvenId;
    private String sBakeryId;
    private int iCoolingRate;
    private int iHeating_rate;
    private AID[] aidSchedulerAgents;

    protected void setup(){
        Object[] args = getArguments();
        if(!readArgs(args)){
            System.out.println("No parameter given for OvenAgent " + getName());
        }
        addBehaviour(new receiveKillMessage());
        addBehaviour(new shutdown());
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("OrderProcessing");
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
                addBehaviour(new Behaviour() {
                    @Override
                    public void action() {
                        ACLMessage aclMessage = new ACLMessage(ACLMessage.INFORM);
                        for(int i = 0; i < aidSchedulerAgents.length; i++){
                            aclMessage.addReceiver(aidSchedulerAgents[i]);
                        }
                        aclMessage.setContent(sBakeryId);
                        aclMessage.setConversationId(sOvenId);
                        myAgent.send(aclMessage);
                    }

                    @Override
                    public boolean done() {
                        return false;
                    }
                });
            }
        });
        System.out.println("Oven " + getName() + " created");
    }

    private boolean readArgs(Object[] args){
        if (args != null && args.length > 0) {
            JSONArray jsaOvenInfo = new JSONArray(args[0].toString().replaceAll("###",","));
            Iterator<Object> iBakery = jsaOvenInfo.iterator();
            String sName = getName();
            String[] sSplit = sName.split("#");
            sBakeryId = sSplit[0];
            sOvenId = sSplit[1];
            while(iBakery.hasNext()){
                JSONObject jsoBakeryInfo = (JSONObject) iBakery.next();
                if (sBakeryId.equals(String.valueOf(jsoBakeryInfo.get("guid")))) {
                    JSONObject jsoEquipment = (JSONObject) jsoBakeryInfo.get("equipment");
                    JSONArray jsaArray = (JSONArray) jsoEquipment.get("ovens");
                    Iterator<Object> iOvenArray = jsaArray.iterator();
                    while(iOvenArray.hasNext()){
                        JSONObject jsoOven = (JSONObject) iOvenArray.next();
                        if (String.valueOf(jsoOven.get("guid")).equals(sOvenId)) {
                            iCoolingRate = (int) jsoOven.get("coolingRate");
                            iHeating_rate = (int) jsoOven.get("heatingRate");
                        }
                    }
                }
            }
            return false;
        } else {return true;}
    }

}
