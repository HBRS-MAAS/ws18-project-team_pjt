package org.team_pjt.agents;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class TimeKeeper extends Agent{
    private int currentTimeStep;
    private int countAgentsReplied;

    protected void setup() {
        System.out.println("Hallo! time-teller-agent "+getAID().getLocalName()+" is ready.");

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
            return Arrays.asList(result);
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

            for (DFAgentDescription agent : agents) {
                ACLMessage timeMessage = new ACLMessage(ACLMessage.INFORM);
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
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                countAgentsReplied--;
                if (countAgentsReplied <= 0){
                    myAgent.addBehaviour(new SendTimeStep());
                }
            }
            else {
                block();
            }
        }
    }
}