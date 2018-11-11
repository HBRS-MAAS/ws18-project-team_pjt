package org.team_pjt.agents;

import jade.core.AID;
import jade.core.Agent;
import org.team_pjt.objects.Location;
import org.team_pjt.objects.Product;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

public class SchedulerAgent extends Agent {
    private String sBakeryId;
    private Location lLocation;
    private Hashtable<String, Product> htAvailableProducts;
    private Hashtable<String, Float> htKneadingMachines;
    private Hashtable<String, Float> htPrepTables;
    private List<AID> ovens;
    protected void setup(){
        Object[] oArguments = getArguments();
        String sArguments = prepareArguments(oArguments);
        readArgs(sArguments);
        System.out.println("SchedulerAgent " + getName() + " ready");

    }

    private String prepareArguments(Object[] oArguments) {
        String[] stringArray = Arrays.copyOf(oArguments, oArguments.length, String[].class);
        StringBuilder sbBuilder = new StringBuilder();
        for(int i = 0; i< stringArray.length;i++){
            sbBuilder.append(stringArray[i]);
            if(i < stringArray.length - 1){sbBuilder.append(",");}
        }
//        for(String s : stringArray) {
////            if (!(s.contains("#"))){
//              sbBuilder.append(s);
//              sbBuilder.append(",");
////            }
//        }
        String sArguments = sbBuilder.toString();
        return sArguments;
    }

    private boolean readArgs(String sArgs){
        String[] saSplit = sArgs.split("#");
        return false;
    }

}
