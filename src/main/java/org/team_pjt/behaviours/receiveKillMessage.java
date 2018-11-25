package org.team_pjt.behaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class receiveKillMessage extends CyclicBehaviour {
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