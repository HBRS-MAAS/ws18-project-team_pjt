package org.team_pjt;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.team_pjt.agents.Visualisation;
import org.team_pjt.agents.Visualisation.*;

public class Start {
	private static boolean isHost;
	private static String host;
	private static String port;
    private static String bakeries_path;
    private static String clients_path;
    private static String delivery_path;
    private static String meta_path;
    private static String street_network_path;
    private static final String sOPPrefix = ":org.team_pjt.agents.OrderProcessing";
    private static final String sSchPrefix2 = ":org.team_pjt.agents.SchedulerAgent";
    private static final String sCPrefix = ":org.team_pjt.agents.ClientDummy";
    private static final String sProoferPrefix =":org.team_pjt.agents.Proofer";
    private static final String sTPrefix = "timekeeper:org.team_pjt.agents.TimeKeeper";
    private static final String sDougManagerPrefix =":org.team_pjt.agents.DoughManager";
    private static final String sBakingInterfacePrefix =":org.team_pjt.agents.BakingInterface";
    private static List<String> agents = new Vector<>();

    public static void main(String[] args) {
        bakeries_path = "src/main/resources/bakeries.json";
        clients_path = "src/main/resources/clients.json";
        delivery_path = "src/main/resources/delivery.json";
        meta_path = "src/main/resources/meta.json";
        street_network_path = "src/main/resources/street-network.json";
    	if (!decodeArguments(args)) {
            isHost = true;
		}
        String bakeries = readScenarioFile(bakeries_path);
    	String clients = readScenarioFile(clients_path);
        String delivery = readScenarioFile(delivery_path);
        String meta = readScenarioFile(meta_path);
        String street_network = readScenarioFile(street_network_path);
        if(bakeries == null || clients == null || delivery == null || meta == null || street_network == null) {
            System.exit(-1);
        }
        JSONArray jaBakeries = new JSONArray(bakeries);
        JSONArray jaClients = new JSONArray(clients);
        JSONArray jaDelivery = new JSONArray(delivery);
        JSONObject joMeta = new JSONObject(meta);
        JSONObject joStreet_network = new JSONObject(street_network);
    	if(isHost) {
            agents.add(sTPrefix);
            Iterator<Object> bakery_iterator = jaBakeries.iterator();
            while (bakery_iterator.hasNext()) {
                JSONObject bakery = (JSONObject) bakery_iterator.next();
                String id = bakery.getString("guid");
                String bakery_idNum = id.split("-")[1];
                agents.add(id + sOPPrefix);
                agents.add("scheduler-" + bakery_idNum + sSchPrefix2);
                agents.add("proofer-"+ bakery_idNum + sProoferPrefix);
                agents.add("doughmanager-" + bakery_idNum + sDougManagerPrefix);
                agents.add("bakeryinterface-"+ bakery_idNum + sBakingInterfacePrefix);
//                JSONArray jsaDoughPrepTables = bakery.getJSONArray("doughPrepTables");
//                Iterator<Object> jsaIterator = jsaDoughPrepTables.iterator();
//                while(jsaIterator.hasNext())
            }

        }
        else {
            Iterator<Object> client_iterator = jaClients.iterator();
            while (client_iterator.hasNext()) {
                JSONObject order = (JSONObject) client_iterator.next();
                String id = order.getString("guid");
                System.out.println(id);
                agents.add(id + sCPrefix);
            }
        }
    	List<String> cmd = buildCMD(agents, jaBakeries, jaClients, jaDelivery, joMeta, joStreet_network);
        jade.Boot.main(cmd.toArray(new String[cmd.size()]));

    }

    public static List<String> buildCMD(List<String> agents, JSONArray jaBakeries, JSONArray jaClients, JSONArray jaDelivery, JSONObject joMeta, JSONObject joStreet_network) {
    	StringBuilder sb = new StringBuilder();
        List<String> cmd = new Vector<>();
        Iterator<Object> customer_iterator = jaClients.iterator();
        Iterator<Object> bakery_iterator = jaBakeries.iterator();

    	if(isHost) {
            cmd.add("-local-port");
            cmd.add("8133");
		}
		else {
    	    cmd.add("-container");
            cmd.add("-host");
            cmd.add(host);
            cmd.add("-port");
            cmd.add(port);
		}
        cmd.add("-agents");
    	JSONObject bakery = new JSONObject();
		for (String a : agents) {
            if(isHost){
                if(a.contains("Scheduler")){
                    appendAgentAndArguments(sb, bakery.toString().replaceAll(",", "###") + "," + joMeta.toString().replaceAll(",", "###"), a);
                    sb.append(";");
                    continue;
                }
                if(a.contains("OrderProcessing")) {
                    bakery = (JSONObject)bakery_iterator.next();
                    appendAgentAndArguments(sb, bakery.toString().replaceAll(",", "###") + "," + joMeta.toString().replaceAll(",", "###"), a);
                    sb.append(";");
                    continue;
                }
                if(a.contains("doughmanager")){
//                    bakery = (JSONObject)bakery_iterator.next();
                    appendAgentAndArguments(sb, bakery.toString().replaceAll(",", "###"), a);
                    sb.append(";");
                    continue;
                }
            }
            else {
                if(a.contains("Client")){
                    JSONObject client = (JSONObject)customer_iterator.next();
                    appendAgentAndArguments(sb, client.toString().replaceAll(",", "###") + "," + joMeta.toString().replaceAll(",", "###"), a);
                    sb.append(";");
                    continue;
                }
            }
			sb.append(a);
            sb.append(";");
		}

        cmd.add(sb.toString());

    	return cmd;
	}

	private static void appendAgentAndArguments(StringBuilder sb, String argument, String agent) {
        sb.append(agent);
        sb.append("(");
        sb.append(argument);
        sb.append(")");
    }

    private static void parsingBakeryId(StringBuilder sb, JSONObject joObject) {
        sb.append(joObject.get("guid"));
        sb.append("#");
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

    private static boolean decodeArguments(String[] args) {
    	for (int i = 0; i < args.length; ++i) {
    		if (args[i].equals("-isHost")) {
    			isHost = true;
    			continue;
			}
			if (args[i].equals("-host")) {
				host = args[i+1];
				++i;
			}
			if (args[i].equals("-port")) {
				port = args[i+1];
				++i;
			}
            if (args[i].equals("-path")) {
                bakeries_path = args[i+1];
                ++i;
            }
			if (args[i].equals("-h")) {
				// TODO: implement help output
				System.out.println();
			}
		}
		if (!isHost && (port == null || host == null)) {
			System.out.println("instance is not host and host and port have to be specified!");
			return false;
		}
		return true;
	}
}
