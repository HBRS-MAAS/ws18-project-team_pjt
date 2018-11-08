package org.team_pjt.agents;

import jade.core.Agent;

public class OvenAgent extends Agent {
    private String sOvenId;
    private String sBakeryId;
    private int iCoolingRate;
    private int iHeating_rate;

    protected void setup(){
        // 2, cooling_rate
        // oven-005, guid
        // 5, heating_rate
        // bakery-002 bakeryId
        System.out.println("OvenAgent ready");
        Object[] args = getArguments();
        if(!readArgs(args)){
            System.out.println("No parameter given for OvenAgent " + getName());
        }
        System.out.println("Oven " + getName() + " created");
    }

    private boolean readArgs(Object[] args){
        if (args != null && args.length > 0) {
            if (args[0] instanceof String) {
                try {
                    this.iCoolingRate = Integer.parseInt((String) args[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (args[1] instanceof String) {
                try {
                    this.sOvenId = (String) args[1];
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (args[2] instanceof String) {
                try {
                    this.iHeating_rate = Integer.parseInt((String) args[2]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (args[3] instanceof String) {
                try {
                    this.sBakeryId = (String) args[3];
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        return false;
    };

}
