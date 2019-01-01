package org.team_pjt.agents;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BakingInterface extends BaseAgent {

    protected void setup() {
        super.setup();
        System.out.println(getAID().getLocalName() + " is ready.");
        this.register("Baking-interface", "JADE-bakery");
        this.getProoferAIDs();

        addBehaviour(new ReceiveDoughNotifications());

    }

    public void getProoferAIDs() {
        AID [] prooferAgents;
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType("Proofer");
        template.addServices(sd);
        try {
            DFAgentDescription [] result = DFService.search(this, template);
            System.out.println(getAID().getLocalName() + "Found the following Proofer agents:");
            prooferAgents = new AID [result.length];

            for (int i = 0; i < result.length; ++i) {
                prooferAgents[i] = result[i].getName();
                System.out.println(prooferAgents[i].getName());
            }

        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    protected void takeDown() {
        System.out.println(getAID().getLocalName() + ": Terminating.");
        this.deRegister();
    }

    /* This is the behaviour used for receiving doughNotifications */
  private class ReceiveDoughNotifications extends CyclicBehaviour {
    public void action() {
        finished();
        // baseAgent.finished(); //call it if there are no generic behaviours
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchConversationId("dough-notification"));

        ACLMessage msg = baseAgent.receive(mt);

        if (msg != null) {
            System.out.println("-------> " + getAID().getLocalName()+" Received dough Notification from " + msg.getSender());

            String content = msg.getContent();

            ACLMessage reply = msg.createReply();

            reply.setPerformative(ACLMessage.CONFIRM);

            reply.setContent("Dough Notification was received");

            baseAgent.sendMessage(reply);

            System.out.println(content);

            myAgent.doDelete();

        }
        else {
            block();
        }
    }
}


}
