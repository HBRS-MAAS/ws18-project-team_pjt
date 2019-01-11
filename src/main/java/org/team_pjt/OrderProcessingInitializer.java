package org.team_pjt;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class OrderProcessingInitializer extends Initializer {
    private static String bakeries_path;
    private static String meta_path;
    private static final String sOPPrefix = ":org.team_pjt.agents.OrderProcessing";
    private static final String sSchPrefix2 = ":org.team_pjt.agents.SchedulerAgent";

    @Override
    public String initialize(String scenarioDirectory) {
        StringBuilder agentSB = new StringBuilder();
        String opiconfigPath = Initializer.configPath;
        opiconfigPath += scenarioDirectory + "/";
        bakeries_path = opiconfigPath + "bakeries.json";
        meta_path = opiconfigPath + "meta.json";

        String bakeries = readScenarioFile(bakeries_path);
        String meta = readScenarioFile(meta_path);
        if(bakeries == null || meta == null) {
            System.out.println("Some scenarios couldn't be read!");
            return null;
        }

        JSONArray jaBakeries = new JSONArray(bakeries);
        JSONObject joMeta = new JSONObject(meta);

        Iterator<Object> bakery_iterator = jaBakeries.iterator();
        while (bakery_iterator.hasNext()) {
            JSONObject bakery = (JSONObject) bakery_iterator.next();
            String id = bakery.getString("guid");
            String bakery_idNum = id.split("-")[1];

            String bakeryString = bakery.toString().replaceAll(",","###");
            String metaString = joMeta.toString().replaceAll(",","###");

            String orderProcAgent = id + sOPPrefix;
            String schedulerAgent = "scheduler-" + bakery_idNum + sSchPrefix2;
            appendAgentAndArguments(
                    agentSB,
                    bakeryString + "," + metaString,
                    orderProcAgent
            );
            agentSB.append(";");

            appendAgentAndArguments(
                    agentSB,
                    bakeryString + "," + metaString,
                    schedulerAgent
            );
            agentSB.append(";");

        }
        return agentSB.toString();
    }

    public static void appendAgentAndArguments(StringBuilder sb, String argument, String agent) {
        sb.append(agent);
        sb.append("(");
        sb.append(argument);
        sb.append(")");
    }

    public static String readScenarioFile(String path) {
        String jsonString = null;
        try {
            StringBuilder sb = new StringBuilder();
            String line = null;
            FileReader fileReader = new FileReader(path);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            jsonString = sb.toString();
        }
        catch(IOException e) {
            e.printStackTrace();
            System.out.println("Error reading scenario file!");
        }
        return jsonString;
    }
}
