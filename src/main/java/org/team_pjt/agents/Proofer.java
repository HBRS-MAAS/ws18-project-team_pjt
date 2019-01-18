package org.team_pjt.agents;

import com.google.gson.Gson;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.team_pjt.JSONConverter;
import org.team_pjt.messages.DoughNotification;
import org.team_pjt.messages.ProofingRequest;

import java.util.Vector;

public class Proofer extends BaseAgent {
    private AID [] bakingInterfaceAgents;
    private String sBakeryId;
    private Vector<String> guids;
    private String productType;
    private Vector<Integer> productQuantities;

    protected void setup() {
        super.setup();

        System.out.println(getAID().getLocalName() + " is ready.");

        this.register("Proofer", getName().split("@")[0]);
        sBakeryId = getName().split("@")[0].split("-")[1];
        // Get Agents AIDS
        this.getDoughManagerAIDs();
        this.getBakingInterfaceAIDs();

        addBehaviour(new ReceiveProofingRequests());
    }

    protected void takeDown() {
        System.out.println(getAID().getLocalName() + ": Terminating.");
        this.deRegister();
    }

    public void getDoughManagerAIDs() {
        AID [] doughManagerAgents;
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Dough-manager");
        sd.setName("doughmanager-"+sBakeryId);
        template.addServices(sd);
        try {
            DFAgentDescription [] result = DFService.search(this, template);
            while (result.length == 0) {
                result = DFService.search(this, template);
                System.out.println(getAID().getLocalName() + "Found the following Dough-manager agents:");
                doughManagerAgents = new AID [result.length];

                for (int i = 0; i < result.length; ++i) {
                    doughManagerAgents[i] = result[i].getName();
                    System.out.println(doughManagerAgents[i].getName());
                }
            }

        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }


    public void getBakingInterfaceAIDs() {
        boolean bFound = false;
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setName("bakeryinterface-"+sBakeryId);
        sd.setType("Baking-interface");
        template.addServices(sd);
        try {
            DFAgentDescription [] result = DFService.search(this, template);
            while (result.length == 0) {
                bFound = true;
                result = getDfAgentDescriptions(template);
            }
            if(!bFound){
                result = getDfAgentDescriptions(template);
            }

        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private DFAgentDescription[] getDfAgentDescriptions(DFAgentDescription template) throws FIPAException {
        DFAgentDescription[] result;
        result = DFService.search(this, template);
        System.out.println(getAID().getLocalName() + "Found the following Baking-interface agents:");
        bakingInterfaceAgents = new AID[result.length];

        for (int i = 0; i < result.length; ++i) {
            bakingInterfaceAgents[i] = result[i].getName();
            System.out.println(bakingInterfaceAgents[i].getName());
        }
        return result;
    }

    /* This is the behaviour used for receiving proofing requests */
    private class ReceiveProofingRequests extends CyclicBehaviour {
        public void action() {
            if(!getAllowAction()) {
                return;
            }
            finished();
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId("proofing-request"));

            ACLMessage msg = baseAgent.receive(mt);


            if (msg != null) {
//                System.out.println(getAID().getLocalName() + " Received preparation request from " + msg.getSender());

                String content = msg.getContent();

//                System.out.println("Proofing request contains -> " + content);
                ProofingRequest proofingRequest = JSONConverter.parseProofingRequest(content);
//                ProofingRequest proofingRequest = JSONConverter.parseProofingRequest(content);
                ACLMessage reply = msg.createReply();

                reply.setPerformative(ACLMessage.CONFIRM);
                reply.setContent("Proofing request was received");
                baseAgent.sendMessage(reply);
                Float proofingTime = proofingRequest.getProofingTime();
                guids = proofingRequest.getGuids();
                productType = proofingRequest.getProductType();
                productQuantities = proofingRequest.getProductQuantities();

                addBehaviour(new Proofing(proofingTime));

            }
            else {
                block();
            }
        }
    }

  // This is the behaviour that performs the proofing process.

    private class Proofing extends Behaviour {
        private float proofingTime;
        private float proofingCounter = (float) 0;
        private int option = 0;
        boolean isDone = false;

        public Proofing(float proofingTime){
            this.proofingTime = proofingTime;
//            System.out.println(getAID().getLocalName() + " proofing for " + proofingTime);
        }
        public void action(){
//                if (getAllowAction() == true){
                while(proofingCounter < proofingTime){
                    proofingCounter++;
//                        System.out.println("----> " + getAID().getLocalName() + " proofing Counter " + proofingCounter);
                }
                addBehaviour(new SendDoughNotification(bakingInterfaceAgents));
                isDone = true;
//                }

        }
        public boolean done(){
//            baseAgent.finished();
            return isDone;
        }
    }



    // This is the behaviour used for sending a doughNotification msg to the BakingInterface agent
    private class SendDoughNotification extends Behaviour {
        private AID [] bakingInterfaceAgents;
        private MessageTemplate mt;
        private int option = 0;
        private Gson gson = new Gson();
        private DoughNotification doughNotification = new DoughNotification(guids, productType, productQuantities);
        private String doughNotificationString = gson.toJson(doughNotification);

        public SendDoughNotification(AID [] bakingInterfaceAgents){
            this.bakingInterfaceAgents = bakingInterfaceAgents;
        }

        public void action() {

            switch (option) {
                case 0:
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

                    msg.setContent(doughNotificationString);

                    msg.setConversationId("dough-notification");

                    // Send doughNotification msg to bakingInterfaceAgents
                    for (int i=0; i<bakingInterfaceAgents.length; i++){
                        msg.addReceiver(bakingInterfaceAgents[i]);
                    }

                    msg.setReplyWith("msg" + System.currentTimeMillis());

                    baseAgent.sendMessage(msg);  // calling sendMessage instead of send

                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("dough-notification"),

                    MessageTemplate.MatchInReplyTo(msg.getReplyWith()));

                    option = 1;

                    System.out.println(getAID().getLocalName() + " Sent doughNotification");

                    break;

                case 1:
                    ACLMessage reply = baseAgent.receive(mt);

                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.CONFIRM) {
                            System.out.println(getAID().getLocalName() + " Received confirmation from " + reply.getSender() );
                            option = 2;
                        }
                    }
                    else {
                        block();
                    }
                    break;

                default:
                    break;
                }
            }

        public boolean done() {
            if (option == 2) {
//                baseAgent.finished();
//                myAgent.doDelete();
                return true;
            }
            return false;
        }
    }
}
