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
import org.team_pjt.behaviours.receiveKillMessage;
import org.team_pjt.behaviours.shutdown;

public class OvenAgent extends Agent {
    private String sOvenId;
    private String sBakeryId;
    private int iCoolingRate;
    private int iHeating_rate;
    private AID[] aidSchedulerAgents;

    protected void setup(){
        // 2, cooling_rate
        // oven-005, guid
        // 5, heating_rate
        // bakery-002 bakeryId
//        System.out.println("OvenAgent ready");
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
            if (args[0] instanceof String) {
                try {
                    this.iCoolingRate = Integer.parseInt((String) args[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (args[1] instanceof String) {
                try {
                    this.sOvenId = (String) args[1];
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (args[2] instanceof String) {
                try {
                    this.iHeating_rate = Integer.parseInt((String) args[2]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (args[3] instanceof String) {
                try {
                    this.sBakeryId = (String) args[3];
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        return false;
    }

}
