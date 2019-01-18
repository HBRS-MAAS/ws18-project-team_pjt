package org.team_pjt.agents;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.team_pjt.JSONConverter;
import org.team_pjt.messages.DoughNotification;
import org.team_pjt.messages.ProofingRequest;

import com.google.gson.Gson;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

// This agent receives a ProofingRequest, executes it ands sends a DoughNotification to the interface agent of the Baking Stage.

public class Proofer extends BaseAgent {
    private AID [] bakingInterfaceAgents;

    private AtomicBoolean proofingInProcess = new AtomicBoolean(false);
    private AtomicInteger messageProcessing = new AtomicInteger(0);
    private AtomicInteger proofingCounter = new AtomicInteger(0);

    private Vector<String> guids;
    private String productType;
    private Vector<Integer> productQuantities;

    private AID doughManager;
    private String bakeryId;
    private String doughManagerAgentName;

    private boolean isAvailable = true;
    private Float proofingTime;

    protected void setup() {
        super.setup();

        Object[] args = getArguments();

        if(args != null && args.length > 0){
            this.bakeryId = (String) args[0];
        }

        // Name of the doughManager the Proofer communicates with
        doughManagerAgentName = "DoughManager_" + bakeryId;
        AID doughManager = new AID(doughManagerAgentName, AID.ISLOCALNAME);

        this.register("Proofer_" + bakeryId, "JADE-bakery");

        System.out.println("Hello! " + getAID().getLocalName() + " is ready.");

        // Get Agents AIDS
        this.getBakingInterfaceAIDs();

        proofingCounter.set(0);
        addBehaviour(new timeTracker());
        addBehaviour(new ReceiveProposalRequests());
        addBehaviour(new ReceiveProofingRequests());
    }

    protected void takeDown() {
        System.out.println(getAID().getLocalName() + ": Terminating.");
        baseAgent.deRegister();
    }


    public void getBakingInterfaceAIDs() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType("Baking-interface");
        template.addServices(sd);
        try {
            DFAgentDescription [] result = DFService.search(this, template);
            System.out.println(getAID().getLocalName() + " Found the following Baking-interface agents:");
            bakingInterfaceAgents = new AID [result.length];

            for (int i = 0; i < result.length; ++i) {
                bakingInterfaceAgents[i] = result[i].getName();
                System.out.println(bakingInterfaceAgents[i].getName());
            }

        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private class timeTracker extends CyclicBehaviour {
        public void action() {
            if (!baseAgent.getAllowAction()) {
                return;
            }else{
                if (proofingInProcess.get()){
                    int curCount = proofingCounter.incrementAndGet();
//                    System.out.println(">>>>> Proofing Counter -> " + getAID().getLocalName() + " " + proofingCounter + " <<<<<");
                    addBehaviour(new Proofing());
                }
            }
            if (messageProcessing.get() <= 0)
            {
                baseAgent.finished();
            }
        }
    }

    private class ReceiveProposalRequests extends CyclicBehaviour{
        public void action(){
            messageProcessing.incrementAndGet();

            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.CFP),
                    MessageTemplate.MatchConversationId("proofing-request"));

            ACLMessage msg = baseAgent.receive(mt);

            if (msg != null){
                String content = msg.getContent();
                // System.out.println(getAID().getLocalName() + "has received a proposal request from " + msg.getSender().getName());

                ACLMessage reply = msg.createReply();
                if (isAvailable){
                    //System.out.println(getAID().getLocalName() + " is available");
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent("Hey I am free, do you wanna use me ;)?");
                }else{
                    // System.out.println(getAID().getLocalName() + " is unavailable");
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("Sorry, I am married potato :c");
                }
                baseAgent.sendMessage(reply);
                messageProcessing.decrementAndGet();
            }

            else{
                messageProcessing.decrementAndGet();
                block();
            }
        }
    }

    /* This is the behaviour used for receiving proofing requests */
    private class ReceiveProofingRequests extends CyclicBehaviour {
        public void action() {

            messageProcessing.getAndIncrement();
            MessageTemplate mt =
                    MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            // MessageTemplate.MatchSender(doughManager));
            //MessageTemplate.MatchConversationId("proofing-request"));
            ACLMessage msg = baseAgent.receive(mt);

            if (msg != null) {
                ACLMessage reply = msg.createReply();

                if (!isAvailable){
                    // System.out.println(getAID().getLocalName()  + " is already taken");

                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("Proofer is taken");
                    //reply.setConversationId("proofing-request");
                    //baseAgent.sendMessage(reply);
                    //System.out.println(getAID().getLocalName() + " failed proofing of " + msg.getContent());

                }
                else{

                    isAvailable = false;

                    String content = msg.getContent();
//                    System.out.println(getAID().getLocalName() + " WILL perform Proofing for " + msg.getSender() + "Proofing information -> " + content);

                    ProofingRequest proofingRequest = JSONConverter.parseProofingRequest(content);

                    //ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("Proofing request was received");
                    //reply.setConversationId("proofing-request");
                    //baseAgent.sendMessage(reply);

                    proofingTime = proofingRequest.getProofingTime();
                    guids = proofingRequest.getGuids();
                    productType = proofingRequest.getProductType();
                    productQuantities = proofingRequest.getProductQuantities();

                    // proofingInProcess.set(true);
                    //messageProcessing.getAndDecrement();
                    addBehaviour(new Proofing());

                }
                baseAgent.sendMessage(reply);
                messageProcessing.decrementAndGet();
            }

            else {
                messageProcessing.decrementAndGet();
                block();
            }
        }
    }


    // This is the behaviour that performs the proofing process.
    private class Proofing extends OneShotBehaviour {
        public void action(){
            if (proofingCounter.get() < proofingTime){
                if (!proofingInProcess.get()){
                    // System.out.println("======================================");
//                    System.out.println("----> " + getAID().getLocalName() + " Proofing for " + proofingTime + " " + productType);
                    // System.out.println("======================================");
                    proofingInProcess.set(true);
                    isAvailable = false;
                }

            }else{
                proofingInProcess.set(false);
                isAvailable = true;
                proofingCounter.set(0);
//                System.out.println("======================================");
//                System.out.println(getAID().getLocalName() + " Finishing proofing " + productType);
//                System.out.println("======================================");
                // System.out.println("----> " + guidAvailable + " finished Kneading");
                // addBehaviour(new SendDoughNotification());
            }
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

        //TODO remove me when debugging is done
        private boolean killMessageSent = false;

        public void action() {

            messageProcessing.getAndIncrement();

            switch (option) {
                case 0:
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

                    msg.setContent(doughNotificationString);

                    msg.setConversationId("dough-Notification");

                    // Send doughNotification msg to bakingInterfaceAgents
                    for (int i=0; i<bakingInterfaceAgents.length; i++){
                        msg.addReceiver(bakingInterfaceAgents[i]);
                    }

                    msg.setReplyWith("msg" + System.currentTimeMillis());

                    baseAgent.sendMessage(msg);  // calling sendMessage instead of send

                    option = 1;
//                    System.out.println(getAID().getLocalName() + " Sent doughNotification");
                    messageProcessing.getAndDecrement();
                    break;

                case 1:
                    // MatchConversationId dough-Notification
                    mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
                            MessageTemplate.MatchConversationId("dough-notification-reply"));

                    ACLMessage reply = baseAgent.receive(mt);

                    if (reply != null) {
                        System.out.println(getAID().getLocalName() + " Received confirmation from " + reply.getSender());
                        option = 2;
                        messageProcessing.getAndDecrement();
                    }
                    else {
                        if (!killMessageSent)
                        {
                            System.out.println("Waiting for reply. Kill me!");
                            killMessageSent = true;
                        }
                        messageProcessing.getAndDecrement();
                        block();
                    }
                    break;

                default:
                    messageProcessing.decrementAndGet();
                    break;
            }
        }

        public boolean done() {
            //baseAgent.finished();
            //myAgent.doDelete();
            return option == 2;
        }
    }
}
