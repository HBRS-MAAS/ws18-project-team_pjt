package org.team_pjt.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@SuppressWarnings("serial")
public class TimeKeeper extends Agent{
	private int currentTimeStep;
	private int countAgentsReplied;
	
	protected void setup() {
		System.out.println("\tHello! time-teller-agent "+getAID().getLocalName()+" is ready.");
		
        /* Wait for all the agents to start
         */
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

		addBehaviour(new SendTimeStep());
		addBehaviour(new TimeStepConfirmationBehaviour());
	}
	
	protected void takeDown() {
        //TODO: call shutdown behaviour
	}
	
    /* Get the AID for all alive agents
     */
	private List<DFAgentDescription> getAllAgents(){
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		template.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(this, template);
//			result[0].getName();
//			DFAgentDescription[] dfFinalResult = new DFAgentDescription[2];
			List<DFAgentDescription> lAgents = new ArrayList<>();
			for (int i = 0; i < result.length; i++){
				if((result[i].getName().getName().contains("scheduler")) || (result[i].getName().getName().contains("bakery")) || result[i].getName().getName().contains("customer")){
					lAgents.add(result[i]);
				}
			}
			return lAgents;
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
			return new Vector<DFAgentDescription>();
		}
	}
	
    /* Send next time step to all agents so that they can proceed with their tasks
     */
	private class SendTimeStep extends OneShotBehaviour {
		public void action() {
            List<DFAgentDescription> agents = getAllAgents();
            currentTimeStep++;
            countAgentsReplied = agents.size();
            System.out.println(">>>>> " + currentTimeStep + " <<<<<");
            for (DFAgentDescription agent : agents) {
                ACLMessage timeMessage = new ACLMessage(55);
                timeMessage.addReceiver(agent.getName());
                timeMessage.setContent(Integer.toString(currentTimeStep));
                myAgent.send(timeMessage);
            } 
		}
	}
	
    /* Get `finish` message from all agents (BaseAgent) and once all message are received
     * call SendTimeStep to increment time step
     */
	private class TimeStepConfirmationBehaviour extends CyclicBehaviour {
        private List<AID> agents;

        public TimeStepConfirmationBehaviour(){
            this.agents = new Vector<AID> ();
        }
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
                AID agent = msg.getSender();
                if (!this.agents.contains(agent)){
                    this.agents.add(agent);
                    countAgentsReplied--;
                    if (countAgentsReplied <= 0){
                        myAgent.addBehaviour(new SendTimeStep());
                        this.agents.clear();
                    }
                }
			}
			else {
				block();
			}
		}
	}
}
