package org.team_pjt;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

public class DoughPrepInitializer extends Initializer {
    private static final String sDougManagerPrefix =":org.team_pjt.agents.DoughManager";
    private static final String sProoferPrefix =":org.team_pjt.agents.Proofer";
    private static final String sBakingInterfacePrefix =":org.team_pjt.agents.BakingInterface";
    @Override
    public String initialize(String scenarioDirectory) {
        StringBuilder agentSB = new StringBuilder();
        String dpiConfigPath = "src/main/resources/config/";
        String sPath = dpiConfigPath + scenarioDirectory + "/" + "bakeries.json";
//        String sBakeriesPath = sPath + "bakeries.json";
        String sBakeries = OrderProcessingInitializer.readScenarioFile(sPath);
        if(sBakeries == null){
            System.out.println("Some scenarios couldn't be read");
            return null;
        }
        JSONArray jsaBakeries = new JSONArray(sBakeries);
        Iterator<Object> bakery_iterator = jsaBakeries.iterator();
        while(bakery_iterator.hasNext()){
            JSONObject bakery = (JSONObject) bakery_iterator.next();
            String id = bakery.getString("guid");
            String bakery_idNum = id.split("-")[1];
            
            String doughManaAgent = "doughmanager-" + bakery_idNum + sDougManagerPrefix;
            String bakeringinterfaceAgent = "bakeryinterface-"+ bakery_idNum + sBakingInterfacePrefix;
            String proofAgent = "proofer-"+ bakery_idNum + sProoferPrefix;
            OrderProcessingInitializer.appendAgentAndArguments(agentSB, bakery.toString().replaceAll(",", "###"), doughManaAgent);
            agentSB.append(";");
            OrderProcessingInitializer.appendAgentAndArguments(agentSB, bakery.toString().replaceAll(",", "###"), proofAgent);
            agentSB.append(";");
            OrderProcessingInitializer.appendAgentAndArguments(agentSB, bakery.toString().replaceAll(",", "###"), bakeringinterfaceAgent);
            agentSB.append(";");
        }
        return agentSB.toString();
    }
}
