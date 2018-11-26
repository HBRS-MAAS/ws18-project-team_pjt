package org.team_pjt.behaviours;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;
import org.team_pjt.agents.BaseAgent;

public class shutdown extends OneShotBehaviour {
    public void action() {
        ACLMessage shutdownMessage = new ACLMessage(ACLMessage.REQUEST);
        Codec codec = new SLCodec();
        myAgent.getContentManager().registerLanguage(codec);
        myAgent.getContentManager().registerOntology(JADEManagementOntology.getInstance());
        shutdownMessage.addReceiver(myAgent.getAMS());
        shutdownMessage.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
        shutdownMessage.setOntology(JADEManagementOntology.getInstance().getName());
        System.out.println("shutdown " + myAgent.getName());
        try {
            myAgent.doDelete();
            myAgent.getContentManager().fillContent(shutdownMessage,new Action(myAgent.getAID(), new ShutdownPlatform()));
            myAgent.send(shutdownMessage);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
