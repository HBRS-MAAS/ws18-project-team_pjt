package org.team_pjt.agents;

import jade.core.Agent;
import org.team_pjt.Objects.*;
import org.team_pjt.behaviours.receiveKillMessage;
import org.team_pjt.behaviours.shutdown;

public class TruckAgent extends Agent {
//    {
//        "load_capacity": 14,
//            "guid": "truck-004",
//            "location": {
//        "y": 6.0700000000000003,
//                "x": 3.4700000000000002
//    }
//    }
//      ],
    private int iLoadCapacity;
    private String sBakeryGuid;
    private String sTruckGuid;
    private Location lLocation;
    protected void setup(){
        Object[] oArguments = getArguments();
        this.sBakeryGuid = (String) oArguments[0];
        this.sTruckGuid = (String) oArguments[1];
        this.iLoadCapacity = Integer.parseInt(String.valueOf(oArguments[2]));
        this.lLocation = new Location(Float.parseFloat(String.valueOf(oArguments[3])), Float.parseFloat(String.valueOf(oArguments[4])));
        addBehaviour(new receiveKillMessage());
        addBehaviour(new shutdown());
        System.out.println("TruckAgent " + getName() + " ready");
    }
}
