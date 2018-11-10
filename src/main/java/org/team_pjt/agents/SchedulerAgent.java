package org.team_pjt.agents;

import jade.core.AID;
import jade.core.Agent;
import org.team_pjt.objects.Location;
import org.team_pjt.objects.Product;

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
        System.out.println("SchedulerAgent " + getName() + " ready");
    }

}
