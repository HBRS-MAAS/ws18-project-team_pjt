package org.team_pjt.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.team_pjt.behaviours.receiveKillMessage;

public class CoolingRacksAgent extends Agent {
    private String bakery_id;
    private AID receiver_agent;
    private AID transmitter_agent;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (!readArgs(args)) {
            System.out.println("not enough arguments for CoolingRacksAgent!");
        }
        addBehaviour(new coolingDoneMessage());
        addBehaviour(new receiveKillMessage());
    }

    private boolean readArgs(Object[] args) {
        if(args.length == 3) {
            bakery_id = (String) args[0];
            receiver_agent = new AID((String) args[1]);
            transmitter_agent = new AID((String) args[2]);
            return true;
        }
        return false;
    }

    private class coolingDoneMessage extends CyclicBehaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.and(MessageTemplate.MatchConversationId("cooling-done"), MessageTemplate.MatchSender(transmitter_agent)));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                reply.setConversationId("cooling-done");
                reply.setContent(msg.getContent()); //Content should be a JSON String with product name, amount, order_id
                reply.addReceiver(receiver_agent);
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }
}
