package org.team_pjt.agents;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class OvenAgent extends Agent {
    private String sOvenId;
    private String sBakeryId;
    private int iCoolingRate;
    private int iHeating_rate;

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
        addBehaviour(new receiveKillMessgae());
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

    private class receiveKillMessgae extends CyclicBehaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE),
                    MessageTemplate.MatchConversationId("kill"));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                System.out.println("killing: " + myAgent.getAID());
                myAgent.addBehaviour(new shutdown());
                myAgent.doDelete();
            }
            else {
                block();
            }
        }
    }

    private class shutdown extends OneShotBehaviour {
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
            }
            catch (Exception e) {
                //LOGGER.error(e);
            }

        }
    }

}
