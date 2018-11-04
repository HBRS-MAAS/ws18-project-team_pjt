package org.team_pjt.agents;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.team_pjt.Objects.Clock;


@SuppressWarnings("serial")
public class SystemClockAgent extends Agent {
	private Clock systemClock;

	protected void setup() {
		this.systemClock = new Clock();
		addBehaviour(new updateClock(this, 1000));

        try {
 			Thread.sleep(3000);
 		} catch (InterruptedException e) {
 			//e.printStackTrace();
 		}
		// addBehaviour(new shutdown());

	}
	protected void takeDown() {
		System.out.println(getAID().getLocalName() + ": Terminating.");
	}

    // Taken from http://www.rickyvanrijn.nl/2017/08/29/how-to-shutdown-jade-agent-platform-programmatically/
	private class shutdown extends OneShotBehaviour{
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

	private class updateClock extends TickerBehaviour {

		public updateClock(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			systemClock.incr();
			myAgent.addBehaviour(new notifyClockUpdate());
			System.out.println(systemClock.toString());
		}
	}

	private class notifyClockUpdate extends OneShotBehaviour {

		@Override
		public void action() {
			SearchConstraints sc = new SearchConstraints();
			sc.setMaxResults(-1l);
			AMSAgentDescription[] evalAgents;
			try {
				evalAgents = AMSService.search(myAgent, new AMSAgentDescription(), sc);
			} catch (FIPAException e) {
				e.printStackTrace();
				return;
			}

			ACLMessage msg = new ACLMessage(ACLMessage.PROPAGATE);
			msg.setConversationId("clock-update");
			msg.setContent(systemClock.toString());

			for (AMSAgentDescription agent : evalAgents) {
				msg.addReceiver(agent.getName());
			}
			myAgent.send(msg);
		}
	}
}
