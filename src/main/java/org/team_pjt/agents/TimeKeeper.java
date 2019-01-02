package org.team_pjt.agents;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.util.Scanner;
import com.fasterxml.jackson.core.type.TypeReference;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

// for shutdown behaviour
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.domain.FIPANames;

import org.team_pjt.objects.Meta;
import org.team_pjt.utils.Time;
import org.team_pjt.utils.JsonConverter;

@SuppressWarnings("serial")
public class TimeKeeper extends Agent{
	/*
	 * Defines a performative value outside the range of values defined in ACLMessage (-1 to 19)
	 * so that TimeStep messages do not interfere with other communications
	 */
	public static final int BROADCAST_TIMESTEP_PERFORMATIVE = 55;
	
	private int currentTimeStep;
    private Time currentTime;
    private Time singleTimeStep;
	private int countAgentsReplied;
    private Time endTime;
    private List<AID> finishedAgents;
	
	protected void setup() {
		System.out.println("\tHello! time-keeper-agent "+getAID().getLocalName()+" is ready.");

        this.currentTime = new Time();
        finishedAgents = new Vector<AID> ();

        /* Wait for all the agents to start
         */
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Object[] args = getArguments();
        String scenarioDirectory;
        if (args != null && args.length > 0) {
            scenarioDirectory = (String) args[0];
            String endTimeString = (String) args[1];
            endTime = new Time(endTimeString);
        } else {
            scenarioDirectory = "small";
            endTime = new Time(0,12,0);
        }
        this.readSingleTimeStepFromMeta(scenarioDirectory);

		addBehaviour(new SendTimeStep());
		addBehaviour(new TimeStepConfirmationBehaviour());
	}

    /*
     * read meta.json file and read the single time step 
     */
    private void readSingleTimeStepFromMeta(String scenarioDirectory){
        String filePath = "config/" + scenarioDirectory + "/meta.json";
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(filePath).getFile());
        String fileString = "";
        try (Scanner sc = new Scanner(file)) {
            sc.useDelimiter("\\Z"); 
            fileString = sc.next();
            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TypeReference<?> type = new TypeReference<Meta>(){};
        Meta m = JsonConverter.getInstance(fileString, type);
        this.singleTimeStep = m.getTimeStep();
    }
	
	protected void takeDown() {
        System.out.println("\t" + this.getAID().getLocalName() + " terminating.");
	}
	
    /* Get the AID for all alive agents who have registered with "JADE-bakery" name
     */
	private List<DFAgentDescription> getAllAgents(){
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
        sd.setName("JADE-bakery");
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
            currentTime.add(singleTimeStep);
            if (currentTime.greaterThan(endTime)) {
                myAgent.addBehaviour(new shutdown());
                return;
            }
            countAgentsReplied = agents.size();
            finishedAgents.clear();
            System.out.println(">>>>> " + currentTime + " <<<<<");
            for (DFAgentDescription agent : agents) {
                ACLMessage timeMessage = new ACLMessage(BROADCAST_TIMESTEP_PERFORMATIVE);
                timeMessage.addReceiver(agent.getName());
                timeMessage.setContent(currentTime.toString());
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
                AID agent = msg.getSender();
                if (!finishedAgents.contains(agent)){
                    finishedAgents.add(agent);
                    countAgentsReplied--;
                    if (countAgentsReplied == 0){
                        myAgent.addBehaviour(new SendTimeStep());
                        block();
                    }
                }
			}
			else {
				block();
			}
		}
	}
    
    // Taken from http://www.rickyvanrijn.nl/2017/08/29/how-to-shutdown-jade-agent-platform-programmatically/
    private class shutdown extends OneShotBehaviour{
        public void action() {
            System.out.println("Simulation ended. Shutting down platform.");
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
            catch (Exception e) {}
        }
    }
}
