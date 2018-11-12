package org.team_pjt.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.team_pjt.Objects.Product;
import org.team_pjt.behaviours.receiveKillMessage;
import org.team_pjt.Objects.Location;
import org.team_pjt.behaviours.shutdown;
//import org.team_pjt.objects.Product;

import java.util.*;

public class SchedulerAgent extends Agent {
    private String sBakeryId;
    private Location lLocation;
    private Hashtable<String, Product> htAvailableProducts;
    private Hashtable<String, Float> htKneadingMachines;
    private Hashtable<String, Float> htPrepTables;
    private Vector<AID> ovens;
    private HashMap<String, Integer> hmKneadingMachine;
    private HashMap<String, Product> hmProducts;
    private String[] sSplit;
    protected void setup(){
        Object[] oArguments = getArguments();
        String sArguments = prepareArguments(oArguments);
        readArgs(sArguments);
        registerAgent();
        ovens = new Vector<>();
        addBehaviour(new receiveKillMessage());
        addBehaviour(new shutdown());
        addBehaviour(new TickerBehaviour(this, 500) {

            @Override
            protected void onTick() {
                ACLMessage aclmReceive = (ACLMessage) myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                if(aclmReceive != null &&(aclmReceive.getPerformative() == ACLMessage.INFORM)){
                    if (aclmReceive.getContent().equals(sBakeryId)){
//                        ovens.put();
                        ovens.add(aclmReceive.getSender());
                    };

                }
            }
        });
//        addBehaviour(new OneShotBehaviour() {
//            @Override
//            public void action() {
//
//            }
//        });
        System.out.println("SchedulerAgent " + getName() + " ready");

    }

    private void registerAgent() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("schedulerbakery");
        sd.setName(this.sBakeryId);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private String prepareArguments(Object[] oArguments) {
        String[] stringArray = Arrays.copyOf(oArguments, oArguments.length, String[].class);
        StringBuilder sbBuilder = new StringBuilder();
        for(int i = 0; i< stringArray.length;i++){
            sbBuilder.append(stringArray[i]);
            if(i < stringArray.length - 1){sbBuilder.append(",");}
        }
        String sArguments = sbBuilder.toString();
        return sArguments;
    }

    private boolean readArgs(String sArgs){
        hmKneadingMachine = new HashMap<>();
        hmProducts = new HashMap<>();
        String[] saSplit = sArgs.split("#");
        for (int i = 0; i<saSplit.length; i++) {
            if (!(saSplit[i].isEmpty())){
                switch (i){
                    case 0: this.sBakeryId = saSplit[i];
                            break;
                    case 1: sSplit = saSplit[i].split(",");
                            lLocation = new Location(Float.parseFloat(sSplit[0]), Float.parseFloat(sSplit[1]));
                            break;
                    case 2: sSplit = saSplit[i].split(",");
                            for(int z = 0; z < sSplit.length; z++){
                                if((z % 12) == 0 && (z != 0)){
                                    hmProducts.put(sSplit[z-12], new Product(sSplit[z-12], Integer.parseInt(sSplit[z-11]), Float.parseFloat(sSplit[z-10]), Integer.parseInt(sSplit[z-9]) , Integer.parseInt(sSplit[z-8]), Integer.parseInt(sSplit[z-7]), Integer.parseInt(sSplit[z-6]), Integer.parseInt(sSplit[z-5]), Integer.parseInt(sSplit[z - 4]), Integer.parseInt(sSplit[z - 3]), Integer.parseInt(sSplit[z - 2]), Float.parseFloat(sSplit[z - 1])));
                                }
                                if (z == sSplit.length - 1){
                                    hmProducts.put(sSplit[z-11], new Product(sSplit[z-11], Integer.parseInt(sSplit[z-10]), Float.parseFloat(sSplit[z-9]), Integer.parseInt(sSplit[z-8]) , Integer.parseInt(sSplit[z-7]), Integer.parseInt(sSplit[z-6]), Integer.parseInt(sSplit[z-5]), Integer.parseInt(sSplit[z-4]), Integer.parseInt(sSplit[z - 3]), Integer.parseInt(sSplit[z - 2]), Integer.parseInt(sSplit[z - 1]), Float.parseFloat(sSplit[z])));
                                }
                            }
                            break;
                    case 3: sSplit = saSplit[i].split(",");
                            for(int z = 0; z < sSplit.length; z++){
                                hmKneadingMachine.put(sSplit[z], -1);
                            }
                            break;
                }
            };
        }
        return false;
    }



}
