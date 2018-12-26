package org.team_pjt.agents;

import org.team_pjt.utils.Time;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

@SuppressWarnings("serial")
public abstract class BaseAgent extends Agent {

    private Time currentTime;
    private boolean allowAction = false;
    protected AID clockAgent = new AID("TimeKeeper", AID.ISLOCALNAME);
    //protected AID visualisationAgent = new AID("visualisation", AID.ISLOCALNAME);
    protected BaseAgent baseAgent = this;
	
    /* 
     * Setup to add behaviour to talk with clockAgent
     * Call `super.setup()` from `setup()` function
     */
    protected void setup() {
        this.addBehaviour(new PermitAction());
    }
    
    /*
     * Template method - override this for the task in each time step. 
     * Don't forget to call {@link BaseAgent#finished()} at the end.
     */
    protected void  stepAction() {}

    /* 
     * This function registers the agent to yellow pages
     * Call this in `setup()` function
     */
    protected void register(String type, String name){
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(type);
        sd.setName(name);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    
    /* 
     * This function removes the agent from yellow pages
     * Call this in `doDelete()` function
     */
    protected void deRegister() {
    	try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    /* 
     * This function sends finished message to clockAgent
     * This function should be called by every agent which implements BaseAgent
     * after the agent is done with the task it has to perform in a time step.
     */
    public void finished(){
        this.allowAction = false;
        ACLMessage finish = new ACLMessage(ACLMessage.INFORM);
        finish.addReceiver(this.clockAgent);
        finish.setContent("finished");
        this.send(finish);
    }

    public boolean getAllowAction() {
        return this.allowAction;
    }
    public int getCurrentDay() {
        return this.currentTime.getDay();
    }
    public int getCurrentHour() {
        return this.currentTime.getHour();
    }
    public int getCurrentMinute() {
        return this.currentTime.getMinute();
    }
    public Time getCurrentTime(){
        return this.currentTime;
    }

    /* 
     * This function is used as a middle man which uses the message
     * for different visualisation methods
     * Use `baseAgent.sendMessage(message)` instead of `myAgent.send(message)`
     * in every behaviour.
     * */
    public void sendMessage(ACLMessage msg) {
        this.send(msg);
        this.visualiseHistoricalView(msg);
        this.visualiseIndividualOrderStatus(msg);
        this.visualiseMessageQueuesByAgent(msg);
        this.visualiseOrderBoard(msg);
        this.visualiseStreetNetwork(msg);
    }

    /* 
     * Implementation skeleton code for different visualisation methods
     */
    protected void visualiseHistoricalView(ACLMessage msg) {
    }
    protected void visualiseIndividualOrderStatus(ACLMessage msg) {
    }
    protected void visualiseMessageQueuesByAgent(ACLMessage msg) {
    }
    protected void visualiseOrderBoard(ACLMessage msg) {
       /*
       msg.clearAllReceiver();
       msg.addReceiver(visualisationAgent);
       this.send(msg);
       */
    }
    protected void visualiseStreetNetwork(ACLMessage msg) {
    }


    /* 
     * Behaviour to receive message from clockAgent to proceed further with
     * tasks of next time step
     */
    private class PermitAction extends CyclicBehaviour {
        private MessageTemplate mt;

        public void action(){
            this.mt = MessageTemplate.and(MessageTemplate.MatchPerformative(TimeKeeper.BROADCAST_TIMESTEP_PERFORMATIVE),
                    MessageTemplate.MatchSender(baseAgent.clockAgent));
            ACLMessage msg = myAgent.receive(this.mt);
            if (msg != null) {
                String messageContent = msg.getContent();
                currentTime = new Time(messageContent);
                allowAction = true;
                
                stepAction();
            }
            else {
                block();
            }
        }
   }
}
